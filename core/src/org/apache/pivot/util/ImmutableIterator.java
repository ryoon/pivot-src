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

import java.util.Iterator;

/**
 * Immutable implementation of the {@link Iterator} interface.
 * <p>The only methods that have actual implementations (that just
 * defer to the underlying {@code Iterator}) are:  {@link #hasNext}
 * and {@link #next}.
 * <p>Note: the {@code remove()} method implementation here has
 * been taken out because there is now a default implementation
 * in the {@link Iterator} interface itself in Java 8.
 * @param <T> The type of elements we are iterating over.
 */
public final class ImmutableIterator<T> implements Iterator<T> {
    /**
     * The regular iterator we are protecting from change.
     */
    private Iterator<T> iterator;

    /**
     * Construct an immutable iterator over the base iterator given here.
     *
     * @param iter The base iterator we want to protect from change.
     * @throws IllegalArgumentException if the iterator is {@code null}.
     */
    public ImmutableIterator(final Iterator<T> iter) {
        Utils.checkNull(iter, "iter");

        this.iterator = iter;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public T next() {
        return this.iterator.next();
    }

}
