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
package org.apache.pivot.demos.json;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.io.FileList;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Clipboard;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.effects.OverlayDecorator;

/**
 * Utility application that allows the user to browse a JSON structure using a
 * tree view component.
 */
public final class JSONViewer implements Application {
    /** The main application window. */
    private Window window = null;

    /** The {@link TreeView} used to display the JSON data. */
    @BXML
    private TreeView treeView = null;

    /** A prompt displayed over the tree until some content is displayed. */
    private OverlayDecorator promptDecorator = new OverlayDecorator();

    /** Key used by the serializer to reference this application object
     * during serialization.
     */
    public static final String APPLICATION_KEY = "application";
    /** The static window title. */
    public static final String WINDOW_TITLE = "JSON Viewer";

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        bxmlSerializer.getNamespace().put(APPLICATION_KEY, this);

        window = (Window) bxmlSerializer.readObject(JSONViewer.class, "json_viewer.bxml");
        bxmlSerializer.bind(this);

        Label prompt = new Label("Drag or paste JSON here");
        prompt.putStyle(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        prompt.putStyle(Style.verticalAlignment, VerticalAlignment.CENTER);
        promptDecorator.setOverlay(prompt);
        treeView.getDecorators().add(promptDecorator);
        treeView.putStyle(Style.showGridLines, true);
        treeView.putStyle(Style.showHighlight, true);

        window.setTitle(WINDOW_TITLE);
        window.open(display);
        window.requestFocus();

        if (System.in.available() > 0) {
            JSONSerializer jsonSerializer = new JSONSerializer();
            try {
                setValue(jsonSerializer.readObject(System.in));
            } catch (Exception exception) {
                // No-op
            }
        }
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * Process "paste" request to view a JSON snippet copied from another application.
     */
    public void paste() {
        Manifest clipboardContent = Clipboard.getContent();

        if (clipboardContent != null && clipboardContent.containsText()) {
            String json = null;
            JSONSerializer jsonSerializer = new JSONSerializer();
            try {
                json = clipboardContent.getText();
                setValue(jsonSerializer.readObject(new StringReader(json)));
            } catch (Exception exception) {
                Prompt.prompt(exception.getMessage(), window);
            }

            window.setTitle(WINDOW_TITLE);
        }
    }

    /**
     * Process a "drop" action where a JSON file is dragged from somewhere and dropped
     * into this program for viewing.
     *
     * @param dragContent The object manifest that is being "dropped" to us.
     * @return What to do with this "drop" action -- {@link DropAction#COPY} for a single
     * file drop, or {@code null} for anything else.
     */
    public DropAction drop(final Manifest dragContent) {
        DropAction dropAction = null;

        try {
            FileList fileList = dragContent.getFileList();
            if (fileList.getLength() == 1) {
                File file = fileList.get(0);

                JSONSerializer jsonSerializer = new JSONSerializer();
                try (InputStream fileInputStream = Files.newInputStream(file.toPath())) {
                    setValue(jsonSerializer.readObject(fileInputStream));
                } catch (Exception exception) {
                    Prompt.prompt(exception.getMessage(), window);
                }

                window.setTitle(WINDOW_TITLE + " - " + file.getName());

                dropAction = DropAction.COPY;
            } else {
                Prompt.prompt("Multiple files not supported.", window);
            }
        } catch (IOException exception) {
            Prompt.prompt(exception.getMessage(), window);
        }

        return dropAction;
    }

    /**
     * Private method to set the content to be viewed; builds the {@link TreeView}
     * to display the data.
     *
     * @param value Should be either a {@link Map} or a {@link List} to be viewed.
     */
    private void setValue(final Object value) {
        assert (value instanceof Map<?, ?> || value instanceof List<?>);
        // Remove prompt decorator
        if (promptDecorator != null) {
            treeView.getDecorators().remove(promptDecorator);
            promptDecorator = null;
        }

        TreeBranch treeData = new TreeBranch();
        treeData.add(build(value));
        treeView.setTreeData(treeData);
        treeView.expandBranch(new Path(0));
    }

    /**
     * Build the {@link TreeView} from the data. This is a recursive function.
     *
     * @param value The root object of the data.
     * @return The root {@link TreeNode} of the built tree.
     */
    @SuppressWarnings("unchecked")
    private static TreeNode build(final Object value) {
        TreeNode treeNode;

        if (value == null) {
            treeNode = new TreeNode("<null>");
        } else if (value instanceof Map<?, ?>) {
            TreeBranch treeBranch = new TreeBranch("{ }");
            // Set the branch comparator to alphabetize the nodes underneath
            treeBranch.setComparator((node1, node2) -> node1.getText().compareTo(node2.getText()));

            Map<String, Object> map = (Map<String, Object>) value;
            for (String key : map) {
                TreeNode valueNode = build(map.get(key));

                String text = valueNode.getText();
                if (text == null) {
                    valueNode.setText(key);
                } else {
                    valueNode.setText(key + " : " + text);
                }

                treeBranch.add(valueNode);
            }

            treeNode = treeBranch;
        } else if (value instanceof List<?>) {
            TreeBranch treeBranch = new TreeBranch("[ ]");

            List<Object> list = (List<Object>) value;
            for (int i = 0, n = list.getLength(); i < n; i++) {
                TreeNode itemNode = build(list.get(i));

                String text = itemNode.getText();
                if (text == null) {
                    itemNode.setText("[" + i + "]");
                } else {
                    itemNode.setText("[" + i + "] " + text);
                }

                treeBranch.add(itemNode);
            }

            treeNode = treeBranch;
        } else if (value instanceof String) {
            treeNode = new TreeNode("\"" + value.toString() + "\"");
        } else {
            // Misc object type should use "toString()" to get the object text
            // (includes Number, Boolean, or anything else we forgot about)
            treeNode = new TreeNode(value.toString());
        }

        return treeNode;
    }

    /**
     * Main method of the desktop application.
     *
     * @param args The parsed command line arguments.
     */
    public static void main(final String[] args) {
        DesktopApplicationContext.main(JSONViewer.class, args);
    }

}
