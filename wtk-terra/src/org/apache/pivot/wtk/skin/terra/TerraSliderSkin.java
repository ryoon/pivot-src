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

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.SliderSkin;

/**
 * Terra slider skin.
 */
public class TerraSliderSkin extends SliderSkin {
    /**
     * Slider thumb component.
     */
    protected class Thumb extends Component {
        public Thumb() {
            setSkin(new ThumbSkin());
        }
    }

    /**
     * Slider thumb skin.
     */
    protected class ThumbSkin extends ComponentSkin {
        private boolean highlighted = false;

        @Override
        public boolean isFocusable() {
            return true;
        }

        @Override
        public int getPreferredWidth(final int height) {
            return 0;
        }

        @Override
        public int getPreferredHeight(final int width) {
            return 0;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(final Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            if (!themeIsFlat()) {
                graphics.setPaint(new GradientPaint(width / 2f, 0, buttonBevelColor, width / 2f,
                    height, buttonBackgroundColor));

            } else {
                graphics.setPaint(buttonBackgroundColor);
            }
            graphics.fillRect(0, 0, width, height);

            float alpha = (highlighted || dragOffset != null) ? 0.25f : 0.0f;
            graphics.setPaint(new Color(0, 0, 0, alpha));
            graphics.fillRect(0, 0, width, height);

            if (!themeIsFlat()) {
                graphics.setPaint(buttonBorderColor);
                GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
            }
        }

        @Override
        public void enabledChanged(final Component component) {
            super.enabledChanged(component);

            highlighted = false;
            repaintComponent();
        }

        @Override
        public void focusedChanged(final Component component, final Component obverseComponent) {
            super.focusedChanged(component, obverseComponent);

            TerraSliderSkin.this.repaintComponent();
        }

        @Override
        public boolean mouseMove(final Component component, final int x, final int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (Mouse.getCapturer() == component) {
                Slider slider = getSlider();
                Point sliderLocation = thumb.mapPointToAncestor(slider, x, y);
                float ratio;

                if (slider.getOrientation() == Orientation.HORIZONTAL) {
                    int sliderWidth = slider.getWidth();
                    int thumbWidthLocal = thumb.getWidth();
                    int sliderX = sliderLocation.x;

                    int minX = dragOffset.x;
                    if (sliderX < minX) {
                        sliderX = minX;
                    }

                    int maxX = (sliderWidth - thumbWidthLocal) + dragOffset.x;
                    if (sliderX > maxX) {
                        sliderX = maxX;
                    }

                    ratio = (float) (sliderX - dragOffset.x) / (sliderWidth - thumbWidthLocal);
                } else {
                    int sliderHeight = slider.getHeight();
                    int thumbHeightLocal = thumb.getHeight();
                    int sliderY = sliderLocation.y;

                    int minY = dragOffset.y;
                    if (sliderY < minY) {
                        sliderY = minY;
                    }

                    int maxY = (sliderHeight - thumbHeightLocal) + dragOffset.y;
                    if (sliderY > maxY) {
                        sliderY = maxY;
                    }

                    ratio = (float) (sliderY - dragOffset.y) / (sliderHeight - thumbHeightLocal);
                }

                int start = slider.getStart();
                int end = slider.getEnd();

                int value = (int) (start + (end - start) * ratio);
                slider.setValue(value);
            }

            return consumed;
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

            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            component.requestFocus();

            if (button == Mouse.Button.LEFT) {
                dragOffset = new Point(x, y);
                Mouse.capture(component);
                repaintComponent();

                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (Mouse.getCapturer() == component) {
                dragOffset = null;
                Mouse.release();
                repaintComponent();
            }

            return consumed;
        }

        /**
         * {@link KeyCode#LEFT LEFT} or {@link KeyCode#DOWN DOWN} Decrement the
         * slider's value.<br> {@link KeyCode#RIGHT RIGHT} or {@link KeyCode#UP
         * UP} Increment the slider's value.
         */
        @Override
        public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
            boolean consumed = super.keyPressed(component, keyCode, keyLocation);

            Slider slider = getSlider();

            int start = slider.getStart();
            int end = slider.getEnd();
            int length = end - start;

            int value = slider.getValue();
            int increment = length / 10;

            if (keyCode == KeyCode.LEFT || keyCode == KeyCode.DOWN) {
                slider.setValue(Math.max(start, value - increment));
                consumed = true;
            } else if (keyCode == KeyCode.RIGHT || keyCode == KeyCode.UP) {
                slider.setValue(Math.min(end, value + increment));
                consumed = true;
            }

            return consumed;
        }
    }

    private Thumb thumb = new Thumb();
    private Point dragOffset = null;

    private Color trackColor;
    private Color buttonBackgroundColor;
    private Color buttonBorderColor;
    private int trackWidth;
    private int thumbWidth;
    private int thumbHeight;
    private int tickSpacing;

    // Derived color
    private Color buttonBevelColor;

    public static final int DEFAULT_WIDTH = 120;
    public static final int MINIMUM_THUMB_WIDTH = 4;
    public static final int MINIMUM_THUMB_HEIGHT = 4;

    public TerraSliderSkin() {
        setTrackColor(6);
        setButtonBackgroundColor(10);
        setButtonBorderColor(7);

        trackWidth = 2;
        thumbWidth = 8;
        thumbHeight = 16;

        tickSpacing = -1;
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        Slider slider = (Slider) component;
        slider.add(thumb);
    }

    @Override
    public int getPreferredWidth(final int height) {
        Slider slider = getSlider();

        int preferredWidth;
        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            preferredWidth = DEFAULT_WIDTH;
        } else {
            preferredWidth = thumbHeight;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        Slider slider = getSlider();

        int preferredHeight;
        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            preferredHeight = thumbHeight;
        } else {
            preferredHeight = DEFAULT_WIDTH;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public void layout() {
        Slider slider = getSlider();

        int width = getWidth();
        int height = getHeight();

        int start = slider.getStart();
        int end = slider.getEnd();
        int value = slider.getValue();

        float ratio = (float) (value - start) / (end - start);

        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            thumb.setSize(thumbWidth, thumbHeight);
            thumb.setLocation((int) ((width - thumbWidth) * ratio), (height - thumbHeight) / 2);
        } else {
            thumb.setSize(thumbHeight, thumbWidth);
            thumb.setLocation((width - thumbHeight) / 2, (int) ((height - thumbWidth) * ratio));
        }
    }

    private static final BasicStroke DASH_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

    @Override
    public void paint(final Graphics2D graphics) {
        super.paint(graphics);

        Slider slider = getSlider();

        int width = getWidth();
        int height = getHeight();

        graphics.setColor(trackColor);
        GraphicsUtilities.setAntialiasingOn(graphics);

        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            graphics.fillRect(0, (height - trackWidth) / 2, width, trackWidth);
            if (tickSpacing > 0) {
                int start = slider.getStart();
                int end = slider.getEnd();
                int value = start;
                while (value <= end) {
                    float ratio = (float) (value - start) / (end - start);
                    int x = (int) (width * ratio);
                    graphics.drawLine(x, height / 3, x, height * 2 / 3);
                    value += tickSpacing;
                }
            }
        } else {
            graphics.fillRect((width - trackWidth) / 2, 0, trackWidth, height);
            if (tickSpacing > 0) {
                int start = slider.getStart();
                int end = slider.getEnd();
                int value = start;
                while (value <= end) {
                    float ratio = (float) (value - start) / (end - start);
                    int y = (int) (height * ratio);
                    graphics.drawLine(width / 3, y, width * 2 / 3, y);
                    value += tickSpacing;
                }
            }
        }

        if (thumb.isFocused()) {
            graphics.setStroke(DASH_STROKE);
            graphics.setColor(buttonBorderColor);

            graphics.drawRect(0, 0, width - 1, height - 1);
        }
    }

    public Color getTrackColor() {
        return trackColor;
    }

    public void setTrackColor(final Color trackColorValue) {
        Utils.checkNull(trackColorValue, "trackColor");

        this.trackColor = trackColorValue;
        repaintComponent();
    }

    public final void setTrackColor(final String trackColorString) {
        setTrackColor(GraphicsUtilities.decodeColor(trackColorString, "trackColor"));
    }

    public final void setTrackColor(final int trackColorIndex) {
        setTrackColor(getColor(trackColorIndex));
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(final Color buttonBackgroundColorValue) {
        Utils.checkNull(buttonBackgroundColorValue, "buttonBackgroundColor");

        this.buttonBackgroundColor = buttonBackgroundColorValue;
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);
        repaintComponent();
    }

    public final void setButtonBackgroundColor(final String buttonBackgroundColorString) {
        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColorString, "buttonBackgroundColor"));
    }

    public final void setButtonBackgroundColor(final int buttonBackgroundColorIndex) {
        setButtonBackgroundColor(getColor(buttonBackgroundColorIndex));
    }

    public Color getButtonBorderColor() {
        return buttonBorderColor;
    }

    public void setButtonBorderColor(final Color buttonBorderColorValue) {
        Utils.checkNull(buttonBorderColorValue, "buttonBorderColor");

        this.buttonBorderColor = buttonBorderColorValue;
        repaintComponent();
    }

    public final void setButtonBorderColor(final String buttonBorderColorString) {
        setButtonBorderColor(GraphicsUtilities.decodeColor(buttonBorderColorString, "buttonBorderColor"));
    }

    public final void setButtonBorderColor(final int buttonBorderColorIndex) {
        setButtonBorderColor(getColor(buttonBorderColorIndex));
    }

    public int getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(final int trackWidthValue) {
        Utils.checkNonNegative(trackWidthValue, "trackWidth");

        this.trackWidth = trackWidthValue;
        repaintComponent();
    }

    public void setTrackWidth(final Number trackWidthValue) {
        Utils.checkNull(trackWidthValue, "trackWidth");

        setTrackWidth(trackWidthValue.intValue());
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(final int thumbWidthValue) {
        if (thumbWidthValue < MINIMUM_THUMB_WIDTH) {
            throw new IllegalArgumentException("thumbWidth must be greater than or equal to "
                + MINIMUM_THUMB_WIDTH);
        }

        this.thumbWidth = thumbWidthValue;
        invalidateComponent();
    }

    public void setThumbWidth(final Number thumbWidthValue) {
        Utils.checkNull(thumbWidthValue, "thumbWidth");

        setThumbWidth(thumbWidthValue.intValue());
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(final int thumbHeightValue) {
        if (thumbHeightValue < MINIMUM_THUMB_HEIGHT) {
            throw new IllegalArgumentException("thumbHeight must be greater than or equal to "
                + MINIMUM_THUMB_HEIGHT);
        }

        this.thumbHeight = thumbHeightValue;
        invalidateComponent();
    }

    public void setThumbHeight(final Number thumbHeightValue) {
        Utils.checkNull(thumbHeightValue, "thumbHeight");

        setThumbHeight(thumbHeightValue.intValue());
    }

    public int getTickSpacing() {
        return tickSpacing;
    }

    /**
     * Set the tick spacing value along the slider axis.
     *
     * @param tickSpacingValue An integer number of pixels to use to
     * space out the tick marks along the axis. Less or equal zero
     * implies don't draw tick marks.
     */
    public void setTickSpacing(final int tickSpacingValue) {
        this.tickSpacing = tickSpacingValue;
        repaintComponent();
    }

    public void setTickSpacing(final Number tickSpacingValue) {
        Utils.checkNull(tickSpacingValue, "tickSpacing");

        setTickSpacing(tickSpacingValue.intValue());
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button,
        final int x, final int y, final int count) {
        thumb.requestFocus();
        return super.mouseClick(component, button, x, y, count);
    }

    @Override
    public void rangeChanged(final Slider slider, final int previousStart, final int previousEnd) {
        invalidateComponent();
    }

    @Override
    public void orientationChanged(final Slider slider) {
        invalidateComponent();
    }

    @Override
    public void valueChanged(final Slider slider, final int previousValue) {
        layout();
    }
}
