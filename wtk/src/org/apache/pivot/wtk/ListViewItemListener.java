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
 * List view item listener interface.
 */
public interface ListViewItemListener {
    /**
     * List view item listeners.
     */
    public static class Listeners extends ListenerList<ListViewItemListener>
        implements ListViewItemListener {
        @Override
        public void itemInserted(ListView listView, int index) {
            forEach(listener -> listener.itemInserted(listView, index));
        }

        @Override
        public void itemsRemoved(ListView listView, int index, int count) {
            forEach(listener -> listener.itemsRemoved(listView, index, count));
        }

        @Override
        public void itemUpdated(ListView listView, int index) {
            forEach(listener -> listener.itemUpdated(listView, index));
        }

        @Override
        public void itemsCleared(ListView listView) {
            forEach(listener -> listener.itemsCleared(listView));
        }

        @Override
        public void itemsSorted(ListView listView) {
            forEach(listener -> listener.itemsSorted(listView));
        }
    }

    /**
     * List view item listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements ListViewItemListener {
        @Override
        public void itemInserted(ListView listView, int index) {
            // empty block
        }

        @Override
        public void itemsRemoved(ListView listView, int index, int count) {
            // empty block
        }

        @Override
        public void itemUpdated(ListView listView, int index) {
            // empty block
        }

        @Override
        public void itemsCleared(ListView listView) {
            // empty block
        }

        @Override
        public void itemsSorted(ListView listView) {
            // empty block
        }
    }

    /**
     * Called when an item has been inserted into the list view.
     *
     * @param listView The source of the event.
     * @param index The index of the item that was inserted.
     */
    default void itemInserted(ListView listView, int index) {
    }

    /**
     * Called when items have been removed from the list view.
     *
     * @param listView The source of the event.
     * @param index The first index affected by the event.
     * @param count The number of items that were removed, or <code>-1</code> if all
     * items were removed.
     */
    default void itemsRemoved(ListView listView, int index, int count) {
    }

    /**
     * Called when an item in the list view has been updated.
     *
     * @param listView The source of the event.
     * @param index The first index affected by the event.
     */
    default void itemUpdated(ListView listView, int index) {
    }

    /**
     * Called when the items in a list view have been cleared.
     *
     * @param listView The source of the event.
     */
    default void itemsCleared(ListView listView) {
    }

    /**
     * Called when the items in a list view have been sorted.
     *
     * @param listView The source of the event.
     */
    default void itemsSorted(ListView listView) {
    }
}
