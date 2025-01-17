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
import org.apache.pivot.util.ListenerList;

/**
 * Map listener interface.
 *
 * @param <K> Type of key object used in this map.
 * @param <V> Type of value object stored for each key.
 */
public interface MapListener<K, V> {
    /**
     * Map listeners.
     *
     * @param <K> Type of key object used in this map.
     * @param <V> Type of value object stored for each key.
     */
    class Listeners<K, V> extends ListenerList<MapListener<K, V>> implements
        MapListener<K, V> {
        @Override
        public void valueAdded(final Map<K, V> map, final K key) {
            forEach(listener -> listener.valueAdded(map, key));
        }

        @Override
        public void valueRemoved(final Map<K, V> map, final K key, final V value) {
            forEach(listener -> listener.valueRemoved(map, key, value));
        }

        @Override
        public void valueUpdated(final Map<K, V> map, final K key, final V previousValue) {
            forEach(listener -> listener.valueUpdated(map, key, previousValue));
        }

        @Override
        public void mapCleared(final Map<K, V> map) {
            forEach(listener -> listener.mapCleared(map));
        }

        @Override
        public void comparatorChanged(final Map<K, V> map, final Comparator<K> previousComparator) {
            forEach(listener -> listener.comparatorChanged(map, previousComparator));
        }
    }

    /**
     * Map listener adapter.
     *
     * @param <K> Type of key object used in this map.
     * @param <V> Type of value object stored for each key.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    final class Adapter<K, V> implements MapListener<K, V> {
        @Override
        public void valueAdded(final Map<K, V> map, final K key) {
            // empty block
        }

        @Override
        public void valueUpdated(final Map<K, V> map, final K key, final V previousValue) {
            // empty block
        }

        @Override
        public void valueRemoved(final Map<K, V> map, final K key, final V value) {
            // empty block
        }

        @Override
        public void mapCleared(final Map<K, V> map) {
            // empty block
        }

        @Override
        public void comparatorChanged(final Map<K, V> map, final Comparator<K> previousComparator) {
            // empty block
        }
    }

    /**
     * Called when a key/value pair has been added to a map.
     *
     * @param map The source of the map event.
     * @param key The key that was added to the map.
     */
    default void valueAdded(Map<K, V> map, K key) {
    }

    /**
     * Called when a map value has been updated.
     *
     * @param map The source of the map event.
     * @param key The key whose value was updated.
     * @param previousValue The value that was previously associated with the
     * key.
     */
    default void valueUpdated(Map<K, V> map, K key, V previousValue) {
    }

    /**
     * Called when a key/value pair has been removed from a map.
     *
     * @param map The source of the map event.
     * @param key The key that was removed.
     * @param value The value that was removed.
     */
    default void valueRemoved(Map<K, V> map, K key, V value) {
    }

    /**
     * Called when map data has been reset.
     *
     * @param map The source of the map event.
     */
    default void mapCleared(Map<K, V> map) {
    }

    /**
     * Called when a map's comparator has changed.
     *
     * @param map The source of the event.
     * @param previousComparator The previous comparator value.
     */
    default void comparatorChanged(Map<K, V> map, Comparator<K> previousComparator) {
    }
}
