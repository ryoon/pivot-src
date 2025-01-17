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
package org.apache.pivot.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.annotations.UnsupportedOperation;

/**
 * Abstract base class for listener lists. <p> NOTE This class is not inherently
 * thread safe. Subclasses that require thread-safe access should synchronize
 * method access appropriately. Callers must manually synchronize on the
 * listener list instance to ensure thread safety during iteration.
 *
 * @param <T> The listener type contained in this list.
 */
public abstract class ListenerList<T> implements Iterable<T> {

    /**
     * Iterator through the current array of elements.
     */
    private class NodeIterator implements Iterator<T> {
        /** The current position in the list for the iteration. */
        private int index;

        /** Construct and start iteration at the beginning. */
        NodeIterator() {
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return (index < last);
        }

        @Override
        public T next() {
            if (index >= last) {
                throw new NoSuchElementException();
            }

            return list[index++];
        }

        @Override
        @UnsupportedOperation
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /** We don't expect many listeners (1 or 2 typically), so start off with this size. */
    private static final int DEFAULT_SIZE = 5;

    /**
     * The current array of items (some of which are null).
     * <p> All non-null objects are at the beginning of the array
     * and the array is reorganized on "remove".
     */
    @SuppressWarnings("unchecked")
    private T[] list = (T[]) new Object[DEFAULT_SIZE];
    /** The current length of the active list. */
    private int last = 0;

    /**
     * Adds a listener to the list, if it has not previously been added.
     *
     * @param listener New listener to add to the list.
     */
    public void add(final T listener) {
        if (indexOf(listener) >= 0) {
            System.err.println("Duplicate listener " + listener + " added to " + this);
            return;
        }

        // If no slot is available, increase the size of the array
        if (last >= list.length) {
            list = Arrays.copyOf(list, list.length + DEFAULT_SIZE);
        }

        list[last++] = listener;
    }

    /**
     * Inserts a new listener to the specified position in the list.
     *
     * @param index The 0-based position in the list where to add the new listener.
     * @param listener New listener to add there.
     */
    public void add(final int index, final T listener) {
        Utils.checkZeroBasedIndex(index, last);

        if (indexOf(listener) >= 0) {
            System.err.println("Duplicate listener " + listener + " added to " + this);
            return;
        }

        // If no slot is available, increase the size of the array
        if (last >= list.length) {
            @SuppressWarnings("unchecked")
            T[] newList = (T[]) new Object[list.length + DEFAULT_SIZE];
            if (index > 0) {
                System.arraycopy(list, 0, newList, 0, index);
            }
            if (last > index) {
                System.arraycopy(list, index, newList, index + 1, last - index);
            }
            list = newList;
        } else {
            if (last > index) {
                System.arraycopy(list, index, list, index + 1, last - index);
            }
        }

        list[index] = listener;
        last++;
    }

    /**
     * Removes a listener from the list, if it has previously been added.
     *
     * @param listener The listener to remove from the list.
     */
    public void remove(final T listener) {
        int index = indexOf(listener);

        if (index < 0) {
            System.err.println("Nonexistent listener " + listener + " removed from " + this);
            return;
        }

        // Once we find the entry in the list, copy the rest of the
        // existing entries down by one position
        if (index < last - 1) {
            System.arraycopy(list, index + 1, list, index, last - 1 - index);
        }

        list[--last] = null;
    }

    /**
     * Search for the given listener in the list.
     *
     * @param listener The listener to find.
     * @return The index {@code >= 0} of the listener if found, or {@code -1}
     * if not found.
     */
    private int indexOf(final T listener) {
        Utils.checkNull(listener, "listener");

        for (int i = 0; i < last; i++) {
            if (list[i] == listener) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Tests the existence of a listener in the list.
     *
     * @param listener The listener to test.
     * @return {@code true} if the listener exists in the list; {@code false},
     * otherwise.
     */
    public boolean contains(final T listener) {
        return indexOf(listener) >= 0;
    }

    /**
     * Tests the emptiness of the list.
     *
     * @return {@code true} if the list contains no listeners; {@code false},
     * otherwise.
     */
    public boolean isEmpty() {
        return last == 0;
    }

    /**
     * Get the number of elements in the list.
     *
     * @return the number of elements.
     */
    public int getLength() {
        return last;
    }

    /**
     * Get the indexed element in the list.
     *
     * @param index Position of the element to retrieve.
     * @return The element at position {@code index}.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public T get(final int index) {
        Utils.checkZeroBasedIndex(index, last);
        return list[index];
    }

    @Override
    public final Iterator<T> iterator() {
        return new NodeIterator();
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        return StringUtils.append(sb, this).toString();
    }

}
