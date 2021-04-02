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
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TabPaneAttributeListener;
import org.apache.pivot.wtk.TabPaneListener;
import org.apache.pivot.wtk.TabPaneSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.effects.ClipDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quadratic;
import org.apache.pivot.wtk.skin.ButtonSkin;
import org.apache.pivot.wtk.skin.TabPaneSkin;

/**
 * Tab pane skin.
 */
public class TerraTabPaneSkin extends TabPaneSkin implements TabPaneListener,
    TabPaneSelectionListener, TabPaneAttributeListener {
    /**
     * Tab button component.
     */
    public class TabButton extends Button {
        private final Component tab;

        public TabButton(final Component tabValue) {
            tab = tabValue;
            super.setToggleButton(true);

            setSkin(new TabButtonSkin());
        }

        @Override
        public Object getButtonData() {
            return TabPane.getTabData(tab);
        }

        @Override
        @UnsupportedOperation
        public void setButtonData(final Object buttonData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Button.DataRenderer getDataRenderer() {
            TabPane tabPane = getTabPane();
            return tabPane.getTabDataRenderer();
        }

        @Override
        @UnsupportedOperation
        public void setDataRenderer(final Button.DataRenderer dataRenderer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTooltipText() {
            return TabPane.getTooltipText(tab);
        }

        @Override
        @UnsupportedOperation
        public void setTooltipText(final String tooltipText) {
            throw new UnsupportedOperationException();
        }

        @Override
        @UnsupportedOperation
        public void setToggleButton(final boolean toggleButton) {
            throw new UnsupportedOperationException();
        }

        @Override
        @UnsupportedOperation
        public void setTriState(final boolean triState) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void press() {
            // If the tab pane is collapsible, toggle the button selection;
            // otherwise, select it
            TabPane tabPane = getTabPane();
            setSelected(tabPane.isCollapsible() ? !isSelected() : true);
            super.press();
        }
    }

    /**
     * Tab button skin. <p> Note that this class does not respect preferred size
     * constraints, because it will never be called to use them.
     */
    public class TabButtonSkin extends ButtonSkin {
        private TabButton getTabButton() {
            return (TabButton) getComponent();
        }

        @Override
        public int getPreferredWidth(final int height) {
            Dimensions preferredSize = getPreferredSize();
            return preferredSize.width;
        }

        @Override
        public int getPreferredHeight(final int width) {
            Dimensions preferredSize = getPreferredSize();
            return preferredSize.height;
        }

        @Override
        public Dimensions getPreferredSize() {
            TabButton tabButton = getTabButton();
            TabPane tabPane = getTabPane();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Dimensions preferredContentSize = dataRenderer.getPreferredSize();

            int preferredWidth = 0;
            int preferredHeight = 0;

            switch (tabOrientation) {
                case HORIZONTAL:
                    preferredWidth = preferredContentSize.width + buttonPadding.getWidth() + 2;
                    preferredHeight = preferredContentSize.height + buttonPadding.getHeight() + 2;

                    if (tabPane.isCloseable() && tabButton.isSelected()) {
                        preferredWidth += CLOSE_TRIGGER_SIZE + buttonSpacing;
                    }

                    break;

                case VERTICAL:
                    preferredWidth = preferredContentSize.height + buttonPadding.getHeight() + 2;
                    preferredHeight = preferredContentSize.width + buttonPadding.getWidth() + 2;

                    if (tabPane.isCloseable() && tabButton.isSelected()) {
                        preferredHeight += CLOSE_TRIGGER_SIZE + buttonSpacing;
                    }

                    break;

                default:
                    break;
            }

            Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);
            return preferredSize;
        }

        @Override
        public int getBaseline(final int width, final int height) {
            TabButton tabButton = getTabButton();

            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            int clientWidth = Math.max(width - (buttonPadding.getWidth() + 2), 0);
            int clientHeight = Math.max(height - (buttonPadding.getHeight() + 2), 0);

            int baseline = dataRenderer.getBaseline(clientWidth, clientHeight);

            if (baseline != -1) {
                baseline += buttonPadding.top + 1;
            }

            return baseline;
        }

        @Override
        public void paint(final Graphics2D graphics) {
            TabButton tabButton = getTabButton();
            TabPane tabPane = getTabPane();

            boolean active = (selectionChangeTransition != null && selectionChangeTransition.getTab() == tabButton.tab);

            Color backgroundColor, buttonBevelColor;
            if (tabButton.isSelected() || active) {
                backgroundColor = activeTabColor;
                buttonBevelColor = activeButtonBevelColor;
            } else {
                backgroundColor = inactiveTabColor;
                buttonBevelColor = inactiveButtonBevelColor;
            }

            int width = getWidth();
            int height = getHeight();

            // Draw the background
            GraphicsUtilities.setAntialiasingOn(graphics);

            if (!themeIsFlat()) {
                switch (tabOrientation) {
                    case HORIZONTAL:
                        graphics.setPaint(new GradientPaint(width / 2f, 0, buttonBevelColor,
                            width / 2f, height / 2f, backgroundColor));
                        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1
                            + buttonCornerRadius, buttonCornerRadius, buttonCornerRadius));
                        break;

                    case VERTICAL:
                        graphics.setPaint(new GradientPaint(0, height / 2f, buttonBevelColor,
                            width / 2f, height / 2f, backgroundColor));
                        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1
                            + buttonCornerRadius, height - 1, buttonCornerRadius, buttonCornerRadius));
                        break;

                    default:
                        break;
                }

                // Draw the border
                graphics.setPaint((tabButton.isSelected() || active) ? borderColor : inactiveBorderColor);
                graphics.setStroke(new BasicStroke(1));

                switch (tabOrientation) {
                    case HORIZONTAL:
                        graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height
                            + buttonCornerRadius - 1, buttonCornerRadius, buttonCornerRadius));
                        break;

                    case VERTICAL:
                        graphics.draw(new RoundRectangle2D.Double(0.5, 0.5, width + buttonCornerRadius
                            - 1, height - 1, buttonCornerRadius, buttonCornerRadius));
                        break;

                    default:
                        break;
                }

                if (!(tabButton.isSelected() || active)) {
                    graphics.setPaint(borderColor);
                    // Draw divider
                    switch (tabOrientation) {
                        case HORIZONTAL:
                            graphics.draw(new Line2D.Double(0.5, height - 0.5, width - 0.5, height - 0.5));
                            break;

                        case VERTICAL:
                            graphics.draw(new Line2D.Double(width - 0.5, 0.5, width - 0.5, height - 0.5));
                            break;

                        default:
                            break;
                    }
                }
            } else {
                switch (tabOrientation) {
                    case HORIZONTAL:
                        graphics.setPaint(backgroundColor);
                        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1
                            + buttonCornerRadius, buttonCornerRadius, buttonCornerRadius));
                        break;

                    case VERTICAL:
                        graphics.setPaint(backgroundColor);
                        graphics.fill(new RoundRectangle2D.Double(0.5, 0.5, width - 1
                            + buttonCornerRadius, height - 1, buttonCornerRadius, buttonCornerRadius));
                        break;

                    default:
                        break;
                }

            }

            // Paint the content
            Button.DataRenderer dataRenderer = tabButton.getDataRenderer();
            dataRenderer.render(tabButton.getButtonData(), tabButton, false);

            Graphics2D contentGraphics = (Graphics2D) graphics.create();
            GraphicsUtilities.setAntialiasingOff(contentGraphics);

            int contentWidth;

            switch (tabOrientation) {
                case HORIZONTAL:
                    contentWidth = getWidth() - (buttonPadding.getWidth() + 2);
                    if (tabPane.isCloseable() && tabButton.isSelected()) {
                        contentWidth -= (CLOSE_TRIGGER_SIZE + buttonSpacing);
                    }

                    dataRenderer.setSize(Math.max(contentWidth, 0),
                        Math.max(getHeight() - (buttonPadding.getHeight() + 2), 0));

                    contentGraphics.translate(buttonPadding.left + 1, buttonPadding.top + 1);

                    break;

                case VERTICAL:
                    contentWidth = getHeight() - (buttonPadding.getHeight() + 2);
                    if (tabPane.isCloseable() && tabButton.isSelected()) {
                        contentWidth -= (CLOSE_TRIGGER_SIZE + buttonSpacing);
                    }

                    dataRenderer.setSize(Math.max(contentWidth, 0),
                        Math.max(getWidth() - (buttonPadding.getWidth() + 2), 0));

                    contentGraphics.translate(buttonPadding.top + 1, buttonPadding.left + 1);
                    contentGraphics.rotate(-Math.PI / 2d);
                    contentGraphics.translate(-dataRenderer.getWidth(), 0);

                    break;

                default:
                    break;
            }

            contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
            dataRenderer.paint(contentGraphics);

            contentGraphics.dispose();

            // Draw the close trigger
            if (tabPane.isCloseable() && tabButton.isSelected()) {
                graphics.setStroke(new BasicStroke(2.5f));

                int x = 0;
                int y = 0;
                switch (tabOrientation) {
                    case HORIZONTAL:
                        x = width - (buttonPadding.right + CLOSE_TRIGGER_SIZE + 1);
                        y = (height - CLOSE_TRIGGER_SIZE) / 2;
                        break;

                    case VERTICAL:
                        x = (width - CLOSE_TRIGGER_SIZE) / 2;
                        y = height - (buttonPadding.bottom + CLOSE_TRIGGER_SIZE + 1);
                        break;

                    default:
                        break;
                }

                graphics.draw(new Line2D.Double(x, y, x + CLOSE_TRIGGER_SIZE - 1,
                    y + CLOSE_TRIGGER_SIZE - 1));
                graphics.draw(new Line2D.Double(x, y + CLOSE_TRIGGER_SIZE - 1,
                    x + CLOSE_TRIGGER_SIZE - 1, y));
            }
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseClick(final Component component, final Mouse.Button button,
                final int x, final int y, final int count) {
            boolean consumed = super.mouseClick(component, button, x, y, count);

            TabButton tabButton = getTabButton();
            TabPane tabPane = getTabPane();

            if (tabPane.isCloseable() && tabButton.isSelected()
                && getCloseTriggerBounds().contains(x, y)) {
                tabPane.getTabs().remove(tabButton.tab);
            } else {
                tabButton.press();
            }

            return consumed;
        }

        public Font getFont() {
            return buttonFont;
        }

        public Color getColor() {
            return buttonColor;
        }

        public Color getDisabledColor() {
            return disabledButtonColor;
        }

        @Override
        public void stateChanged(final Button button, final Button.State previousState) {
            super.stateChanged(button, previousState);
            invalidateComponent();
        }

        public Bounds getCloseTriggerBounds() {
            Bounds bounds = null;

            // Include an extra 2 pixels around the trigger for ease of use
            switch (tabOrientation) {
                case HORIZONTAL:
                    bounds = new Bounds(getWidth() - (CLOSE_TRIGGER_SIZE + buttonPadding.right + 1)
                        - 2, (getHeight() - CLOSE_TRIGGER_SIZE) / 2 - 2, CLOSE_TRIGGER_SIZE + 4,
                        CLOSE_TRIGGER_SIZE + 4);
                    break;

                case VERTICAL:
                    bounds = new Bounds((getWidth() - CLOSE_TRIGGER_SIZE) / 2 - 2, getHeight()
                        - (CLOSE_TRIGGER_SIZE + buttonPadding.bottom + 1) - 2,
                        CLOSE_TRIGGER_SIZE + 4, CLOSE_TRIGGER_SIZE + 4);
                    break;

                default:
                    break;
            }

            return bounds;
        }
    }

    /**
     * Selection change transition.
     */
    public class SelectionChangeTransition extends Transition {
        public final int index;
        public final boolean expand;

        private Easing easing = new Quadratic();

        public SelectionChangeTransition(final int indexValue, final boolean expandValue) {
            super(selectionChangeDuration, selectionChangeRate, false);

            index = indexValue;
            expand = expandValue;
        }

        public Component getTab() {
            TabPane tabPane = getTabPane();
            return tabPane.getTabs().get(index);
        }

        public float getScale() {
            int elapsedTime = getElapsedTime();
            int duration = getDuration();

            float scale;
            if (expand) {
                scale = easing.easeOut(elapsedTime, 0, 1, duration);
            } else {
                scale = easing.easeIn(elapsedTime, 1, -1, duration);
            }

            return scale;
        }

        @Override
        public void start(final TransitionListener transitionListener) {
            TabPane tabPane = getTabPane();

            if (expand) {
                getTab().setVisible(true);
            }

            getTab().getDecorators().add(clipDecorator);
            tabPane.setEnabled(false);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            TabPane tabPane = getTabPane();

            if (!expand) {
                getTab().setVisible(false);
            }

            getTab().getDecorators().remove(clipDecorator);
            tabPane.setEnabled(true);

            super.stop();
        }

        @Override
        protected void update() {
            invalidateComponent();
        }
    }

    private Panorama buttonPanorama = new Panorama();
    private ButtonGroup tabButtonGroup = new ButtonGroup();

    private Color activeTabColor;
    private Color inactiveTabColor;
    private Color borderColor;
    private Color inactiveBorderColor;
    private Insets padding;
    private Font buttonFont;
    private Color buttonColor;
    private Color disabledButtonColor;
    private Insets buttonPadding;
    private int buttonSpacing;
    private int buttonCornerRadius;

    private Color activeButtonBevelColor;
    private Color inactiveButtonBevelColor;

    private Orientation tabOrientation = Orientation.HORIZONTAL;

    private int selectionChangeDuration = DEFAULT_SELECTION_CHANGE_DURATION;
    private int selectionChangeRate = DEFAULT_SELECTION_CHANGE_RATE;

    private SelectionChangeTransition selectionChangeTransition = null;
    private ClipDecorator clipDecorator = new ClipDecorator();

    private ComponentStateListener tabStateListener = new ComponentStateListener() {
        @Override
        public void enabledChanged(final Component component) {
            TabPane tabPane = getTabPane();
            int i = tabPane.getTabs().indexOf(component);
            buttonBoxPane.get(i).setEnabled(component.isEnabled());
        }
    };

    private static final int CLOSE_TRIGGER_SIZE = 6;
    private static final int DEFAULT_SELECTION_CHANGE_DURATION = 250;
    private static final int DEFAULT_SELECTION_CHANGE_RATE = 30;

    public TerraTabPaneSkin() {
        Theme theme = currentTheme();
        activeTabColor = theme.getColor(11);
        inactiveTabColor = theme.getColor(9);
        borderColor = theme.getColor(7);
        inactiveBorderColor = theme.getColor(7);
        padding = new Insets(6);
        buttonFont = theme.getFont();
        buttonColor = theme.getColor(1);
        disabledButtonColor = theme.getColor(7);
        buttonPadding = new Insets(3, 4, 3, 4);
        buttonSpacing = 6;
        buttonCornerRadius = 4;

        activeButtonBevelColor = TerraTheme.brighten(activeTabColor);
        inactiveButtonBevelColor = TerraTheme.brighten(inactiveTabColor);

        buttonBoxPane.putStyle(Style.fill, true);

        buttonPanorama.putStyle(Style.buttonBackgroundColor, borderColor);
        buttonPanorama.putStyle(Style.buttonPadding, 6);
        buttonPanorama.setView(buttonBoxPane);

        tabButtonGroup.getButtonGroupListeners().add(new ButtonGroupListener() {
            @Override
            public void selectionChanged(final ButtonGroup buttonGroup, final Button previousSelection) {
                Button button = tabButtonGroup.getSelection();
                int index = (button == null) ? -1 : buttonBoxPane.indexOf(button);

                TabPane tabPane = getTabPane();
                tabPane.setSelectedIndex(index);
            }
        });

        setButtonSpacing(2);
    }

    private TabPane getTabPane() {
        return (TabPane) getComponent();
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        TabPane tabPane = (TabPane) component;

        // Add this as a listener on the tab pane
        tabPane.getTabPaneListeners().add(this);
        tabPane.getTabPaneSelectionListeners().add(this);
        tabPane.getTabPaneAttributeListeners().add(this);

        // Add the tab button container
        tabPane.add(buttonPanorama);
    }

    /**
     * @return The full padding width, which includes the actual padding plus
     * two pixels for the border lines.
     */
    private int fullPaddingWidth() {
        return padding.getWidth() + 2;
    }

    /**
     * @return The full padding height, which includes the actual padding plus
     * two pixels for the border lines.
     */
    private int fullPaddingHeight() {
        return padding.getHeight() + 2;
    }

    @Override
    public int getPreferredWidth(final int height) {
        int preferredWidth = 0;
        int heightValue = height;

        TabPane tabPane = getTabPane();

        Component selectedTab = tabPane.getSelectedTab();
        Component corner = tabPane.getCorner();

        switch (tabOrientation) {
            case HORIZONTAL:
                if (heightValue != -1) {
                    if (corner != null) {
                        heightValue = Math.max(
                            heightValue
                                - Math.max(corner.getPreferredHeight(-1),
                                    Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0)), 0);
                    } else {
                        heightValue = Math.max(heightValue - (buttonPanorama.getPreferredHeight(-1) - 1), 0);
                    }

                    heightValue = Math.max(heightValue - fullPaddingHeight(), 0);
                }

                preferredWidth = getPreferredTabWidth(heightValue) + fullPaddingWidth();

                int buttonAreaPreferredWidth = buttonPanorama.getPreferredWidth(-1);
                if (corner != null) {
                    buttonAreaPreferredWidth += corner.getPreferredWidth(-1);
                }

                preferredWidth = Math.max(preferredWidth, buttonAreaPreferredWidth);

                break;

            case VERTICAL:
                if (heightValue != -1) {
                    heightValue = Math.max(heightValue - fullPaddingHeight(), 0);
                }

                if (selectedTab == null && selectionChangeTransition == null) {
                    preferredWidth = 1;
                } else {
                    preferredWidth = getPreferredTabWidth(heightValue) + padding.getWidth();

                    if (selectionChangeTransition != null) {
                        float scale = selectionChangeTransition.getScale();
                        preferredWidth = (int) (preferredWidth * scale);
                    }

                    preferredWidth += 2;
                }

                if (corner != null) {
                    preferredWidth += Math.max(corner.getPreferredWidth(-1),
                        Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0));
                } else {
                    preferredWidth += Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0);
                }

                break;

            default:
                break;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        int preferredHeight = 0;
        int widthValue = width;

        TabPane tabPane = getTabPane();

        Component selectedTab = tabPane.getSelectedTab();
        Component corner = tabPane.getCorner();

        switch (tabOrientation) {
            case HORIZONTAL:
                if (widthValue != -1) {
                    widthValue = Math.max(widthValue - fullPaddingWidth(), 0);
                }

                if (selectedTab == null && selectionChangeTransition == null) {
                    preferredHeight = 1;
                } else {
                    preferredHeight = getPreferredTabHeight(widthValue) + padding.getHeight();

                    if (selectionChangeTransition != null) {
                        float scale = selectionChangeTransition.getScale();
                        preferredHeight = (int) (preferredHeight * scale);
                    }

                    preferredHeight += 2;
                }

                if (corner != null) {
                    preferredHeight += Math.max(corner.getPreferredHeight(-1),
                        Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0));
                } else {
                    preferredHeight += Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0);
                }

                break;

            case VERTICAL:
                if (widthValue != -1) {
                    if (corner != null) {
                        widthValue = Math.max(
                            widthValue
                                - Math.max(corner.getPreferredWidth(-1),
                                    Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0)), 0);
                    } else {
                        widthValue = Math.max(widthValue - (buttonPanorama.getPreferredWidth(-1) - 1), 0);
                    }

                    widthValue = Math.max(widthValue - fullPaddingWidth(), 0);
                }

                preferredHeight = getPreferredTabHeight(widthValue) + fullPaddingHeight();

                int buttonAreaPreferredHeight = buttonPanorama.getPreferredHeight(-1);
                if (corner != null) {
                    buttonAreaPreferredHeight += corner.getPreferredHeight(-1);
                }

                preferredHeight = Math.max(preferredHeight, buttonAreaPreferredHeight);

                break;

            default:
                break;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        TabPane tabPane = getTabPane();

        int preferredWidth;
        int preferredHeight;

        Component selectedTab = tabPane.getSelectedTab();
        Component corner = tabPane.getCorner();

        switch (tabOrientation) {
            case HORIZONTAL:
                if (selectedTab == null && selectionChangeTransition == null) {
                    preferredWidth = getPreferredTabWidth(-1) + fullPaddingWidth();
                    preferredHeight = 1;
                } else {
                    Dimensions preferredTabSize = getPreferredTabSize();
                    preferredWidth = preferredTabSize.width + fullPaddingWidth();
                    preferredHeight = preferredTabSize.height + padding.getHeight();

                    if (selectionChangeTransition != null) {
                        float scale = selectionChangeTransition.getScale();
                        preferredHeight = (int) (preferredHeight * scale);
                    }

                    preferredHeight += 2;
                }

                int buttonAreaPreferredWidth = buttonPanorama.getPreferredWidth(-1);
                if (corner != null) {
                    buttonAreaPreferredWidth += corner.getPreferredWidth(-1);
                    preferredHeight += Math.max(corner.getPreferredHeight(-1),
                        Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0));
                    buttonAreaPreferredWidth += 2; // space between corner and panorama
                } else {
                    preferredHeight += Math.max(buttonPanorama.getPreferredHeight(-1) - 1, 0);
                }

                preferredWidth = Math.max(preferredWidth, buttonAreaPreferredWidth);

                break;

            case VERTICAL:
                if (selectedTab == null && selectionChangeTransition == null) {
                    preferredWidth = 1;
                    preferredHeight = getPreferredTabHeight(-1) + fullPaddingHeight();
                } else {
                    Dimensions preferredTabSize = getPreferredTabSize();

                    preferredWidth = preferredTabSize.width + padding.getWidth();
                    preferredHeight = preferredTabSize.height + fullPaddingHeight();

                    if (selectionChangeTransition != null) {
                        float scale = selectionChangeTransition.getScale();
                        preferredWidth = (int) (preferredWidth * scale);
                    }

                    preferredWidth += 2;
                }

                int buttonAreaPreferredHeight = buttonPanorama.getPreferredHeight(-1);
                if (corner != null) {
                    buttonAreaPreferredHeight += corner.getPreferredHeight(-1);
                    preferredWidth += Math.max(corner.getPreferredWidth(-1),
                        Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0));
                    buttonAreaPreferredHeight += 2; // space between corner and panorama
                } else {
                    preferredWidth += Math.max(buttonPanorama.getPreferredWidth(-1) - 1, 0);
                }

                preferredHeight = Math.max(preferredHeight, buttonAreaPreferredHeight);

                break;

            default:
                preferredWidth = 0;
                preferredHeight = 0;
                break;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(final int width, final int height) {
        int baseline = -1;

        if (tabOrientation == Orientation.HORIZONTAL && buttonBoxPane.getLength() > 0) {
            TabButton firstButton = (TabButton) buttonBoxPane.get(0);

            int buttonHeight = buttonBoxPane.getPreferredHeight();
            baseline = firstButton.getBaseline(firstButton.getPreferredWidth(buttonHeight), buttonHeight);
        }

        return baseline;
    }

    private int getPreferredTabWidth(final int height) {
        int preferredTabWidth = 0;

        TabPane tabPane = getTabPane();
        for (Component tab : tabPane.getTabs()) {
            preferredTabWidth = Math.max(preferredTabWidth, tab.getPreferredWidth(height));
        }

        return preferredTabWidth;
    }

    private int getPreferredTabHeight(final int width) {
        int preferredTabHeight = 0;

        TabPane tabPane = getTabPane();
        for (Component tab : tabPane.getTabs()) {
            preferredTabHeight = Math.max(preferredTabHeight, tab.getPreferredHeight(width));
        }

        return preferredTabHeight;
    }

    private Dimensions getPreferredTabSize() {
        int preferredTabWidth = 0;
        int preferredTabHeight = 0;

        TabPane tabPane = getTabPane();
        for (Component tab : tabPane.getTabs()) {
            Dimensions preferredSize = tab.getPreferredSize();
            preferredTabWidth = Math.max(preferredTabWidth, preferredSize.width);
            preferredTabHeight = Math.max(preferredTabHeight, preferredSize.height);
        }

        return new Dimensions(preferredTabWidth, preferredTabHeight);
    }

    @Override
    public void layout() {
        TabPane tabPane = getTabPane();
        int width = getWidth();
        int height = getHeight();

        int tabX = 0;
        int tabY = 0;
        int tabWidth = 0;
        int tabHeight = 0;

        Component corner = tabPane.getCorner();
        Dimensions buttonPanoramaSize = buttonPanorama.getPreferredSize();
        int buttonPanoramaWidth, buttonPanoramaHeight;
        int buttonPanoramaX, buttonPanoramaY;

        switch (tabOrientation) {
            case HORIZONTAL:
                buttonPanoramaWidth = Math.min(width, buttonPanoramaSize.width);
                buttonPanoramaHeight = buttonPanoramaSize.height;
                buttonPanoramaY = 0;

                if (corner != null) {
                    int cornerWidth = Math.max(width - buttonPanoramaWidth - 2,
                        corner.getPreferredWidth());
                    if (cornerWidth > width - 2) {
                        cornerWidth = Math.max(width - 2, 0);
                    }
                    if (buttonPanoramaWidth + 2 + cornerWidth > width) {
                        buttonPanoramaWidth = Math.max(width - 2 - cornerWidth, 0);
                    }
                    int cornerHeight = Math.max(corner.getPreferredHeight(-1),
                        buttonPanoramaSize.height - 1);
                    int cornerX = width - cornerWidth;
                    int cornerY = Math.max(buttonPanoramaHeight - cornerHeight - 1, 0);

                    buttonPanoramaY = Math.max(cornerHeight - buttonPanoramaHeight + 1, 0);

                    corner.setLocation(cornerX, cornerY);
                    corner.setSize(cornerWidth, cornerHeight);
                }

                buttonPanorama.setLocation(0, buttonPanoramaY);
                buttonPanorama.setSize(buttonPanoramaWidth, buttonPanoramaHeight);

                tabX = padding.left + 1;
                tabY = padding.top + buttonPanoramaY + buttonPanoramaHeight;

                tabWidth = Math.max(width - fullPaddingWidth(), 0);
                tabHeight = Math.max(height
                    - (padding.getHeight() + buttonPanoramaY + buttonPanoramaHeight + 1),
                    0);

                break;

            case VERTICAL:
                buttonPanoramaWidth = buttonPanoramaSize.width;
                buttonPanoramaHeight = Math.min(height, buttonPanoramaSize.height);
                buttonPanoramaX = 0;

                if (corner != null) {
                    int cornerHeight = Math.max(height - buttonPanoramaHeight - 2,
                        corner.getPreferredHeight());
                    if (cornerHeight > height - 2) {
                        cornerHeight = Math.max(height - 2, 0);
                    }
                    if (buttonPanoramaHeight + 2 + cornerHeight > height) {
                        buttonPanoramaHeight = Math.max(height - 2 - cornerHeight, 0);
                    }
                    int cornerWidth = Math.max(corner.getPreferredWidth(-1),
                        buttonPanoramaSize.width - 1);
                    int cornerX = Math.max(buttonPanoramaWidth - cornerWidth - 1, 0);
                    int cornerY = height - cornerHeight;

                    buttonPanoramaX = Math.max(cornerWidth - buttonPanoramaWidth + 1, 0);

                    corner.setLocation(cornerX, cornerY);
                    corner.setSize(cornerWidth, cornerHeight);
                }

                buttonPanorama.setLocation(buttonPanoramaX, 0);
                buttonPanorama.setSize(buttonPanoramaWidth, buttonPanoramaHeight);

                tabX = padding.left + buttonPanoramaX + buttonPanoramaWidth;
                tabY = padding.top + 1;
                tabWidth = Math.max(width
                    - (padding.left + padding.right + buttonPanoramaX + buttonPanoramaWidth + 1), 0);
                tabHeight = Math.max(height - fullPaddingHeight(), 0);

                break;

            default:
                break;
        }

        // Lay out the tabs
        for (Component tab : tabPane.getTabs()) {
            tab.setLocation(tabX, tabY);

            if (selectionChangeTransition != null && selectionChangeTransition.isRunning()) {
                clipDecorator.setSize(tabWidth, tabHeight);

                switch (tabOrientation) {
                    case HORIZONTAL:
                        tab.setSize(tabWidth, getPreferredTabHeight(tabWidth));
                        break;

                    case VERTICAL:
                        tab.setSize(getPreferredTabWidth(tabHeight), tabHeight);
                        break;

                    default:
                        break;
                }
            } else {
                tab.setSize(tabWidth, tabHeight);
            }
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        TabPane tabPane = getTabPane();

        Bounds tabPaneBounds = tabPane.getBounds();

        // Call the base class to paint the background
        super.paint(graphics);

        // Paint the content background and border
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        switch (tabOrientation) {
            case HORIZONTAL:
                x = 0;
                y = Math.max(buttonPanorama.getY() + buttonPanorama.getHeight() - 1, 0);
                width = tabPaneBounds.width;
                height = Math.max(tabPaneBounds.height - y, 0);

                break;

            case VERTICAL:
                x = Math.max(buttonPanorama.getX() + buttonPanorama.getWidth() - 1, 0);
                y = 0;
                width = Math.max(tabPaneBounds.width - x, 0);
                height = tabPaneBounds.height;

                break;

            default:
                break;
        }

        TabButton activeTabButton;
        if (selectionChangeTransition == null) {
            activeTabButton = (TabButton) tabButtonGroup.getSelection();
        } else {
            activeTabButton = (TabButton) buttonBoxPane.get(selectionChangeTransition.index);
        }

        if (activeTabButton != null) {
            Bounds contentBounds = new Bounds(x, y, width, height);

            GraphicsUtilities.setAntialiasingOn(graphics);

            // Paint the background
            graphics.setPaint(activeTabColor);
            graphics.fillRect(contentBounds.x, contentBounds.y, contentBounds.width,
                contentBounds.height);

            if (!themeIsFlat()) {
                // Draw the border
                double top = contentBounds.y + 0.5;
                double left = contentBounds.x + 0.5;
                double bottom = top + contentBounds.height - 1;
                double right = left + contentBounds.width - 1;

                graphics.setPaint(borderColor);

                // Draw the right and bottom borders
                graphics.draw(new Line2D.Double(right, top, right, bottom));
                graphics.draw(new Line2D.Double(left, bottom, right, bottom));

                // Draw the left and top borders
                Point selectedTabButtonLocation;
                switch (tabOrientation) {
                    case HORIZONTAL:
                        graphics.draw(new Line2D.Double(left, top, left, bottom));

                        selectedTabButtonLocation = activeTabButton.mapPointToAncestor(tabPane, 0, 0);
                        graphics.draw(new Line2D.Double(left, top, selectedTabButtonLocation.x + 0.5, top));
                        graphics.draw(new Line2D.Double(selectedTabButtonLocation.x
                            + activeTabButton.getWidth() - 0.5, top, right, top));

                        break;

                    case VERTICAL:
                        graphics.draw(new Line2D.Double(left, top, right, top));

                        selectedTabButtonLocation = activeTabButton.mapPointToAncestor(tabPane, 0, 0);
                        graphics.draw(new Line2D.Double(left, top, left,
                            selectedTabButtonLocation.y + 0.5));
                        graphics.draw(new Line2D.Double(left, selectedTabButtonLocation.y
                            + activeTabButton.getHeight() - 0.5, left, bottom));

                        break;

                    default:
                        break;
                }
            }
        }
    }

    public Color getActiveTabColor() {
        return activeTabColor;
    }

    public void setActiveTabColor(final Object colorValue) {
        activeTabColor = colorFromObject(colorValue, "activeTabColor");
        activeButtonBevelColor = TerraTheme.brighten(activeTabColor);
        repaintComponent();
    }

    public Color getInactiveTabColor() {
        return inactiveTabColor;
    }

    public void setInactiveTabColor(final Object colorValue) {
        inactiveTabColor = colorFromObject(colorValue, "inactiveTabColor");
        inactiveButtonBevelColor = TerraTheme.brighten(inactiveTabColor);
        repaintComponent();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(final Object colorValue) {
        borderColor = colorFromObject(colorValue, "borderColor");
        buttonPanorama.putStyle(Style.buttonBackgroundColor, borderColor);
        repaintComponent();
    }

    public Color getInactiveBorderColor() {
        return inactiveBorderColor;
    }

    public void setInactiveBorderColor(final Object colorValue) {
        inactiveBorderColor = colorFromObject(colorValue, "inactiveBorderColor");
        repaintComponent();
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(final Object paddingValue) {
        padding = Insets.fromObject(paddingValue, "padding");
        invalidateComponent();
    }

    public Font getButtonFont() {
        return buttonFont;
    }

    public void setButtonFont(final Object fontValue) {
        buttonFont = fontFromObject(fontValue);
        invalidateComponent();
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(final Object colorValue) {
        buttonColor = colorFromObject(colorValue, "buttonColor");
        repaintComponent();
    }

    public Insets getButtonPadding() {
        return buttonPadding;
    }

    public void setButtonPadding(final Object buttonPaddingValue) {
        buttonPadding = Insets.fromObject(buttonPaddingValue, "buttonPadding");
        invalidateComponent();

        for (Component tabButton : buttonBoxPane) {
            tabButton.invalidate();
        }
    }

    public int getButtonSpacing() {
        return buttonBoxPane.getStyleInt(Style.spacing);
    }

    public void setButtonSpacing(final int spacingValue) {
        buttonBoxPane.putStyle(Style.spacing, spacingValue);
    }

    public final void setButtonCornerRadius(final int radiusValue) {
        buttonCornerRadius = radiusValue;
    }

    public final void setButtonCornerRadius(final Number radiusValue) {
        Utils.checkNull(radiusValue, "buttonCornerRadius");

        setButtonCornerRadius(radiusValue.intValue());
    }

    public final void setButtonCornerRadius(final String radiusString) {
        Utils.checkNullOrEmpty(radiusString, "buttonCornerRadius");

        setButtonCornerRadius(Integer.valueOf(radiusString));
    }

    public Orientation getTabOrientation() {
        return tabOrientation;
    }

    public void setTabOrientation(final Orientation orientationValue) {
        Utils.checkNull(orientationValue, "tabOrientation");

        tabOrientation = orientationValue;

        // Invalidate the tab buttons since their preferred sizes have changed
        for (Component tabButton : buttonBoxPane) {
            tabButton.invalidate();
        }

        buttonBoxPane.setOrientation(tabOrientation);

        switch (tabOrientation) {
            case HORIZONTAL:
                buttonBoxPane.putStyle(Style.horizontalAlignment, HorizontalAlignment.LEFT);
                break;
            case VERTICAL:
                buttonBoxPane.putStyle(Style.verticalAlignment, VerticalAlignment.TOP);
                break;
            default:
                break;
        }
    }

    public int getSelectionChangeDuration() {
        return selectionChangeDuration;
    }

    public void setSelectionChangeDuration(final int durationValue) {
        selectionChangeDuration = durationValue;
    }

    public int getSelectionChangeRate() {
        return selectionChangeRate;
    }

    public void setSelectionChangeRate(final int rateValue) {
        selectionChangeRate = rateValue;
    }

    /**
     * Key presses have no effect if the event has already been consumed.<p>
     * CommandModifier + {@link KeyCode#KEYPAD_1 KEYPAD_1} to
     * {@link KeyCode#KEYPAD_9 KEYPAD_9}<br>or CommandModifier +
     * {@link KeyCode#N1 1} to {@link KeyCode#N9 9} Select the (enabled) tab at
     * index 0 to 8 respectively.
     * <p> CommandModifier + Tab to cycle forward through the tabs,
     * CommandModifier + Shift + Tab to cycle backward.
     *
     * @see Platform#getCommandModifier()
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        Modifier commandModifier = Platform.getCommandModifier();
        if (!consumed && Keyboard.isPressed(commandModifier)) {
            TabPane tabPane = getTabPane();
            TabPane.TabSequence tabs = tabPane.getTabs();

            int selectedIndex = -1;

            switch (keyCode) {
                case KeyCode.KEYPAD_1:
                case KeyCode.N1:
                    selectedIndex = 0;
                    break;

                case KeyCode.KEYPAD_2:
                case KeyCode.N2:
                    selectedIndex = 1;
                    break;

                case KeyCode.KEYPAD_3:
                case KeyCode.N3:
                    selectedIndex = 2;
                    break;

                case KeyCode.KEYPAD_4:
                case KeyCode.N4:
                    selectedIndex = 3;
                    break;

                case KeyCode.KEYPAD_5:
                case KeyCode.N5:
                    selectedIndex = 4;
                    break;

                case KeyCode.KEYPAD_6:
                case KeyCode.N6:
                    selectedIndex = 5;
                    break;

                case KeyCode.KEYPAD_7:
                case KeyCode.N7:
                    selectedIndex = 6;
                    break;

                case KeyCode.KEYPAD_8:
                case KeyCode.N8:
                    selectedIndex = 7;
                    break;

                case KeyCode.KEYPAD_9:
                case KeyCode.N9:
                    selectedIndex = 8;
                    break;

                case KeyCode.TAB:
                    selectedIndex = tabPane.getSelectedIndex();
                    if (Keyboard.isPressed(Modifier.SHIFT)) {
                        if (selectedIndex <= 0) {
                            selectedIndex = tabs.getLength() - 1;
                        } else {
                           selectedIndex--;
                        }
                    } else {
                        if (selectedIndex >= tabs.getLength() - 1) {
                            selectedIndex = 0;
                        } else {
                            selectedIndex++;
                        }
                    }
                    break;

                default:
                    break;
            }

            if (selectedIndex >= 0 && selectedIndex < tabs.getLength()
                && tabs.get(selectedIndex).isEnabled()) {
                tabPane.setSelectedIndex(selectedIndex);
                consumed = true;
            }
        }

        return consumed;
    }

    // Tab pane events
    @Override
    public void tabInserted(final TabPane tabPane, final int index) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        Component tab = tabPane.getTabs().get(index);
        tab.setVisible(false);

        // Create a new button for the tab
        TabButton tabButton = new TabButton(tab);
        tabButton.setButtonGroup(tabButtonGroup);
        buttonBoxPane.insert(tabButton, index);

        // Listen for state changes on the tab
        tabButton.setEnabled(tab.isEnabled());
        tab.getComponentStateListeners().add(tabStateListener);

        // If this is the first tab, select it
        if (tabPane.getTabs().getLength() == 1) {
            tabPane.setSelectedIndex(0);
        }

        invalidateComponent();
    }

    @Override
    public Vote previewRemoveTabs(final TabPane tabPane, final int index, final int count) {
        return Vote.APPROVE;
    }

    @Override
    public void removeTabsVetoed(final TabPane tabPane, final Vote vote) {
        // No-op
    }

    @Override
    public void tabsRemoved(final TabPane tabPane, final int index, final Sequence<Component> removed) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        // Remove the buttons
        Sequence<Component> removedButtons = buttonBoxPane.remove(index, removed.getLength());

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            TabButton tabButton = (TabButton) removedButtons.get(i);
            tabButton.setButtonGroup(null);

            // Stop listening for state changes on the tab
            tabButton.tab.getComponentStateListeners().remove(tabStateListener);
        }

        invalidateComponent();
    }

    @Override
    public void cornerChanged(final TabPane tabPane, final Component previousCorner) {
        invalidateComponent();
    }

    @Override
    public void tabDataRendererChanged(final TabPane tabPane, final Button.DataRenderer previousRenderer) {
        for (Component tabButton : buttonBoxPane) {
            tabButton.invalidate();
        }
    }

    @Override
    public void closeableChanged(final TabPane tabPane) {
        Button selectedTabButton = tabButtonGroup.getSelection();

        if (selectedTabButton != null) {
            selectedTabButton.invalidate();
        }
    }

    @Override
    public void collapsibleChanged(final TabPane tabPane) {
        // No-op
    }

    // Tab pane selection events
    @Override
    public Vote previewSelectedIndexChange(final TabPane tabPane, final int selectedIndex) {
        Vote vote;

        if (tabPane.isCollapsible()) {
            if (tabPane.isShowing() && selectionChangeTransition == null) {
                int previousSelectedIndex = tabPane.getSelectedIndex();

                if (selectedIndex == -1) {
                    // Collapse
                    selectionChangeTransition = new SelectionChangeTransition(
                        previousSelectedIndex, false);
                } else {
                    if (previousSelectedIndex == -1) {
                        // Expand
                        selectionChangeTransition = new SelectionChangeTransition(selectedIndex,
                            true);
                    }
                }

                if (selectionChangeTransition != null) {
                    selectionChangeTransition.start(new TransitionListener() {
                        @Override
                        public void transitionCompleted(final Transition transition) {
                            TabPane tabPaneLocal = getTabPane();

                            SelectionChangeTransition selChangeTransitionLocal = (SelectionChangeTransition) transition;

                            int selectedIndexLocal;
                            if (selChangeTransitionLocal.expand) {
                                selectedIndexLocal = selChangeTransitionLocal.index;
                            } else {
                                selectedIndexLocal = -1;
                            }

                            tabPaneLocal.setSelectedIndex(selectedIndexLocal);

                            TerraTabPaneSkin.this.selectionChangeTransition = null;
                        }
                    });
                }
            }

            if (selectionChangeTransition == null || !selectionChangeTransition.isRunning()) {
                vote = Vote.APPROVE;
            } else {
                vote = Vote.DEFER;
            }
        } else {
            vote = Vote.APPROVE;
        }

        return vote;
    }

    @Override
    public void selectedIndexChangeVetoed(final TabPane tabPane, final Vote reason) {
        if (reason == Vote.DENY && selectionChangeTransition != null) {
            // NOTE We stop, rather than end, the transition so the completion
            // event isn't fired; if the event fires, the listener will set
            // the selection state
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
            invalidateComponent();
        }
    }

    @Override
    public void selectedIndexChanged(final TabPane tabPane, final int previousSelectedIndex) {
        int selectedIndex = tabPane.getSelectedIndex();

        if (selectedIndex != previousSelectedIndex) {
            // This was not an indirect selection change
            if (selectedIndex == -1) {
                Button button = tabButtonGroup.getSelection();
                if (button != null) {
                    button.setSelected(false);
                }
            } else {
                final Button button = (Button) buttonBoxPane.get(selectedIndex);
                button.setSelected(true);

                Component selectedTab = tabPane.getTabs().get(selectedIndex);
                selectedTab.setVisible(true);
                selectedTab.requestFocus();

                ApplicationContext.queueCallback(() ->
                    button.scrollAreaToVisible(0, 0, button.getWidth(), button.getHeight()));
            }

            if (previousSelectedIndex != -1) {
                Component previousSelectedTab = tabPane.getTabs().get(previousSelectedIndex);
                previousSelectedTab.setVisible(false);
            }
        }

        if (selectedIndex == -1 || previousSelectedIndex == -1) {
            invalidateComponent();
        }
    }

    // Tab pane attribute events
    @Override
    public void tabDataChanged(final TabPane tabPane, final Component component,
            final Object previousTabData) {
        int i = tabPane.getTabs().indexOf(component);
        buttonBoxPane.get(i).invalidate();
    }

    @Override
    public void tooltipTextChanged(final TabPane tabPane, final Component component,
            final String previousTooltipText) {
        // No-op
    }

}
