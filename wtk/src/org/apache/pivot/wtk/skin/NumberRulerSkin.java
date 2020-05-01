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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.StringCharacterIterator;
import java.util.Arrays;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.NumberRuler;
import org.apache.pivot.wtk.NumberRulerListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;

/**
 * Skin for the {@link NumberRuler} component, which can be used as a horizontal or
 * vertical header for viewports.
 */
public class NumberRulerSkin extends ComponentSkin implements NumberRulerListener {
    /** Number of pixels to use for major division tic marks. */
    private static final int MAJOR_SIZE = 10;
    /** Number of pixels to use for minor division tic marks. */
    private static final int MINOR_SIZE = 8;
    /** Number of pixels to use for regular tic marks. */
    private static final int REGULAR_SIZE = 5;

    private Font font;
    private Color color;
    private Color backgroundColor;
    private int padding = 2;
    private int markerSpacing;
    private Insets markerInsets;
    private Insets rowPadding;
    private int majorDivision;
    private int minorDivision;
    private boolean showZeroNumber;
    private boolean showMajorNumbers;
    private boolean showMinorNumbers;
    private float charHeight, descent;
    private int lineHeight;

    @Override
    public void install(final Component component) {
        super.install(component);

        Theme theme = Theme.getTheme();
        setFont(theme.getFont());

        setColor(0);
        setBackgroundColor(19);

        markerSpacing = 5;
        markerInsets = new Insets(0);

        rowPadding = new Insets(0);

        // Note: these aren't settable
        majorDivision = 10;
        minorDivision = 5;
        // But these are
        showZeroNumber = false;
        showMajorNumbers = true;
        showMinorNumbers = false;

        NumberRuler ruler = (NumberRuler) component;
        ruler.getRulerListeners().add(this);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public int getPreferredHeight(final int width) {
        NumberRuler ruler = (NumberRuler) getComponent();
        Orientation orientation = ruler.getOrientation();

        // Give a little extra height if showing numbers
        return (orientation == Orientation.HORIZONTAL)
            ? ((showZeroNumber || showMajorNumbers || showMinorNumbers)
                ? ((int) Math.ceil(charHeight) + MAJOR_SIZE + 5) : MAJOR_SIZE * 2) : 0;
    }

    @Override
    public int getPreferredWidth(final int height) {
        NumberRuler ruler = (NumberRuler) getComponent();
        Orientation orientation = ruler.getOrientation();

        if (orientation == Orientation.VERTICAL) {
            int textSize = ruler.getTextSize();

            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            char[] digits = new char[textSize];
            Arrays.fill(digits, '0');
            String text = new String(digits);

            Rectangle2D stringBounds = font.getStringBounds(text, fontRenderContext);
            return (int) Math.ceil(stringBounds.getWidth()) + padding;
        }

        return 0;
    }

    private void showNumber(final Graphics2D graphics, final FontRenderContext fontRenderContext,
            final int number, final int x, final int y) {
        String num = Integer.toString(number);

        StringCharacterIterator line;
        GlyphVector glyphVector;
        Rectangle2D textBounds;
        float width, height;
        float fx, fy;

        // Draw the whole number just off the tip of the line given by (x,y)
        line = new StringCharacterIterator(num);
        glyphVector = font.createGlyphVector(fontRenderContext, line);
        textBounds = glyphVector.getLogicalBounds();
        width = (float) textBounds.getWidth();
        height = (float) textBounds.getHeight();
        fx = (float) x - (width / 2.0f);
        fy = (float) (y - 2);
        graphics.drawGlyphVector(glyphVector, fx, fy);
    }

    @Override
    public void paint(final Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        int bottom = height - markerInsets.bottom;

        Rectangle clipRect = graphics.getClipBounds();

        NumberRuler ruler = (NumberRuler) getComponent();
        int textSize = ruler.getTextSize();

        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(color);

        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        graphics.setFont(font);

        Orientation orientation = ruler.getOrientation();
        Rectangle fullRect = new Rectangle(width, height);
        Rectangle clippedRect = fullRect.intersection(clipRect);

        Rectangle lineRect, clippedLineRect;

        switch (orientation) {
            case HORIZONTAL:
                int start = bottom - 1;
                int end2 = start - (MAJOR_SIZE - 1);
                int end3 = start - (MINOR_SIZE - 1);
                int end4 = start - (REGULAR_SIZE - 1);

                lineRect = new Rectangle(0, height - 1, width - 1, 0);
                clippedLineRect = lineRect.intersection(clipRect);
                graphics.drawLine(clippedLineRect.x, clippedLineRect.y,
                                  clippedLineRect.x + clippedLineRect.width, clippedLineRect.y);

                for (int i = 0, n = width / markerSpacing + 1; i < n; i++) {
                    int x = i * markerSpacing + markerInsets.left;

                    if (majorDivision != 0 && i % majorDivision == 0) {
                        graphics.drawLine(x, start, x, end2);
                        if ((showZeroNumber && i == 0) || (showMajorNumbers && i > 0)) {
                            showNumber(graphics, fontRenderContext, i, x, end2);
                        }
                    } else if (minorDivision != 0 && i % minorDivision == 0) {
                        graphics.drawLine(x, start, x, end3);
                        if ((showZeroNumber && i == 0) || (showMinorNumbers && i > 0)) {
                            // Show the minor numbers at the same y point as the major
                            showNumber(graphics, fontRenderContext, i, x, end2);
                        }
                    } else {
                        graphics.drawLine(x, start, x, end4);
                    }
                }
                break;

            case VERTICAL:
                lineRect = new Rectangle(width - 1, 0, 0, height - 1);
                clippedLineRect = lineRect.intersection(clipRect);
                graphics.drawLine(clippedLineRect.x, clippedLineRect.y,
                                  clippedLineRect.x, clippedLineRect.y + clippedLineRect.height);

                // Optimize drawing by only starting just above the current clip bounds
                // down to the bottom (plus one) of the end of the clip bounds.
                // This is a 100x speed improvement for 500,000 lines.
                int linesAbove = clipRect.y / lineHeight;
                int linesBelow = (height - (clipRect.y + clipRect.height)) / lineHeight;
                int totalLines = height / lineHeight + 1;

                for (int num = 1 + linesAbove, n = totalLines - (linesBelow - 1); num < n; num++) {
                    String numberString = Integer.toString(num);
                    StringCharacterIterator line = new StringCharacterIterator(numberString);

                    int lineY = (num - 1) * lineHeight + markerInsets.top;
                    Graphics2D lineGraphics = (Graphics2D) graphics.create(0, lineY, width, lineHeight);

                    float y = (float) (lineHeight - rowPadding.bottom) - descent;
                    GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, line);
                    Rectangle2D textBounds = glyphVector.getLogicalBounds();
                    float lineWidth = (float) textBounds.getWidth();
                    float x = (float) width - (lineWidth + (float) padding);
                    lineGraphics.drawGlyphVector(glyphVector, x, y);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void orientationChanged(final NumberRuler ruler) {
        invalidateComponent();
    }

    @Override
    public void textSizeChanged(final NumberRuler ruler, final int previousSize) {
        invalidateComponent();
    }

    /**
     * @return The insets for the markers (only applicable for horizontal orientation).
     */
    public Insets getMarkerInsets() {
        return markerInsets;
    }

    public final void setMarkerInsets(final Insets insets) {
        Utils.checkNull(insets, "markerInsets");

        this.markerInsets = insets;
        repaintComponent();
    }

    public final void setMarkerInsets(final Dictionary<String, ?> insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(final Sequence<?> insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(final int insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(final Number insets) {
        setMarkerInsets(new Insets(insets));
    }

    public final void setMarkerInsets(final String insets) {
        setMarkerInsets(Insets.decode(insets));
    }

    public final void setRowPadding(final Insets padding) {
        Utils.checkNull(padding, "rowPadding");

        this.rowPadding = padding;
        // Do the line height calculations again with this new padding
        if (this.font != null) {
            setFont(this.font);
        } else {
            lineHeight = rowPadding.getHeight();
            invalidateComponent();
        }
    }

    public final void setRowPadding(final Dictionary<String, ?> padding) {
        setRowPadding(new Insets(padding));
    }

    public final void setRowPadding(final Sequence<?> padding) {
        setRowPadding(new Insets(padding));
    }

    public final void setRowPadding(final int padding) {
        setRowPadding(new Insets(padding));
    }

    public final void setRowPadding(final Number padding) {
        setRowPadding(new Insets(padding));
    }

    public final void setRowPadding(final String padding) {
        setRowPadding(Insets.decode(padding));
    }

    /**
     * @return The number of pixels interval at which to draw markers.
     */
    public int getMarkerSpacing() {
        return markerSpacing;
    }

    /**
     * Set the number of pixels interval at which to draw the markers
     * (for horizontal orientation only).
     *
     * @param spacing The number of pixels between markers (must be &gt;= 1).
     */
    public final void setMarkerSpacing(final int spacing) {
        Utils.checkPositive(spacing, "markerSpacing");

        this.markerSpacing = spacing;
        invalidateComponent();
    }

    /**
     * Set the number of pixels interval at which to draw the markers
     * (for horizontal orientation only).
     *
     * @param spacing The integer number of pixels between markers (must be &gt;= 1).
     */
    public final void setMarkerSpacing(final Number spacing) {
        Utils.checkNull(spacing, "markerSpacing");

        setMarkerSpacing(spacing.intValue());
    }

    /**
     * @return Whether to display a number at the zero point
     * (only applicable for horizontal orientation).
     */
    public boolean getShowZeroNumber() {
        return showZeroNumber;
    }

    /**
     * Sets the flag to say whether to show a number at the zero point
     * (only for horizontal orientation).
     *
     * @param showZeroNumber Whether a number should be shown for the zero point.
     */
    public final void setShowZeroNumber(final boolean showZeroNumber) {
        this.showZeroNumber = showZeroNumber;

        NumberRuler ruler = (NumberRuler) getComponent();
        if (ruler.getOrientation() == Orientation.HORIZONTAL) {
            invalidateComponent();
        }
    }

    /**
     * @return Whether to display numbers at each major division
     * (only applicable for horizontal orientation).
     */
    public boolean getShowMajorNumbers() {
        return showMajorNumbers;
    }

    /**
     * Sets the flag to say whether to show numbers at each major division
     * (only for horizontal orientation).
     *
     * @param showMajorNumbers Whether numbers should be shown for major divisions.
     */
    public final void setShowMajorNumbers(final boolean showMajorNumbers) {
        this.showMajorNumbers = showMajorNumbers;

        NumberRuler ruler = (NumberRuler) getComponent();
        if (ruler.getOrientation() == Orientation.HORIZONTAL) {
            invalidateComponent();
        }
    }

    /**
     * @return Whether to display numbers at each minor division
     * (only for horizontal orientation).
     */
    public boolean getShowMinorNumbers() {
        return showMinorNumbers;
    }

    /**
     * Sets the flag to say whether to show numbers at each minor division
     * (for horizontal orientation only).
     *
     * @param showMinorNumbers Whether numbers should be shown for minor divisions.
     */
    public final void setShowMinorNumbers(final boolean showMinorNumbers) {
        this.showMinorNumbers = showMinorNumbers;

        NumberRuler ruler = (NumberRuler) getComponent();
        if (ruler.getOrientation() == Orientation.HORIZONTAL) {
            invalidateComponent();
        }
    }

    /**
     * @return The font used to render the Ruler's text.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font used in rendering the Ruler's text.
     *
     * @param font The new font to use.
     */
    public void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;

        // Make some size calculations for the drawing code
        FontRenderContext fontRenderContext = Platform.getFontRenderContext();
        LineMetrics lm = this.font.getLineMetrics("0", fontRenderContext);
        this.charHeight = lm.getAscent();
        this.descent = lm.getDescent();
        this.lineHeight = (int) Math.ceil(lm.getHeight())
            + (rowPadding != null ? rowPadding.getHeight() : 0);

        invalidateComponent();
    }

    /**
     * Sets the font used in rendering the Ruler's text.
     *
     * @param font A {@link ComponentSkin#decodeFont(String) font specification}
     */
    public final void setFont(final String font) {
        setFont(decodeFont(font));
    }

    /**
     * Sets the font used in rendering the Ruler's text.
     *
     * @param font A dictionary {@link Theme#deriveFont describing a font}
     */
    public final void setFont(final Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    /**
     * Returns the foreground color of the text of the ruler.
     *
     * @return The foreground (text) color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color of the text of the ruler.
     *
     * @param color The foreground (that is, the text) color.
     */
    public void setColor(final Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color of the text of the ruler.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    /**
     * Sets the foreground color of the text of the ruler to one of the
     * theme colors.
     *
     * @param color Index of the theme color to use.
     */
    public final void setColor(final int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    /**
     * Returns the background color of the ruler.
     *
     * @return The current background color.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the ruler.
     *
     * @param backgroundColor New background color value.
     */
    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    /**
     * Sets the background color of the ruler.
     *
     * @param backgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    /**
     * Sets the background color of the ruler to one of the theme colors.
     *
     * @param backgroundColor Index of the theme color to use for the background.
     */
    public final void setBackgroundColor(final int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

}
