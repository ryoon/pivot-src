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

import org.apache.pivot.util.BooleanResult;
import org.apache.pivot.util.ListenerList;

/**
 * Container mouse listener interface. Container mouse events are "tunneling"
 * events that are fired as the event propagates down the component hierarchy.
 */
public interface ContainerMouseListener {
    /**
     * Container mouse listeners.
     */
    public static class Listeners extends ListenerList<ContainerMouseListener>
        implements ContainerMouseListener {
        @Override
        public boolean mouseMove(Container container, int x, int y) {
            BooleanResult consumed = new BooleanResult(false);

            forEach(listener -> consumed.or(listener.mouseMove(container, x, y)));

            return consumed.get();
        }

        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            BooleanResult consumed = new BooleanResult(false);

            forEach(listener -> consumed.or(listener.mouseDown(container, button, x, y)));

            return consumed.get();
        }

        @Override
        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            BooleanResult consumed = new BooleanResult(false);

            forEach(listener -> consumed.or(listener.mouseUp(container, button, x, y)));

            return consumed.get();
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            BooleanResult consumed = new BooleanResult(false);

            forEach(listener -> consumed.or(listener.mouseWheel(container, scrollType,
                    scrollAmount, wheelRotation, x, y)));

            return consumed.get();
        }
    }

    /**
     * Container mouse listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ContainerMouseListener {
        @Override
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return false;
        }
    }

    /**
     * Called when the mouse is moved over a container.
     *
     * @param container The container under the mouse.
     * @param x         The X-position of the mouse.
     * @param y         The Y-position of the mouse.
     * @return {@code true} to consume the event; {@code false} to allow it to
     * propagate.
     */
    default boolean mouseMove(Container container, int x, int y) {
        return false;
    }

    /**
     * Called when the mouse is pressed over a container.
     *
     * @param container The container under the mouse.
     * @param button    Which button was pressed.
     * @param x         The X-position of the mouse.
     * @param y         The Y-position of the mouse.
     * @return {@code true} to consume the event; {@code false} to allow it to
     * propagate.
     */
    default boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
        return false;
    }

    /**
     * Called when the mouse is released over a container.
     *
     * @param container The container under the mouse.
     * @param button    Which mouse button was released.
     * @param x         The X-position at the time of release.
     * @param y         The Y-position at the time of release.
     * @return {@code true} to consume the event; {@code false} to allow it to
     * propagate.
     */
    default boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
        return false;
    }

    /**
     * Called when the mouse wheel is scrolled over a container.
     *
     * @param container     The container under the mouse.
     * @param scrollType    Which type of scroll happened.
     * @param scrollAmount  How much scrolling was requested.
     * @param wheelRotation The amount and direction of wheel rotation.
     * @param x             The X-position of the mouse at the time.
     * @param y             The Y-position of the mouse.
     * @return {@code true} to consume the event; {@code false} to allow it to
     * propagate.
     */
    default boolean mouseWheel(Container container, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        return false;
    }
}
