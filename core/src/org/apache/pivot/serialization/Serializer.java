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
package org.apache.pivot.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines an interface for writing objects to and reading objects from a data
 * stream.
 *
 * @param <T> The type of data being read and written.
 */
public interface Serializer<T> {
    /**
     * Reads an object from an input stream.
     *
     * @param inputStream The data stream from which the object will be read.
     * @return The deserialized object.
     * @throws IOException for any errors accessing or reading the object.
     * @throws SerializationException for any formatting errors encountered
     * while deserializing the object.
     */
    public T readObject(InputStream inputStream) throws IOException, SerializationException;

    /**
     * Writes an object to an output stream.
     *
     * @param object The object to serialize.
     * @param outputStream The data stream to which the object will be written.
     * @throws IOException for any errors accessing or reading the object.
     * @throws SerializationException for any formatting errors encountered
     * while deserializing the object.
     */
    public void writeObject(T object, OutputStream outputStream) throws IOException,
        SerializationException;

    /**
     * Returns the MIME type of the data read and written by this serializer.
     *
     * @param object If provided, allows the serializer to attach parameters to
     * the returned MIME type containing more detailed information about the
     * data. If {@code null}, the base MIME type is returned.
     * @return The MIME type of the current data.
     */
    public String getMIMEType(T object);
}
