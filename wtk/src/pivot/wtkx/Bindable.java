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

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;

import pivot.collections.ArrayList;
import pivot.collections.HashMap;
import pivot.serialization.SerializationException;
import pivot.util.Resources;

/**
 * Base class for objects that wish to leverage WTKX binding annotations.
 *
 * @author tvolkert
 */
public abstract class Bindable {
    /**
     * WTKX binding annotation.
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Load {
        public String name();
        public String resources() default "\0";
        public String locale() default "\0";
    }

    /**
     * WTKX binding annotation.
     *
     * @author gbrown
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected static @interface Bind {
        public String property();
        public String id() default "\0";
    }

    /**
     * Applies WTKX binding annotations to this bindable object.
     */
    @BindMethodProcessor.BindMethod
    protected final void bind() throws IOException, BindException {
        // Walk fields and resolve annotations
        ArrayList<Class<?>> typeHierarchy = new ArrayList<Class<?>>();
        Class<?> type = getClass();
        while (type != Bindable.class) {
            typeHierarchy.add(type);
            type = type.getSuperclass();
        }

        for (int i = typeHierarchy.getLength() - 1; i >= 0; i--) {
            // Maps resource field name to the serializer that loaded the
            // resource; we create it here so each subclass gets its own scope
            HashMap<String, WTKXSerializer> wtkxSerializers = new HashMap<String, WTKXSerializer>();

            type = typeHierarchy.get(i);
            Field[] fields = type.getDeclaredFields();

            for (int j = 0, n = fields.length; j < n; j++) {
                Field field = fields[j];
                String fieldName = field.getName();
                int fieldModifiers = field.getModifiers();

                Load loadAnnotation = field.getAnnotation(Load.class);
                if (loadAnnotation != null) {
                    // Ensure that we can write to the field
                    if ((fieldModifiers & Modifier.FINAL) > 0) {
                        throw new BindException(fieldName + " is final.");
                    }

                    if ((fieldModifiers & Modifier.PUBLIC) == 0) {
                        try {
                            field.setAccessible(true);
                        } catch(SecurityException exception) {
                            throw new BindException(fieldName + " is not accessible.");
                        }
                    }

                    assert(!wtkxSerializers.containsKey(fieldName));

                    // Get the name of the resource file to use
                    Resources resources = null;
                    boolean defaultResources = false;

                    String baseName = loadAnnotation.resources();
                    if (baseName.equals("\0")) {
                        baseName = type.getName();
                        defaultResources = true;
                    }

                    // Get the resource locale
                    Locale locale;
                    String language = loadAnnotation.locale();
                    if (language.equals("\0")) {
                        locale = Locale.getDefault();
                    } else {
                        locale = new Locale(language);
                    }

                    // Attmpt to load the resources
                    try {
                        resources = new Resources(baseName, locale, "UTF8");
                    } catch(SerializationException exception) {
                        throw new BindException(exception);
                    } catch(MissingResourceException exception) {
                        if (!defaultResources) {
                            throw new BindException(baseName + " not found.");
                        }
                    }

                    // Deserialize the value
                    WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
                    wtkxSerializers.put(fieldName, wtkxSerializer);

                    URL location = type.getResource(loadAnnotation.name());
                    Object resource;
                    try {
                        resource = wtkxSerializer.readObject(location);
                    } catch (SerializationException exception) {
                        throw new BindException(exception);
                    }

                    // Set the deserialized value into the field
                    try {
                        field.set(this, resource);
                    } catch (IllegalAccessException exception) {
                        throw new BindException(exception);
                    }
                }

                Bind bindAnnotation = field.getAnnotation(Bind.class);
                if (bindAnnotation != null) {
                    if (loadAnnotation != null) {
                        throw new BindException("Cannot combine " + Load.class.getName()
                            + " and " + Bind.class.getName() + " annotations.");
                    }

                    // Ensure that we can write to the field
                    if ((fieldModifiers & Modifier.FINAL) > 0) {
                        throw new BindException(fieldName + " is final.");
                    }

                    if ((fieldModifiers & Modifier.PUBLIC) == 0) {
                        try {
                            field.setAccessible(true);
                        } catch(SecurityException exception) {
                            throw new BindException(fieldName + " is not accessible.");
                        }
                    }

                    // Bind to the value loaded by the property's serializer
                    String property = bindAnnotation.property();
                    WTKXSerializer wtkxSerializer = wtkxSerializers.get(property);
                    if (wtkxSerializer == null) {
                        throw new BindException("Property \"" + property + "\" has not been loaded.");
                    }

                    String id = bindAnnotation.id();
                    if (id.equals("\0")) {
                        id = field.getName();
                    }

                    Object value = wtkxSerializer.getObjectByName(id);
                    if (value == null) {
                        throw new BindException("\"" + id + "\" does not exist.");
                    }

                    // Set the value into the field
                    try {
                        field.set(this, value);
                    } catch (IllegalAccessException exception) {
                        throw new BindException(exception);
                    }
                }
            }
        }
    }
}
