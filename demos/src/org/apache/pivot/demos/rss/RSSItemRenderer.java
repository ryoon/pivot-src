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
package org.apache.pivot.demos.rss;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.TextNode;
import org.apache.pivot.xml.XML;

public class RSSItemRenderer extends BoxPane implements ListView.ItemRenderer {
    private Label titleLabel = new Label();
    private Label categoriesLabel = new Label();
    private Label submitterLabel = new Label();

    public RSSItemRenderer() {
        super(Orientation.VERTICAL);

        putStyle(Style.padding, new Insets(2, 2, 8, 2));
        putStyle(Style.fill, true);

        titleLabel.putStyle(Style.wrapText, true);
        add(titleLabel);

        categoriesLabel.putStyle(Style.wrapText, true);
        add(categoriesLabel);

        submitterLabel.putStyle(Style.wrapText, true);
        add(submitterLabel);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object item, int index, ListView listView, boolean selected,
        Button.State state, boolean highlighted, boolean disabled) {
        if (item != null) {
            Element itemElement = (Element) item;

            String title = XML.getText(itemElement, "title");
            titleLabel.setText(title);

            String categories = "Categories:";
            List<Element> categoryElements = itemElement.getElements("category");
            for (int i = 0, n = categoryElements.getLength(); i < n; i++) {
                Element categoryElement = categoryElements.get(i);
                TextNode categoryTextNode = (TextNode) categoryElement.get(0);
                String category = categoryTextNode.getText();

                if (i > 0) {
                    categories += ", ";
                }

                categories += category;
            }

            categoriesLabel.setText(categories);

            String submitter = XML.getText(itemElement, "dz:submitter/dz:username");
            submitterLabel.setText("Submitter: " + submitter);
        }

        Font font = listView.getStyleFont(Style.font);
        Font largeFont = font.deriveFont(Font.BOLD, 14);
        titleLabel.putStyle(Style.font, largeFont);
        categoriesLabel.putStyle(Style.font, font);
        submitterLabel.putStyle(Style.font, font);

        Style colorStyle;
        if (listView.isEnabled() && !disabled) {
            if (selected) {
                if (listView.isFocused()) {
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

        Color color = listView.getStyleColor(colorStyle);

        titleLabel.putStyle(Style.color, color);
        categoriesLabel.putStyle(Style.color, color);
        submitterLabel.putStyle(Style.color, color);
    }

    @Override
    public String toString(Object item) {
        return XML.getText((Element) item, "title");
    }
}
