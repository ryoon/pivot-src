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
package org.apache.pivot.collections.concurrent;

import java.util.Comparator;

import org.apache.pivot.collections.Set;
import org.apache.pivot.collections.SetListener;
import org.apache.pivot.util.ListenerList;

/**
 * Synchronized implementation of the {@link Set} interface.
 *
 * @author gbrown
 */
public class SynchronizedSet<E> extends SynchronizedCollection<E>
    implements Set<E> {
    private static class SynchronizedSetListenerList<E>
        extends SetListenerList<E> {
        @Override
        public synchronized void add(SetListener<E> listener) {
            super.add(listener);
        }

        @Override
        public synchronized void remove(SetListener<E> listener) {
            super.remove(listener);
        }

        @Override
        public synchronized void elementAdded(Set<E> set, E element) {
            super.elementAdded(set, element);
        }

        @Override
        public synchronized void elementRemoved(Set<E> set, E element) {
            super.elementRemoved(set, element);
        }

        @Override
        public synchronized void setCleared(Set<E> set) {
            super.setCleared(set);
        }

        @Override
        public synchronized void comparatorChanged(Set<E> set, Comparator<E> previousComparator) {
            super.comparatorChanged(set, previousComparator);
        }
    }

    private SynchronizedSetListenerList<E> setListeners = new SynchronizedSetListenerList<E>();

    public SynchronizedSet(Set<E> set) {
        super(set);
    }

    public synchronized boolean add(E element) {
        boolean added = false;

        if (!contains(element)) {
            ((Set<E>)collection).add(element);
            added = true;

            setListeners.elementAdded(this, element);
        }

        return added;
    }

    public synchronized boolean remove(E element) {
        boolean removed = false;

        if (contains(element)) {
            ((Set<E>)collection).remove(element);
            removed = true;

            setListeners.elementRemoved(this, element);
        }

        return removed;
    }

    public synchronized boolean contains(E element) {
        return ((Set<E>)collection).contains(element);
    }

    public synchronized boolean isEmpty() {
        return ((Set<E>)collection).isEmpty();
    }

    public synchronized int count() {
        return ((Set<E>)collection).count();
    }

    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }
}
