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

import java.awt.MouseInfo;

import org.apache.pivot.util.Utils;

/**
 * Class representing the system mouse.
 */
public final class Mouse {
    /**
     * Private constructor since this is a utility class (all static methods).
     */
    private Mouse() {
    }

    /**
     * Enumeration representing mouse buttons.
     */
    public enum Button {
        LEFT, RIGHT, MIDDLE;

        public int getMask() {
            return 1 << ordinal();
        }
    }

    /**
     * Enumeration defining supported scroll types.
     */
    public enum ScrollType {
        UNIT, BLOCK
    }

    private static int buttons = 0;
    private static Component capturer = null;

    /**
     * @return A bitfield representing the mouse buttons that are currently
     * pressed.
     */
    public static int getButtons() {
        return buttons;
    }

    protected static void setButtons(final int buttons) {
        Mouse.buttons = buttons;
    }

    /**
     * Tests the pressed state of a button.
     *
     * @param button The button to test.
     * @return <tt>true</tt> if the button is pressed; <tt>false</tt>, otherwise.
     */
    public static boolean isPressed(final Button button) {
        return (buttons & button.getMask()) > 0;
    }

    /**
     * @return The number of mouse buttons.
     */
    public static int getButtonCount() {
        return MouseInfo.getNumberOfButtons();
    }

    /**
     * "Captures" the mouse, causing all mouse input to be delegated to the
     * given component rather than propagating down the component hierarchy.
     *
     * @param capturerArgument The component that wants to capture the mouse.
     * The mouse pointer must currently be over the component.
     */
    public static void capture(final Component capturerArgument) {
        Utils.checkNull(capturerArgument, "capturer");

        if (!capturerArgument.isMouseOver()) {
            throw new IllegalArgumentException("Mouse pointer is not currently over capturer.");
        }

        if (Mouse.capturer != null) {
            throw new IllegalStateException("Mouse is already captured.");
        }

        Mouse.capturer = capturerArgument;
    }

    /**
     * Releases mouse capture, causing mouse input to resume propagation down
     * the component hierarchy.
     */
    public static void release() {
        if (capturer == null) {
            throw new IllegalStateException("Mouse is not currently captured.");
        }

        Display display = capturer.getDisplay();
        Point location = display.getMouseLocation();

        Component descendant = null;
        if (location != null) {
            descendant = display.getDescendantAt(location.x, location.y);
        }

        while (descendant != null && descendant != capturer) {
            descendant = descendant.getParent();
        }

        if (descendant == null) {
            // The mouse is no longer over the capturer
            capturer.mouseOut();
            Mouse.setCursor(display);

            // Allow the mouse to re-enter the display
            if (location != null) {
                display.mouseMove(location.x, location.y);
            }
        }

        capturer = null;
    }

    /**
     * Returns the mouse capturer.
     *
     * @return The component that has captured the mouse, or <tt>null</tt> if
     * the mouse is not currently captured.
     */
    public static Component getCapturer() {
        return capturer;
    }

    /**
     * @return The current cursor.
     *
     * @throws IllegalStateException If the mouse is not currently captured.
     */
    public static Cursor getCursor() {
        if (capturer == null) {
            throw new IllegalStateException("Mouse is not currently captured.");
        }

        Display display = capturer.getDisplay();
        ApplicationContext.DisplayHost displayHost = display.getDisplayHost();

        return Cursor.getCursor(displayHost.getCursor().getType());
    }

    /**
     * Sets the cursor to an explicit value.
     *
     * @param cursor The new mouse cursor value.
     * @throws IllegalArgumentException if the cursor value is {@code null}.
     * @throws IllegalStateException If the mouse is not currently captured.
     */
    public static void setCursor(final Cursor cursor) {
        Utils.checkNull(cursor, "cursor");

        if (capturer == null) {
            throw new IllegalStateException("Mouse is not currently captured.");
        }

        Display display = capturer.getDisplay();
        ApplicationContext.DisplayHost displayHost = display.getDisplayHost();
        displayHost.setCursor(cursor.getAWTCursor());
    }

    /**
     * Sets the cursor based on a given component.
     *
     * @param component The component used to set the cursor.
     * @throws IllegalArgumentException if the component is {@code null}, or
     * if the component is not currently visible.
     */
    public static void setCursor(final Component component) {
        Utils.checkNull(component, "component");

        if (!component.isVisible()) {
            throw new IllegalArgumentException("Component for mouse cursor is not visible.");
        }

        Component componentOrParent = component;
        Cursor cursor = null;

        if (componentOrParent.isEnabled()) {
            cursor = componentOrParent.getCursor();
            while (cursor == null && componentOrParent != null
                && !(componentOrParent instanceof Display)) {
                componentOrParent = componentOrParent.getParent();
                if (componentOrParent != null) {
                    cursor = componentOrParent.getCursor();
                }
            }
        }

        if (componentOrParent != null) {
            Display display = componentOrParent.getDisplay();
            ApplicationContext.DisplayHost displayHost = display.getDisplayHost();
            displayHost.setCursor((cursor == null) ? java.awt.Cursor.getDefaultCursor()
                : cursor.getAWTCursor());
        }
    }
}
