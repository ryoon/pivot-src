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
package org.apache.pivot.wtk;

import java.awt.Graphics2D;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.wtk.skin.DisplaySkin;

/**
 * Container that serves as the root of a component hierarchy.
 */
public final class Display extends Container {
    /**
     * The display host (bridge to AWT) that really implements this display.
     */
    private ApplicationContext.DisplayHost displayHost;

    /**
     * Construct a display on the given {@link ApplicationContext.DisplayHost}.
     *
     * @param host The display host that implements this display.
     */
    public Display(final ApplicationContext.DisplayHost host) {
        displayHost = host;
        super.setSkin(new DisplaySkin());
    }

    /**
     * @return The display host attached to this display.
     */
    public ApplicationContext.DisplayHost getDisplayHost() {
        return displayHost;
    }

    /**
     * @return The AWT host window of this display.
     */
    public java.awt.Window getHostWindow() {
        java.awt.Container parent = displayHost.getParent();
        while (parent != null && !(parent instanceof java.awt.Window)) {
            parent = parent.getParent();
        }

        if (parent == null) {
            throw new IllegalArgumentException("Window does not have a native host.");
        }

        return (java.awt.Window) parent;
    }

    @Override
    @UnsupportedOperation
    protected void setSkin(final Skin skin) {
        throw new UnsupportedOperationException("Can't replace Display skin.");
    }

    @Override
    @UnsupportedOperation
    protected void setParent(final Container parent) {
        throw new UnsupportedOperationException("Display can't have a parent.");
    }

    @Override
    @UnsupportedOperation
    public void setLocation(final int x, final int y) {
        throw new UnsupportedOperationException("Can't change the location of the display.");
    }

    @Override
    @UnsupportedOperation
    public void setVisible(final boolean visible) {
        throw new UnsupportedOperationException("Can't change the visibility of the display.");
    }

    @Override
    @UnsupportedOperation
    public void setTooltipText(final String tooltipText) {
        throw new UnsupportedOperationException("Can't set a tooltip on the display.");
    }

    @Override
    public void repaint(final int x, final int y, final int width, final int height, final boolean immediate) {
        if (immediate) {
            Graphics2D graphics = (Graphics2D) displayHost.getGraphics();

            // If the display host has been made non-displayable (as will happen when the native peer closes),
            // graphics will be null
            if (graphics != null) {
                double scale = displayHost.getScale();
                if (scale == 1) {
                    graphics.clipRect(x, y, width, height);
                } else {
                    graphics.clipRect((int) Math.floor(x * scale), (int) Math.floor(y * scale),
                        (int) Math.ceil(width * scale) + 1, (int) Math.ceil(height * scale) + 1);
                }

                displayHost.paint(graphics);
                graphics.dispose();
            }
        } else {
            displayHost.repaint(x, y, width, height);
        }
    }

    @Override
    public void insert(final Component component, final int index) {
        if (!(component instanceof Window)) {
            throw new IllegalArgumentException("component must be an instance of "
                + Window.class);
        }

        super.insert(component, index);
    }

    @Override
    protected void descendantAdded(final Component descendant) {
        super.descendantAdded(descendant);

        String automationID = descendant.getAutomationID();

        if (automationID != null) {
            Automation.add(automationID, descendant);
        }
    }

    @Override
    protected void descendantRemoved(final Component descendant) {
        super.descendantRemoved(descendant);

        String automationID = descendant.getAutomationID();

        if (automationID != null) {
            Automation.remove(automationID);
        }
    }

    @Override
    public FocusTraversalPolicy getFocusTraversalPolicy() {
        return null;
    }
}
