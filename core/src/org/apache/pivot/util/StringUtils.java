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

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * A set of static methods that perform various string manipulation
 * functions.
 */
public final class StringUtils {
    /** Private constructor since this is a utility class. */
    private StringUtils() {
    }

    /**
     * Make a string the consists of "n" copies of the given character.
     * <p> Note: "n" must be positive and less than 512K (arbitrary).
     *
     * @param ch The character to copy.
     * @param n  The number of times to copy this character.
     * @return   The resulting string.
     */
    public static String fromNChars(final char ch, final int n) {
        if (n == 0) {
            return "";
        }
        if (n < 0 || n > Integer.MAX_VALUE / 4) {
           throw new IllegalArgumentException("Requested string size " + n + " is out of range.");
        }

        // Nothing fancy here, but allocate the space and set length upfront
        // because we know how big the result should be.
        StringBuilder builder = new StringBuilder(n);
        builder.setLength(n);
        if (ch != '\0') {
            for (int i = 0; i < n; i++) {
                builder.setCharAt(i, ch);
            }
        }
        return builder.toString();
    }

    /**
     * Convert a string of characters into a hex value string.
     *
     * @param charSequence The string of characters to represent.
     * @return A string in the form of <code>"[<i>xx</i>,<i>xx</i>,<i>xx</i>...]"</code>
     * where the "<i>xx</i>" are the hex representations of each character.
    */
    public static String toHexString(final CharSequence charSequence) {
        StringBuilder builder = new StringBuilder(charSequence.length() * 3 + 1);
        for (int i = 0; i < charSequence.length(); i++) {
            builder.append((i == 0) ? '[' : ',');
            builder.append(Integer.toHexString((int) charSequence.charAt(i)));
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Convert a string to a {@link Number}, with a possible known type to
     * convert to.
     * <p> If there isn't a known type, this is tricky, so go with Integer first,
     * then try Double, or finally BigDecimal.
     *
     * @param <T> The numeric type to return.
     * @param string The string representation of a number.
     * @param type The desired numeric type of this value, or {@code null} to
     * figure out the appropriate type ourselves.
     * @return Either an {@link Integer}, {@link Double} or {@link BigDecimal}
     * value, depending on the format of the input string (if the input type
     * is {@code null}), or a value of the given type.
     * @throws NumberFormatException if the input string doesn't contain a value
     * parseable by one of these methods.
     */
    public static <T extends Number> Number toNumber(final String string,
        final Class<? extends Number> type) {
        Utils.checkNullOrEmpty(string, "string");

        if (type == null) {
            try {
                return Integer.valueOf(string);
            } catch (NumberFormatException nfe) {
                try {
                    return Double.valueOf(string);
                } catch (NumberFormatException nfe2) {
                    return new BigDecimal(string);
                }
            }
        } else {
            if (type == Byte.class || type == byte.class) {
                return Byte.valueOf(string);
            } else if (type == Short.class || type == short.class) {
                return Short.valueOf(string);
            } else if (type == Integer.class || type == int.class) {
                return Integer.valueOf(string);
            } else if (type == Long.class || type == long.class) {
                return Long.valueOf(string);
            } else if (type == Float.class || type == float.class) {
                return Float.valueOf(string);
            } else if (type == Double.class || type == double.class) {
                return Double.valueOf(string);
            } else if (type == BigDecimal.class) {
                return new BigDecimal(string);
            } else if (type == BigInteger.class) {
                return new BigInteger(string);
            }
            // TODO: maybe throw exception
            return null;
        }
    }

    /**
     * Extension of {@link Boolean#parseBoolean} that is both more exact
     * and supports more features.  Specifically it will recognize:
     * <ul>
     * <li>{@code true} or {@code false} in mixed case.</li>
     * <li><code>yes</code> or <code>no</code> in mixed case.</li>
     * <li><code>on</code> or <code>off</code> in mixed case.</li>
     * <li><code>T</code> or <code>F</code> in mixed case.</li>
     * <li><code>Y</code> or <code>N</code> in mixed case.</li>
     * <li><code>1</code> or <code>0</code>.</li>
     * </ul>
     *
     * @param input The string value to convert to a boolean.
     * @return The boolean value.
     * @throws IllegalArgumentException if the value can't be converted according to the
     * above rules.
     */
    public static boolean toBoolean(final String input) {
        Utils.checkNullOrEmpty(input, "input");

        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("T")
         || input.equalsIgnoreCase("yes")  || input.equalsIgnoreCase("Y")
         || input.equalsIgnoreCase("on")) {
            return true;
        } else if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("F")
                || input.equalsIgnoreCase("no")    || input.equalsIgnoreCase("N")
                || input.equalsIgnoreCase("off")) {
            return false;
        } else {
            try {
                double d = Double.parseDouble(input);
                if (d == 1.0d) {
                    return true;
                } else if (d == 0.0d) {
                    return false;
                }
                throw new IllegalArgumentException("Unable to convert \"" + input + "\""
                    + " to a boolean value.");
            } catch (NumberFormatException nfe) {
                throw nfe;
            }
        }
    }

    /**
     * Given an iterable list of items, construct a string representation of the list
     * that looks like:
     * <p><code>[<i>item1</i>, <i>item2</i>, ...]</code></p>
     *
     * @param <T> The type of items in the list.
     * @param list The iterable list of items.
     * @return A string representation of the list.
     */
    public static <T> String toString(final Iterable<T> list) {
        StringBuilder sb = new StringBuilder();
        append(sb, list);
        return sb.toString();
    }

    /**
     * Given an iterable list of items, construct a string representation of the list
     * that looks like:
     * <p><code>[<i>item1</i>, <i>item2</i>, ...]</code></p>
     * appending the results to the given string builder for further use.
     * <p> If the {@link StringBuilder} has any preceding text (that is, {@code length > 0})
     * then append a blank before the list representation.
     *
     * @param <T> The type of items in the list.
     * @param sb The {@link StringBuilder} already in progress.
     * @param list The iterable list of items.
     * @return The input {@code StringBuilder} for further use.
     */
    public static <T> StringBuilder append(final StringBuilder sb, final Iterable<T> list) {
        // Separate this text from any preceding text
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append('[');

        int i = 0;
        for (T item : list) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(item);
            i++;
        }

        sb.append(']');

        return sb;
    }

}

