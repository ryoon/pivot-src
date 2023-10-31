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
package org.apache.pivot.demos.colors;

import java.awt.Color;
import java.awt.Font;
import java.util.Set;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FontUtilities;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;

public final class Colors implements Application {
    private static final int CELLS_PER_ROW = 5;

    private Window mainWindow;

    private Label makeLabel(final String text) {
        Label label = new Label(text);
        label.putStyle(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        return label;
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) {
        GridPane gridPane = new GridPane(CELLS_PER_ROW);
        gridPane.putStyle(Style.padding, 6);

        Font fontBold    = FontUtilities.getFont(FontUtilities.SANS_SERIF_FONTS, Font.BOLD,   13);
        Font fontRegular = FontUtilities.getFont(FontUtilities.SANS_SERIF_FONTS, Font.PLAIN,  12);
        Font fontItalic  = FontUtilities.getFont(FontUtilities.SANS_SERIF_FONTS, Font.ITALIC, 11);

        int cell = 0;
        GridPane.Row row = null;

        int numColors = CSSColor.numberOfColors();

        for (CSSColor color : CSSColor.values()) {
            if (cell % CELLS_PER_ROW == 0) {
                row = new GridPane.Row(gridPane);
            }

            BoxPane container = new BoxPane(Orientation.VERTICAL);
            container.putStyle(Style.padding, 4);
            container.putStyle(Style.fill, true);

            BoxPane colorFill = new BoxPane(Orientation.VERTICAL);

            Color fillColor  = color.getColor();
            String colorName = color.toString();
            int r = fillColor.getRed();
            int g = fillColor.getGreen();
            int b = fillColor.getBlue();

            colorFill.putStyle(Style.backgroundColor, fillColor);
            colorFill.setPreferredWidth(372);
            colorFill.setPreferredHeight(100);
            Set<CSSColor> matchingColors = CSSColor.getMatchingColors(color);
            String matches = matchingColors.size() == 0
                ? "No matches."
                : "Matches: " + StringUtils.toString(matchingColors);
            colorFill.setTooltipText(matches);

            Label nameLabel = makeLabel(color.toString());
            nameLabel.putStyle(Style.font, fontBold);

            String rgbText = String.format("R=%1$3d, G=%2$3d, B=%3$3d", r, g, b);
            Label rgbLabel = makeLabel(rgbText);
            rgbLabel.putStyle(Style.font, fontRegular);

            float[] hsbValues = Color.RGBtoHSB(r, g, b, null);
            String hsbText = String.format("H=%1$5.3f, S=%2$5.3f, V=%3$5.3f",
                hsbValues[0], hsbValues[1], hsbValues[2]);
            Label hsbLabel = makeLabel(hsbText);
            hsbLabel.putStyle(Style.font, fontRegular);

            String seqText = String.format("%1$d / %2$d", cell + 1, numColors);
            Label seqLabel = makeLabel(seqText);
            seqLabel.putStyle(Style.font, fontItalic);

            container.add(colorFill);
            container.add(nameLabel);
            container.add(rgbLabel);
            container.add(hsbLabel);
            container.add(seqLabel);

            row.add(new Border(container));
            cell++;
        }
        ScrollPane scrollPane = new ScrollPane(ScrollPane.ScrollBarPolicy.FILL,
                ScrollPane.ScrollBarPolicy.AUTO);
        scrollPane.setView(gridPane);
        mainWindow = new Window(scrollPane);
        mainWindow.setMaximized(true);
        mainWindow.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (mainWindow != null) {
            mainWindow.close();
            mainWindow = null;
        }
        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(Colors.class, args);
    }
}
