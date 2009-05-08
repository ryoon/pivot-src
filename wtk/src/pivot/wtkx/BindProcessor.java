/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtkx;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import pivot.collections.ArrayList;
import pivot.collections.ArrayStack;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.source.util.Trees;

/**
 * Annotation processor that injects <tt>__bind__(Map)</tt> overrides into
 * classes that use the <tt>@Load</tt> and <tt>@Bind</tt> annotations to
 * perform WTKX loading and binding.
 *
 * @author tvolkert
 */
@SupportedAnnotationTypes("pivot.wtkx.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class BindProcessor extends AbstractProcessor {
    /**
     * Holds pertinent information about a class' member variables that use
     * the <tt>@Load</tt> and <tt>@Bind</tt> annotations. A dossier object
     * is pushed onto a stack before visiting a class and popped off the
     * stack after visiting it, allowing us to know if any members variables
     * contained in the class need processing.
     *
     * @author tvolkert
     */
    private static class AnnotationDossier {
        /**
         * Encapsulates a load field and the bind fields that are bound to that
         * load field.
         *
         * @author tvolkert
         */
        public static class LoadGroup {
            public JCVariableDecl loadField = null;
            public ArrayList<JCVariableDecl> bindFields = null;

            private LoadGroup(JCVariableDecl loadField) {
                this.loadField = loadField;
            }
        }

        private HashMap<String, LoadGroup> loadGroups = null;
        private ArrayList<JCVariableDecl> strandedBindFields = null;

        /**
         * Gets the load groups that have been recorded in this dossier,
         * indexed by load field name.
         *
         * @return
         * The load groups map, or <tt>null</tt> if no load groups have been
         * recorded in this dossier
         */
        public Map<String, LoadGroup> getLoadGroups() {
            return loadGroups;
        }

        /**
         * Gets the bind fields that were recorded in this dossier whose
         * associated load fields were missing from the dossier. When bind
         * fields are first recorded, they can be stranded if they appear in
         * the source file before their associated load field (since the source
         * file is processed linearly). Calling {@link reconcile()} after all
         * fields have been visited will pair these stranded bind fields up
         * with their associated load group and remove them from the stranded
         * list. After <tt>reconcile</tt> has been called, any bind fields that
         * remain in the stranded list are assumed to be bound to
         * <tt>public</tt> or <tt>protected</tt> load fields in a superclass.
         * It is up to the <tt>__bind__</tt> method to handle these stranded
         * bind fields correctly.
         */
        public List<JCVariableDecl> getStrandedBindFields() {
            return strandedBindFields;
        }

        /**
         * Creates a load group for the specified load field.
         *
         * @param loadField
         * The AST load variable declaration node
         */
        public void createLoadGroup(JCVariableDecl loadField) {
            if (loadGroups == null) {
                // Lazily create the load groups map
                loadGroups = new HashMap<String, LoadGroup>();
            }

            // Create a new load group for this load field
            String loadFieldName = loadField.name.toString();
            loadGroups.put(loadFieldName, new LoadGroup(loadField));
        }

        /**
         * Adds the specified bind field to its appropriate load group, if one
         * exists. If it is bound to a property that has not yet been
         * encountered by the bind injector visitor, it is added to the
         * stranded list.
         *
         * @param loadField
         * The AST load variable declaration node
         */
        public void addToLoadGroup(JCVariableDecl bindField) {
            addToLoadGroup(bindField, true);
        }

        /**
         * Adds the specified bind field to its appropriate load group, if one
         * exists. If it is bound to a property that has not yet been
         * encountered by the bind injector visitor, and <tt>recordStranded</tt>
         * is <tt>true</tt>, then it is added to the stranded list.
         *
         * @param loadField
         * The AST load variable declaration node
         *
         * @param recordStranded
         * <tt>true</tt> to add group-less fields to the stranded list
         *
         * @return
         * <tt>true</tt> if the field was added to a load group; <tt>false</tt>
         * if it was not
         */
        private boolean addToLoadGroup(JCVariableDecl bindField, boolean recordStranded) {
            boolean added = false;

            JCAnnotation bindAnnotation = getBindAnnotation(bindField);
            String loadFieldName = getAnnotationProperty(bindAnnotation, "property");

            if (loadGroups != null
                && loadGroups.containsKey(loadFieldName)) {
                added = true;
                LoadGroup loadGroup = loadGroups.get(loadFieldName);

                if (loadGroup.bindFields == null) {
                    // Lazily create the bind fields list
                    loadGroup.bindFields = new ArrayList<JCVariableDecl>();
                }

                // Add this bind field to its load group
                loadGroup.bindFields.add(bindField);
            }

            if (!added && recordStranded) {
                if (strandedBindFields == null) {
                    // Lazily create the stranded list
                    strandedBindFields = new ArrayList<JCVariableDecl>();
                }

                strandedBindFields.add(bindField);
            }

            return added;
        }

        /**
         * Attempts to find a load group for all stranded bind variable
         * declarations. Those that are found to have a load group will be
         * placed in the load group and removed from the stranded list. Those
         * left in the stranded list are assumed to be binding to a superclass'
         * load field.
         */
        public void reconcile() {
            if (strandedBindFields != null) {
                for (int i = strandedBindFields.getLength() - 1; i >= 0; i--) {
                    if (addToLoadGroup(strandedBindFields.get(i), false)) {
                        // Remove it from the stranded list
                        strandedBindFields.remove(i, 1);
                    }
                }
            }
        }
    }

    /**
     * This actually does the work of bind method override injection.
     *
     * @author tvolkert
     */
    private class BindInjector extends TreeTranslator {
        private ArrayStack<AnnotationDossier> stack = new ArrayStack<AnnotationDossier>();

        /**
         * Injects an override implementation of the <tt>__bind__(Map)</tt>
         * method into the specified class if any member variables are found to
         * be annotated with the <tt>@Load</tt> or <tt>@Bind</tt> annotations.
         *
         * @param classDeclaration
         * The AST class declaration node
         */
        @Override
        public void visitClassDef(JCClassDecl classDeclaration) {
            AnnotationDossier annotationDossier = new AnnotationDossier();

            // Visit all of the class' nodes to record a full dossier
            stack.push(annotationDossier);
            super.visitClassDef(classDeclaration);
            stack.pop();

            // Reconcile the dossier
            annotationDossier.reconcile();

            Map<String, AnnotationDossier.LoadGroup> loadGroups = annotationDossier.getLoadGroups();
            List<JCVariableDecl> strandedBindFields = annotationDossier.getStrandedBindFields();

            if (loadGroups != null || strandedBindFields != null) {
                // There is some bind work to be done in this class; start by
                // creating the source code buffer
                StringBuilder sourceCode = new StringBuilder("class _A {");
                sourceCode.append("@Override ");
                sourceCode.append("protected void __bind__(pivot.collections.Map<String,pivot.wtkx.WTKXSerializer> namedSerializers) {");
                sourceCode.append("super.__bind__(namedSerializers);");

                // Local variable declarations
                sourceCode.append("pivot.wtkx.WTKXSerializer wtkxSerializer;");
                sourceCode.append("Object object;");
                sourceCode.append("java.net.URL location;");
                sourceCode.append("java.util.Locale locale;");
                sourceCode.append("pivot.util.Resources resources;");

                if (loadGroups != null) {
                    for (String loadFieldName : loadGroups) {
                        AnnotationDossier.LoadGroup loadGroup = loadGroups.get(loadFieldName);
                        JCVariableDecl loadField = loadGroup.loadField;
                        JCAnnotation loadAnnotation = getLoadAnnotation(loadField);

                        String resourceName = getAnnotationProperty(loadAnnotation, "name");
                        String baseName = getAnnotationProperty(loadAnnotation, "resources");
                        String language = getAnnotationProperty(loadAnnotation, "locale");
                        boolean defaultResources = (baseName == null);

                        if (DEBUG) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                String.format("Processing load(%s, %s#%s)", resourceName,
                                classDeclaration.name.toString(), loadFieldName));
                        }

                        if (defaultResources) {
                            baseName = classDeclaration.name.toString();
                            if ("".equals(baseName)) {
                                baseName = null;
                            }
                        }

                        // Attempt to load the resources
                        sourceCode.append("resources = null;");
                        if (baseName != null) {
                            if (language == null) {
                                sourceCode.append("locale = java.util.Locale.getDefault();");
                            } else {
                                sourceCode.append(String.format("locale = new java.util.Locale(\"%s\");", language));
                            }
                            sourceCode.append("try {");
                            sourceCode.append(String.format("resources = new pivot.util.Resources(%s, locale, \"UTF8\");",
                                defaultResources ? (baseName + ".class.getName()") : ("\"" + baseName + "\"")));
                            sourceCode.append("} catch(java.io.IOException ex) {");
                            sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                            sourceCode.append("} catch (pivot.serialization.SerializationException ex) {");
                            sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                            sourceCode.append("} catch (java.util.MissingResourceException ex) {");
                            if (!defaultResources) {
                                sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                            }
                            sourceCode.append("}");
                        }

                        // Load the WTKX resource
                        sourceCode.append("wtkxSerializer = new pivot.wtkx.WTKXSerializer(resources);");
                        sourceCode.append(String.format("location = getClass().getResource(\"%s\");", resourceName));
                        sourceCode.append("try {");
                        sourceCode.append("object = wtkxSerializer.readObject(location);");
                        sourceCode.append("} catch (Exception ex) {");
                        sourceCode.append("throw new pivot.wtkx.BindException(ex);");
                        sourceCode.append("}");

                        // Bind the resource to the field
                        sourceCode.append(String.format("%s = (%s)object;", loadFieldName,
                            loadField.vartype.toString()));

                        // Public and protected fields get kept for subclasses
                        if ((loadField.mods.flags & (Flags.PUBLIC | Flags.PROTECTED)) != 0) {
                            sourceCode.append(String.format("namedSerializers.put(\"%s\", wtkxSerializer);",
                                loadFieldName));
                        }

                        // Bind the resource lookups to their corresponding fields
                        if (loadGroup.bindFields != null) {
                            for (JCVariableDecl bindField : loadGroup.bindFields) {
                                String bindFieldName = bindField.name.toString();
                                JCAnnotation bindAnnotation = getBindAnnotation(bindField);

                                String bindName = getAnnotationProperty(bindAnnotation, "name");
                                if (bindName == null) {
                                    // The bind name defaults to the field name
                                    bindName = bindFieldName;
                                }

                                if (DEBUG) {
                                    String property = getAnnotationProperty(bindAnnotation, "property");
                                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                        String.format("Processing bind(%s.%s, %s#%s)", property,
                                        bindName, classDeclaration.name.toString(), bindFieldName));
                                }

                                sourceCode.append(String.format("object = wtkxSerializer.getObjectByName(\"%s\");",
                                    bindName));
                                sourceCode.append("if (object == null) {");
                                sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Element not found: %s.\");", bindName));
                                sourceCode.append("}");
                                sourceCode.append(String.format("%s = (%s)object;", bindFieldName,
                                    bindField.vartype.toString()));
                            }
                        }
                    }
                }

                if (strandedBindFields != null) {
                    for (JCVariableDecl bindField : strandedBindFields) {
                        String bindFieldName = bindField.name.toString();
                        JCAnnotation bindAnnotation = getBindAnnotation(bindField);
                        String loadFieldName = getAnnotationProperty(bindAnnotation, "property");

                        String bindName = getAnnotationProperty(bindAnnotation, "name");
                        if (bindName == null) {
                            // The bind name defaults to the field name
                            bindName = bindFieldName;
                        }

                        sourceCode.append(String.format("wtkxSerializer = namedSerializers.get(\"%s\");",
                            loadFieldName));

                        sourceCode.append("if (wtkxSerializer == null) {");
                        sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Property not found: %s.\");", loadFieldName));
                        sourceCode.append("}");

                        sourceCode.append(String.format("object = wtkxSerializer.getObjectByName(\"%s\");",
                            bindName));
                        sourceCode.append("if (object == null) {");
                        sourceCode.append(String.format("throw new pivot.wtkx.BindException(\"Element not found: %s.\");", bindName));
                        sourceCode.append("}");
                        sourceCode.append(String.format("%s = (%s)object;", bindFieldName,
                            bindField.vartype.toString()));
                    }
                }

                sourceCode.append("}");
                sourceCode.append("}");

                // Parse the source code and extract the method declaration
                Scanner scanner = scannerFactory.newScanner(sourceCode.toString());
                Parser parser = parserFactory.newParser(scanner, false, false);
                JCCompilationUnit parsedCompilationUnit = parser.compilationUnit();
                JCClassDecl parsedClassDeclaration = (JCClassDecl)parsedCompilationUnit.defs.head;
                JCMethodDecl parsedMethodDeclaration = (JCMethodDecl)parsedClassDeclaration.defs.head;

                // Add the AST method declaration to our class
                classDeclaration.defs = classDeclaration.defs.prepend(parsedMethodDeclaration);
            }
        }

        /**
         * Looks for the <tt>@Load</tt> and <tt>@Bind</tt> annotations on
         * member variable declarations. When found, it records pertinent
         * information in the current bind scope, to be used before we exit
         * the containing class.
         *
         * @param variableDeclaration
         * The AST variable declaration node
         */
        @Override
        public void visitVarDef(JCVariableDecl variableDeclaration) {
            super.visitVarDef(variableDeclaration);

            JCAnnotation loadAnnotation = getLoadAnnotation(variableDeclaration);
            JCAnnotation bindAnnotation = getBindAnnotation(variableDeclaration);

            if (loadAnnotation != null
                && bindAnnotation != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Cannot combine " + Bindable.Load.class.getName()
                    + " and " + Bindable.Bind.class.getName() + " annotations.");
            } else if (loadAnnotation != null) {
                AnnotationDossier annotationDossier = stack.peek();
                annotationDossier.createLoadGroup(variableDeclaration);

                // Increment the tally for reporting purposes
                loadTally++;
            } else if (bindAnnotation != null) {
                AnnotationDossier annotationDossier = stack.peek();
                annotationDossier.addToLoadGroup(variableDeclaration);

                // Increment the tally for reporting purposes
                bindTally++;
            }
        }
    }

    private int loadTally = 0;
    private int bindTally = 0;

    private Trees trees;
    private Context context;
    private Scanner.Factory scannerFactory;
    private Parser.Factory parserFactory;
    private BindInjector bindInjector = new BindInjector();

    private static final boolean DEBUG = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        trees = Trees.instance(processingEnvironment);
        context = ((JavacProcessingEnvironment)processingEnvironment).getContext();
        scannerFactory = Scanner.Factory.instance(context);
        parserFactory = Parser.Factory.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (!roundEnvironment.processingOver()) {
            for (Element rootElement : roundEnvironment.getRootElements()) {
                if (rootElement.getKind() == ElementKind.CLASS) {
                    // Visit each AST class node with our BindInjector visitor
                    JCClassDecl classDeclaration = (JCClassDecl)trees.getTree(rootElement);
                    classDeclaration.accept(bindInjector);
                }
            }
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                String.format("%d WTKX %s processed (bound to %d %s).",
                loadTally, loadTally == 1 ? "load" : "loads",
                bindTally, bindTally == 1 ? "variable" : "variables"));
        }

        return true;
    }

    /**
     * Gets the <tt>Load</tt> AST annotation node that's associated with
     * the specified AST variable declaration node.
     *
     * @param variableDeclaration
     * The AST variable declaration node
     *
     * @return
     * The AST annotation node, or <tt>null</tt> if no such annotation is
     * associated with the variable declaration
     */
    private static JCAnnotation getLoadAnnotation(JCVariableDecl variableDeclaration) {
        return getAnnotation(variableDeclaration, Bindable.Load.class.getSimpleName());
    }

    /**
     * Gets the <tt>Bind</tt> AST annotation node that's associated with
     * the specified AST variable declaration node.
     *
     * @param variableDeclaration
     * The AST variable declaration node
     *
     * @return
     * The AST annotation node, or <tt>null</tt> if no such annotation is
     * associated with the variable declaration
     */
    private static JCAnnotation getBindAnnotation(JCVariableDecl variableDeclaration) {
        return getAnnotation(variableDeclaration, Bindable.Bind.class.getSimpleName());
    }

    /**
     * Gets the AST annotation node with the given name that's associated
     * with the specified AST variable declaration node.
     *
     * @param variableDeclaration
     * The AST variable declaration node
     *
     * @param name
     * The simple (unqualified) name of the annotation
     *
     * @return
     * The AST annotation node, or <tt>null</tt> if no such annotation is
     * associated with the variable declaration
     */
    private static JCAnnotation getAnnotation(JCVariableDecl variableDeclaration, String name) {
        JCAnnotation result = null;

        if (variableDeclaration.mods != null
            && variableDeclaration.mods.annotations != null) {
            for (JCAnnotation annotation : variableDeclaration.mods.annotations) {
                JCIdent identifier = (JCIdent)annotation.annotationType;

                if (identifier.name.contentEquals(name)) {
                    result = annotation;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the given property's value within the specified AST annotation
     * node.
     *
     * @param annotation
     * The AST annotation node
     *
     * @param propertyName
     * The name of the property to retrieve
     *
     * @return
     * The value of the property, or <tt>null</tt> if it is not explicitly
     * set in the annotation
     */
    private static String getAnnotationProperty(JCAnnotation annotation, String propertyName) {
        String result = null;

        for (JCExpression arg : annotation.args) {
            JCAssign assign = (JCAssign)arg;
            JCIdent key = (JCIdent)assign.lhs;

            if (key.name.contentEquals(propertyName)) {
                JCLiteral value = (JCLiteral)assign.rhs;
                result = (String)value.value;
                break;
            }
        }

        return result;
    }
}
