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

/**
 * Definition of a generic Monad, which is a type that wraps another type and
 * gives some form of quality to the underlying type (see
 * <a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)">
 * https://en.wikipedia.org/wiki/Monad_(functional_programming)</a>).
 *
 * @param <T> The underlying type wrapped by this Monad.
 */
public abstract class Monad<T> implements MonadicOperations<T> {

    /**
     * Default constructor.
     */
    protected Monad() {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Monad(" + getClass().getTypeParameters()[0].getName() + ")";
    }

}
