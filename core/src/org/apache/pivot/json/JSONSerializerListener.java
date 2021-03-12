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
package org.apache.pivot.json;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * JSON serializer listener interface.
 */
public interface JSONSerializerListener {
    /**
     * JSON Serializer listeners.
     */
    final class Listeners extends ListenerList<JSONSerializerListener>
        implements JSONSerializerListener {
        @Override
        public void beginDictionary(final JSONSerializer jsonSerializer, final Dictionary<String, ?> value) {
            forEach(listener -> listener.beginDictionary(jsonSerializer, value));
        }

        @Override
        public void endDictionary(final JSONSerializer jsonSerializer) {
            forEach(listener -> listener.endDictionary(jsonSerializer));
        }

        @Override
        public void readKey(final JSONSerializer jsonSerializer, final String key) {
            forEach(listener -> listener.readKey(jsonSerializer, key));
        }

        @Override
        public void beginSequence(final JSONSerializer jsonSerializer, final Sequence<?> value) {
            forEach(listener -> listener.beginSequence(jsonSerializer, value));
        }

        @Override
        public void endSequence(final JSONSerializer jsonSerializer) {
            forEach(listener -> listener.endSequence(jsonSerializer));
        }

        @Override
        public void readString(final JSONSerializer jsonSerializer, final String value) {
            forEach(listener -> listener.readString(jsonSerializer, value));
        }

        @Override
        public void readNumber(final JSONSerializer jsonSerializer, final Number value) {
            forEach(listener -> listener.readNumber(jsonSerializer, value));
        }

        @Override
        public void readBoolean(final JSONSerializer jsonSerializer, final Boolean value) {
            forEach(listener -> listener.readBoolean(jsonSerializer, value));
        }

        @Override
        public void readNull(final JSONSerializer jsonSerializer) {
            forEach(listener -> listener.readNull(jsonSerializer));
        }
    }

    /**
     * JSON serializer listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    final class Adapter implements JSONSerializerListener {
        @Override
        public void beginDictionary(final JSONSerializer jsonSerializer, final Dictionary<String, ?> value) {
            // empty block
        }

        @Override
        public void endDictionary(final JSONSerializer jsonSerializer) {
            // empty block
        }

        @Override
        public void readKey(final JSONSerializer jsonSerializer, final String key) {
            // empty block
        }

        @Override
        public void beginSequence(final JSONSerializer jsonSerializer, final Sequence<?> value) {
            // empty block
        }

        @Override
        public void endSequence(final JSONSerializer jsonSerializer) {
            // empty block
        }

        @Override
        public void readString(final JSONSerializer jsonSerializer, final String value) {
            // empty block
        }

        @Override
        public void readNumber(final JSONSerializer jsonSerializer, final Number value) {
            // empty block
        }

        @Override
        public void readBoolean(final JSONSerializer jsonSerializer, final Boolean value) {
            // empty block
        }

        @Override
        public void readNull(final JSONSerializer jsonSerializer) {
            // empty block
        }
    }

    /**
     * Called when the serializer has begun reading a dictionary value.
     *
     * @param jsonSerializer The serializer in question.
     * @param value The dictionary just started.
     */
    default void beginDictionary(JSONSerializer jsonSerializer, Dictionary<String, ?> value) {
    }

    /**
     * Called when the serializer has finished reading a dictionary value.
     *
     * @param jsonSerializer The serializer in operation.
     */
    default void endDictionary(JSONSerializer jsonSerializer) {
    }

    /**
     * Called when the serializer has read a dictionary key.
     *
     * @param jsonSerializer The active serializer.
     * @param key The key just read.
     */
    default void readKey(JSONSerializer jsonSerializer, String key) {
    }

    /**
     * Called when the serializer has begun reading a sequence value.
     *
     * @param jsonSerializer The serializer.
     * @param value The sequence just started.
     */
    default void beginSequence(JSONSerializer jsonSerializer, Sequence<?> value) {
    }

    /**
     * Called when the serializer has finished reading a sequence value.
     *
     * @param jsonSerializer The current serializer.
     */
    default void endSequence(JSONSerializer jsonSerializer) {
    }

    /**
     * Called when the serializer has read a string value.
     *
     * @param jsonSerializer The active serializer.
     * @param value The string value just read.
     */
    default void readString(JSONSerializer jsonSerializer, String value) {
    }

    /**
     * Called when the serializer has read a numeric value.
     *
     * @param jsonSerializer The active serializer.
     * @param value The numeric value just read.
     */
    default void readNumber(JSONSerializer jsonSerializer, Number value) {
    }

    /**
     * Called when the serializer has read a boolean value.
     *
     * @param jsonSerializer The serializer.
     * @param value The boolean value just read.
     */
    default void readBoolean(JSONSerializer jsonSerializer, Boolean value) {
    }

    /**
     * Called when the serializer has read a null value.
     *
     * @param jsonSerializer The currently active serializer.
     */
    default void readNull(JSONSerializer jsonSerializer) {
    }
}
