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
package org.apache.pivot.collections;

import java.util.Comparator;

/**
 * List listener interface.
 *
 * @param <T> Type of object stored in this list.
 */
public interface ListListener<T> {
    /**
     * List listener adapter.
     *
     * @param <T> Type of object stored in this list.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    final class Adapter<T> implements ListListener<T> {
        @Override
        public void itemInserted(final List<T> list, final int index) {
            // empty block
        }

        @Override
        public void itemsRemoved(final List<T> list, final int index, final Sequence<T> items) {
            // empty block
        }

        @Override
        public void itemUpdated(final List<T> list, final int index, final T previousItem) {
            // empty block
        }

        @Override
        public void listCleared(final List<T> list) {
            // empty block
        }

        @Override
        public void comparatorChanged(final List<T> list, final Comparator<T> previousComparator) {
            // empty block
        }
    }

    /**
     * Called when an item has been inserted into a list.
     *
     * @param list The source of the list event.
     * @param index The index at which the item was added.
     */
    default void itemInserted(List<T> list, int index) {
    }

    /**
     * Called when items have been removed from a list.
     *
     * @param list The source of the list event.
     * @param index The starting index from which items have been removed.
     * @param items The items that were removed from the list.
     */
    default void itemsRemoved(List<T> list, int index, Sequence<T> items) {
    }

    /**
     * Called when a list item has been updated.
     *
     * @param list The source of the list event.
     * @param index The index of the item that was updated.
     * @param previousItem The item that was previously stored at {@code index}.
     */
    default void itemUpdated(List<T> list, int index, T previousItem) {
    }

    /**
     * Called when list data has been reset.
     *
     * @param list The source of the list event.
     */
    default void listCleared(List<T> list) {
    }

    /**
     * Called when a list's comparator has changed.
     *
     * @param list The source of the event.
     * @param previousComparator The previous comparator value.
     */
    default void comparatorChanged(List<T> list, Comparator<T> previousComparator) {
    }
}
