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
import java.util.Iterator;

import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Queue} interface that is backed by an {@link ArrayList}.
 *
 * @param <T> The type of object stored in this queue.
 */
public class ArrayQueue<T> implements Queue<T>, Serializable {
    private static final long serialVersionUID = -3856732506886968324L;

    /** The underlying array list used to implement this queue. */
    private ArrayList<T> arrayList = new ArrayList<>();
    /** The maximum permitted length of the queue (0 = unlimited). */
    private int maxLength = 0;
    /** List of queue listeners for this queue. */
    private transient QueueListener.Listeners<T> queueListeners = new QueueListener.Listeners<>();

    /**
     * Construct an empty queue, with all defaults.
     */
    public ArrayQueue() {
        this(null);
    }

    /**
     * Construct an empty queue with the given comparator used to order the elements in it.
     *
     * @param comparator A comparator used to sort the elements in the queue as they are added.
     */
    public ArrayQueue(final Comparator<T> comparator) {
        setComparator(comparator);
    }

    /**
     * Construct an empty queue with a given initial capacity.
     *
     * @param capacity The initial capacity for the queue.
     */
    public ArrayQueue(final int capacity) {
        ensureCapacity(capacity);
    }

    /**
     * Construct an empty queue with a given initial capacity and maxmimum length.
     *
     * @param capacity The initial capacity for the queue.
     * @param maxLen   The maximum permitted queue length.
     */
    public ArrayQueue(final int capacity, final int maxLen) {
        ensureCapacity(capacity);
        setMaxLength(maxLen);
    }

    /**
     * Construct an empty queue with a given initial capacity, maximum length,
     * and comparator for ordering the queue.
     *
     * @param capacity   The initial capacity for the queue.
     * @param maxLen     The maximum permitted queue length.
     * @param comparator The comparator to use for ordering the elements.
     */
    public ArrayQueue(final int capacity, final int maxLen, final Comparator<T> comparator) {
        ensureCapacity(capacity);
        setMaxLength(maxLen);
        setComparator(comparator);
    }

    @Override
    public void enqueue(final T item) {
        if (maxLength == 0 || arrayList.getLength() < maxLength) {
            if (getComparator() == null) {
                arrayList.insert(item, 0);
            } else {
                arrayList.add(item);
            }

            queueListeners.itemEnqueued(this, item);
        }
    }

    @Override
    public T dequeue() {
        int length = arrayList.getLength();
        if (length == 0) {
            throw new IllegalStateException("queue is empty");
        }

        T item = arrayList.remove(length - 1, 1).get(0);
        queueListeners.itemDequeued(this, item);

        return item;
    }

    @Override
    public T peek() {
        T item = null;
        int length = arrayList.getLength();
        if (length > 0) {
            item = arrayList.get(length - 1);
        }

        return item;
    }

    @Override
    public void clear() {
        if (arrayList.getLength() > 0) {
            arrayList.clear();
            queueListeners.queueCleared(this);
        }
    }

    @Override
    public boolean isEmpty() {
        return (arrayList.getLength() == 0);
    }

    @Override
    public int getLength() {
        return arrayList.getLength();
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public void setMaxLength(final int maxLen) {
        Utils.checkNonNegative(maxLen, "maxLen");
        this.maxLength = maxLen;
    }

    /**
     * Ensure that the queue has sufficient internal capacity to satisfy
     * the given number of elements.
     *
     * @param capacity The capacity to ensure (to make sure no further
     * allocations are done to accommodate this number of elements).
     */
    public void ensureCapacity(final int capacity) {
        arrayList.ensureCapacity(capacity);
    }

    @Override
    public Comparator<T> getComparator() {
        return arrayList.getComparator();
    }

    /**
     * Set/remove the comparator for this queue.
     * <p> If a new comparator is set the queue will be reordered
     * if it contains any elements. Removing the comparator will not
     * reorder any elements.
     * <p> Calls the {@link QueueListener#comparatorChanged} method for
     * each registered listener after setting the new comparator.
     *
     * @param comparator The new comparator used to order the elements
     * in the queue. Can be {@code null} to remove the existing comparator.
     */
    @Override
    public void setComparator(final Comparator<T> comparator) {
        Comparator<T> previousComparator = getComparator();
        arrayList.setComparator(comparator);

        queueListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<>(arrayList.iterator());
    }

    @Override
    public ListenerList<QueueListener<T>> getQueueListeners() {
        return queueListeners;
    }
}
