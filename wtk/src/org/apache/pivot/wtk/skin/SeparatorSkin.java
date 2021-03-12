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
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.SeparatorListener;
import org.apache.pivot.wtk.Theme;

/**
 * Separator skin.
 */
public class SeparatorSkin extends ComponentSkin implements SeparatorListener {
    /** Current font used to render any text for the separator. */
    private Font font;
    /** Color used to draw the separator. */
    private Color color;
    /** Color used to paint the heading text. */
    private Color headingColor;
    /** Thickness of the separator line. */
    private int thickness;
    /** Padding around the separator. */
    private Insets padding;

    /** Construct and set defaults. */
    public SeparatorSkin() {
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        setDefaultStyles();

        Separator separator = (Separator) component;
        separator.getSeparatorListeners().add(this);
    }

    @Override
    public int getPreferredWidth(final int height) {
        int preferredWidth = 0;

        Separator separator = (Separator) getComponent();
        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(heading, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth()) + padding.getWidth();
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int preferredHeight = thickness;

        Separator separator = (Separator) getComponent();
        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(heading, fontRenderContext);
            preferredHeight = Math.max(
                (int) Math.ceil(lm.getAscent() + lm.getDescent() + lm.getLeading()),
                preferredHeight);
        }

        preferredHeight += padding.getHeight();

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = thickness;

        Separator separator = (Separator) getComponent();
        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            Rectangle2D headingBounds = font.getStringBounds(heading, fontRenderContext);
            LineMetrics lm = font.getLineMetrics(heading, fontRenderContext);
            preferredWidth = (int) Math.ceil(headingBounds.getWidth());
            preferredHeight = Math.max(
                (int) Math.ceil(lm.getAscent() + lm.getDescent() + lm.getLeading()),
                preferredHeight);
        }

        preferredHeight += padding.getHeight();
        preferredWidth += padding.getWidth();

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(final Graphics2D graphics) {
        Separator separator = (Separator) getComponent();
        int width = getWidth();
        int separatorY = padding.top;

        String heading = separator.getHeading();

        if (heading != null && heading.length() > 0) {
            FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics, font, headingColor);
            LineMetrics lm = font.getLineMetrics(heading, fontRenderContext);

            graphics.drawString(heading, padding.left, lm.getAscent() + padding.top);

            Rectangle2D headingBounds = font.getStringBounds(heading, fontRenderContext);

            Area titleClip = new Area(graphics.getClip());
            titleClip.subtract(new Area(new Rectangle2D.Double(padding.left, padding.top,
                headingBounds.getWidth() + padding.right, headingBounds.getHeight())));
            graphics.clip(titleClip);

            separatorY += (lm.getAscent() + lm.getDescent()) / 2 + 1;
        }

        graphics.setStroke(new BasicStroke(thickness));
        graphics.setColor(color);
        graphics.drawLine(0, separatorY, width, separatorY);
    }

    /**
     * @return {@code false}; spacers are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    /**
     * @return The font used in rendering the Separator's heading.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the Separator's heading.
     *
     * @param newFont The new font for the heading.
     */
    public void setFont(final Font newFont) {
        Utils.checkNull(newFont, "font");

        this.font = newFont;
        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the Separator's heading.
     *
     * @param fontString A {@linkplain ComponentSkin#decodeFont(String) font specification}.
     */
    public final void setFont(final String fontString) {
        setFont(decodeFont(fontString));
    }

    /**
     * Sets the font used in rendering the Separator's heading.
     *
     * @param fontDictionary A dictionary {@link Theme#deriveFont describing a font}.
     */
    public final void setFont(final Dictionary<String, ?> fontDictionary) {
        setFont(Theme.deriveFont(fontDictionary));
    }

    /**
     * @return The color of the Separator's horizontal rule.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the Separator's horizontal rule.
     *
     * @param newColor The new color for the horizontal rule.
     */
    public void setColor(final Color newColor) {
        Utils.checkNull(newColor, "color");

        this.color = newColor;
        repaintComponent();
    }

    /**
     * Sets the color of the Separator's horizontal rule.
     *
     * @param colorString Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(final String colorString) {
        setColor(GraphicsUtilities.decodeColor(colorString, "color"));
    }

    /**
     * @return The color of the text in the heading.
     */
    public Color getHeadingColor() {
        return headingColor;
    }

    /**
     * Sets the color of the text in the heading.
     *
     * @param newHeadingColor The new color for the heading text.
     */
    public void setHeadingColor(final Color newHeadingColor) {
        Utils.checkNull(newHeadingColor, "headingColor");

        this.headingColor = newHeadingColor;
        repaintComponent();
    }

    /**
     * Sets the color of the text in the heading.
     *
     * @param headingColorString Any of the {@linkplain GraphicsUtilities#decodeColor
     * color values recognized by Pivot}.
     */
    public final void setHeadingColor(final String headingColorString) {
        setHeadingColor(GraphicsUtilities.decodeColor(headingColorString, "headingColor"));
    }

    /**
     * @return The thickness of the Separator's horizontal rule.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Sets the thickness of the Separator's horizontal rule.
     *
     * @param newThickness The new rule thickness (in pixels).
     */
    public void setThickness(final int newThickness) {
        Utils.checkNonNegative(newThickness, "thickness");

        this.thickness = newThickness;
        invalidateComponent();
    }

    /**
     * Sets the thickness of the Separator's horizontal rule.
     *
     * @param newThickness The new integer value for the rule thickness (in pixels).
     */
    public final void setThickness(final Number newThickness) {
        Utils.checkNull(newThickness, "thickness");

        setThickness(newThickness.intValue());
    }

    /**
     * @return The amount of space surrounding (left/right) the Separator's
     * heading, and above and below the entire component.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param paddingInsets The new padding values.
     */
    public void setPadding(final Insets paddingInsets) {
        Utils.checkNull(paddingInsets, "padding");

        this.padding = paddingInsets;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param paddingDictionary A dictionary with keys in the set {left, top, bottom,
     * right}.
     */
    public final void setPadding(final Dictionary<String, ?> paddingDictionary) {
        setPadding(new Insets(paddingDictionary));
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param paddingSequence A sequence of values in the order of [top, left, bottom, right].
     */
    public final void setPadding(final Sequence<?> paddingSequence) {
        setPadding(new Insets(paddingSequence));
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param paddingValue The new single padding value for all areas.
     */
    public final void setPadding(final int paddingValue) {
        setPadding(new Insets(paddingValue));
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param paddingValue The new integer value to use for padding in all areas.
     */
    public final void setPadding(final Number paddingValue) {
        setPadding(new Insets(paddingValue));
    }

    /**
     * Sets the amount of space to leave around the Separator's heading, and
     * above and below the entire component.
     *
     * @param paddingString A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setPadding(final String paddingString) {
        setPadding(Insets.decode(paddingString));
    }

    // Separator events
    @Override
    public void headingChanged(final Separator separator, final String previousHeading) {
        invalidateComponent();
    }

}
