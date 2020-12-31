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

/**
 * Utility methods for other parts of the code.
 */
public final class Utils {
    /**
     * Non-public constructor for a utility class.
     */
    private Utils() {
    }

    /**
     * Decide if two strings are the same content (not just the same reference).
     * <p> Works properly for either string being {@code null}.
     * @param s1 First string to compare (can be {@code null}).
     * @param s2 Second string to compare (can also be {@code null}).
     * @return  {@code true} if both strings are {@code null} or if
     * <code>s1.equals(s2)</code>.
     */
    public static boolean stringsAreEqual(final String s1, final String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if ((s1 != null && s2 == null) || (s1 == null && s2 != null)) {
            return false;
        }
        return s1.equals(s2);
    }

    /**
     * Check if the input argument is {@code null} and throw an
     * {@link IllegalArgumentException} if so, with an optional
     * message derived from the given string.
     *
     * @param value The argument value to check for {@code null}.
     * @param description A description for the value used to
     * construct a message like {@code "xxx must not be null."}. Can be
     * {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNull(final Object value, final String description) {
        if (value == null) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must not be null.");
            }
        }
    }

    /**
     * Check if the input argument is {@code null} and throw an
     * {@link IllegalArgumentException} with an empty message if so.
     *
     * @param value The argument value to check for {@code null}.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNull(final Object value) {
        checkNull(value, null);
    }

    /**
     * Check if the input string is {@code null} or empty (or all whitespace).
     *
     * @param value The string to check.
     * @return {@code true} if the input is {@code null} or empty, {@code false}
     * otherwise.
     */
    public static boolean isNullOrEmpty(final String value) {
        if (value == null) {
            return true;
        }
        return value.trim().isEmpty();
    }

    /**
     * Check if the input value is {@code null} or if it is a string and is empty
     * (or all whitespace).
     *
     * @param value The object to check.
     * @return {@code true} if the input is {@code null} or an empty string,
     * {@code false} otherwise (which would include a non-null object other
     * than a string).
     */
    public static boolean isNullOrEmpty(final Object value) {
        if (value == null) {
            return true;
        }
        return (value instanceof String) && ((String) value).trim().isEmpty();
    }

    /**
     * Check if the input argument is {@code null} and if it is a string
     * if it is empty, and throw an {@link IllegalArgumentException} if so,
     * with an optional message derived from the given string.
     *
     * @param value The argument value to check for {@code null} or empty.
     * @param description A description for the argument, used to
     * construct a message like {@code "xxx must not be null or empty."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNullOrEmpty(final Object value, final String description) {
        if (value == null || (value instanceof String && isNullOrEmpty((String) value))) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must not be null or empty.");
            }
        }
    }

    /**
     * Check if the input argument is {@code null} and if it is a string
     * if it is empty, and throw an {@link IllegalArgumentException} if so.
     *
     * @param value The string to check.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNullOrEmpty(final Object value) {
        checkNullOrEmpty(value, null);
    }

    /**
     * If the first argument given is {@code null} then substitute the second argument
     * for it, else just return the given argument.
     *
     * @param <T>   Type of value being tested and returned.
     * @param value The argument to check for &quot;null-ness&quot;.
     * @param substituteForNull The value to use instead of the {@code null} value.
     * @return Either the value or the substituted one (which could be null, but then
     * why would you call this method?).
     */
    public static <T> T ifNull(final T value, final T substituteForNull) {
        return (value == null) ? substituteForNull : value;
    }

    /**
     * Check if the input argument is negative (less than zero), and throw an
     * {@link IllegalArgumentException} if so, with or without a descriptive message,
     * depending on the {@code description} supplied.
     *
     * @param value The value to check.
     * @param description A description for the argument, used to
     * construct a message like {@code "xxx must not be negative."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkNonNegative(final int value, final String description) {
        if (value < 0) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must not be negative.");
            }
        }
    }

    /**
     * Check if the input argument is negative (less than zero), and throw an
     * {@link IllegalArgumentException} if so.
     *
     * @param value The value to check.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkNonNegative(final int value) {
        checkNonNegative(value, null);
    }

    /**
     * Check if the input argument is negative (less than zero), and throw an
     * {@link IllegalArgumentException} if so, with or without a descriptive message,
     * depending on the {@code description} supplied.
     *
     * @param value The value to check.
     * @param description A description for the argument, used to
     * construct a message like {@code "xxx must not be negative."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkNonNegative(final float value, final String description) {
        if (value < 0.0f) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must not be negative.");
            }
        }
    }

    /**
     * Check if the input argument is negative (less than zero), and throw an
     * {@link IllegalArgumentException} if so.
     *
     * @param value The value to check.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkNonNegative(final float value) {
        checkNonNegative(value, null);
    }

    /**
     * Check if the input argument is positive (greater than zero), and throw an
     * {@link IllegalArgumentException} if not, with or without a descriptive message,
     * depending on the {@code description} supplied.
     *
     * @param value The value to check.
     * @param description A description for the argument, used to
     * construct a message like {@code "xxx must be positive."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative or zero.
     */
    public static void checkPositive(final int value, final String description) {
        if (value <= 0) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must be positive.");
            }
        }
    }

    /**
     * Check if the input argument is positive (greater than zero), and throw an
     * {@link IllegalArgumentException} if not.
     *
     * @param value The value to check.
     * @throws IllegalArgumentException if the value is negative or zero.
     */
    public static void checkPositive(final int value) {
        checkPositive(value, null);
    }

    /**
     * Check if the input argument is positive (greater than zero), and throw an
     * {@link IllegalArgumentException} if not, with or without a descriptive message,
     * depending on the {@code description} supplied.
     *
     * @param value The value to check.
     * @param description A description for the argument, used to
     * construct a message like {@code "xxx must be positive."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkPositive(final float value, final String description) {
        if (value <= 0.0f) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must be positive.");
            }
        }
    }

    /**
     * Check if the input argument is positive (greater than zero), and throw an
     * {@link IllegalArgumentException} if not.
     *
     * @param value The value to check.
     * @throws IllegalArgumentException if the value is negative or zero.
     */
    public static void checkPositive(final float value) {
        checkPositive(value, null);
    }

    /**
     * Check that the given value falls within the range of a non-negative "short" value, that is
     * between 0 and {@link Short#MAX_VALUE} (inclusive).
     *
     * @param value The value to check.
     * @param description The optional argument used to describe the value in case it is out of range
     * (used in the thrown exception).
     * @throws IllegalArgumentException if the value is out of range.
     */
    public static void checkInRangeOfShort(final int value, final String description) {
        if (value < 0 || value > (int) Short.MAX_VALUE) {
            String valueMsg = ifNull(description, "value");
            throw new IllegalArgumentException(valueMsg + " must be less than or equal "
                + Short.MAX_VALUE + ".");
        }
    }

    /**
     * Check that the given value falls within the range of a non-negative "short" value, that is
     * between 0 and {@link Short#MAX_VALUE} (inclusive).
     *
     * @param value The value to check.
     * @throws IllegalArgumentException if the value is out of range.
     */
    public static void checkInRangeOfShort(final int value) {
        checkInRangeOfShort(value, null);
    }

    /**
     * Check that the given {@code index} is between the values of {@code start} and {@code end}.
     *
     * @param index  The candidate index into the range.
     * @param start  The start of the acceptable range (inclusive).
     * @param end    The end of the acceptable range (inclusive).
     *
     * @throws IllegalArgumentException if {@code end < start}.
     * @throws IndexOutOfBoundsException if {@code index < start} or {@code index > end}.
     */
    public static void checkIndexBounds(final int index, final int start, final int end) {
        if (end < start) {
            throw new IllegalArgumentException("end (" + end + ") < " + "start (" + start + ")");
        }
        if (index < start || index > end) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [" + start + ","
                + end + "].");
        }
    }

    /**
     * Special case of {@link #checkIndexBounds(int, int, int)} for the case that start is zero and therefore
     * the end case is usually size - 1.
     *
     * @param index   The candidate index into the zero-based range.
     * @param size    The size of the array/list/etc. (so the proper range is {@code 0 .. size - 1}).
     * @throws IndexOutOfBoundsException if the {@code index < 0} or {@code index >= size}.
     */
    public static void checkZeroBasedIndex(final int index, final int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [0," + (size - 1) + "].");
        }
    }

    /**
     * Check that the given {@code index} plus {@code count} are between the values of
     * {@code start} and {@code end}.
     *
     * @param index  The candidate index into the range.
     * @param count  The number of elements in the indexed selection.
     * @param start  The start of the acceptable range (inclusive).
     * @param end    The end of the acceptable range (inclusive).
     *
     * @throws IllegalArgumentException if {@code end < start}, or
     * if {@code count} or {@code start} are {@code < zero}.
     * @throws IndexOutOfBoundsException if {@code index < start} or
     * if {@code index + count > end}.
     */
    public static void checkIndexBounds(final int index, final int count, final int start, final int end) {
        if (end < start) {
            throw new IllegalArgumentException("end (" + end + ") < " + "start (" + start + ")");
        }
        if (count < 0 || start < 0) {
            throw new IllegalArgumentException("count (" + count + ") < 0 or start (" + start
                + ") < 0");
        }
        if (index < start) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [" + start + ","
                + end + "].");
        }
        if (index + count > end) {
            throw new IndexOutOfBoundsException("Index + count (" + index + " + " + count
                + ") out of bounds [" + start + "," + end + "].");
        }
    }

    /**
     * Check that the given {@code startIndex} and {@code endIndex} are between
     * the values of {@code start} and {@code end}.
     *
     * @param startIndex  The beginning index to check.
     * @param endIndex    The ending index (inclusive) to check.
     * @param start  The start of the acceptable range (inclusive).
     * @param end    The end of the acceptable range (inclusive).
     *
     * @throws IllegalArgumentException if {@code startIndex > endIndex}.
     * @throws IndexOutOfBoundsException if {@code startIndex < start} or
     * {@code endIndex > end}.
     */
    public static void checkTwoIndexBounds(final int startIndex, final int endIndex, final int start, final int end) {
        if (startIndex > endIndex) {
            throw new IllegalArgumentException("endIndex (" + endIndex + ") < " + "startIndex (" + startIndex + ")");
        }

        if (startIndex < start || endIndex > end) {
            throw new IndexOutOfBoundsException("startIndex " + startIndex + " or endIndex " + endIndex
                + " out of bounds [" + start + "," + end + "].");
        }
    }

}
