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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.Utils;

/**
 * Interface representing an ordered sequence of items.
 *
 * @param <T> The type of elements stored in this sequence.
 */
public interface Sequence<T> {
    /**
     * Collection of static utility methods providing path access to nested
     * sequence data.
     *
     * @param <T> note that in Tree the type parameter currently is not used
     */
    public static class Tree<T> {
        /**
         * An object representing a path to a nested node in nested sequence
         * data.
         */
        public static class Path implements Sequence<Integer>, Iterable<Integer> {
            private ArrayList<Integer> elements;

            public Path() {
                elements = new ArrayList<>();
            }

            public Path(final Integer... elements) {
                this.elements = new ArrayList<>(elements);
            }

            public Path(final Path path) {
                elements = new ArrayList<>(path.elements);
            }

            public Path(final Path path, final int depth) {
                elements = new ArrayList<>(path.elements, 0, depth);
            }

            private Path(final ArrayList<Integer> elements) {
                this.elements = elements;
            }

            @Override
            public int add(final Integer element) {
                return elements.add(element);
            }

            @Override
            public void insert(final Integer element, final int index) {
                elements.insert(element, index);
            }

            @Override
            public Integer update(final int index, final Integer element) {
                return elements.update(index, element);
            }

            @Override
            @UnsupportedOperation
            public int remove(final Integer element) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Sequence<Integer> remove(final int index, final int count) {
                return elements.remove(index, count);
            }

            @Override
            public Integer get(final int index) {
                return elements.get(index);
            }

            @Override
            public int indexOf(final Integer element) {
                return elements.indexOf(element);
            }

            @Override
            public int getLength() {
                return elements.getLength();
            }

            @Override
            public Iterator<Integer> iterator() {
                return new ImmutableIterator<>(elements.iterator());
            }

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();

                sb.append("[");

                int i = 0;
                for (Integer element : elements) {
                    if (i > 0) {
                        sb.append(", ");
                    }

                    sb.append(element);
                    i++;
                }

                sb.append("]");

                return sb.toString();
            }

            public Integer[] toArray() {
                return elements.toArray(Integer[].class);
            }

            public static Path forDepth(final int depth) {
                return new Path(new ArrayList<Integer>(depth));
            }
        }

        /**
         * Class representing an immutable path.
         */
        public static final class ImmutablePath extends Path {
            public ImmutablePath(final Integer... elements) {
                super(elements);
            }

            public ImmutablePath(final Path path) {
                super(path);
            }

            @Override
            @UnsupportedOperation
            public int add(final Integer element) {
                throw new UnsupportedOperationException();
            }

            @Override
            @UnsupportedOperation
            public void insert(final Integer element, final int index) {
                throw new UnsupportedOperationException();
            }

            @Override
            @UnsupportedOperation
            public Integer update(final int index, final Integer element) {
                throw new UnsupportedOperationException();
            }

            @Override
            @UnsupportedOperation
            public Sequence<Integer> remove(final int index, final int count) {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * Nested sequence item iterator interface.
         *
         * @param <T> Type of object contained in the sequence and the iterator.
         */
        public interface ItemIterator<T> extends Iterator<T> {
            /**
             * Gets the path within the nested sequence to the item most
             * recently returned by a call to {@code next()}.
             *
             * @return The path (from the root sequence) to the current item.
             * @throws IllegalStateException If {@code next()} has not yet been
             * called on this iterator.
             */
            public Path getPath();
        }

        private static class DepthFirstItemIterator<T> implements ItemIterator<T> {
            private ArrayStack<Sequence<T>> stack = new ArrayStack<>();
            private Path previousPath = null;
            private Path nextPath = new Path();

            public DepthFirstItemIterator(final Sequence<T> sequence) {
                stack.push(sequence);
                nextPath.add(0);
                normalize();
            }

            @Override
            public boolean hasNext() {
                return (stack.peek() != null);
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                Sequence<T> sequence = stack.peek();

                if (sequence == null) {
                    throw new NoSuchElementException();
                }

                previousPath = new Path(nextPath);

                int n = nextPath.getLength();
                int index = nextPath.get(n - 1);

                T item = sequence.get(index);

                if (item instanceof Sequence<?>) {
                    stack.push((Sequence<T>) item);
                    nextPath.add(0);
                } else {
                    nextPath.update(n - 1, index + 1);
                }

                normalize();

                return item;
            }

            /**
             * Normalizes {@code stack} and {@code nextPath} such that the
             * iterator is pointing to a valid item or the end of the nested
             * sequence.
             */
            private void normalize() {
                Sequence<T> sequence = stack.peek();

                int n = nextPath.getLength();
                int index = nextPath.get(n - 1);

                while (sequence != null && index >= sequence.getLength()) {
                    stack.pop();
                    sequence = stack.peek();

                    nextPath.remove(--n, 1);

                    if (n > 0) {
                        index = nextPath.get(n - 1);
                        nextPath.update(n - 1, ++index);
                    }
                }
            }

            @Override
            @UnsupportedOperation
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Path getPath() {
                if (previousPath == null) {
                    throw new IllegalStateException("'getPath' called before 'next' during iteration");
                }

                return previousPath;
            }
        }

        /**
         * Adds an item to a nested sequence.
         *
         * @param <T> Type of the items in this sequence.
         * @param sequence The root sequence.
         * @param item The item to be added to the sequence.
         * @param path The path of the sequence to which the item should be
         * added.
         * @return The index at which the item was inserted, relative to the
         * parent sequence.
         */
        @SuppressWarnings("unchecked")
        public static <T> int add(final Sequence<T> sequence, final T item, final Path path) {
            return ((Sequence<T>) get(sequence, path)).add(item);
        }

        /**
         * Inserts an item into a nested sequence.
         *
         * @param <T> Type of items in this sequence.
         * @param sequence The root sequence.
         * @param item The item to be inserted into the sequence.
         * @param path The path of the sequence into which the item should be
         * inserted.
         * @param index The index at which the item should be inserted within the
         * parent sequence.
         */
        @SuppressWarnings("unchecked")
        public static <T> void insert(final Sequence<T> sequence, final T item, final Path path, final int index) {
            ((Sequence<T>) get(sequence, path)).insert(item, index);
        }

        /**
         * Updates an item in a nested sequence.
         *
         * @param <T> The type of items in this sequence.
         * @param sequence The root sequence.
         * @param path The path of the item to update.
         * @param item The item that will replace any existing value at the given
         * path.
         * @return The item that was previously stored at the given path.
         */
        @SuppressWarnings("unchecked")
        public static <T> T update(final Sequence<T> sequence, final Path path, final T item) {
            Utils.checkNull(sequence, "sequence");
            Utils.checkNull(path, "path");

            int i = 0, n = path.getLength() - 1;
            Sequence<T> sequenceUpdated = sequence;
            while (i < n) {
                sequenceUpdated = (Sequence<T>) sequenceUpdated.get(path.get(i++));
            }

            return sequenceUpdated.update(path.get(i), item);
        }

        /**
         * Removes the first occurrence of an item from a nested sequence.
         *
         * @param <T> The type of items in this sequence.
         * @param sequence The root sequence.
         * @param item The item to remove.
         * @return The path of the item that was removed.
         */
        public static <T> Path remove(final Sequence<T> sequence, final T item) {
            Path path = pathOf(sequence, item);
            if (path == null) {
                throw new IllegalArgumentException("item is not a descendant of sequence.");
            }

            remove(sequence, path, 1);

            return path;
        }

        /**
         * Removes items from a nested sequence.
         *
         * @param <T> The type of items in this sequence.
         * @param sequence The root sequence.
         * @param path The path of the item(s) to remove.
         * @param count The number of items to remove.
         * @return The sequence of items that were removed.
         */
        @SuppressWarnings("unchecked")
        public static <T> Sequence<T> remove(final Sequence<T> sequence, final Path path, final int count) {
            Utils.checkNull(sequence, "sequence");
            Utils.checkNull(path, "path");

            int i = 0, n = path.getLength() - 1;
            Sequence<T> sequenceUpdated = sequence;
            while (i < n) {
                sequenceUpdated = (Sequence<T>) sequenceUpdated.get(path.get(i++));
            }

            return sequenceUpdated.remove(path.get(i), count);
        }

        /**
         * Retrieves an item from a nested sequence.
         *
         * @param <T> The type of items in this sequence.
         * @param sequence The root sequence.
         * @param path The path of the item to retrieve.
         * @return The item at the given path, or {@code null} if the path is
         * empty.
         */
        @SuppressWarnings("unchecked")
        public static <T> T get(final Sequence<T> sequence, final Path path) {
            Utils.checkNull(sequence, "sequence");
            Utils.checkNull(path, "path");

            T item;
            if (path.getLength() == 0) {
                item = null;
            } else {
                int i = 0, n = path.getLength() - 1;
                Sequence<T> sequenceUpdated = sequence;
                while (i < n) {
                    sequenceUpdated = (Sequence<T>) sequenceUpdated.get(path.get(i++));
                }

                item = sequenceUpdated.get(path.get(i));
            }

            return item;
        }

        /**
         * Returns the path to an item in a nested sequence.
         *
         * @param <T> The type of items in this sequence.
         * @param sequence The root sequence.
         * @param item The item to locate.
         * @return The path of first occurrence of the item if it exists in the
         * sequence; {@code null} otherwise.
         */
        @SuppressWarnings("unchecked")
        public static <T> Path pathOf(final Sequence<T> sequence, final T item) {
            Utils.checkNull(sequence, "sequence");
            Utils.checkNull(item, "item");

            Path path = null;

            for (int i = 0, n = sequence.getLength(); i < n && path == null; i++) {
                T t = sequence.get(i);

                if (t.equals(item)) {
                    path = new Path();
                    path.add(i);
                } else {
                    if (t instanceof Sequence<?>) {
                        path = pathOf((Sequence<T>) t, item);

                        if (path != null) {
                            path.insert(i, 0);
                        }
                    }
                }
            }

            return path;
        }

        /**
         * Returns an iterator that will perform a depth-first traversal of the
         * nested sequence.
         * @param <T> The type of items in this sequence.
         * @param sequence The sequence for which we are requesting an iterator.
         * @return The new iterator over the sequence (depth-first order).
         */
        public static <T> ItemIterator<T> depthFirstIterator(final Sequence<T> sequence) {
            return new DepthFirstItemIterator<>(sequence);
        }

        /**
         * Determines whether the path represented by the second argument is a
         * descendant of the path represented by the first argument.
         *
         * @param ancestorPath The ancestor path to test.
         * @param descendantPath The descendant path to test.
         * @return {@code true} if the second argument is a descendant of the first
         * path argument, {@code false} otherwise.
         */
        public static boolean isDescendant(final Path ancestorPath, final Path descendantPath) {
            int ancestorLength = ancestorPath.getLength();
            int descendantLength = descendantPath.getLength();

            boolean result = (ancestorLength <= descendantLength);

            if (result) {
                for (int i = 0, n = ancestorLength; i < n; i++) {
                    int index1 = ancestorPath.get(i);
                    int index2 = descendantPath.get(i);

                    if (index1 != index2) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }
    }

    /**
     * Adds an item to the sequence.
     *
     * @param item The item to be added to the sequence.
     * @return The index at which the item was added, or <code>-1</code> if the item
     * was not added to the sequence.
     */
    int add(T item);

    /**
     * Inserts an item into the sequence at a specific index.
     *
     * @param item The item to be added to the sequence.
     * @param index The index at which the item should be inserted. Must be a
     * value between <code>0</code> and {@code getLength()}.
     */
    void insert(T item, int index);

    /**
     * Updates the item at the given index.
     *
     * @param index The index of the item to update.
     * @param item The item that will replace any existing value at the given
     * index.
     * @return The item that was previously stored at the given index.
     */
    T update(int index, T item);

    /**
     * Removes the first occurrence of the given item from the sequence.
     *
     * @param item The item to remove.
     * @return The index of the item that was removed, or <code>-1</code> if the item
     * could not be found.
     * @see #remove(int, int)
     */
    int remove(T item);

    /**
     * Removes one or more items from the sequence.
     *
     * @param index The starting index to remove.
     * @param count The number of items to remove, beginning with {@code index}.
     * @return A sequence containing the items that were removed.
     */
    Sequence<T> remove(int index, int count);

    /**
     * Retrieves the item at the given index.
     *
     * @param index The index of the item to retrieve.
     * @return The item at this index in the sequence.
     */
    T get(int index);

    /**
     * Returns the index of an item in the sequence.
     *
     * @param item The item to locate.
     * @return The index of first occurrence of the item if it exists in the
     * sequence; <code>-1</code>, otherwise.
     */
    int indexOf(T item);

    /**
     * Returns the length of the sequence.
     *
     * @return The number of items in the sequence.
     */
    int getLength();

    /**
     * Add all the elements of the given collection to this sequence
     * using the {@link #add} method of this interface.
     *
     * @param c The other collection to add to this sequence.
     */
    default void addAll(Collection<T> c) {
        c.forEach(item -> add(item));
    }

    /**
     * Add all the elements of the given array to this sequence
     * using the {@link #add} method of this interface.
     *
     * @param array The array to add to this sequence.
     */
    default void addAll(T[] array) {
        for (T item : array) {
            add(item);
        }
    }

}
