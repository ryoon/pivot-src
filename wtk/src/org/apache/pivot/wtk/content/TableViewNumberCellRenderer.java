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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.pivot.json.JSON;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Style;

/**
 * Default renderer for table view cells that contain numeric data. Renders cell
 * contents as a formatted number.
 */
public class TableViewNumberCellRenderer extends TableViewCellRenderer {
    private NumberFormat numberFormat = DEFAULT_NUMBER_FORMAT;

    public static final NumberFormat DEFAULT_NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public TableViewNumberCellRenderer() {
        putStyle(Style.horizontalAlignment, HorizontalAlignment.RIGHT);

        // Apply more padding on the right so the right-aligned cells don't
        // appear to run into left-aligned cells in the next column
        putStyle(Style.padding, new Insets(2, 2, 2, 6));
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(NumberFormat numberFormat) {
        Utils.checkNull(numberFormat, "numberFormat");

        this.numberFormat = numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        Utils.checkNullOrEmpty(numberFormat, "numberFormat");

        setNumberFormat(new DecimalFormat(numberFormat));
    }

    @Override
    public String toString(Object row, String columnName) {
        Object cellData = JSON.get(row, columnName);

        String string;
        if (cellData instanceof Number) {
            string = numberFormat.format(cellData);
        } else {
            string = (cellData == null) ? null : cellData.toString();
        }

        return string;
    }
}
