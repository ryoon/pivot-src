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

import org.apache.pivot.util.ListenerList;

/**
 * Interface representing a last-in, first-out (LIFO) stack when unsorted, and a
 * priority stack when sorted.
 *
 * @param <T> The type of element kept in the stack.
 */
public interface Stack<T> extends Collection<T> {
    /**
     * "Pushes" an item onto the stack. If the stack is unsorted, the item is
     * added at the top of the stack ({@code getLength()}). Otherwise, it is
     * inserted at the appropriate index.
     * <p> If there is a maximum stack depth defined and the stack goes past
     * this maximum depth, the deepest item (which could be this new item,
     * depending on the comparator) will be removed.
     *
     * @param item The item to push onto the stack.
     */
    void push(T item);

    /**
     * Removes the top item from the stack and returns it.
     *
     * @return The top item from the stack (removed from it).
     * @throws IllegalStateException If the stack contains no items.
     */
    T pop();

    /**
     * Returns the item on top of the stack without removing it from the stack.
     * Returns null if the stack contains no items. Will also return null if the
     * top item in the stack is null. {@code getLength()} can be used to
     * distinguish between these two cases.
     * @return The top item from the stack (which remains there).
     */
    T peek();

    /**
     * Tests the emptiness of the stack.
     *
     * @return {@code true} if the stack contains no items; {@code false},
     * otherwise.
     */
    @Override
    boolean isEmpty();

    /**
     * @return The stack depth.
     */
    int getDepth();

    /**
     * @return The maximum permitted stack/queue length (0 = unlimited).
     */
    int getMaxDepth();

    /**
     * Set the maximum permitted stack/queue depth (0 = unlimited).
     *
     * @param maxDepth The new maximum depth.
     */
    void setMaxDepth(int maxDepth);

    /**
     * @return The stack listener list.
     */
    ListenerList<StackListener<T>> getStackListeners();
}
