/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.collections;

import java.util.Comparator;
import java.util.Iterator;

import pivot.util.ListenerList;

public class HashMap<K, V> implements Map<K, V> {
    // TODO We're temporarily using a java.util.HashMap to back this map.
    // Eventually, we'll replace this with an internal map representation.
    protected java.util.HashMap<K, V> hashMap = null;

    private Comparator<K> comparator = null;
    private MapListenerList<K, V> mapListeners = new MapListenerList<K, V>();

    public HashMap() {
        hashMap = new java.util.HashMap<K, V>();
    }

    public HashMap(Map<K, V> map) {
        hashMap = new java.util.HashMap<K, V>();

        for (K key : map) {
            put(key, map.get(key));
        }
    }

    public HashMap(Comparator<K> comparator) {
        throw new UnsupportedOperationException("HashMap auto-sorting is not yet supported.");

        // this.comparator = comparator;
    }

    public V get(K key) {
        return hashMap.get(key);
    }

    public V put(K key, V value) {
        boolean update = hashMap.containsKey(key);
        V previousValue = hashMap.put(key, value);

        if (update) {
            mapListeners.valueUpdated(this, key, previousValue);
        }
        else {
            mapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    public V remove(K key) {
        V value = null;

        if (hashMap.containsKey(key)) {
            value = hashMap.remove(key);
            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    public void clear() {
        hashMap.clear();
        mapListeners.mapCleared(this);
    }

    public boolean containsKey(K key) {
        return hashMap.containsKey(key);
    }

    public boolean isEmpty() {
        return hashMap.isEmpty();
    }

    public Comparator<K> getComparator() {
        return comparator;
    }

    public void setComparator(Comparator<K> comparator) {
        // TODO
        throw new UnsupportedOperationException("HashMap auto-sorting is not yet supported.");
    }

    public Iterator<K> iterator() {
        return hashMap.keySet().iterator();
    }

    public ListenerList<MapListener<K, V>> getMapListeners() {
        return mapListeners;
    }
}
