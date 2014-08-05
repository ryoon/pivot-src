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

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;

/**
 * Default button data renderer.
 */
public class ButtonDataRenderer extends BoxPane implements Button.DataRenderer {
    protected ImageView imageView = new ImageView();
    protected Label label = new Label();

    public ButtonDataRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        add(imageView);
        add(label);

        imageView.getStyles().put("backgroundColor", null);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @Override
    public void render(Object data, Button button, boolean highlighted) {
        Image icon = null;
        String text = null;

        if (data instanceof ButtonData) {
            ButtonData buttonData = (ButtonData) data;
            icon = buttonData.getIcon();
        } else if (data instanceof Image) {
            icon = (Image) data;
        }
        text = toString(data);

        // Update the image view
        if (icon == null) {
            imageView.setVisible(false);
            imageView.setImage(icon);
        } else {
            imageView.setVisible(true);
            imageView.setImage(icon);

            imageView.getStyles().put("opacity", button.isEnabled() ? Float.valueOf(1.0f) : Float.valueOf(0.5f));

            if (getFillIcon()) {
                int buttonWidth = button.getWidth();
                int buttonHeight = button.getHeight();
                Insets padding = (Insets) button.getStyles().get("padding");
                if (buttonWidth > 0) {
                    imageView.setPreferredWidth(Math.max(buttonWidth
                        - (padding.left + padding.right + 2), 0));
                }
                if (buttonHeight > 0) {
                    imageView.setPreferredWidth(Math.max(buttonHeight
                        - (padding.top + padding.bottom + 2), 0));
                }
            }
        }

        // Update the label
        label.setText(text != null ? text : "");

        if (text == null || (text.length() == 0 && getFillIcon())) {
            label.setVisible(false);
        } else {
            label.setVisible(true);

            Font font = (Font) button.getStyles().get("font");
            label.getStyles().put("font", font);

            Color color;
            if (button.isEnabled()) {
                color = (Color) button.getStyles().get("color");
            } else {
                color = (Color) button.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);
        }
    }

    public int getIconWidth() {
        return imageView.getPreferredWidth(-1);
    }

    public void setIconWidth(int iconWidth) {
        imageView.setPreferredWidth(iconWidth);
    }

    public int getIconHeight() {
        return imageView.getPreferredHeight(-1);
    }

    public void setIconHeight(int iconHeight) {
        imageView.setPreferredHeight(iconHeight);
    }

    public boolean getShowIcon() {
        return imageView.isVisible();
    }

    public void setShowIcon(boolean showIcon) {
        imageView.setVisible(showIcon);
    }

    public boolean getFillIcon() {
        return ((Boolean) imageView.getStyles().get("fill")).booleanValue();
    }

    public void setFillIcon(boolean fillIcon) {
        imageView.getStyles().put("fill", Boolean.valueOf(fillIcon));
        getStyles().put("fill", Boolean.valueOf(fillIcon));
    }

    @Override
    public String toString(Object data) {
        String string = null;

        if (data instanceof ButtonData) {
            ButtonData buttonData = (ButtonData) data;
            string = buttonData.getText();
        } else if (!(data instanceof Image)) {
            if (data != null) {
                string = data.toString();
            }
        }

        return string;
    }
}
