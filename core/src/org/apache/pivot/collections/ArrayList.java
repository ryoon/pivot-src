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
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link List} interface that is backed by an array. <p>
 * NOTE This class is not thread-safe. For concurrent access, use a
 * {@link org.apache.pivot.collections.concurrent.SynchronizedList}.
 *
 * @param <T> Type of the list elements.
 */
public class ArrayList<T> implements List<T>, Serializable {
    private static final long serialVersionUID = 2123086211369612675L;

    /**
     * Iterator through the items of the ArrayList.
     */
    private class ArrayListItemIterator implements ItemIterator<T> {
        /** Current index into the list for this iterator. */
        private int index = 0;
        /** Modification count for the list at the time this iterator was created;
         * used to detect concurrent modifications to the list.
         */
        private int iteratorModificationCount;
        /** Current direction of iteration. */
        private boolean forward = true;

        /**
         * Initialize the iterator.
         */
        ArrayListItemIterator() {
            iteratorModificationCount = ArrayList.this.modificationCount;
        }

        @Override
        public boolean hasNext() {
            checkConcurrentModification();

            return (index < length);
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            forward = true;
            return get(index++);
        }

        @Override
        public boolean hasPrevious() {
            checkConcurrentModification();

            return (index > 0);
        }

        @Override
        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            forward = false;
            return get(--index);
        }

        @Override
        public void toStart() {
            index = 0;
        }

        @Override
        public void toEnd() {
            index = length;
        }

        @Override
        public void insert(final T item) {
            indexBoundsCheck();

            ArrayList.this.insert(item, index);
            iteratorModificationCount++;
        }

        @Override
        public void update(final T item) {
            indexBoundsCheck();

            ArrayList.this.update(forward ? index - 1 : index, item);
            iteratorModificationCount++;
        }

        @Override
        public void remove() {
            indexBoundsCheck();

            if (forward) {
                index--;
            }

            ArrayList.this.remove(index, 1);
            iteratorModificationCount++;
        }

        /**
         * Check if the modification count saved at the beginning of our iteration
         * is still the same as the main list. Throw the exception if not.
         * @throws ConcurrentModificationException if the list was changed while iterating
         */
        private void checkConcurrentModification() {
            if (iteratorModificationCount != ArrayList.this.modificationCount) {
                throw new ConcurrentModificationException();
            }
        }

        /**
         * Check the current index against the ArrayList length.
         * @throws IndexOutOfBoundsException if the index is out of range.
         */
        private void indexBoundsCheck() {
            Utils.checkIndexBounds(index, 0, ArrayList.this.length);
        }
    }

    /**
     * The internal array of items used to store the elements of this list.
     */
    private Object[] items;

    /**
     * The current length of this list.
     */
    private int length = 0;

    /**
     * The comparator used for sorting the list (if any).
     */
    private Comparator<T> comparator = null;

    /**
     * Modification count - used to tell if concurrent modifications
     * occur during iteration.
     */
    private transient int modificationCount = 0;
    /**
     * The listeners for changes to the list (which would include any components
     * using this list as their data model).
     */
    private transient ListListenerList<T> listListeners = null;

    /**
     * Default initial capacity of this list if none is specified
     * at construction.
     */
    public static final int DEFAULT_CAPACITY = 10;

    /**
     * The desirable load factor (excess allocation at resize time).
     */
    private static final float LOAD_FACTOR = 1.5f;

    /**
     * The prime multiplier used in the {@link #hashCode} calculation to give
     * nicely spaced values.
     */
    private static final int HASH_MULTIPLIER = 31;


    /**
     * Construct a new unsorted ArrayList with the default capacity.
     */
    public ArrayList() {
        items = new Object[DEFAULT_CAPACITY];
    }

    /**
     * Construct a new ArrayList to be sorted by the given comparator, with the
     * default capacity.
     *
     * @param newComparator A comparator to sort the entries in the list.
     */
    public ArrayList(final Comparator<T> newComparator) {
        this();
        comparator = newComparator;
    }

    /**
     * Construct a new unsorted ArrayList with the given initial capacity.
     *
     * @param capacity The initial capacity for this list.
     * @throws IllegalArgumentException if the given capacity is negative.
     */
    public ArrayList(final int capacity) {
        Utils.checkNonNegative(capacity, "capacity");

        items = new Object[capacity];
    }

    /**
     * Construct a new ArrayList to be sorted by the given comparator,
     * with the given capacity.
     *
     * @param capacity The initial capacity for this list.
     * @param newComparator The comparator to use when sorting the list.
     */
    public ArrayList(final int capacity, final Comparator<T> newComparator) {
        this(capacity);
        comparator = newComparator;
    }

    /**
     * Construct a new ArrayList with the given list of items.
     *
     * @param initialItems The initial list of values for the list.
     */
    @SuppressWarnings("unchecked")
    public ArrayList(final T... initialItems) {
        this(initialItems, 0, initialItems.length);
    }

    /**
     * Construct a new ArrayList with a subset of the given list of items.
     *
     * @param initialItems The full array of items to choose from.
     * @param index The starting location of the items to choose.
     * @param count The count of items to pick from the full array, starting at the index.
     * @throws IndexOutOfBoundsException if the given index is negative or greater than the count.
     */
    public ArrayList(final T[] initialItems, final int index, final int count) {
        Utils.checkNull(initialItems, "initial items");
        Utils.checkIndexBounds(index, count, 0, initialItems.length);

        items = new Object[count];
        System.arraycopy(initialItems, index, items, 0, count);

        length = count;
    }

    /**
     * Construct a new ArrayList with the given sequence of items.
     *
     * @param initialItems The initial list of values for the list.
     */
    public ArrayList(final Sequence<T> initialItems) {
        this(initialItems, 0, initialItems.getLength());
    }

    /**
     * Construct a new ArrayList with a subset of the given sequence of items.
     *
     * @param initialItems The full sequence of items to choose from.
     * @param index The starting location of the items to choose.
     * @param count The count of items to pick from the full sequence, starting at the index.
     * @throws IndexOutOfBoundsException if the given index is negative or greater than the count.
     */
    public ArrayList(final Sequence<T> initialItems, final int index, final int count) {
        Utils.checkNull(initialItems, "initial items");
        Utils.checkIndexBounds(index, count, 0, initialItems.getLength());

        items = new Object[count];

        for (int i = 0; i < count; i++) {
            items[i] = initialItems.get(index + i);
        }

        length = count;
    }

    /**
     * Copy the given ArrayList into a new one.
     *
     * @param list The existing list to copy into this one.
     */
    public ArrayList(final ArrayList<T> list) {
        this(list, 0, list.length);
    }

    /**
     * Construct a new ArrayList with a subset of the given ArrayList.
     *
     * @param list The full list of items to choose from.
     * @param index The starting location of the items to choose.
     * @param count The count of items to pick from the full list, starting at the index.
     * @throws IndexOutOfBoundsException if the given index is negative or greater than the count.
     */
    public ArrayList(final ArrayList<T> list, final int index, final int count) {
        Utils.checkNull(list, "list");
        Utils.checkIndexBounds(index, count, 0, list.length);

        items = new Object[count];
        length = count;

        System.arraycopy(list.items, index, items, 0, count);

        comparator = list.comparator;
    }

    /**
     * Copy the given collection into a new ArrayList.
     *
     * @param c The existing collection to copy into this list.
     */
    public ArrayList(final java.util.Collection<T> c) {
        Utils.checkNull(c, "c");

        items = c.toArray();
        length = c.size();
    }

    @Override
    public final int add(final T item) {
        int index = -1;

        if (comparator == null) {
            index = length;
            insert(item, index);
        } else {
            // Perform a binary search to find the insertion point
            index = binarySearch(this, item, comparator);
            if (index < 0) {
                index = -(index + 1);
            }

            insert(item, index, false);
        }

        return index;
    }

    @Override
    public final void insert(final T item, final int index) {
        insert(item, index, true);
    }

    /**
     * Private method to insert an item into the list, with an option to validate
     * its position with any comparator.
     *
     * @param item The item to insert.
     * @param index The position at which to insert the item.
     * @param validate Whether or not to ensure the item is being inserted into
     * the correct sorted position if the list has a comparator.
     * @throws IllegalArgumentException if the "validate" parameter is true, and
     * there is a comparator set, and the given insertion point is incorrect for
     * the value of the item.
     */
    private void insert(final T item, final int index, final boolean validate) {
        Utils.checkIndexBounds(index, 0, length);

        if (comparator != null && validate) {
            int i = binarySearch(this, item, comparator);
            if (i < 0) {
                i = -(i + 1);
            }

            if (index != i) {
                throw new IllegalArgumentException(
                    "Given insertion point " + index + " does not match the sorted insertion location " + i + ".");
            }
        }

        // Insert item
        ensureCapacity(length + 1);
        System.arraycopy(items, index, items, index + 1, length - index);
        items[index] = item;

        length++;
        modificationCount++;

        if (listListeners != null) {
            listListeners.itemInserted(this, index);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T update(final int index, final T item) {
        Utils.checkIndexBounds(index, 0, length - 1);

        T previousItem = (T) items[index];

        if (previousItem != item) {
            if (comparator != null) {
                // Ensure that the new item is greater or equal to its
                // predecessor and less than or equal to its successor
                T predecessorItem = (index > 0 ? (T) items[index - 1] : null);
                T successorItem = (index < length - 1 ? (T) items[index + 1] : null);

                if ((predecessorItem != null && comparator.compare(item, predecessorItem) < 0)
                    || (successorItem != null && comparator.compare(item, successorItem) > 0)) {
                    throw new IllegalArgumentException(
                        "Updated item at index " + index + " is not in correct sorted order.");
                }
            }

            items[index] = item;

            modificationCount++;
        }

        if (listListeners != null) {
            listListeners.itemUpdated(this, index, previousItem);
        }

        return previousItem;
    }

    @Override
    public final int remove(final T item) {
        int index = indexOf(item);

        if (index >= 0) {
            remove(index, 1);
        }

        return index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Sequence<T> remove(final int index, final int count) {
        Utils.checkIndexBounds(index, count, 0, length);

        ArrayList<T> removed = new ArrayList<>((T[]) items, index, count);

        // Remove items
        if (count > 0) {
            int end = index + count;
            System.arraycopy(items, index + count, items, index, length - end);

            length -= count;
            modificationCount++;

            // Clear any orphaned references
            for (int i = length, n = length + count; i < n; i++) {
                items[i] = null;
            }

            if (listListeners != null) {
                listListeners.itemsRemoved(this, index, removed);
            }
        }

        return removed;
    }

    @Override
    public final void clear() {
        if (length > 0) {
            items = new Object[items.length];
            length = 0;
            modificationCount++;

            if (listListeners != null) {
                listListeners.listCleared(this);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T get(final int index) {
        Utils.checkIndexBounds(index, 0, length - 1);

        return (T) items[index];
    }

    @Override
    public final int indexOf(final T item) {
        int i;

        if (comparator == null) {
            for (i = 0; i < length; i++) {
                if ((item == null && items[i] == null) || item.equals(items[i])) {
                    return i;
                }
            }
            return -1;
        } else {
            // For a sorted list perform a binary search to find the index
            i = binarySearch(this, item, comparator);
            return (i < 0) ? -1 : i;
        }
    }

    @Override
    public final boolean isEmpty() {
        return (length == 0);
    }

    @Override
    public final int getLength() {
        return length;
    }

    /**
     * Trim the internal storage for this list to exactly fit the current
     * number of items in it.
     */
    public final void trimToSize() {
        Object[] newItems = new Object[length];
        System.arraycopy(items, 0, newItems, 0, length);

        items = newItems;
        length = newItems.length;
    }

    /**
     * Ensure there is sufficient capacity in the internal storage for the given
     * number of items.  This can be used before a large number of inserts to
     * avoid many incremental storage updates during the inserts.
     * <p> If there is already sufficient storage for the given capacity nothing
     * happens, regardless of how many items are currently in the list.  This means
     * that to ensure capacity for the current length plus "n" new items, this
     * method should be called with the {@link getLength} plus the number of items
     * to insert.
     *
     * @param capacity The new capacity to allow for.
     */
    public final void ensureCapacity(final int capacity) {
        if (capacity > items.length) {
            int capacityMax = Math.max((int) (items.length * LOAD_FACTOR), capacity);
            Object[] newItems = new Object[capacityMax];
            System.arraycopy(items, 0, newItems, 0, length);

            items = newItems;
        }
    }

    /**
     * @return The current capacity of the list, that is, how many items can be
     * stored before allocating more memory.
     */
    public final int getCapacity() {
        return items.length;
    }

    /**
     * @return The current contents of the list as an array of objects.
     */
    public final Object[] toArray() {
        return Arrays.copyOf(items, length);
    }

    /**
     * @return The current contents of the list as an array of the given type.
     * @param type The type of the array elements to be returned (which should
     * match the declared type of this ArrayList).
     */
    public final T[] toArray(final Class<? extends T[]> type) {
        return Arrays.copyOf(items, length, type);
    }

    @Override
    public final Comparator<T> getComparator() {
        return comparator;
    }

    @Override
    public final void setComparator(final Comparator<T> newComparator) {
        Comparator<T> previousComparator = comparator;

        if (newComparator != null) {
            sort(this, newComparator);
        }

        comparator = newComparator;

        if (listListeners != null) {
            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    @Override
    public final ItemIterator<T> iterator() {
        return new ArrayListItemIterator();
    }

    @Override
    public final ListenerList<ListListener<T>> getListListeners() {
        if (listListeners == null) {
            listListeners = new ListListenerList<>();
        }

        return listListeners;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object o) {
        boolean equals = false;

        if (this == o) {
            equals = true;
        } else if (o instanceof List) {
            List<T> list = (List<T>) o;

            if (length == list.getLength()) {
                Iterator<T> iterator = list.iterator();
                equals = true;

                for (T element : this) {
                    if (!(iterator.hasNext() && element.equals(iterator.next()))) {
                        equals = false;
                        break;
                    }
                }
            }
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        for (Object item : items) {
            hashCode *= HASH_MULTIPLIER;
            if (item != null) {
                hashCode += item.hashCode();
            }
        }

        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        return StringUtils.append(sb, this).toString();
    }

    /**
     * Sort the current contents of the given list using the given comparator.
     *
     * @param <T> Type of the list elements.
     * @param list The list to sort.
     * @param comparator The comparator to use to establish the sort order.
     */
    public static final <T> void sort(final ArrayList<T> list, final Comparator<T> comparator) {
        sort(list, 0, list.getLength(), comparator);
    }

    /**
     * Sort a portion of the given list.
     *
     * @param <T> Type of the list elements.
     * @param list The list to sort.
     * @param from The beginning index in the list of the items to sort (inclusive).
     * @param to The ending index of the items to sort (exclusive), that is, the elements
     * from "from" to "to - 1" are sorted on return.
     * @param comparator The comparator to use to establish the sorted order.
     */
    @SuppressWarnings("unchecked")
    public static final <T> void sort(final ArrayList<T> list, final int from, final int to,
        final Comparator<T> comparator) {
        Utils.checkNull(list, "list");
        Utils.checkNull(comparator, "comparator");

        Arrays.sort((T[]) list.items, from, to, comparator);

        list.modificationCount++;
    }

    /**
     * Sort the given array list according to the "natural" sort order of the comparable elements.
     * <p> The elements must implement the {@link Comparable} interface, as the default sort calls
     * the {@link Comparable#compareTo} method to order the elements.
     *
     * @param <T> The comparable type of the elements in the list.
     * @param list The list to sort.
     */
    public static final <T extends Comparable<? super T>> void sort(final ArrayList<T> list) {
        sort(list, (o1, o2) -> o1.compareTo(o2));
    }

    /**
     * Search for a given element in the list using the "binary search" algorithm, which requires a comparator
     * to establish the sort order of the elements.
     *
     * @param <T> Type of the list elements.
     * @param list The list to search.
     * @param item The item to search for in the list.
     * @param comparator Comparator to use for testing; if {@code null} then the "natural" ordering of the objects
     * is used (see the caveats of {@link Arrays#binarySearch(Object[], Object)}).
     * @return The index of the item in the list if found, or -1 if the item cannot be found in the list.
     */
    @SuppressWarnings("unchecked")
    public static final <T> int binarySearch(final ArrayList<T> list, final T item, final Comparator<T> comparator) {
        Utils.checkNull(list, "list");
        Utils.checkNull(item, "item");

        int index = (comparator == null)
              ? Arrays.binarySearch((T[]) list.items, 0, list.length, item)
              : Arrays.binarySearch((T[]) list.items, 0, list.length, item, comparator);

        return index;
    }

    /**
     * Search for an item in the given list using the elements' "natural" ordering.
     *
     * @param <T> The comparable type of the elements in the list.
     * @param list The list to search.
     * @param item The item to search for.
     * @return The index of the item in the list if found, or -1 if the item is not found.
     */
    public static final <T extends Comparable<? super T>> int binarySearch(final ArrayList<T> list, final T item) {
        return binarySearch(list, item, null);
    }

}
