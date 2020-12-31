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

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.NoSuchFileException;


/**
 * Utility class that has various methods to help with exception handling
 * and display.
 */
public final class ExceptionUtils {
    /** Private constructor since this is a utility class. */
    private ExceptionUtils() {
    }

    /**
     * Standalone function to produce a reasonable string of the given
     * exception, including the message and cause (all the way up to
     * the original exception thrown).
     *
     * @param ex  Any old exception we want to report on.
     * @return    A string representation of the error.
     */
    public static String toString(final Throwable ex) {
        StringBuilder buf = new StringBuilder();
        toString(ex, buf);
        return buf.toString();
    }

    /**
     * Standalone version to do a "pure" string (using spaces instead of newlines).
     *
     * @param ex        The exception to report.
     * @param useSpaces Whether to use spaces instead of newlines to separate
     *                  the chain of causal exceptions.
     * @return          String representation of the exception.
     */
    public static String toString(final Throwable ex, final boolean useSpaces) {
        StringBuilder buf = new StringBuilder();
        toString(ex, buf, false, useSpaces, false);
        return buf.toString();
    }

    /**
     * Incremental version which allows for prepended or appended
     * content by being passed the buffer in which to work.
     *
     * @param    ex    The exception to report.
     * @param    buf   The buffer used to build the content.
     */
    public static void toString(final Throwable ex, final StringBuilder buf) {
        toString(ex, buf, false, false, false);
    }

    /**
     * Incremental version with more options.
     *
     * @param    ex          The exception to report.
     * @param    buf         The buffer to build the string representation in.
     * @param    useToString {@code true} to format using the {@link Throwable#toString}
     *                       method instead of the {@link Throwable#getMessage} for the text.
     * @param    useSpaces   {@code true} to use spaces instead of newlines to separate
     *                       the chained exceptions.
     * @param    convertTabs Convert any tab characters to single spaces (for use in controls
     *                       that don't deal with tabs correctly; some do).
     */
    public static void toString(
        final Throwable ex,
        final StringBuilder buf,
        final boolean useToString,
        final boolean useSpaces,
        final boolean convertTabs) {

        for (Throwable next = ex; next != null;) {
            String msg, className;

            if (useToString) {
                msg = next.toString();
            } else {
                msg       = next.getLocalizedMessage();
                className = next.getClass().getSimpleName();

                if (msg == null) {
                    msg = className;
                } else if ((next instanceof UnknownHostException)
                        || (next instanceof NoClassDefFoundError)
                        || (next instanceof ClassNotFoundException)
                        || (next instanceof NullPointerException)
                        || (next instanceof CharacterCodingException)
                        || (next instanceof FileNotFoundException)
                        || (next instanceof NoSuchFileException)
                        || (next instanceof UnsupportedOperationException)
                        || (next instanceof NumberFormatException)) {
                    msg = String.format("%1$s: %2$s", className, msg);
                }
            }
            buf.append(msg);

            next = next.getCause();

            if (next != null) {
                buf.append(useSpaces ? ' ' : '\n');
            }
        }

        if (convertTabs) {
            int ix = 0;
            while ((ix = buf.indexOf("\t", ix)) >= 0) {
                buf.setCharAt(ix++, ' ');
            }
        }
    }

}

