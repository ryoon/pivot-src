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

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Quadratic;
import org.apache.pivot.wtk.skin.WindowSkin;


/**
 * Sheet skin class.
 * <p>
 * TODO Wire up the "resizable" flag. It current exists but does nothing.
 *
 * @author gbrown
 * @author tvolkert
 */
public class TerraSheetSkin extends WindowSkin implements SheetStateListener {
    public class WindowStateTransition extends Transition {
        private boolean close;

        public WindowStateTransition(boolean close) {
            super(TRANSITION_DURATION, TRANSITION_RATE, false, close);
            this.close = close;
        }

        @Override
        public void update() {
            invalidateComponent();
        }

        @Override
        public void reverse() {
            super.reverse();
            close = !close;
        }
    }

    private Color borderColor;
    private Insets padding;
    private boolean resizable;

    // Derived colors
    private Color bevelColor;

    private WindowStateTransition windowStateTransition = null;
    private Quadratic easing = new Quadratic();

    private ComponentListener ownerComponentListener = new ComponentListener.Adapter() {
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            alignToOwnerContent();
        }

        public void locationChanged(Component component, int previousX, int previousY) {
            alignToOwnerContent();
        }
    };

    private DropShadowDecorator dropShadowDecorator = null;

    private static final int TRANSITION_DURATION = 250;
    private static final int TRANSITION_RATE = 30;

    public TerraSheetSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        Color backgroundColor = theme.getColor(11);
        backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(),
            backgroundColor.getBlue(), 235);
        setBackgroundColor(backgroundColor);

        borderColor = theme.getColor(7);
        padding = new Insets(8);
        resizable = false;

        // Set the derived colors
        bevelColor = TerraTheme.darken(backgroundColor);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Sheet sheet = (Sheet)component;
        sheet.getSheetStateListeners().add(this);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        sheet.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public void uninstall() {
        Sheet sheet = (Sheet)getComponent();
        sheet.getSheetStateListeners().remove(this);

        // Detach the drop shadow decorator
        sheet.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom + 2), 0);
            }

            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right + 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right + 2), 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom + 2);
        preferredHeight = getEasedPreferredHeight(preferredHeight);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            Dimensions preferredContentSize = content.getPreferredSize();
            preferredWidth = preferredContentSize.width;
            preferredHeight = preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right + 2);
        preferredHeight += (padding.top + padding.bottom + 2);
        preferredHeight = getEasedPreferredHeight(preferredHeight);

        Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);

        return preferredSize;
    }

    public int getEasedPreferredHeight(int preferredHeight) {
        if (windowStateTransition != null
            && windowStateTransition.isRunning()) {
            float scale;
            if (windowStateTransition.close) {
                scale = easing.easeIn(windowStateTransition.getElapsedTime(), 0, 1,
                    windowStateTransition.getDuration());
            } else {
                scale = easing.easeOut(windowStateTransition.getElapsedTime(), 0, 1,
                    windowStateTransition.getDuration());
            }

            preferredHeight = (int)(scale * preferredHeight);
        }

        return preferredHeight;
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);

                content.setLocation(padding.left + 1, padding.top + 1);

                int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                int contentHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);

        graphics.setPaint(bevelColor);
        GraphicsUtilities.drawLine(graphics, 1, height - 2, width - 2, Orientation.HORIZONTAL);
    }

    @Override
    public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        super.sizeChanged(component, previousWidth, previousHeight);

        alignToOwnerContent();
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Sheet sheet = (Sheet)getComponent();

        if (keyCode == Keyboard.KeyCode.ENTER) {
            sheet.close(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            sheet.close(false);
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        bevelColor = TerraTheme.darken(backgroundColor);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        invalidateComponent();
    }

    @Override
    public void windowOpened(final Window window) {
        super.windowOpened(window);

        dropShadowDecorator.setShadowOpacity(DropShadowDecorator.DEFAULT_SHADOW_OPACITY);

        Window owner = window.getOwner();
        owner.getComponentListeners().add(ownerComponentListener);

        windowStateTransition = new WindowStateTransition(false);
        windowStateTransition.start(new TransitionListener() {
            public void transitionCompleted(Transition transition) {
                windowStateTransition = null;
            }
        });
    }

    public Vote previewSheetClose(final Sheet sheet, final boolean result) {
        // Start a close transition, return false, and close the window
        // when the transition is complete
        Vote vote = Vote.APPROVE;

        // Don't start the transition if the sheet is being closed as a result
        // of the owner closing
        Window owner = sheet.getOwner();
        if (!owner.isClosing()) {
            TransitionListener transitionListener = new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    sheet.close(result);
                    windowStateTransition = null;
                }
            };

            if (windowStateTransition == null) {
                // Start the close transition
                windowStateTransition = new WindowStateTransition(true);
                windowStateTransition.start(transitionListener);
            } else {
                // Reverse the open transition
                if (!windowStateTransition.close
                    && windowStateTransition.isRunning()) {
                    windowStateTransition.reverse(transitionListener);
                }
            }

            vote = (windowStateTransition != null
                && windowStateTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
        }

        return vote;
    }

    public void sheetCloseVetoed(Sheet sheet, Vote reason) {
        if (reason == Vote.DENY
            && windowStateTransition != null) {
            windowStateTransition.stop();
            windowStateTransition = null;
        }
    }

    public void sheetClosed(Sheet sheet) {
        Window owner = sheet.getOwner();
        owner.getComponentListeners().remove(ownerComponentListener);
    }

    public void alignToOwnerContent() {
        Sheet sheet = (Sheet)getComponent();

        Window owner = sheet.getOwner();
        Component content = owner.getContent();
        Point contentLocation = content.mapPointToAncestor(owner.getDisplay(), 0, 0);
        sheet.setLocation(contentLocation.x + (content.getWidth() - getWidth()) / 2,
            contentLocation.y);
    }
}
