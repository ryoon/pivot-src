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

import org.apache.pivot.wtk.skin.DisplaySkin;

/**
 * Container that serves as the root of a component hierarchy.
 *
 * @author gbrown
 */
public final class Display extends Container {
    private class ValidateCallback implements Runnable {
        public void run() {
            validate();
            validateCallback = null;
        }
    }

    private ApplicationContext.DisplayHost displayHost;
    private ValidateCallback validateCallback = null;

    protected Display(ApplicationContext.DisplayHost displayHost) {
        this.displayHost = displayHost;
        super.setSkin(new DisplaySkin());
    }

    public ApplicationContext.DisplayHost getDisplayHost() {
        return displayHost;
    }

    public Point getMouseLocation() {
        return displayHost.getMouseLocation();
    }

    @Override
    protected void setSkin(Skin skin) {
        throw new UnsupportedOperationException("Can't replace Display skin.");
    }

    @Override
    protected void setParent(Container parent) {
        throw new UnsupportedOperationException("Display can't have a parent.");
    }

    @Override
    public void setLocation(int x, int y) {
        throw new UnsupportedOperationException("Can't change the location of the display.");
    }

    @Override
    public void invalidate() {
        if (validateCallback == null) {
            validateCallback = new ValidateCallback();
            ApplicationContext.queueCallback(validateCallback);
        }

        super.invalidate();
    }

    @Override
    public void repaint(int x, int y, int width, int height, boolean immediate) {
        if (immediate) {
            Graphics2D graphics = (Graphics2D)displayHost.getGraphics();

            // If the display host has been made non-displayable (as will
            // happen when the native peer closes), graphics will be null
            if (graphics != null) {
                double scale = displayHost.getScale();
                if (scale == 1) {
                    graphics.clipRect(x, y, width, height);
                } else {
                    graphics.clipRect((int)Math.floor(x * scale), (int)Math.floor(y * scale),
                        (int)Math.ceil(width * scale) + 1, (int)Math.ceil(height * scale) + 1);
                }

                displayHost.paint(graphics);
                graphics.dispose();
            }
        } else {
            displayHost.repaint(x, y, width, height);
        }
    }

    @Override
    public void insert(Component component, int index) {
        if (!(component instanceof Window)) {
            throw new IllegalArgumentException("component must be an instance "
               + "of " + Window.class);
        }

        super.insert(component, index);
    }

    @Override
    public FocusTraversalPolicy getFocusTraversalPolicy() {
        return null;
    }
}
