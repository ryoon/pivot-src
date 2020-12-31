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
 * Implementation of the {@link Stack} interface that is backed by an {@link ArrayList}.
 * <p> Note: a stack with a comparator set is more like a priority queue than a stack
 * because it no longer maintains the LIFO (last-in, first-out) ordering of a normal stack.
 *
 * @param <T> The type of elements stored in this stack.
 */
public class ArrayStack<T> implements Stack<T>, Serializable {
    private static final long serialVersionUID = 3175064065273930731L;

    /** The backing array for this stack. */
    private ArrayList<T> arrayList = new ArrayList<>();
    /** The maximum permitted stack depth (0 = unlimited). */
    private int maxDepth = 0;
    /** The list of listeners for changes in the stack. */
    private transient StackListener.Listeners<T> stackListeners = new StackListener.Listeners<>();

    /**
     * Construct an empty stack, with all defaults.
     */
    public ArrayStack() {
        this(null);
    }

    /**
     * Construct an empty stack with the given comparator used to order the elemnts in it.
     *
     * @param comparator A comparator used to sort the elements in the stack as they are pushed.
     */
    public ArrayStack(final Comparator<T> comparator) {
        setComparator(comparator);
    }

    /**
     * Construct an empty stack with the given initial capacity.
     *
     * @param capacity The initial capacity for the stack.
     */
    public ArrayStack(final int capacity) {
        ensureCapacity(capacity);
    }

    /**
     * Construct an empty stack with the given initial capacity and maximum depth.
     *
     * @param capacity The initial capacity for the stack.
     * @param depth    The maximum depth to enforce for this stack (0 = unlimited).
     */
    public ArrayStack(final int capacity, final int depth) {
        ensureCapacity(capacity);
        setMaxDepth(depth);
    }

    /**
     * Construct an empty stack with the given initial capacity, maximum stack
     * depth, and comparator used to order the stack elements.
     *
     * @param capacity   The initial stack capacity.
     * @param depth      The maximum stack depth to enforce (0 = unlimited).
     * @param comparator The comparator to use to order the stack elements.
     */
    public ArrayStack(final int capacity, final int depth, final Comparator<T> comparator) {
        ensureCapacity(capacity);
        setMaxDepth(depth);
        setComparator(comparator);
    }

    /**
     * @return The maximum depth this stack is permitted to reach,
     * where 0 means unlimited.
     */
    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Set the maximum depth permitted for this stack, 0 means unlimited.
     *
     * @param depth The new maximum depth for this stack.
     */
    @Override
    public void setMaxDepth(final int depth) {
        Utils.checkNonNegative(depth, "maxDepth");
        this.maxDepth = depth;
    }

    @Override
    public void push(final T item) {
        arrayList.add(item);
        stackListeners.itemPushed(this, item);

        // Now check for too many items on this stack
        if (maxDepth > 0 && arrayList.getLength() > maxDepth) {
            arrayList.remove(0, 1);
        }
    }

    @Override
    public T pop() {
        int length = arrayList.getLength();
        if (length == 0) {
            throw new IllegalStateException((getComparator() == null ? "stack" : "queue") + " is empty");
        }

        T item = arrayList.remove(length - 1, 1).get(0);
        stackListeners.itemPopped(this, item);

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
            stackListeners.stackCleared(this);
        }
    }

    @Override
    public boolean isEmpty() {
        return (arrayList.getLength() == 0);
    }

    @Override
    public int getDepth() {
        return arrayList.getLength();
    }

    /**
     * Ensure that the stack has sufficient internal capacity to satisfy
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
     * Set/remove the comparator for this stack.
     * <p> Setting a non-null comparator changes this stack to a priority
     * queue, because LIFO ordering is no longer maintained.
     * <p> If a new comparator is set the stack will be reordered
     * if it contains any elements. Removing the comparator will not
     * reorder any elements.
     * <p> Calls the {@link StackListener#comparatorChanged} method for
     * each registered listener after setting the new comparator.
     *
     * @param comparator The new comparator used to order the elements
     * in the stack. Can be {@code null} to remove the existing comparator.
     */
    @Override
    public void setComparator(final Comparator<T> comparator) {
        Comparator<T> previousComparator = getComparator();
        arrayList.setComparator(comparator);

        stackListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<>(arrayList.iterator());
    }

    @Override
    public ListenerList<StackListener<T>> getStackListeners() {
        return stackListeners;
    }
}
