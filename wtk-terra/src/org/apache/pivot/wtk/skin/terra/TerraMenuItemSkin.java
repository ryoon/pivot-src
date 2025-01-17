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
package org.apache.pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Transparency;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.MenuItemSkin;

/**
 * Terra menu item skin.
 */
public class TerraMenuItemSkin extends MenuItemSkin {
    public final class CheckmarkImage extends Image {
        public static final int SIZE = 14;
        public static final int CHECKMARK_SIZE = 10;

        @Override
        public int getWidth() {
            return SIZE;
        }

        @Override
        public int getHeight() {
            return SIZE;
        }

        @Override
        public void paint(Graphics2D graphics) {
            Menu.Item menuItem = (Menu.Item) getComponent();
            Menu menu = (Menu) menuItem.getParent();

            Color color = menu.getStyleColor(Style.color);
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(2.5f));

            GraphicsUtilities.setAntialiasingOn(graphics);

            // Draw a checkmark
            int n = CHECKMARK_SIZE / 2;
            int m = CHECKMARK_SIZE / 4;
            int offsetX = (SIZE - (n + m)) / 2;
            int offsetY = (SIZE - n) / 2;

            graphics.drawLine(offsetX, (n - m) + offsetY, m + offsetX, n + offsetY);
            graphics.drawLine(m + offsetX, n + offsetY, (m + n) + offsetX, offsetY);
        }
    }

    private Image checkmarkImage = new CheckmarkImage();

    public static final int EXPANDER_SIZE = 11;
    public static final int EXPANDER_ICON_SIZE = 5;

    @Override
    public void install(Component component) {
        super.install(component);

        Menu.Item menuItem = (Menu.Item) component;
        menuItem.setCursor(Cursor.DEFAULT);
    }

    private Button.DataRenderer prepare() {
        Menu.Item menuItem = (Menu.Item) getComponent();
        return prepare(menuItem, false);
    }

    private Button.DataRenderer prepare(Menu.Item menuItem, boolean highlight) {
        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, highlight);
        return dataRenderer;
    }

    @Override
    public int getPreferredWidth(int height) {
        Button.DataRenderer dataRenderer = prepare();

        return dataRenderer.getPreferredWidth(height) + EXPANDER_SIZE;
    }

    @Override
    public int getPreferredHeight(int width) {
        Button.DataRenderer dataRenderer = prepare();

        return Math.max(dataRenderer.getPreferredHeight(width), EXPANDER_SIZE);
    }

    @Override
    public Dimensions getPreferredSize() {
        Button.DataRenderer dataRenderer = prepare();

        Dimensions preferredSize = dataRenderer.getPreferredSize();

        return new Dimensions(preferredSize.width + EXPANDER_SIZE,
                              Math.max(preferredSize.height, EXPANDER_SIZE));
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        Menu.Item menuItem = (Menu.Item) getComponent();
        Menu menu = (Menu) menuItem.getParent();

        int width = getWidth();
        int height = getHeight();

        boolean highlight = menuItem.isActive();

        // Paint highlight state
        if (highlight) {
            Color activeBackgroundColor = menu.getStyleColor(Style.activeBackgroundColor);
            graphics.setPaint(new GradientPaint(width / 2f, 0,
                TerraTheme.brighten(activeBackgroundColor), width / 2f, height,
                activeBackgroundColor));
            graphics.fillRect(0, 0, width, height);
        }

        // Paint the content
        Button.DataRenderer dataRenderer = prepare(menuItem, highlight);
        dataRenderer.setSize(Math.max(width - EXPANDER_SIZE, 0), height);

        dataRenderer.paint(graphics);

        // Paint the expander
        if (menuItem.getMenu() != null) {
            Color color = menu.getStyleColor(highlight ? Style.activeColor : Style.color);
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(0));

            GraphicsUtilities.setAntialiasingOn(graphics);

            graphics.translate(dataRenderer.getWidth() + (EXPANDER_SIZE - EXPANDER_ICON_SIZE) / 2,
                (height - EXPANDER_ICON_SIZE) / 2);

            int[] xPoints = {0, EXPANDER_ICON_SIZE, 0};
            int[] yPoints = {0, EXPANDER_ICON_SIZE / 2, EXPANDER_ICON_SIZE};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    @Override
    public boolean isOpaque() {
        boolean opaque = false;

        Menu.Item menuItem = (Menu.Item) getComponent();

        if (menuItem.isActive()) {
            Menu menu = (Menu) menuItem.getParent();
            Color activeBackgroundColor = menu.getStyleColor(Style.activeBackgroundColor);
            opaque = (activeBackgroundColor.getTransparency() == Transparency.OPAQUE);
        }

        return opaque;
    }

    public Image getCheckmarkImage() {
        return checkmarkImage;
    }

    public Color getPopupBorderColor() {
        return menuPopup.getStyleColor(Style.borderColor);
    }

    public void setPopupBorderColor(Color popupBorderColor) {
        menuPopup.putStyle(Style.borderColor, popupBorderColor);
    }

    public void setPopupBorderColor(String popupBorderColor) {
        setPopupBorderColor(GraphicsUtilities.decodeColor(popupBorderColor, "popupBorderColor"));
    }

}
