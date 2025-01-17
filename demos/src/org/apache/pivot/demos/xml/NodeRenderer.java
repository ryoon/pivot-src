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
package org.apache.pivot.demos.xml;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.TextNode;

/**
 * Custom tree view node renderer for presenting XML nodes.
 */
public class NodeRenderer extends Label implements TreeView.NodeRenderer {
    /**
     * Maximum text length to display (without ellipsis) for a node.
     */
    public static final int MAXIMUM_TEXT_LENGTH = 20;

    @Override
    public void setSize(final int width, final int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(final Object node, final Sequence.Tree.Path path, final int rowIndex,
        final TreeView treeView, final boolean expanded, final boolean selected,
        final TreeView.NodeCheckState checkState, final boolean highlighted, final boolean disabled) {
        if (node != null) {
            String text;
            if (node instanceof Element) {
                Element element = (Element) node;
                text = "<" + element.getName() + ">";
            } else if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                text = textNode.getText();

                if (text.length() > MAXIMUM_TEXT_LENGTH) {
                    text = "\"" + text.substring(0, MAXIMUM_TEXT_LENGTH) + "\"...";
                } else {
                    text = "\"" + text + "\"";
                }
            } else {
                throw new IllegalArgumentException("Unknown node type: " + node.getClass().getName());
            }

            setText(text);

            copyStyle(Style.font, treeView);

            Style colorStyle;
            if (treeView.isEnabled() && !disabled) {
                if (selected) {
                    if (treeView.isFocused()) {
                        colorStyle = Style.selectionColor;
                    } else {
                        colorStyle = Style.inactiveSelectionColor;
                    }
                } else {
                    colorStyle = Style.color;
                }
            } else {
                colorStyle = Style.disabledColor;
            }

            putStyle(Style.color, treeView.getStyleColor(colorStyle));
        }
    }

    @Override
    public String toString(final Object node) {
        String string;
        if (node instanceof Element) {
            Element element = (Element) node;
            string = element.getName();
        } else if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            string = textNode.getText();
        } else {
            throw new IllegalArgumentException("Unknown node type: " + node.getClass().getName());
        }

        return string;
    }
}
