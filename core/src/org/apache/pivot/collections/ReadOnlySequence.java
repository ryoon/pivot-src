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

import org.apache.pivot.annotations.UnsupportedOperation;

/**
 * A read-only implementation of the {@link Sequence} interface that can be used
 * to easily implement other read-only sequences, lists, adapters, etc.
 * <p> "Read-only" because all operations (such as {@link #add}, {@link #insert},
 * and etc. will throw {@link UnsupportedOperationException} and are marked with
 * {@link UnsupportedOperation} annotation.
 *
 * @param <T> The base type of objects in this sequence.
 */
public abstract class ReadOnlySequence<T> implements Sequence<T>, Serializable {
    private static final long serialVersionUID = -2547032333033014540L;

    /** The simple name of our (derived) class, for message purposes. */
    private final String simpleClassName = this.getClass().getSimpleName();

    /** Format of the default exception message. */
    private static final String MSG_FORMAT =
        "The \"%1$s\" method is unsupported because %2$s is read-only (immutable).";

    /**
     * @return A new {@link UnsupportedOperationException} with a fancy message
     * detailing the method and class name that was in error.
     */
    public UnsupportedOperationException defaultException() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] modifiedStackTrace = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
        String callingMethodName = modifiedStackTrace[0].getMethodName();
        String message = String.format(MSG_FORMAT, callingMethodName, simpleClassName);
        UnsupportedOperationException exception = new UnsupportedOperationException(message);
        exception.setStackTrace(modifiedStackTrace);
        return exception;
    }

    /**
     * Adding an item to a read-only sequence is unsupported.
     * @throws UnsupportedOperationException always
     */
    @Override
    @UnsupportedOperation
    public final int add(final T item) {
        throw defaultException();
    }

    /**
     * Inserting an item into a read-only sequence is unsupported.
     * @throws UnsupportedOperationException always
     */
    @Override
    @UnsupportedOperation
    public final void insert(final T item, final int index) {
        throw defaultException();
    }

    /**
     * Updating an item in a read-only sequence is unsupported.
     * @throws UnsupportedOperationException always
     */
    @Override
    @UnsupportedOperation
    public final T update(final int index, final T item) {
        throw defaultException();
    }

    /**
     * Removing an item from a read-only sequence is unsupported.
     * @throws UnsupportedOperationException always
     */
    @Override
    @UnsupportedOperation
    public final int remove(final T item) {
        throw defaultException();
    }

    /**
     * Removing an item from a read-only sequence is unsupported.
     * @throws UnsupportedOperationException always
     */
    @Override
    @UnsupportedOperation
    public final Sequence<T> remove(final int index, final int count) {
        throw defaultException();
    }

}
