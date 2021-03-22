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
package org.apache.pivot.wtk.skin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BorderListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.CornerRadii;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Platform;

/**
 * Border skin. <p> TODO Add styles to support different border styles (e.g.
 * inset, outset) or create subclasses for these border types.
 */
public class BorderSkin extends ContainerSkin implements BorderListener {
    private Font font;
    private Color color;
    private Color titleColor;
    private int thickness;
    private int topThickness;
    private float titleAscent;
    private Insets padding;
    private CornerRadii cornerRadii;

    /**
     * Default constructor.
     */
    public BorderSkin() {
        font = getThemeFont().deriveFont(Font.BOLD);

        // Note: these get overridden by "setDefaultStyles" in "install"
        setBackgroundColor(defaultBackgroundColor());
        color = defaultForegroundColor();
        titleColor = defaultForegroundColor();
    }

    /**
     * @return Our component, cast to a {@link Border} object, for use internally.
     */
    private Border getBorder() {
        return (Border) getComponent();
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        setDefaultStyles();

        Border border = (Border) component;
        border.getBorderListeners().add(this);

        calculateTitleSize();
    }

    /**
     * @return The total padding width plus twice the thickness, for use in
     * width calculations.
     */
    private int paddingThicknessWidth() {
        return padding.getWidth() + (thickness * 2);
    }

    /**
     * @return The total padding height plus top and bottom thickenss, for use in
     * height calculations.
     */
    private int paddingThicknessHeight() {
        return padding.getHeight() + (topThickness + thickness);
    }

    @Override
    public int getPreferredWidth(final int trialHeight) {
        int preferredWidth = 0;

        Border border = getBorder();

        String title = border.getTitle();
        if (title != null && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(title, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth());
        }

        Component content = border.getContent();
        if (content != null) {
            int heightUpdated = trialHeight;
            if (heightUpdated != -1) {
                heightUpdated = Math.max(heightUpdated - paddingThicknessHeight(), 0);
            }

            preferredWidth = Math.max(preferredWidth, content.getPreferredWidth(heightUpdated));
        }

        preferredWidth += paddingThicknessWidth();

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int trialWidth) {
        int preferredHeight = 0;

        Border border = getBorder();

        Component content = border.getContent();
        if (content != null) {
            int widthUpdated = trialWidth;
            if (widthUpdated != -1) {
                widthUpdated = Math.max(widthUpdated - paddingThicknessWidth(), 0);
            }

            preferredHeight = content.getPreferredHeight(widthUpdated);
        }

        preferredHeight += paddingThicknessHeight();

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Border border = getBorder();

        String title = border.getTitle();
        if (title != null && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(title, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth());
        }

        Component content = border.getContent();
        if (content != null) {
            Dimensions preferredSize = content.getPreferredSize();
            preferredWidth = Math.max(preferredWidth, preferredSize.width);
            preferredHeight += preferredSize.height;
        }

        preferredWidth += paddingThicknessWidth();
        preferredHeight += paddingThicknessHeight();

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(final int trialWidth, final int trialHeight) {
        int baseline = -1;

        Border border = getBorder();

        // Delegate baseline calculation to the content component
        Component content = border.getContent();
        if (content != null) {
            int clientWidth = Math.max(trialWidth - paddingThicknessWidth(), 0);
            int clientHeight = Math.max(trialHeight - paddingThicknessHeight(), 0);

            baseline = content.getBaseline(clientWidth, clientHeight);
        }

        // Include top padding value and top border thickness
        if (baseline != -1) {
            baseline += (padding.top + topThickness);
        }

        return baseline;
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Border border = getBorder();

        Component content = border.getContent();
        if (content != null) {
            content.setLocation(padding.left + thickness, padding.top + topThickness);

            int contentWidth = Math.max(width - paddingThicknessWidth(), 0);
            int contentHeight = Math.max(height - paddingThicknessHeight(), 0);

            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        Border border = getBorder();

        String title = border.getTitle();

        // TODO Java2D doesn't support variable corner radii; we'll need to
        // "fake" this by drawing multiple arcs
        int cornerRadius = cornerRadii.topLeft;

        int width = getWidth();
        int height = getHeight();

        int strokeX = thickness / 2;
        int strokeY = topThickness / 2;
        int strokeWidth = Math.max(width - thickness, 0);
        int strokeHeight = Math.max(height - (int) Math.ceil((topThickness + thickness) * 0.5), 0);

        // Draw the background
        Paint backgroundPaint = getBackgroundPaint();
        if (backgroundPaint != null) {
            graphics.setPaint(backgroundPaint);

            if (cornerRadius > 0) {
                GraphicsUtilities.setAntialiasingOn(graphics);

                graphics.fillRoundRect(strokeX, strokeY, strokeWidth, strokeHeight,
                    cornerRadius, cornerRadius);

                GraphicsUtilities.setAntialiasingOff(graphics);
            } else {
                graphics.fillRect(strokeX, strokeY, strokeWidth, strokeHeight);
            }
        }

        // Draw the title
        if (title != null) {
            FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics, font, titleColor);

            // Note that we add one pixel to the string bounds for spacing
            Rectangle2D titleBounds = font.getStringBounds(title, fontRenderContext);
            titleBounds = new Rectangle2D.Double(
                padding.left + thickness, (topThickness - titleBounds.getHeight()) / 2,
                titleBounds.getWidth() + 1, titleBounds.getHeight());

            graphics.drawString(title, (int) titleBounds.getX(), (int) (titleBounds.getY() + titleAscent));

            Area titleClip = new Area(graphics.getClip());
            titleClip.subtract(new Area(titleBounds));
            graphics.clip(titleClip);
        }

        // Draw the border
        if (thickness > 0 && !themeIsFlat()) {
            graphics.setPaint(color);

            if (cornerRadius > 0) {
                GraphicsUtilities.setAntialiasingOn(graphics);

                graphics.setStroke(new BasicStroke(thickness));
                graphics.draw(new RoundRectangle2D.Double(0.5 * thickness, 0.5 * topThickness,
                    strokeWidth, strokeHeight, cornerRadius, cornerRadius));

                GraphicsUtilities.setAntialiasingOff(graphics);
            } else {
                int y = (topThickness - thickness) / 2;
                GraphicsUtilities.drawRect(graphics, 0, y, width, Math.max(height - y, 0),
                    thickness);
            }
        }
    }

    /**
     * Redo the top thickness calculation when the title, font, or thickness changes.
     * <p> Caches the {@link #topThickness} and {@link #titleAscent} values for painting.
     */
    private void calculateTitleSize() {
        topThickness = thickness;
        titleAscent = 0.0f;

        String title = getBorder().getTitle();
        if (title != null && title.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(title, fontRenderContext);
            titleAscent = lm.getAscent();
            topThickness = Math.max((int) Math.ceil(lm.getHeight()), topThickness);
        }
    }

    /**
     * @return The font used in rendering the title.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the title.
     *
     * @param fontValue The new font to use for the border title of a type supported by
     * {@link fontFromObject(Object)}.
     */
    public void setFont(final Object fontValue) {
        font = fontFromObject(fontValue);
        calculateTitleSize();
        invalidateComponent();
    }

    /**
     * @return The color of the border.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the border.
     *
     * @param colorValue The new color for the border.
     */
    public void setColor(final Object colorValue) {
        color = colorFromObject(colorValue, "color");
        repaintComponent();
    }

    /**
     * @return The color for the title on the border.
     */
    public Color getTitleColor() {
        return titleColor;
    }

    /**
     * Sets the color for the border title.
     *
     * @param colorValue The new color for the title.
     */
    public void setTitleColor(final Object colorValue) {
        titleColor = colorFromObject(colorValue, "titleColor");
        repaintComponent();
    }

    /**
     * @return The thickness of the border.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness of the border.
     *
     * @param thicknessValue The border thickness (in pixels).
     */
    public void setThickness(final int thicknessValue) {
        Utils.checkNonNegative(thicknessValue, "thickness");

        thickness = thicknessValue;
        calculateTitleSize();
        invalidateComponent();
    }

    /**
     * Sets the thickness of the border.
     *
     * @param thicknessValue The border thickness (integer value in pixels).
     */
    public void setThickness(final Number thicknessValue) {
        Utils.checkNull(thicknessValue, "thickness");

        setThickness(thicknessValue.intValue());
    }

    /**
     * @return The amount of space between the edge of the Border and its content.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the Border and its content.
     *
     * @param paddingValues The set of padding values of any type supported by
     * {@link Insets#fromObject}.
     */
    public void setPadding(final Object paddingValues) {
        padding = Insets.fromObject(paddingValues, "padding");
        invalidateComponent();
    }

    /**
     * @return A {@link CornerRadii}, describing the radius of each of the
     * Border's corners.
     */
    public CornerRadii getCornerRadii() {
        return cornerRadii;
    }

    /**
     * Sets the radii of the Border's corners.
     *
     * @param cornerRadiiValues The radii for each of the corners of any type
     * supported by {@link CornerRadii#fromObject}.
     */
    public void setCornerRadii(final Object cornerRadiiValues) {
        cornerRadii = CornerRadii.fromObject(cornerRadiiValues);
        repaintComponent();
    }

    // Border events
    @Override
    public void titleChanged(final Border border, final String previousTitle) {
        calculateTitleSize();
        invalidateComponent();
    }

    @Override
    public void contentChanged(final Border border, final Component previousContent) {
        invalidateComponent();
    }
}
