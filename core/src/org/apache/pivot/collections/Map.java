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
 * Collection interface representing set of key/value pairs.
 */
public interface Map<K, V> extends Dictionary<K, V>, Collection<K> {
    /**
     * Class representing a key/value pair.
     */
    public static final class Pair<K, V> {
        public final K key;
        public final V value;

        public Pair(K key, V value) {
            if (key == null) {
                throw new IllegalArgumentException();
            }

            this.key = key;
            this.value = value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object object) {
           boolean equals = false;

           if (object instanceof Pair<?, ?>) {
              Pair<K, V> pair = (Pair<K, V>)object;
              equals = (key.equals(pair.key)
                  && ((value == null && pair.value == null)
                      || (value != null && value.equals(pair.value))));
           }

           return equals;
        }

        @Override
        public int hashCode() {
           return key.hashCode();
        }

        @Override
        public String toString() {
           return "{" + key + ": " + value + "}";
        }
    }

    /**
     * Map listener list.
     */
    public static class MapListenerList<K, V>
        extends ListenerList<MapListener<K, V>> implements MapListener<K, V> {
        public void valueAdded(Map<K, V> map, K key) {
            for (MapListener<K, V> listener : this) {
                listener.valueAdded(map, key);
            }
        }

        public void valueRemoved(Map<K, V> map, K key, V value) {
            for (MapListener<K, V> listener : this) {
                listener.valueRemoved(map, key, value);
            }
        }

        public void valueUpdated(Map<K, V> map, K key, V previousValue) {
            for (MapListener<K, V> listener : this) {
                listener.valueUpdated(map, key, previousValue);
            }
        }

        public void mapCleared(Map<K, V> map) {
            for (MapListener<K, V> listener : this) {
                listener.mapCleared(map);
            }
        }

        public void comparatorChanged(Map<K, V> map, Comparator<K> previousComparator) {
            for (MapListener<K, V> listener : this) {
                listener.comparatorChanged(map, previousComparator);
            }
        }
    }

    /**
     * Sets the value of the given key, creating a new entry or replacing the
     * existing value, and firing a corresponding event.
     *
     * @param key
     * The key whose value is to be set.
     *
     * @param value
     * The value to be associated with the given key.
     *
     * @see MapListener#valueAdded(Map, Object)
     * @see MapListener#valueUpdated(Map, Object, Object)
     */
    public V put(K key, V value);

    /**
     * @see MapListener#valueRemoved(Map, Object, Object)
     */
    public V remove(K key);

    /**
     * Removes all entries in the map.
     *
     * @see MapListener#mapCleared(Map)
     */
    public void clear();

    /**
     * Returns the number of entries in the map.
     */
    public int getCount();

    /**
     * @see MapListener#comparatorChanged(Map, Comparator)
     */
    public void setComparator(Comparator<K> comparator);

    /**
     * Returns the map listener collection.
     */
    public ListenerList<MapListener<K, V>> getMapListeners();
}
