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

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowListener;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Window skin.
 */
public class WindowSkin extends ContainerSkin implements Window.Skin, WindowListener,
    WindowStateListener {
    /**
     * Focus traversal policy that always returns the window's content. This
     * ensures that focus does not traverse out of the window.
     */
    private static FocusTraversalPolicy focusTraversalPolicy = (container, component, direction) -> {
        assert (container instanceof Window) : "container is not a Window";

        Utils.checkNull(direction, "direction");

        return ((Window) container).getContent();
    };

    /**
     * Default and only constructor - set the default color.
     */
    public WindowSkin() {
        setBackgroundColor(defaultBackgroundColor());
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        Window window = (Window) component;
        window.getWindowListeners().add(this);
        window.getWindowStateListeners().add(this);

        window.setFocusTraversalPolicy(focusTraversalPolicy);
    }

    /**
     * @return The content component of this window.
     */
    private Component getContent() {
        return ((Window) getComponent()).getContent();
    }

    @Override
    public int getPreferredWidth(final int height) {
        Component content = getContent();

        return (content != null) ? content.getPreferredWidth(height) : 0;
    }

    @Override
    public int getPreferredHeight(final int width) {
        Component content = getContent();

        return (content != null) ? content.getPreferredHeight(width) : 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        Component content = getContent();

        return (content != null) ? content.getPreferredSize() : Dimensions.ZERO;
    }

    @Override
    public void layout() {
        Window window = (Window) getComponent();
        Component content = window.getContent();

        if (content != null) {
            content.setSize(window.getSize());
        }
    }

    @Override
    public Bounds getClientArea() {
        return new Bounds(getSize());
    }

    @Override
    public boolean mouseDown(final Container container, final Mouse.Button button,
            final int x, final int y) {
        boolean consumed = super.mouseDown(container, button, x, y);

        Window window = (Window) container;
        window.moveToFront();

        return consumed;
    }

    // Window events
    @Override
    public void contentChanged(final Window window, final Component previousContent) {
        invalidateComponent();
    }

    // Window state events
    @Override
    public void windowClosed(final Window window, final Display display, final Window owner) {
        window.getIcons().remove(0, window.getIcons().getLength());
        // invalidateComponent();
    }

}
