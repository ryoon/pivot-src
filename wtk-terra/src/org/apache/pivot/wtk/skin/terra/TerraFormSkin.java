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
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.FormAttributeListener;
import org.apache.pivot.wtk.FormListener;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.effects.Decorator;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Terra form skin. <p> TODO Animate preferred size calculations when flags
 * change (make this configurable via a style flag)
 */
public class TerraFormSkin extends ContainerSkin implements FormListener, FormAttributeListener {
    /**
     * The decorator for the popup flags.
     */
    private class PopupFieldIndicatorDecorator implements Decorator {
        private Graphics2D graphics = null;

        @Override
        public Graphics2D prepare(final Component component, final Graphics2D graphicsArgument) {
            this.graphics = graphicsArgument;
            return graphicsArgument;
        }

        @Override
        public void update() {
            GeneralPath arrow = new GeneralPath(Path2D.WIND_EVEN_ODD);
            arrow.moveTo(POPUP_FIELD_INDICATOR_OFFSET, 0);
            arrow.lineTo(POPUP_FIELD_INDICATOR_OFFSET + POPUP_FIELD_INDICATOR_WIDTH / 2,
                -POPUP_FIELD_INDICATOR_HEIGHT);
            arrow.lineTo(POPUP_FIELD_INDICATOR_OFFSET + POPUP_FIELD_INDICATOR_WIDTH, 0);
            arrow.closePath();

            GraphicsUtilities.setAntialiasingOn(graphics);

            graphics.setStroke(new BasicStroke(0));
            graphics.setColor(flagMessageWindow.getStyleColor(Style.backgroundColor));

            graphics.draw(arrow);
            graphics.fill(arrow);

            graphics = null;
        }

        @Override
        public Bounds getBounds(final Component component) {
            return new Bounds(POPUP_FIELD_INDICATOR_OFFSET, -POPUP_FIELD_INDICATOR_HEIGHT,
                POPUP_FIELD_INDICATOR_WIDTH, POPUP_FIELD_INDICATOR_HEIGHT);
        }

        @Override
        public AffineTransform getTransform(final Component component) {
            return new AffineTransform();
        }
    }

    /**
     * Decorator for the inline flags.
     */
    private class InlineFlagMessageDecorator implements Decorator {
        private Graphics2D graphics = null;

        @Override
        public Graphics2D prepare(final Component component, final Graphics2D graphicsArgument) {
            this.graphics = graphicsArgument;
            return graphicsArgument;
        }

        @Override
        public void update() {
            if (showFlagMessagesInline) {
                Form form = getForm();
                Form.SectionSequence sections = form.getSections();

                for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount;
                    sectionIndex++) {
                    Form.Section section = sections.get(sectionIndex);

                    for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                        Component field = section.get(fieldIndex);

                        if (field.isVisible()) {
                            Form.Flag flag = Form.getFlag(field);

                            if (flag != null) {
                                String message = flag.getMessage();
                                MessageType messageType = flag.getMessageType();
                                Color messageColor = null;
                                Color messageBackgroundColor = null;

                                switch (messageType) {
                                    case ERROR:
                                        messageColor = errorMessageColor;
                                        messageBackgroundColor = errorMessageBackgroundColor;
                                        break;

                                    case WARNING:
                                        messageColor = warningMessageColor;
                                        messageBackgroundColor = warningMessageBackgroundColor;
                                        break;

                                    case QUESTION:
                                        messageColor = questionMessageColor;
                                        messageBackgroundColor = questionMessageBackgroundColor;
                                        break;

                                    case INFO:
                                        messageColor = infoMessageColor;
                                        messageBackgroundColor = infoMessageBackgroundColor;
                                        break;

                                    default:
                                        break;
                                }

                                // Draw the label
                                flagMessageLabel.setText(message);
                                flagMessageLabel.setSize(flagMessageLabel.getPreferredSize());
                                flagMessageLabel.validate();
                                flagMessageLabel.putStyle(Style.color, messageColor);
                                flagMessageLabel.putStyle(Style.backgroundColor,
                                    messageBackgroundColor);

                                int flagMessageX = field.getX() + field.getWidth()
                                    + INLINE_FIELD_INDICATOR_WIDTH - 2;
                                int flagMessageY = field.getY() - field.getBaseline()
                                    + flagMessageLabel.getBaseline();

                                graphics.translate(flagMessageX, flagMessageY);
                                flagMessageLabel.paint(graphics);

                                // Draw the arrow
                                GeneralPath arrow = new GeneralPath(Path2D.WIND_EVEN_ODD);
                                arrow.moveTo(0, 0);
                                arrow.lineTo(-INLINE_FIELD_INDICATOR_WIDTH,
                                    (float) flagMessageLabel.getHeight() / 2);
                                arrow.lineTo(0, flagMessageLabel.getHeight());
                                arrow.closePath();

                                GraphicsUtilities.setAntialiasingOn(graphics);

                                graphics.setColor(messageBackgroundColor);
                                graphics.fill(arrow);

                                // Restore the graphics context
                                graphics.translate(-flagMessageX, -flagMessageY);
                            }
                        }
                    }
                }
            }

            graphics = null;
        }

        @Override
        public Bounds getBounds(final Component component) {
            return new Bounds(0, 0, component.getWidth(), component.getHeight());
        }

        @Override
        public AffineTransform getTransform(final Component component) {
            return new AffineTransform();
        }
    }

    private ArrayList<Separator> separators = new ArrayList<>();
    private ArrayList<ArrayList<Label>> labels = new ArrayList<>();

    private Label flagMessageLabel = new Label();
    private Window flagMessageWindow = new Window(flagMessageLabel);

    private Insets padding;
    private int horizontalSpacing;
    private int verticalSpacing;
    private int flagIconOffset;
    private boolean fill;
    private boolean showFlagIcons;
    private boolean showFlagHighlight;
    private boolean showFlagMessagesInline;
    private boolean leftAlignLabels;
    private String delimiter;
    private Font labelFont;
    private Image errorIcon = null;
    private Color errorMessageColor = null;
    private Color errorMessageBackgroundColor = null;
    private Color errorHighlightColor = null;
    private Image warningIcon = null;
    private Color warningMessageColor = null;
    private Color warningMessageBackgroundColor = null;
    private Color warningHighlightColor = null;
    private Image questionIcon = null;
    private Color questionMessageColor = null;
    private Color questionMessageBackgroundColor = null;
    private Color questionHighlightColor = null;
    private Image infoIcon = null;
    private Color infoMessageColor = null;
    private Color infoMessageBackgroundColor = null;
    private Color infoHighlightColor = null;
    private Color separatorColor = null;
    private Color separatorHeadingColor = null;

    private int maximumFlagImageWidth = 0;

    private ComponentMouseListener fieldMouseListener = new ComponentMouseListener() {
        @Override
        public void mouseOver(final Component component) {
            if (!showFlagMessagesInline) {
                Form.Flag flag = Form.getFlag(component);

                if (flag != null) {
                    String message = flag.getMessage();

                    if (message != null) {
                        flagMessageLabel.setText(message);

                        MessageType messageType = flag.getMessageType();

                        Color color = null;
                        Color backgroundColor = null;
                        switch (messageType) {
                            case ERROR:
                                color = errorMessageColor;
                                backgroundColor = errorMessageBackgroundColor;
                                break;

                            case WARNING:
                                color = warningMessageColor;
                                backgroundColor = warningMessageBackgroundColor;
                                break;

                            case QUESTION:
                                color = questionMessageColor;
                                backgroundColor = questionMessageBackgroundColor;
                                break;

                            case INFO:
                                color = infoMessageColor;
                                backgroundColor = infoMessageBackgroundColor;
                                break;

                            default:
                                break;
                        }

                        flagMessageLabel.putStyle(Style.color, color);
                        flagMessageWindow.putStyle(Style.backgroundColor, backgroundColor);

                        // Open the window
                        Point location = component.mapPointToAncestor(component.getDisplay(), 0,
                            component.getHeight());

                        int y = location.y + POPUP_FIELD_INDICATOR_HEIGHT - 4;
                        if (showFlagHighlight) {
                            y += FLAG_HIGHLIGHT_PADDING;
                        }

                        flagMessageWindow.setLocation(location.x, y);
                        flagMessageWindow.open(component.getWindow());
                    }
                }
            }
        }

        @Override
        public void mouseOut(final Component component) {
            flagMessageWindow.close();
        }
    };

    private static final int FLAG_HIGHLIGHT_PADDING = 2;

    private static final int POPUP_FIELD_INDICATOR_WIDTH = 13;
    private static final int POPUP_FIELD_INDICATOR_HEIGHT = 6;
    private static final int POPUP_FIELD_INDICATOR_OFFSET = 10;
    private static final int HIDE_POPUP_MESSAGE_DELAY = 3500;

    private static final int INLINE_FIELD_INDICATOR_WIDTH = 9;

    private static final String DEFAULT_DELIMITER = ":";

    public TerraFormSkin() {
        padding = new Insets(4);
        horizontalSpacing = 6;
        verticalSpacing = 6;
        flagIconOffset = 4;
        fill = false;
        showFlagIcons = true;
        showFlagHighlight = true;
        showFlagMessagesInline = false;
        leftAlignLabels = false;
        delimiter = DEFAULT_DELIMITER;

        // Get theme icons/colors
        TerraTheme theme = (TerraTheme) Theme.getTheme();

        Font themeFont = theme.getFont();
        labelFont = themeFont;
        flagMessageLabel.putStyle(Style.font, themeFont);

        errorIcon = theme.getSmallMessageIcon(MessageType.ERROR);
        errorMessageColor = theme.getColor(4);
        errorMessageBackgroundColor = theme.getColor(22);
        errorHighlightColor = theme.getColor(21);

        warningIcon = theme.getSmallMessageIcon(MessageType.WARNING);
        warningMessageColor = theme.getColor(1);
        warningMessageBackgroundColor = theme.getColor(19);
        warningHighlightColor = theme.getColor(18);

        questionIcon = theme.getSmallMessageIcon(MessageType.QUESTION);
        questionMessageColor = theme.getColor(4);
        questionMessageBackgroundColor = theme.getColor(14);
        questionHighlightColor = theme.getColor(13);

        infoIcon = theme.getSmallMessageIcon(MessageType.INFO);
        infoMessageColor = theme.getColor(1);
        infoMessageBackgroundColor = theme.getColor(10);
        infoHighlightColor = theme.getColor(9);

        separatorColor = theme.getColor(7);
        separatorHeadingColor = theme.getColor(12);

        // Determine maximum icon size
        maximumFlagImageWidth = Math.max(maximumFlagImageWidth, errorIcon.getWidth());
        maximumFlagImageWidth = Math.max(maximumFlagImageWidth, warningIcon.getWidth());
        maximumFlagImageWidth = Math.max(maximumFlagImageWidth, questionIcon.getWidth());
        maximumFlagImageWidth = Math.max(maximumFlagImageWidth, infoIcon.getWidth());

        // Create the flag message popup
        flagMessageLabel.putStyle(Style.padding, new Insets(3, 4, 3, 4));

        if (!themeIsFlat()) {
            flagMessageWindow.getDecorators().add(new DropShadowDecorator());
        }
        flagMessageWindow.getDecorators().add(new PopupFieldIndicatorDecorator());

        flagMessageWindow.getWindowStateListeners().add(new WindowStateListener() {
            private ApplicationContext.ScheduledCallback scheduledHideFlagMessageCallback = null;

            @Override
            public void windowOpened(final Window window) {
                // Set a timer to hide the message
                Runnable hideFlagMessageCallback = new Runnable() {
                    @Override
                    public void run() {
                        flagMessageWindow.close();
                    }
                };

                scheduledHideFlagMessageCallback = ApplicationContext.scheduleCallback(
                    hideFlagMessageCallback, HIDE_POPUP_MESSAGE_DELAY);
            }

            @Override
            public void windowClosed(final Window window, final Display display, final Window owner) {
                scheduledHideFlagMessageCallback.cancel();
            }
        });
    }

    private Form getForm() {
        return (Form) getComponent();
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        Form form = (Form) component;
        form.getFormListeners().add(this);
        form.getFormAttributeListeners().add(this);

        Form.SectionSequence sections = form.getSections();
        for (int i = 0, n = sections.getLength(); i < n; i++) {
            insertSection(sections.get(i), i);
        }

        form.getDecorators().add(new InlineFlagMessageDecorator());
    }

    @Override
    public int getPreferredWidth(final int height) {
        int preferredWidth = 0;

        int maximumLabelWidth = 0;
        int maximumFieldWidth = 0;
        int maximumSeparatorWidth = 0;

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            if (sectionIndex > 0 || section.getHeading() != null) {
                Separator separator = separators.get(sectionIndex);
                maximumSeparatorWidth = Math.max(maximumSeparatorWidth,
                    separator.getPreferredWidth());
            }

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    maximumLabelWidth = Math.max(maximumLabelWidth, label.getPreferredWidth());

                    int fieldWidth = field.getPreferredWidth();

                    if (showFlagMessagesInline) {
                        // Calculate maximum flag message width
                        Form.Flag flag = Form.getFlag(field);

                        if (flag != null) {
                            String message = flag.getMessage();

                            if (message != null) {
                                flagMessageLabel.setText(message);
                                fieldWidth += (INLINE_FIELD_INDICATOR_WIDTH - 2)
                                    + flagMessageLabel.getPreferredWidth();
                            }
                        }
                    }

                    maximumFieldWidth = Math.max(maximumFieldWidth, fieldWidth);
                }
            }
        }

        preferredWidth = maximumLabelWidth + horizontalSpacing + maximumFieldWidth;

        if (showFlagIcons) {
            preferredWidth += maximumFlagImageWidth + flagIconOffset;
        }

        preferredWidth = Math.max(preferredWidth + padding.left + padding.right,
            maximumSeparatorWidth);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int preferredHeight = 0;

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        // Determine the field width constraint
        int fieldWidth = (fill && width != -1) ? getFieldWidth(width) : -1;

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            if (sectionIndex > 0 || section.getHeading() != null) {
                Separator separator = separators.get(sectionIndex);
                preferredHeight += separator.getPreferredHeight(width);
                preferredHeight += verticalSpacing;
            }

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    // Determine the label size and baseline
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    Dimensions labelSize = label.getPreferredSize();
                    int labelAscent = label.getBaseline(labelSize.width, labelSize.height);
                    int labelDescent = labelSize.height - labelAscent;

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill && fieldWidth != -1) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = fieldSize.height;
                    }

                    int fieldDescent = fieldSize.height - fieldAscent;

                    // Determine the row height
                    int maximumAscent = Math.max(labelAscent, fieldAscent);
                    int maximumDescent = Math.max(labelDescent, fieldDescent);
                    int rowHeight = maximumAscent + maximumDescent;

                    preferredHeight += rowHeight;

                    if (fieldIndex > 0) {
                        preferredHeight += verticalSpacing;
                    }
                }
            }
        }

        preferredHeight += (padding.top + padding.bottom);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public int getBaseline(final int width, final int height) {
        int baseline = -1;

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        // Determine the field width constraint
        int fieldWidth = (fill) ? getFieldWidth(width) : -1;

        int sectionCount = sections.getLength();
        int sectionIndex = 0;

        int rowY = 0;
        while (sectionIndex < sectionCount && baseline == -1) {
            Form.Section section = sections.get(sectionIndex);

            if (sectionIndex > 0 || section.getHeading() != null) {
                Separator separator = separators.get(sectionIndex);
                rowY += separator.getPreferredHeight(width);
                rowY += verticalSpacing;
            }

            int fieldCount = section.getLength();
            int fieldIndex = 0;

            while (fieldIndex < fieldCount && baseline == -1) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    // Determine the label size and baseline
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    Dimensions labelSize = label.getPreferredSize();
                    int labelAscent = label.getBaseline(labelSize.width, labelSize.height);

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill && fieldWidth != -1) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = labelAscent;
                    }

                    // Determine the baseline
                    int maximumAscent = Math.max(labelAscent, fieldAscent);
                    baseline = rowY + maximumAscent;
                }

                fieldIndex++;
            }

            sectionIndex++;
        }

        baseline += padding.top;

        return baseline;
    }

    private int getFieldWidth(final int width) {
        int maximumLabelWidth = 0;
        int maximumFlagMessageWidth = 0;

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    maximumLabelWidth = Math.max(maximumLabelWidth, label.getPreferredWidth());

                    if (showFlagMessagesInline) {
                        // Calculate maximum flag message width
                        Form.Flag flag = Form.getFlag(field);

                        if (flag != null) {
                            String message = flag.getMessage();

                            if (message != null) {
                                flagMessageLabel.setText(message);
                                maximumFlagMessageWidth = Math.max(maximumFlagMessageWidth,
                                    flagMessageLabel.getPreferredWidth());
                            }
                        }
                    }
                }
            }
        }

        int fieldWidth = Math.max(0, width - (maximumLabelWidth + horizontalSpacing));

        if (showFlagIcons) {
            fieldWidth = Math.max(0, fieldWidth - (maximumFlagImageWidth + flagIconOffset));
        }

        if (showFlagMessagesInline) {
            fieldWidth = Math.max(0, fieldWidth
                - (maximumFlagMessageWidth + (INLINE_FIELD_INDICATOR_WIDTH - 2)));
        }

        fieldWidth = Math.max(0, fieldWidth - (padding.left + padding.right));

        return fieldWidth;
    }

    @Override
    public void layout() {
        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        // Determine the maximum label and flag message width
        int maximumLabelWidth = 0;
        int maximumFlagMessageWidth = 0;

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    Label label = labels.get(sectionIndex).get(fieldIndex);
                    maximumLabelWidth = Math.max(maximumLabelWidth, label.getPreferredWidth());

                    if (showFlagMessagesInline) {
                        // Calculate maximum flag message width
                        Form.Flag flag = Form.getFlag(field);

                        if (flag != null) {
                            String message = flag.getMessage();

                            if (message != null) {
                                flagMessageLabel.setText(message);
                                maximumFlagMessageWidth = Math.max(maximumFlagMessageWidth,
                                    flagMessageLabel.getPreferredWidth());
                            }
                        }
                    }
                }
            }
        }

        // Determine the field width
        int width = getWidth();
        int fieldWidth = Math.max(0, width - (maximumLabelWidth + horizontalSpacing));

        if (showFlagIcons) {
            fieldWidth = Math.max(0, fieldWidth - (maximumFlagImageWidth + flagIconOffset));
        }

        if (showFlagMessagesInline) {
            fieldWidth = Math.max(0, fieldWidth
                - (maximumFlagMessageWidth + (INLINE_FIELD_INDICATOR_WIDTH - 2)));
        }

        fieldWidth = Math.max(0, fieldWidth - (padding.left + padding.right));

        // Lay out the components
        int rowY = padding.top;

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            Separator separator = separators.get(sectionIndex);
            if (sectionIndex > 0 || section.getHeading() != null) {
                int separatorWidth = Math.max(width - (padding.left + padding.right), 0);
                separator.setVisible(true);
                separator.setSize(separatorWidth, separator.getPreferredHeight(separatorWidth));
                separator.setLocation(padding.left, rowY);
                rowY += separator.getHeight();
            } else {
                separator.setVisible(false);
            }

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Label label = labels.get(sectionIndex).get(fieldIndex);
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    // Show the label
                    label.setVisible(true);

                    // Determine the label size and baseline
                    Dimensions labelSize = label.getPreferredSize();
                    label.setSize(labelSize);
                    int labelAscent = label.getBaseline(labelSize.width, labelSize.height);
                    int labelDescent = labelSize.height - labelAscent;

                    // Determine the field size and baseline
                    Dimensions fieldSize;
                    if (fill) {
                        fieldSize = new Dimensions(fieldWidth, field.getPreferredHeight(fieldWidth));
                    } else {
                        fieldSize = field.getPreferredSize();
                    }

                    field.setSize(fieldSize);

                    int fieldAscent = field.getBaseline(fieldSize.width, fieldSize.height);
                    if (fieldAscent == -1) {
                        fieldAscent = labelAscent;
                    }

                    int fieldDescent = fieldSize.height - fieldAscent;

                    // Determine the baseline and row height
                    int maximumAscent = Math.max(labelAscent, fieldAscent);
                    int maximumDescent = Math.max(labelDescent, fieldDescent);

                    int baseline = maximumAscent;
                    int rowHeight = maximumAscent + maximumDescent;

                    // Position the label
                    int labelX = padding.left;
                    if (!leftAlignLabels) {
                        labelX += maximumLabelWidth - label.getWidth();
                    }

                    if (showFlagIcons) {
                        labelX += (maximumFlagImageWidth + flagIconOffset);
                    }

                    int labelY = rowY + (baseline - labelAscent);
                    label.setLocation(labelX, labelY);

                    // Position the field
                    int fieldX = padding.left + maximumLabelWidth + horizontalSpacing;
                    if (showFlagIcons) {
                        fieldX += (maximumFlagImageWidth + flagIconOffset);
                    }

                    int fieldY = rowY + (baseline - fieldAscent);
                    field.setLocation(fieldX, fieldY);

                    // Update the row y-coordinate
                    rowY += rowHeight + verticalSpacing;
                } else {
                    // Hide the label
                    label.setVisible(false);
                }
            }
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        super.paint(graphics);

        GraphicsUtilities.setAntialiasingOn(graphics);

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Component field = section.get(fieldIndex);

                if (field.isVisible()) {
                    Form.Flag flag = Form.getFlag(field);

                    if (flag != null) {
                        if (showFlagIcons) {
                            MessageType messageType = flag.getMessageType();
                            Image flagIcon = null;

                            switch (messageType) {
                                case ERROR:
                                    flagIcon = errorIcon;
                                    break;
                                case WARNING:
                                    flagIcon = warningIcon;
                                    break;
                                case QUESTION:
                                    flagIcon = questionIcon;
                                    break;
                                case INFO:
                                default:
                                    flagIcon = infoIcon;
                                    break;
                            }

                            Label label = labels.get(sectionIndex).get(fieldIndex);
                            int flagIconX = label.getX() - (flagIcon.getWidth() + flagIconOffset);
                            int flagIconY = label.getY()
                                + (label.getHeight() - flagIcon.getHeight()) / 2;

                            graphics.translate(flagIconX, flagIconY);
                            flagIcon.paint(graphics);
                            graphics.translate(-flagIconX, -flagIconY);
                        }

                        if (showFlagHighlight) {
                            MessageType messageType = flag.getMessageType();
                            Color highlightColor = null;

                            switch (messageType) {
                                case ERROR:
                                    highlightColor = errorHighlightColor;
                                    break;

                                case WARNING:
                                    highlightColor = warningHighlightColor;
                                    break;

                                case QUESTION:
                                    highlightColor = questionHighlightColor;
                                    break;

                                case INFO:
                                    highlightColor = infoHighlightColor;
                                    break;

                                default:
                                    break;
                            }

                            Bounds fieldBounds = field.getBounds();

                            graphics.setColor(highlightColor);
                            graphics.setStroke(new BasicStroke(1));
                            graphics.drawRect(fieldBounds.x - FLAG_HIGHLIGHT_PADDING, fieldBounds.y
                                - FLAG_HIGHLIGHT_PADDING, fieldBounds.width
                                + FLAG_HIGHLIGHT_PADDING * 2 - 1, fieldBounds.height
                                + FLAG_HIGHLIGHT_PADDING * 2 - 1);
                        }
                    }
                }
            }
        }
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(final Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(final Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final Number padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(final String padding) {
        setPadding(Insets.decode(padding));
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(final int horizontalSpacing) {
        Utils.checkNonNegative(horizontalSpacing, "horizontalSpacing");

        this.horizontalSpacing = horizontalSpacing;
        invalidateComponent();
    }

    public final void setHorizontalSpacing(final Number horizontalSpacing) {
        Utils.checkNull(horizontalSpacing, "horizontalSpacing");

        setHorizontalSpacing(horizontalSpacing.intValue());
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(final int verticalSpacing) {
        Utils.checkNonNegative(verticalSpacing, "verticalSpacing");

        this.verticalSpacing = verticalSpacing;
        invalidateComponent();
    }

    public final void setVerticalSpacing(final Number verticalSpacing) {
        Utils.checkNull(verticalSpacing, "verticalSpacing");

        setVerticalSpacing(verticalSpacing.intValue());
    }

    public int getFlagIconOffset() {
        return flagIconOffset;
    }

    public void setFlagIconOffset(final int flagIconOffset) {
        Utils.checkNonNegative(flagIconOffset, "flagIconOffset");

        this.flagIconOffset = flagIconOffset;
        invalidateComponent();
    }

    public final void setFlagIconOffset(final Number flagIconOffset) {
        Utils.checkNull(flagIconOffset, "flagIconOffset");

        setFlagIconOffset(flagIconOffset.intValue());
    }

    public boolean getFill() {
        return fill;
    }

    public void setFill(final boolean fill) {
        this.fill = fill;
        invalidateComponent();
    }

    public boolean getShowFlagIcons() {
        return showFlagIcons;
    }

    public void setShowFlagIcons(final boolean showFlagIcons) {
        this.showFlagIcons = showFlagIcons;
        invalidateComponent();
    }

    public boolean getShowFlagHighlight() {
        return showFlagHighlight;
    }

    public void setShowFlagHighlight(final boolean showFlagHighlight) {
        this.showFlagHighlight = showFlagHighlight;
        invalidateComponent();
    }

    public boolean getShowFlagMessagesInline() {
        return showFlagMessagesInline;
    }

    public void setShowFlagMessagesInline(final boolean showFlagMessagesInline) {
        this.showFlagMessagesInline = showFlagMessagesInline;
        invalidateComponent();
    }

    public boolean getLeftAlignLabels() {
        return leftAlignLabels;
    }

    public void setLeftAlignLabels(final boolean leftAlignLabels) {
        this.leftAlignLabels = leftAlignLabels;
        invalidateComponent();
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(final String delimiter) {
        Utils.checkNull(delimiter, "delimiter");

        this.delimiter = delimiter;

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Form.Section section = sections.get(i);

            for (int j = 0, m = section.getLength(); j < m; j++) {
                updateFieldLabel(section, j);
            }
        }

        invalidateComponent();
    }

    public Color getSeparatorColor() {
        return separatorColor;
    }

    public void setSeparatorColor(final Color separatorColor) {
        this.separatorColor = separatorColor;

        for (Separator separator : separators) {
            separator.putStyle(Style.color, separatorColor);
        }
    }

    public final void setSeparatorColor(final String separatorColor) {
        setSeparatorColor(GraphicsUtilities.decodeColor(separatorColor, "separatorColor"));
    }

    public Color getSeparatorHeadingColor() {
        return separatorHeadingColor;
    }

    public void setSeparatorHeadingColor(final Color separatorHeadingColor) {
        this.separatorHeadingColor = separatorHeadingColor;

        for (Separator separator : separators) {
            separator.putStyle(Style.headingColor, separatorHeadingColor);
        }
    }

    public final void setSeparatorHeadingColor(final String separatorHeadingColor) {
        setSeparatorHeadingColor(GraphicsUtilities.decodeColor(separatorHeadingColor, "separatorHeadingColor"));
    }

    public final Font getLabelFont() {
        return labelFont;
    }

    public final void setLabelFont(final Font font) {
        Utils.checkNull(font, "labelFont");

        labelFont = font;

        Form form = getForm();
        Form.SectionSequence sections = form.getSections();

        for (int sectionIndex = 0, sectionCount = sections.getLength(); sectionIndex < sectionCount; sectionIndex++) {
            Form.Section section = sections.get(sectionIndex);

            for (int fieldIndex = 0, fieldCount = section.getLength(); fieldIndex < fieldCount; fieldIndex++) {
                Label label = labels.get(sectionIndex).get(fieldIndex);
                label.putStyle(Style.font, labelFont);
            }
        }

        invalidateComponent();
    }

    public final void setLabelFont(final String fontString) {
        Utils.checkNull(fontString, "labelFont");

        setLabelFont(decodeFont(fontString));
    }

    public final void setLabelFont(final Dictionary<String, ?> fontDict) {
        Utils.checkNull(fontDict, "labelFont");

        setLabelFont(Theme.deriveFont(fontDict));
    }

    public final Font getMessageFont() {
        return flagMessageLabel.getStyleFont(Style.font);
    }

    public final void setMessageFont(final Font font) {
        Utils.checkNull(font, "messageFont");

        flagMessageLabel.putStyle(Style.font, font);
    }

    public final void setMessageFont(final String fontString) {
        Utils.checkNull(fontString, "messageFont");

        setMessageFont(decodeFont(fontString));
    }

    public final void setMessageFont(final Dictionary<String, ?> fontDict) {
        Utils.checkNull(fontDict, "messageFont");

        setMessageFont(Theme.deriveFont(fontDict));
    }

    // Form events
    @Override
    public void sectionInserted(final Form form, final int index) {
        insertSection(form.getSections().get(index), index);
    }

    @Override
    public void sectionsRemoved(final Form form, final int index, final Sequence<Form.Section> removed) {
        removeSections(index, removed);
    }

    @Override
    public void sectionHeadingChanged(final Form.Section section) {
        updateSectionHeading(section);
    }

    @Override
    public void fieldInserted(final Form.Section section, final int index) {
        insertField(section, section.get(index), index);
    }

    @Override
    public void fieldsRemoved(final Form.Section section, final int index, final Sequence<Component> fields) {
        Form form = getForm();
        removeFields(form.getSections().indexOf(section), index, fields);
    }

    // Form attribute events
    @Override
    public void labelChanged(final Form form, final Component field, final String previousLabel) {
        Form.Section section = Form.getSection(field);
        updateFieldLabel(section, section.indexOf(field));
    }

    @Override
    public void requiredChanged(final Form form, final Component field) {
        // No-op
    }

    @Override
    public void flagChanged(final Form form, final Component field, final Form.Flag previousFlag) {
        if (showFlagMessagesInline) {
            invalidateComponent();
        } else {
            repaintComponent();
        }
    }

    private void insertSection(final Form.Section section, final int index) {
        Form form = getForm();

        // Insert separator
        Separator separator = new Separator(section.getHeading());
        separator.putStyle(Style.color, separatorColor);
        separator.putStyle(Style.headingColor, separatorHeadingColor);

        separators.insert(separator, index);
        form.add(separator);

        // Insert label list
        ArrayList<Label> sectionLabels = new ArrayList<>();
        labels.insert(sectionLabels, index);

        // Insert fields
        for (int i = 0, n = section.getLength(); i < n; i++) {
            insertField(section, section.get(i), i);
        }

        invalidateComponent();
    }

    private void removeSections(final int index, final Sequence<Form.Section> removed) {
        Form form = getForm();
        int count = removed.getLength();

        // Remove fields
        for (int i = 0; i < count; i++) {
            removeFields(index + i, 0, removed.get(i));
        }

        // Remove labels
        labels.remove(index, count);

        // Remove separators
        Sequence<Separator> removedSeparators = separators.remove(index, count);
        for (int i = 0; i < count; i++) {
            form.remove(removedSeparators.get(i));
        }

        invalidateComponent();
    }

    private void insertField(final Form.Section section, final Component field, final int index) {
        Form form = getForm();
        int sectionIndex = form.getSections().indexOf(section);

        // Create the label
        Label label = new Label();
        label.putStyle(Style.font, labelFont);
        labels.get(sectionIndex).insert(label, index);
        form.add(label);

        // Add mouse listener
        field.getComponentMouseListeners().add(fieldMouseListener);

        // Update the field label
        updateFieldLabel(section, index);

        invalidateComponent();
    }

    private void removeFields(final int sectionIndex, final int index, final Sequence<Component> removed) {
        Form form = getForm();
        int count = removed.getLength();

        // Remove the labels
        Sequence<Label> removedLabels = labels.get(sectionIndex).remove(index, count);

        for (int i = 0; i < count; i++) {
            form.remove(removedLabels.get(i));

            // Remove mouse listener
            Component field = removed.get(i);
            field.getComponentMouseListeners().remove(fieldMouseListener);
        }

        invalidateComponent();
    }

    private void updateSectionHeading(final Form.Section section) {
        Form form = getForm();
        int sectionIndex = form.getSections().indexOf(section);

        Separator separator = separators.get(sectionIndex);
        separator.setHeading(section.getHeading());
    }

    private void updateFieldLabel(final Form.Section section, final int fieldIndex) {
        Form form = getForm();
        Component field = section.get(fieldIndex);

        int sectionIndex = form.getSections().indexOf(section);
        Label label = labels.get(sectionIndex).get(fieldIndex);
        String labelText = Form.getLabel(field);
        label.setText((labelText == null) ? "" : labelText + delimiter);
    }
}
