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
package org.apache.pivot.wtk.content;

import org.apache.pivot.json.JSON;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Default table cell renderer. Renders cell contents as a string, vertically centered.
 */
public class TableViewCellRenderer extends Label implements TableView.CellRenderer {
    public TableViewCellRenderer() {
        putStyle(Style.verticalAlignment, VerticalAlignment.CENTER);
        putStyle(Style.padding, new Insets(2));
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object row, int rowIndex, int columnIndex, TableView tableView,
        String columnName, boolean selected, boolean highlighted, boolean disabled) {
        renderStyles(tableView, selected, disabled);

        String text = null;
        if (row != null && columnName != null) {
            text = toString(row, columnName);
        }

        setText(text != null ? text : "");
    }

    protected void renderStyles(TableView tableView, boolean rowSelected, boolean rowDisabled) {
        copyStyle(Style.font, tableView);

        Style colorStyle;
        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
                if (tableView.isFocused()) {
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

        putStyle(Style.color, tableView.getStyleColor(colorStyle));
    }

    @Override
    public String toString(Object row, String columnName) {
        Object cellData = JSON.get(row, columnName);
        return (cellData == null) ? null : cellData.toString();
    }
}
