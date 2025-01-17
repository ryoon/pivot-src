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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * List view selection listener interface.
 */
public interface ListViewSelectionListener {
    /**
     * List view selection listeners.
     */
    public static class Listeners extends ListenerList<ListViewSelectionListener> implements
        ListViewSelectionListener {
        @Override
        public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
            forEach(listener -> listener.selectedRangeAdded(listView, rangeStart, rangeEnd));
        }

        @Override
        public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
            forEach(listener -> listener.selectedRangeRemoved(listView, rangeStart, rangeEnd));
        }

        @Override
        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelection) {
            forEach(listener -> listener.selectedRangesChanged(listView, previousSelection));
        }

        @Override
        public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
            forEach(listener -> listener.selectedItemChanged(listView, previousSelectedItem));
        }
    }

    /**
     * List view selection listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ListViewSelectionListener {
        @Override
        public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
            // empty block
        }

        @Override
        public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
            // empty block
        }

        @Override
        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
            // empty block
        }

        @Override
        public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
            // empty block
        }
    }

    /**
     * Called when a range has been added to a list view's selection.
     *
     * @param listView The source of the event.
     * @param rangeStart The start index of the range that was added, inclusive.
     * @param rangeEnd The end index of the range that was added, inclusive.
     */
    default void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
    }

    /**
     * Called when a range has been removed from a list view's selection.
     *
     * @param listView The source of the event.
     * @param rangeStart The starting index of the range that was removed,
     * inclusive.
     * @param rangeEnd The starting index of the range that was removed,
     * inclusive.
     */
    default void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
    }

    /**
     * Called when a list view's selection state has changed. Called in any select mode, and
     * will be called BEFORE the {@link #selectedItemChanged} callback if the ListView is in
     * single-select mode.  But, will be called AFTER either {@link #selectedRangeAdded} or
     * {@link #selectedRangeRemoved} if appropriate.
     *
     * @param listView The source of the event.
     * @param previousSelectedRanges If the selection changed directly, contains
     * the ranges that were previously selected. If the selection changed
     * indirectly as a result of a model change, contains the current selection.
     * Otherwise, contains {@code null}.
     */
    default void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
    }

    /**
     * Called when a list view's selected item has changed. Only called in single-select mode.
     *
     * @param listView The source of the event.
     * @param previousSelectedItem The item that was previously selected.
     */
    default void selectedItemChanged(ListView listView, Object previousSelectedItem) {
    }
}
