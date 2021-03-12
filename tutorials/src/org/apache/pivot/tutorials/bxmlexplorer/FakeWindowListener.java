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
package org.apache.pivot.tutorials.bxmlexplorer;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.media.Image;

/**
 * Listener for events on our "fake" window.
 */
public interface FakeWindowListener {
    /**
     * Fake window listeners.
     */
    final class Listeners extends ListenerList<FakeWindowListener> implements FakeWindowListener {
        @Override
        public void titleChanged(final FakeWindow window, final String previousTitle) {
            forEach(listener -> listener.titleChanged(window, previousTitle));
        }

        @Override
        public void iconAdded(final FakeWindow window, final Image addedIcon) {
            forEach(listener -> listener.iconAdded(window, addedIcon));
        }

        @Override
        public void iconInserted(final FakeWindow window, final Image addedIcon, final int index) {
            forEach(listener -> listener.iconInserted(window, addedIcon, index));
        }

        @Override
        public void iconsRemoved(final FakeWindow window, final int index, final Sequence<Image> removed) {
            forEach(listener -> listener.iconsRemoved(window, index, removed));
        }

        @Override
        public void contentChanged(final FakeWindow window, final Component previousContent) {
            forEach(listener -> listener.contentChanged(window, previousContent));
        }

    }

    /**
     * Called when a window's title has changed.
     *
     * @param window The window that has changed.
     * @param previousTitle Previous title for the window.
     */
    default void titleChanged(FakeWindow window, String previousTitle) {
    }

    /**
     * Called when an icon has been added to the window.
     *
     * @param window The window that has changed.
     * @param addedIcon The new icon that has been added to the window.
     */
    default void iconAdded(FakeWindow window, Image addedIcon) {
    }

    /**
     * Called when an icon has been inserted in the window's icon list.
     *
     * @param window The window that has changed.
     * @param addedIcon The new icon that was inserted.
     * @param index The index where the insertion occurred.
     */
    default void iconInserted(FakeWindow window, Image addedIcon, int index) {
    }

    /**
     * Called when an icon has been removed from the window's icon list.
     *
     * @param window The window that has changed.
     * @param index The starting index of the removal.
     * @param removed The list of removed icons.
     */
    default void iconsRemoved(FakeWindow window, int index, Sequence<Image> removed) {
    }

    /**
     * Called when a window's content component has changed.
     *
     * @param window The window that has changed.
     * @param previousContent The previous window content component.
     */
    default void contentChanged(FakeWindow window, Component previousContent) {
    }

}
