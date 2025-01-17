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

import org.apache.pivot.util.ListenerList;

/**
 * Spinner selection listener interface.
 */
public interface SpinnerSelectionListener {
    /**
     * Spinner selection listeners.
     */
    public static class Listeners extends ListenerList<SpinnerSelectionListener>
        implements SpinnerSelectionListener {
        @Override
        public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
            forEach(listener -> listener.selectedIndexChanged(spinner, previousSelectedIndex));
        }

        @Override
        public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
            forEach(listener -> listener.selectedItemChanged(spinner, previousSelectedItem));
        }
    }

    /**
     * Spinner selection listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements SpinnerSelectionListener {
        @Override
        public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
            // empty block
        }

        @Override
        public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
            // empty block
        }
    }

    /**
     * Called when a spinner's selected index has changed.
     *
     * @param spinner The source of the event.
     * @param previousSelectedIndex If the selection changed directly, contains
     * the index that was previously selected. Otherwise, contains the current
     * selection.
     */
    default void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
    }

    /**
     * Called when a spinners's selected item has changed.
     *
     * @param spinner The source of the event.
     * @param previousSelectedItem The item that was previously selected, or
     * {@code null} if the previous selection cannot be determined.
     */
    default void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
    }
}
