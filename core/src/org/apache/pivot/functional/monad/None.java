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
package org.apache.pivot.functional.monad;

import java.util.NoSuchElementException;

/**
 * Container for an immutable no-value, derived from Option.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class None<T> extends Option<T> {
    private static final None INSTANCE = new None<>();

    /**
     * Get the static instance.
     * @param <T> The type of this no-value object.
     * @return the static instance
     */
    public static final <T> None<T> getInstance() {
        return INSTANCE;
    }

    /**
     * Default constructor, does nothing.
     */
    public None() {
        // super(null);
        // no-op
    }

    @Override
    public boolean hasValue() {
        return false;
    }

    @Override
    public T getValue() {
        throw new NoSuchElementException("None does not contain a value");
    }

    @Override
    public String toString() {
        return "None()";
    }

    @Override
    public boolean equals(Object other) {
        return (other == null || other.getClass() != None.class) ? false : true;
    }

    @Override
    public int hashCode() {
        return -31;
    }

}
