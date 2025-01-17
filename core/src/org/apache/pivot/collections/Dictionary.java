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

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

import org.apache.pivot.util.Utils;

/**
 * Interface representing a set of key/value pairs.
 * @param <K> Type for the keys in this dictionary.
 * @param <V> Type for each of the values in the dictionary.
 */
public interface Dictionary<K, V> {
    /**
     * Class representing a key/value pair.
     * @param <K> Type of the key part of the pair.
     * @param <V> Type of the value in the pair.
     */
    final class Pair<K, V> implements Serializable {
        private static final long serialVersionUID = 5010958035775950649L;

        /** The &quot;key&quot; of this key-value pair. */
        public final K key;
        /** The &quot;value&quot; of this key-value pair. */
        public final V value;

        /**
         * The only constructor for this class that takes both the
         * key and the value to be stored, ensuring they are both set
         * all the time.
         * @param newKey The &quot;key&quot; to be stored.
         * @param newValue The &quot;value&quot; to be associated with that key.
         * @throws IllegalArgumentException if the key is {@code null}.
         */
        public Pair(final K newKey, final V newValue) {
            Utils.checkNull(newKey, "key");

            key = newKey;
            value = newValue;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(final Object object) {
            boolean equals = false;

            if (object instanceof Pair<?, ?>) {
                Pair<K, V> pair = (Pair<K, V>) object;
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
     * Retrieves the value for the given key.
     *
     * @param key The key whose value is to be returned.
     * @return The value corresponding to <code>key</code>, or null if the key does
     * not exist. Will also return null if the key refers to a null value. Use
     * {@link #containsKey} to distinguish between these two cases.
     */
    V get(K key);

    /**
     * Sets the value of the given key, creating a new entry or replacing the
     * existing value.
     *
     * @param key The key whose value is to be set.
     * @param value The value to be associated with the given key.
     * @return The value previously associated with the key.
     */
    V put(K key, V value);

    /**
     * Removes a key/value pair from the map.
     *
     * @param key The key whose mapping is to be removed.
     * @return The value that was removed.
     */
    V remove(K key);

    /**
     * Tests the existence of a key in the dictionary.
     *
     * @param key The key whose presence in the dictionary is to be tested.
     * @return {@code true} if the key exists in the dictionary; {@code false},
     * otherwise.
     */
    boolean containsKey(K key);

    /**
     * Determines if any of the given keys exists in the dictionary.
     *
     * @param keys The list of keys to search for in the dictionary.
     * @return {@code true} if {@link #containsKey} returns true for any
     * of the given keys, or {@code false} if none of the keys exist.
     */
    @SuppressWarnings("unchecked")
    default boolean containsAny(K... keys) {
        for (K key : keys) {
            if (containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the value of the first of the given keys that exists in the
     * dictionary (that is, the first key for which {@link #containsKey} returns
     * true).
     *
     * @param keys The list of keys to search for in the dictionary.
     * @return The first value found, or {@code null} if either the value is
     * null for the first key found, or none of the keys exists in the dictionary.
     * Use {@link #containsAny} to determine the difference.
     */
    @SuppressWarnings("unchecked")
    default V getFirst(K... keys) {
        for (K key : keys) {
            if (containsKey(key)) {
                return get(key);
            }
        }
        return null;
    }

    /**
     * Retrieve a String value from this dictionary; returning null if the key
     * does not exist.
     *
     * @param key The key for the (supposed) {@code String} value.
     * @return The string value, or {@code null} if the key is not present.
     */
    default String getString(K key) {
        return (String) get(key);
    }

    /**
     * Retrieve a String value from this dictionary; returning null if the key
     * does not exist.
     *
     * @param key The key for the (supposed) {@code String} value.
     * @param defaultValue The string to return if the key is not present.
     * @return The string value, or the default value if the key is not present.
     */
    default String getString(K key, String defaultValue) {
        if (containsKey(key)) {
            return (String) get(key);
        }
        return defaultValue;
    }

    /**
     * Using the other methods in this interface, retrieve an integer value
     * from this dictionary; returning 0 if the key does not exist.
     *
     * @param key The key for the (supposed) {@code Integer}
     * value to retrieve (actually any {@link Number} will work).
     * @return The integer value, or 0 if the key is not present.
     */
    default int getInt(K key) {
        return getInt(key, 0);
    }

    /**
     * Using the other methods in this interface, retrieve an integer value
     * from this dictionary; returning the given default if the key does not exist.
     *
     * @param key The key for the (supposed) {@code Integer}
     * value to retrieve (actually any {@link Number} will work).
     * @param defaultValue The value to return if the key is not present.
     * @return The integer value, or the default value if the key is not present.
     */
    default int getInt(K key, int defaultValue) {
        if (containsKey(key)) {
            Object value = get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        }
        return defaultValue;
    }

    /**
     * Using the other methods in this interface, retrieve a boolean value
     * from this dictionary; returning false if the key does not exist.
     *
     * @param key The key for the (supposed) {@code Boolean}
     * value to retrieve.
     * @return The boolean value, or false if the key is not present.
     */
    default boolean getBoolean(K key) {
        return getBoolean(key, false);
    }

    /**
     * Using the other methods in this interface, retrieve a boolean value
     * from this dictionary; returning a default value if the key does not exist.
     *
     * @param key The key for the (supposed) {@code Boolean}
     * value to retrieve.
     * @param defaultValue What to return if the key is not present.
     * @return The boolean value, or the default if the key is not present.
     */
    default boolean getBoolean(K key, boolean defaultValue) {
        if (containsKey(key)) {
            Object value = get(key);
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue();
            } else if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
        }
        return defaultValue;
    }

    /**
     * Using the other methods in this interface, retrieve a {@link Color} value
     * from this dictionary; returning {@code null} if the key does not exist.
     *
     * @param key The key for the (supposed) {@code Color}
     * value to retrieve.
     * @return The color value, or {@code null} if the key is not present.
     */
    default Color getColor(K key) {
        return (Color) get(key);
    }

    /**
     * Using the other methods in this interface, retrieve a {@link Font} value
     * from this dictionary; returning {@code null} if the key does not exist.
     *
     * @param key The key for the (supposed) {@code Font}
     * value to retrieve.
     * @return The font value, or {@code null} if the key is not present.
     */
    default Font getFont(K key) {
        return (Font) get(key);
    }

    /**
     * Put all the key/value pairs from the given map into this dictionary.
     *
     * @param map The other map to use.
     */
    default void putAll(Map<K, V> map) {
        for (K key : map) {
            put(key, map.get(key));
        }
    }

    /**
     * Copy the value from one dictionary to this one.
     *
     * @param key Key for value to be copied.
     * @param source The source to copy from.
     * @return The previous value in the target dictionary.
     */
    default Object copy(K key, Dictionary<K, V> source) {
        return put(key, source.get(key));
    }

}
