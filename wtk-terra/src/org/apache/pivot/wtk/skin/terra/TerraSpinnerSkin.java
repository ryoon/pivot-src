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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerListener;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Spinner skin.
 */
public class TerraSpinnerSkin extends ContainerSkin implements Spinner.Skin, SpinnerListener,
    SpinnerSelectionListener {
    /**
     * Encapsulates the code needed to perform timer-controlled spinning.
     */
    private static class AutomaticSpinner {
        public Spinner spinner;
        public int direction;

        private ApplicationContext.ScheduledCallback scheduledSpinnerCallback = null;

        /**
         * Starts spinning the specified spinner.
         *
         * @param spinnerValue The spinner to spin
         * @param directionValue <code>1</code> to adjust the spinner's selected
         * index larger; <code>-1</code> to adjust it smaller
         * @exception IllegalStateException If automatic spinner of any spinner
         * is already in progress. Only one spinner may be automatically spun at
         * one time
         */
        public void start(final Spinner spinnerValue, final int directionValue) {
            assert (directionValue != 0) : "Spinner direction must be positive or negative";

            if (scheduledSpinnerCallback != null) {
                throw new IllegalStateException("Spinner is already running");
            }

            this.spinner = spinnerValue;
            this.direction = directionValue;

            // Run once to register we've started, then wait a timeout period and begin rapidly spinning
            scheduledSpinnerCallback = ApplicationContext.runAndScheduleRecurringCallback(() -> spin(), 400, 30);
        }

        private void spin() {
            boolean circular = spinner.isCircular();
            int selectedIndex = spinner.getSelectedIndex();
            int count = spinner.getSpinnerData().getLength();
            if (count < 1) {
                // empty spinner
                stop();
                return;
            }

            if (direction > 0) {
                if (selectedIndex < count - 1) {
                    spinner.setSelectedIndex(selectedIndex + 1);
                } else if (circular) {
                    spinner.setSelectedIndex(0);
                } else {
                    stop();
                }
            } else {
                if (selectedIndex > 0) {
                    spinner.setSelectedIndex(selectedIndex - 1);
                } else if (circular) {
                    spinner.setSelectedIndex(count - 1);
                } else {
                    stop();
                }
            }
        }

        /**
         * Stops any automatic spinning in progress.
         */
        public void stop() {
            if (scheduledSpinnerCallback != null) {
                scheduledSpinnerCallback.cancel();
                scheduledSpinnerCallback = null;
            }
        }
    }

    /**
     * Component that holds the content of a spinner. It is the focusable part
     * of a spinner.
     */
    protected class SpinnerContent extends Component {
        public SpinnerContent() {
            setSkin(new SpinnerContentSkin());
        }
    }

    /**
     * SpinnerContent skin.
     */
    protected class SpinnerContentSkin extends ComponentSkin {
        @Override
        public int getPreferredWidth(final int height) {
            int preferredWidth = 0;

            Spinner spinner = getSpinner();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            if (sizeToContent) {
                List<?> spinnerData = spinner.getSpinnerData();
                for (Object item : spinnerData) {
                    itemRenderer.render(item, spinner);
                    preferredWidth = Math.max(preferredWidth,
                        itemRenderer.getPreferredWidth(height));
                }
            } else {
                itemRenderer.render(spinner.getSelectedItem(), spinner);
                preferredWidth = itemRenderer.getPreferredWidth(height);
            }

            return preferredWidth;
        }

        @Override
        public int getPreferredHeight(final int width) {
            int preferredHeight = 0;

            Spinner spinner = getSpinner();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            itemRenderer.render(spinner.getSelectedItem(), spinner);
            preferredHeight = itemRenderer.getPreferredHeight(width);

            return preferredHeight;
        }

        @Override
        public int getBaseline(final int width, final int height) {
            Spinner spinner = getSpinner();

            int baseline = -1;

            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();
            itemRenderer.render(spinner.getSelectedItem(), spinner);
            baseline = itemRenderer.getBaseline(width, height);

            return baseline;
        }

        @Override
        public Dimensions getPreferredSize() {
            Dimensions preferredSize;

            Spinner spinner = getSpinner();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            if (sizeToContent) {
                preferredSize = new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
            } else {
                itemRenderer.render(spinner.getSelectedItem(), spinner);
                preferredSize = itemRenderer.getPreferredSize();
            }

            return preferredSize;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(final Graphics2D graphics) {
            SpinnerContent spinnerContentLocal = (SpinnerContent) getComponent();
            Spinner spinner = getSpinner();

            int width = getWidth();
            int height = getHeight();

            // Paint the content
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();
            itemRenderer.render(spinner.getSelectedItem(), spinner);

            Graphics2D contentGraphics = (Graphics2D) graphics.create();
            itemRenderer.setSize(width, height);
            itemRenderer.paint(contentGraphics);
            contentGraphics.dispose();

            // Paint the focus state
            if (spinnerContentLocal.isFocused()) {
                BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

                graphics.setStroke(dashStroke);
                graphics.setColor(borderColor);

                GraphicsUtilities.setAntialiasingOn(graphics);

                graphics.draw(new Rectangle2D.Double(1, 1.5, Math.max(width - 2.5, 0), Math.max(
                    height - 3, 0)));
            }
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void focusedChanged(final Component component, final Component obverseComponent) {
            super.focusedChanged(component, obverseComponent);

            repaintComponent();
        }

        /**
         * {@link KeyCode#UP UP} Select the previous spinner item.<br>
         * {@link KeyCode#DOWN DOWN} Select the next spinner item.
         */
        @Override
        public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
            boolean consumed = false;

            Spinner spinner = getSpinner();

            boolean circular = spinner.isCircular();
            int count = spinner.getSpinnerData().getLength();

            int selectedIndex = spinner.getSelectedIndex();
            int newSelectedIndex = selectedIndex;

            if (keyCode == KeyCode.UP) {
                if (selectedIndex < count - 1) {
                    newSelectedIndex++;
                } else if (circular) {
                    newSelectedIndex = 0;
                }
            } else if (keyCode == KeyCode.DOWN) {
                if (selectedIndex > 0) {
                    newSelectedIndex--;
                } else if (circular) {
                    newSelectedIndex = count - 1;
                }
            } else {
                consumed = super.keyPressed(component, keyCode, keyLocation);
            }

            if (newSelectedIndex != selectedIndex) {
                spinner.setSelectedIndex(newSelectedIndex);
                consumed = true;
            }

            return consumed;
        }

        /**
         * Select the next spinner item where the first character of the
         * rendered text matches the typed key (case insensitive).
         */
        @Override
        public boolean keyTyped(final Component component, final char character) {
            boolean consumed = super.keyTyped(component, character);

            Spinner spinner = getSpinner();
            List<?> spinnerData = spinner.getSpinnerData();
            Spinner.ItemRenderer itemRenderer = spinner.getItemRenderer();

            char characterUpper = Character.toUpperCase(character);

            for (int i = spinner.getSelectedIndex() + 1, n = spinnerData.getLength(); i < n; i++) {
                String string = itemRenderer.toString(spinnerData.get(i));

                if (string != null && string.length() > 0) {
                    char first = Character.toUpperCase(string.charAt(0));

                    if (first == characterUpper) {
                        spinner.setSelectedIndex(i);
                        consumed = true;
                        break;
                    }
                }
            }

            return consumed;
        }
    }

    /**
     * Spinner button.
     */
    protected class SpinButton extends Component {
        private int direction;
        private Image buttonImage;

        public SpinButton(final int direction, final Image buttonImage) {
            this.direction = direction;
            this.buttonImage = buttonImage;

            setSkin(new SpinButtonSkin());
        }

        public int getDirection() {
            return direction;
        }

        public Image getButtonImage() {
            return buttonImage;
        }
    }

    /**
     * Spinner button skin.
     */
    protected class SpinButtonSkin extends ComponentSkin {
        private boolean highlighted = false;
        private boolean pressed = false;

        @Override
        public int getPreferredWidth(final int height) {
            return BUTTON_IMAGE_SIZE + 6;
        }

        @Override
        public int getPreferredHeight(final int width) {
            return BUTTON_IMAGE_SIZE + 2;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(final Graphics2D graphics) {
            // Apply spinner styles to the button
            SpinButton spinButton = (SpinButton) getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            float alpha = pressed ? 0.5f : highlighted ? 0.25f : 0.0f;
            graphics.setPaint(new Color(0, 0, 0, alpha));
            graphics.fillRect(0, 0, width, height);

            // Paint the image
            SpinButtonImage buttonImage = (SpinButtonImage) spinButton.getButtonImage();
            graphics.translate((width - BUTTON_IMAGE_SIZE) / 2, (height - BUTTON_IMAGE_SIZE) / 2);
            buttonImage.paint(graphics);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean isOpaque() {
            return false;
        }

        @Override
        public void enabledChanged(final Component component) {
            super.enabledChanged(component);

            automaticSpinner.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public void mouseOver(final Component component) {
            super.mouseOver(component);

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut(final Component component) {
            super.mouseOut(component);

            automaticSpinner.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                SpinButton spinButton = (SpinButton) getComponent();
                Spinner spinner = getSpinner();

                // Start the automatic spinner. It'll be stopped when we
                // mouse up or mouse out
                automaticSpinner.start(spinner, spinButton.getDirection());

                pressed = true;
                repaintComponent();
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                automaticSpinner.stop();

                pressed = false;
                repaintComponent();
            }

            return consumed;
        }
    }

    /**
     * Abstract base class for button images.
     */
    protected abstract class SpinButtonImage extends Image {
        @Override
        public int getWidth() {
            return BUTTON_IMAGE_SIZE;
        }

        @Override
        public int getHeight() {
            return BUTTON_IMAGE_SIZE;
        }

        @Override
        public void paint(final Graphics2D graphics) {
            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);
        }
    }

    protected class SpinUpImage extends SpinButtonImage {
        @Override
        public void paint(final Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {0, 2, 4};
            int[] yPoints = {3, 1, 3};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    protected class SpinDownImage extends SpinButtonImage {
        @Override
        public void paint(final Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {0, 2, 4};
            int[] yPoints = {1, 3, 1};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    private SpinnerContent spinnerContent = new SpinnerContent();
    private SpinButton upButton = new SpinButton(1, new SpinUpImage());
    private SpinButton downButton = new SpinButton(-1, new SpinDownImage());

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color borderColor;
    private Color buttonColor;
    private Color buttonBackgroundColor;
    private boolean sizeToContent = false;

    // Derived colors
    private Color buttonBevelColor;

    private static AutomaticSpinner automaticSpinner = new AutomaticSpinner();

    public static final int BUTTON_IMAGE_SIZE = 5;

    public TerraSpinnerSkin() {
        setFont(getThemeFont());
    }

    private Spinner getSpinner() {
        return (Spinner) getComponent();
    }

    @Override
    public void setSize(final int width, final int height) {
        int previousWidth = getWidth();
        int previousHeight = getHeight();

        super.setSize(width, height);

        if (previousWidth != width || previousHeight != height) {
            automaticSpinner.stop();
        }
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        setDefaultStyles();

        Spinner spinner = (Spinner) component;
        spinner.getSpinnerListeners().add(this);
        spinner.getSpinnerSelectionListeners().add(this);

        spinner.add(spinnerContent);
        spinner.add(upButton);
        spinner.add(downButton);
    }

    @Override
    public int getPreferredWidth(final int trialHeight) {
        // Preferred width is the sum of our maximum button width plus the
        // content width, plus the border

        // Border thickness
        int preferredWidth = 2;

        int buttonHeight = (trialHeight < 0 ? -1 : trialHeight / 2);
        preferredWidth += Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        int heightConstraint = trialHeight;
        if (heightConstraint >= 0) {
            // Subtract border thickness from height constraint
            heightConstraint = Math.max(heightConstraint - 2, 0);
        }

        preferredWidth += spinnerContent.getPreferredWidth(heightConstraint);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int trialWidth) {
        // Preferred height is the maximum of the button height and the
        // renderer's preferred height (plus the border), where button
        // height is defined as the larger of the two buttons' preferred
        // height, doubled.

        Dimensions upButtonPreferredSize = upButton.getPreferredSize();
        Dimensions downButtonPreferredSize = downButton.getPreferredSize();

        int preferredHeight = Math.max(upButtonPreferredSize.height, downButtonPreferredSize.height) * 2;

        int widthConstraint = trialWidth;
        if (widthConstraint >= 0) {
            // Subtract the button and border width from width constraint
            int buttonWidth = Math.max(upButtonPreferredSize.width, downButtonPreferredSize.width);

            widthConstraint = Math.max(widthConstraint - buttonWidth - 2, 0);
        }

        preferredHeight = Math.max(preferredHeight, spinnerContent.getPreferredHeight(widthConstraint)) + 1;

        return preferredHeight;
    }

    @Override
    public int getBaseline(final int width, final int height) {
        Dimensions upButtonPreferredSize = upButton.getPreferredSize();
        Dimensions downButtonPreferredSize = downButton.getPreferredSize();
        int buttonWidth = Math.max(upButtonPreferredSize.width, downButtonPreferredSize.width);

        int clientWidth = Math.max(width - buttonWidth - 2, 0);
        int clientHeight = Math.max(height - 2, 0);

        int baseline = spinnerContent.getBaseline(clientWidth, clientHeight);

        if (baseline != -1) {
            baseline += 1;
        }

        return baseline;
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        int buttonHeight = Math.max((height - 3) / 2, 0);
        int buttonWidth = Math.max(upButton.getPreferredWidth(buttonHeight),
            downButton.getPreferredWidth(buttonHeight));

        spinnerContent.setSize(Math.max(width - buttonWidth - 3, 0), Math.max(height - 2, 0));
        spinnerContent.setLocation(1, 1);

        upButton.setSize(buttonWidth, buttonHeight);
        upButton.setLocation(width - buttonWidth - 1, 1);

        downButton.setSize(buttonWidth, Math.max(height - buttonHeight - 3, 0));
        downButton.setLocation(width - buttonWidth - 1, buttonHeight + 2);
    }

    @Override
    public void paint(final Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        int buttonX = upButton.getX();
        int buttonWidth = upButton.getWidth();
        int buttonHeight = upButton.getHeight();

        if (!themeIsFlat()) {
            graphics.setPaint(new GradientPaint(buttonX + buttonWidth / 2, 0, buttonBevelColor, buttonX
                + buttonWidth / 2, buttonHeight, buttonBackgroundColor));
            graphics.fillRect(buttonX, 0, buttonWidth, height);

            graphics.setPaint(borderColor);
            GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
            GraphicsUtilities.drawLine(graphics, width - buttonWidth - 2, 0, height,
                Orientation.VERTICAL);
            GraphicsUtilities.drawLine(graphics, width - buttonWidth - 2, buttonHeight + 1,
                buttonWidth + 1, Orientation.HORIZONTAL);
        } else {
            graphics.setPaint(buttonBackgroundColor);
            graphics.fillRect(buttonX, 0, width, height);
        }
    }

    @Override
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);
        repaintComponent();
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button,
            final int x, final int y, final int count) {
        spinnerContent.requestFocus();
        return false;
    }

    protected void invalidateContent() {
        spinnerContent.invalidate();
        spinnerContent.repaint();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(final Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(final int color) {
        setColor(getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(final Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(final String disabledColorString) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColorString, "disabledColor"));
    }

    public final void setDisabledColor(final int disabledColorIndex) {
        setDisabledColor(getColor(disabledColorIndex));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(final Color borderColor) {
        Utils.checkNull(borderColor, "borderColor");

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(final String borderColorString) {
        setBorderColor(GraphicsUtilities.decodeColor(borderColorString, "borderColor"));
    }

    public final void setBorderColor(final int borderColorIndex) {
        setBorderColor(getColor(borderColorIndex));
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(final Color buttonColor) {
        // TODO: is null acceptable here?
        this.buttonColor = buttonColor;
        repaintComponent();
    }

    public final void setButtonColor(final String buttonColorString) {
        setButtonColor(GraphicsUtilities.decodeColor(buttonColorString, "buttonColor"));
    }

    public final void setButtonColor(final int buttonColorIndex) {
        setButtonColor(getColor(buttonColorIndex));
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(final Color buttonBackgroundColor) {
        // TODO: not sure if null is acceptable here (certainly if theme is flat)
        this.buttonBackgroundColor = buttonBackgroundColor;
        this.buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);
        repaintComponent();
    }

    public final void setButtonBackgroundColor(final String buttonBackgroundColorString) {
        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColorString, "buttonBackgroundColor"));
    }

    public final void setButtonBackgroundColor(final int buttonBackgroundColorIndex) {
        setButtonBackgroundColor(getColor(buttonBackgroundColorIndex));
    }

    public Font getFont() {
        return font;
    }

    public void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateContent();
    }

    public final void setFont(final String fontString) {
        setFont(decodeFont(fontString));
    }

    public final void setFont(final Dictionary<String, ?> fontDictionary) {
        setFont(Theme.deriveFont(fontDictionary));
    }

    public boolean isSizeToContent() {
        return sizeToContent;
    }

    public void setSizeToContent(final boolean sizeToContent) {
        this.sizeToContent = sizeToContent;
        invalidateContent();
    }

    // Spinner.Skin methods

    @Override
    public Bounds getContentBounds() {
        return spinnerContent.getBounds();
    }

    // SpinnerListener methods

    @Override
    public void spinnerDataChanged(final Spinner spinner, final List<?> previousSpinnerData) {
        invalidateContent();
    }

    @Override
    public void itemRendererChanged(final Spinner spinner, final Spinner.ItemRenderer previousItemRenderer) {
        invalidateContent();
    }

    @Override
    public void circularChanged(final Spinner spinner) {
        // No-op
    }

    // SpinnerSelectionListener methods
    @Override
    public void selectedIndexChanged(final Spinner spinner, final int previousSelectedIndex) {
        // No-op
    }

    @Override
    public void selectedItemChanged(final Spinner spinner, final Object previousSelectedItem) {
        invalidateContent();
    }
}
