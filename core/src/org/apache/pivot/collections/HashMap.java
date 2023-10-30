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

import java.io.Serializable;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.util.EmptyIterator;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;


/**
 * Implementation of the {@link Map} interface that is backed by a hash table.
 * <p> Differs from the standard Java {@link java.util.HashMap} because it notifies
 * its listeners for changes. This allows automatic updates of the UI whenever the
 * underlying data in this structure changes.
 *
 * @param <K> Type of the key for elements in this map.
 * @param <V> Type of value elements in the map.
 */
public class HashMap<K, V> implements Map<K, V>, Serializable {
    private static final long serialVersionUID = -7079717428744528670L;

    /**
     * Iterator over the keys of this map.
     */
    private class KeyIterator implements Iterator<K> {
        /** Index of the bucket we are currently traversing. */
        private int bucketIndex;
        /** The iterator over the current bucket. */
        private Iterator<Pair<K, V>> entryIterator;
        /** Local modification count to detect changes to the map while iterating. */
        private int localIteratorCount;

        /** Current entry while iterating. */
        private Pair<K, V> entry = null;

        /**
         * Begin the iteration over the map keys.
         */
        KeyIterator() {
            bucketIndex = 0;
            entryIterator = getBucketIterator(bucketIndex);

            localIteratorCount = HashMap.this.count;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            if (localIteratorCount != HashMap.this.count) {
                throw new ConcurrentModificationException();
            }

            // Move to the next bucket
            while (entryIterator != null && !entryIterator.hasNext()) {
                entryIterator = (++bucketIndex < buckets.getLength()) ? getBucketIterator(bucketIndex)
                    : null;
            }

            return (entryIterator != null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            entry = entryIterator.next();
            return entry.key;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            if (entry == null || entryIterator == null) {
                throw new IllegalStateException();
            }

            entryIterator.remove();
            localIteratorCount--;
            HashMap.this.count--;

            if (mapListeners != null) {
                mapListeners.valueRemoved(HashMap.this, entry.key, entry.value);
            }

            entry = null;
        }

        /**
         * Retrieve an iterator over the given bucket.
         *
         * @param index Index of the bucket to iterate over.
         * @return      The iterator over that bucket.
         */
        private Iterator<Pair<K, V>> getBucketIterator(final int index) {
            LinkedList<Pair<K, V>> bucket = buckets.get(index);

            return (bucket == null) ? new EmptyIterator<Pair<K, V>>() : bucket.iterator();
        }
    }

    /**
     * The hash buckets for this map.
     */
    private ArrayList<LinkedList<Pair<K, V>>> buckets;
    /**
     * The desired load factor for this map.
     */
    private float loadFactor;

    /**
     * The current number of elements in this map.
     */
    private int count = 0;
    /**
     * The current list of keys in this map if we're sorting by them.
     */
    private ArrayList<K> keys = null;

    /**
     * The list of listeners for changes in this map.
     */
    private transient MapListener.Listeners<K, V> mapListeners = null;

    /**
     * The default initial capacity of this map.
     */
    public static final int DEFAULT_CAPACITY = 16;
    /**
     * The default load factor for this map.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * A hash multiiplier for computing the hash code for this map.
     */
    private static final int HASH_MULTIPLIER = 31;


    /**
     * Default constructor using default capacity and load factor.
     */
    public HashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct a map sized for the given capacity, and using the default
     * load factor.
     *
     * @param capacity The desired initial capacity of the map.
     */
    public HashMap(final int capacity) {
        this(capacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct a map sized to the given capacity, using the given load factor.
     *
     * @param capacity The desired initial capacity of the map.
     * @param load     Desired load factor for scaling the capacity.
     */
    public HashMap(final int capacity, final float load) {
        loadFactor = load;

        rehash(capacity);
    }

    /**
     * Calculate an appropriate capacity, given the default load factor and the
     * given number of initial entries to insert.
     *
     * @param size Size of an external map we are going to copy into this new one.
     * @return     An appropriate capacity for that size, given the default load factor.
     */
    private static int capacityForSize(final int size) {
        return Math.max((int) (size / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_CAPACITY);
    }

    /**
     * Construct a new map with the given initial entries.
     *
     * @param entries The initial map entries.
     */
    @SafeVarargs
    public HashMap(final Pair<K, V>... entries) {
        this(capacityForSize(entries.length));

        for (Pair<K, V> entry : entries) {
            put(entry.key, entry.value);
        }
    }

    /**
     * Construct a duplicate map from the given one.
     *
     * @param map The other map to copy into this one.
     */
    public HashMap(final Map<K, V> map) {
        this(capacityForSize(map.getCount()));

        for (K key : map) {
            put(key, map.get(key));
        }
    }

    /**
     * Construct a duplicate map from the given standard map structure.
     *
     * @param map A standard {@link java.util.Map} to copy.
     */
    public HashMap(final java.util.Map<K, V> map) {
        this(capacityForSize(map.size()));

        for (K key : map.keySet()) {
            put(key, map.get(key));
        }
    }

    /**
     * Construct an ordered map using the given comparator.
     *
     * @param comparator Comparator used to sort the map keys.
     */
    public HashMap(final Comparator<K> comparator) {
        this();

        setComparator(comparator);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException If {@code key} is {@literal null}.
     */
    @Override
    public V get(final K key) {
        Utils.checkNull(key, "key");

        V value = null;

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                value = entry.value;
                break;
            }
        }

        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException If {@code key} is {@literal null}.
     */
    @Override
    public V put(final K key, final V value) {
        return put(key, value, true);
    }

    /**
     * Put an entry into this map, with or without notifying the listeners.
     * <p> Not notifying will only happen during {@link #rehash} because nothing
     * is changing during that process.
     *
     * @param key             Key of the new entry.
     * @param value           Value for that key.
     * @param notifyListeners Whether to notify the listeners of this change.
     * @return                The previous value (if any) for the key.
     * @throws IllegalArgumentException if the key is {@literal null}.
     */
    private V put(final K key, final V value, final boolean notifyListeners) {
        Utils.checkNull(key, "key");

        V previousValue = null;

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        int i = 0;
        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                // Update the entry
                previousValue = entry.value;
                iterator.update(new Pair<>(key, value));

                if (mapListeners != null && notifyListeners) {
                    mapListeners.valueUpdated(this, key, previousValue);
                }

                break;
            }

            i++;
        }

        if (i == bucket.getLength()) {
            // Add the entry
            bucket.add(new Pair<>(key, value));

            if (keys != null) {
                keys.add(key);
            }

            // Increment the count
            count++;

            int capacity = getCapacity();
            if (count > (int) (capacity * loadFactor)) {
                rehash(capacity * 2);
            }

            if (mapListeners != null && notifyListeners) {
                mapListeners.valueAdded(this, key);
            }
        }

        return previousValue;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException If {@code key} is {@literal null}.
     */
    @Override
    public V remove(final K key) {
        Utils.checkNull(key, "key");

        V value = null;

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                // Remove the entry
                value = entry.value;
                iterator.remove();

                if (keys != null) {
                    keys.remove(key);
                }

                // Decrement the count
                count--;

                if (mapListeners != null) {
                    mapListeners.valueRemoved(this, key, value);
                }

                break;
            }
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        if (count > 0) {
            // Remove all entries
            for (LinkedList<Pair<K, V>> bucket : buckets) {
                if (bucket != null) {
                    bucket.clear();
                }
            }

            if (keys != null) {
                keys.clear();
            }

            // Clear the count
            count = 0;

            if (mapListeners != null) {
                mapListeners.mapCleared(this);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException If {@code key} is {@literal null}.
     */
    @Override
    public boolean containsKey(final K key) {
        Utils.checkNull(key, "key");

        // Locate the entry
        LinkedList<Pair<K, V>> bucket = getBucket(key);

        int i = 0;
        List.ItemIterator<Pair<K, V>> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Pair<K, V> entry = iterator.next();

            if (entry.key.equals(key)) {
                break;
            }

            i++;
        }

        return (i < bucket.getLength());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return (count == 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return count;
    }

    /**
     * Retrieve the capacity of the map (that is, the number of buckets).
     *
     * @return The map's capacity.
     */
    public int getCapacity() {
        return buckets.getLength();
    }

    /**
     * Rebuild the map to the new capacity by rehashing the keys into the new set of buckets.
     *
     * @param newCapacity The enlarged new capacity of the map.
     */
    private void rehash(final int newCapacity) {
        ArrayList<LinkedList<Pair<K, V>>> previousBuckets = this.buckets;
        buckets = new ArrayList<>(newCapacity);

        for (int i = 0; i < newCapacity; i++) {
            buckets.add(null);
        }

        if (previousBuckets != null) {
            count = 0;

            if (keys != null) {
                keys.clear();
            }

            for (LinkedList<Pair<K, V>> bucket : previousBuckets) {
                if (bucket != null) {
                    for (Pair<K, V> entry : bucket) {
                        put(entry.key, entry.value, false);
                    }
                }
            }
        }
    }

    /**
     * Get the appropriate bucket for the given key.
     *
     * @param key Map key to be looked at.
     * @return    The currently correct map bucket for this key.
     */
    private LinkedList<Pair<K, V>> getBucket(final K key) {
        int hashCode = key.hashCode();
        int bucketIndex = Math.abs(hashCode % getCapacity());

        LinkedList<Pair<K, V>> bucket = buckets.get(bucketIndex);
        if (bucket == null) {
            bucket = new LinkedList<>();
            buckets.update(bucketIndex, bucket);
        }

        return bucket;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparator<K> getComparator() {
        return (keys == null) ? null : keys.getComparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setComparator(final Comparator<K> comparator) {
        Comparator<K> previousComparator = getComparator();

        if (comparator == null) {
            keys = null;
        } else {
            if (keys == null) {
                // Populate key list
                ArrayList<K> keysLocal = new ArrayList<>((int) (getCapacity() * loadFactor));
                for (K key : this) {
                    keysLocal.add(key);
                }

                this.keys = keysLocal;
            }

            keys.setComparator(comparator);
        }

        if (mapListeners != null) {
            mapListeners.comparatorChanged(this, previousComparator);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<K> iterator() {
        return (keys == null) ? new KeyIterator() : new ImmutableIterator<>(keys.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListenerList<MapListener<K, V>> getMapListeners() {
        if (mapListeners == null) {
            mapListeners = new MapListener.Listeners<>();
        }

        return mapListeners;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object o) {
        boolean equals = false;

        if (this == o) {
            equals = true;
        } else if (o instanceof Map<?, ?>) {
            Map<K, V> map = (Map<K, V>) o;

            if (count == map.getCount()) {
                for (K key : this) {
                    V value = get(key);

                    if (value == null) {
                        equals = (map.containsKey(key) && map.get(key) == null);
                    } else {
                        equals = value.equals(map.get(key));
                    }

                    if (!equals) {
                        break;
                    }
                }
            }
        }

        return equals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashCode = 1;

        for (K key : this) {
            hashCode = HASH_MULTIPLIER * hashCode + key.hashCode();
        }

        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getSimpleName());
        sb.append(" {");

        int i = 0;
        for (K key : this) {
            if (i++ > 0) {
                sb.append(", ");
            }

            sb.append(key).append(": ").append(get(key));
        }

        sb.append("}");

        return sb.toString();
    }

}
