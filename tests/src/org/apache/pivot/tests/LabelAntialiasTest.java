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
package org.apache.pivot.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.NumericSpinnerData;

public class LabelAntialiasTest implements Application {
    private Window window = null;
    private TablePane.Row labelRow = null;
    private Spinner rotationAngleSpinner = null;
    private int currentRotationAngle = 0;

    private Label buildLabel(double rotation) {
        Label label = new Label();

        Font font = new Font("Arial", Font.BOLD, 64);

        AffineTransform fontAT = new AffineTransform();
        // Derive a new font using a rotation transform
        fontAT.rotate(rotation * java.lang.Math.PI / 180.0d);
        Font fontDerived = font.deriveFont(fontAT);

        label.setText("Hello at " + rotation + " degrees.");
        label.getStyles().put(Style.color, Color.RED);
        label.getStyles().put(Style.font, fontDerived);
        label.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        label.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);

        return label;
    }

    /**
     * Write to console some details of Desktop Hints, for Font Rendering.
     *
     * @see org.apache.pivot.wtk.Platform#initializeFontRenderContext
     */
    private void showFontDesktopHints() {
        System.out.println("Show Font Desktop Hints:");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.util.Map<?, ?> fontDesktopHints = (java.util.Map<?, ?>) toolkit.getDesktopProperty("awt.font.desktophints");

        System.out.println(fontDesktopHints);
    }

    /**
     * Write to console the list of Font families found in the System.
     */
    private void showFontFamilies() {
        System.out.println("Show Font Families:");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilies = ge.getAvailableFontFamilyNames();
        int fontFamiliesNumber = fontFamilies.length;
        StringBuffer fontFamilyNames = new StringBuffer(1024);
        for (int i = 0; i < fontFamiliesNumber; i++) {
            if (i > 0) {
                fontFamilyNames.append(", ");
            }
            fontFamilyNames.append(fontFamilies[i]);
        }
        System.out.println(fontFamilyNames);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) {
        window = new Window();

        showFontDesktopHints();
        showFontFamilies();

        TablePane content = new TablePane();
        new TablePane.Column(content, 1, true);
        BoxPane topBox = new BoxPane(Orientation.HORIZONTAL);
        topBox.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);
        topBox.add(new Label("Rotation angle:"));
        rotationAngleSpinner = new Spinner(new NumericSpinnerData(0, 359));
        rotationAngleSpinner.setCircular(true);
        rotationAngleSpinner.setPreferredWidth(40);
        topBox.add(rotationAngleSpinner);
        TablePane.Row topRow = new TablePane.Row(content, -1);
        topRow.add(topBox);
        labelRow = new TablePane.Row(content, 1, true);

        window.setContent(content);

        window.setTitle("Label Antialiasing Test");
        window.setMaximized(true);

        rotationAngleSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                currentRotationAngle = (Integer) spinner.getSelectedItem();
                if (labelRow.getLength() > 0) {
                    labelRow.remove(0, labelRow.getLength());
                }
                labelRow.add(buildLabel(currentRotationAngle));
            }
        });
        rotationAngleSpinner.setSelectedItem(45);

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(LabelAntialiasTest.class, args);
    }

}
