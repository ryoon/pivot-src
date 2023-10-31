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
package org.apache.pivot.text;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Immutable class representing a span of characters. The range includes all values
 * in the interval <i><code>[start, start+length-1]</code></i> inclusive.  This is
 * the paradigm used in a lot of places (notably the text controls) to indicate a selection.
 * <p> A zero-length span indicates a single caret position at the given start.
 * <p> Negative lengths are not supported and will throw exceptions, as will
 * negative start positions.
 */
public final class CharSpan {
    /** The starting location of this span (zero-based). */
    public final int start;
    /** The length of this span (non-negative). */
    public final int length;

    /** The dictionary key used to retrieve the start location. */
    public static final String START_KEY = "start";
    /** The dictionary key used to retrieve the length. */
    public static final String LENGTH_KEY = "length";

    /**
     * A span of length zero, starting at position zero.
     */
    public static final CharSpan ZERO = new CharSpan();


    /**
     * Construct a default span of length zero at location zero.
     */
    public CharSpan() {
        this(0);
    }

    /**
     * Construct a new char span of length zero at the given location.
     *
     * @param startValue The start of this char span.
     * @throws IllegalArgumentException if the value is negative.
     */
    public CharSpan(final int startValue) {
        this(startValue, 0);
    }

    /**
     * Construct a new char span with the given values.
     *
     * @param startValue The start of this char span.
     * @param lengthValue The length of this char span.
     * @throws IllegalArgumentException if either value is negative.
     */
    public CharSpan(final int startValue, final int lengthValue) {
        Utils.checkNonNegative(startValue, "start");
        Utils.checkNonNegative(lengthValue, "length");

        start = startValue;
        length = lengthValue;
    }

    /**
     * Construct a new char span from another one (a "copy constructor").
     *
     * @param existingCharSpan An existing char span (which must not be {@code null}).
     * @throws IllegalArgumentException if the given char span is {@code null}.
     */
    public CharSpan(final CharSpan existingCharSpan) {
        Utils.checkNull(existingCharSpan, "existingCharSpan");

        start = existingCharSpan.start;
        length = existingCharSpan.length;
    }

    /**
     * Construct a new char span from the given dictionary which must contain
     * the {@link #START_KEY} and can also contain the {@link #LENGTH_KEY} key.
     *
     * @param charSpanDictionary A dictionary containing start and length values.
     * @throws IllegalArgumentException if the given char span is {@code null},
     * if the dictionary does not contain at least the start key, or if either of
     * the dictionary values is negative.
     */
    public CharSpan(final Dictionary<String, ?> charSpanDictionary) {
        Utils.checkNull(charSpanDictionary, "charSpanDictionary");

        int startValue;
        int lengthValue = 0;

        if (charSpanDictionary.containsKey(START_KEY)) {
            startValue = charSpanDictionary.getInt(START_KEY);
            Utils.checkNonNegative(startValue, "start");
        } else {
            throw new IllegalArgumentException(START_KEY + " is required.");
        }

        if (charSpanDictionary.containsKey(LENGTH_KEY)) {
            lengthValue = charSpanDictionary.getInt(LENGTH_KEY);
            Utils.checkNonNegative(lengthValue, "length");
        }

        start = startValue;
        length = lengthValue;
    }

    /**
     * Construct a new char span from the given sequence with two
     * numeric values corresponding to the start and length values
     * respectively, or one numeric value corresponding to the start
     * value (length 0).
     *
     * @param charSpanSequence A sequence containing the start and length values.
     * @throws IllegalArgumentException if the given char span is {@code null}, or
     * zero length, or length is greater than two.
     */
    public CharSpan(final Sequence<?> charSpanSequence) {
        Utils.checkNull(charSpanSequence, "charSpanSequence");

        int startValue;
        int lengthValue = 0;
        int seqLength = charSpanSequence.getLength();

        if (seqLength < 1 || seqLength > 2) {
            throw new IllegalArgumentException("CharSpan needs one or two values in the sequence to construct.");
        }

        startValue = ((Number) charSpanSequence.get(0)).intValue();
        Utils.checkNonNegative(startValue, "start");

        if (seqLength == 2) {
            lengthValue = ((Number) charSpanSequence.get(1)).intValue();
            Utils.checkNonNegative(lengthValue, "length");
        }


        start = startValue;
        length = lengthValue;
    }

    /**
     * Returns the inclusive end value of this char span, which is the
     * <code>start + length - 1</code>.  So, if the length is zero,
     * then the end will be less that the start.
     *
     * @return The computed inclusive end value of this char span.
     */
    public int getEnd() {
        return start + length - 1;
    }

    /**
     * Returns a new {@link CharSpan} with the start value offset by the given amount.
     *
     * @param offset The positive or negative amount by which to "move" this
     * char span (the start value).
     * @return A new {@link CharSpan} with the updated value.
     * @throws IllegalArgumentException if the updated start value goes negative.
     */
    public CharSpan offset(final int offset) {
        return (offset == 0) ? this : new CharSpan(this.start + offset, this.length);
    }

    /**
     * Returns a new {@link CharSpan} with the length value offset by the given amount
     * (either positive to lengthen the span or negative to shorten the span).
     *
     * @param offset The positive or negative amount by which to "lengthen" this
     * char span (the length value).
     * @return A new {@link CharSpan} with the updated value.
     * @throws IllegalArgumentException if the updated length value goes negative.
     */
    public CharSpan lengthen(final int offset) {
        return (offset == 0) ? this : new CharSpan(this.start, this.length + offset);
    }

    @Override
    public boolean equals(final Object o) {
        boolean equal = false;

        if (o == this) {
            return true;
        }

        if (o instanceof CharSpan) {
            CharSpan span = (CharSpan) o;
            equal = (start == span.start && length == span.length);
        }

        return equal;
    }

    @Override
    public int hashCode() {
        return 31 * start + length;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {start:" + start + ", length:" + length + "}";
    }

    /**
     * Convert a string into a char span.
     * <p> If the string value is a JSON map, then parse the map
     * and construct using the {@link #CharSpan(Dictionary)} method.
     * <p> If the string value is a JSON list, then parse the list
     * and construct using the first two values as start and end
     * respectively, using the {@link #CharSpan(int, int)} constructor.
     * <p> Also accepted is a simple list of two integer values
     * separated by comma or semicolon.
     * <p> Otherwise the string should be a single integer value
     * that will be used to construct the char span using the {@link #CharSpan(int)}
     * constructor (just the start value, with a zero length).
     *
     * @param value The string value to decode into a new char span.
     * @return The decoded char span.
     * @throws IllegalArgumentException if the value is {@code null} or empty,
     * if the string starts with <code>"{"</code> but it cannot be parsed as
     * a JSON map, if it starts with <code>"["</code> but cannot be parsed
     * as a JSON list, or cannot be recognized as a simple list of one or
     * two integers.
     */
    public static CharSpan decode(final String value) {
        Utils.checkNullOrEmpty(value, "value");

        CharSpan charSpan;
        if (value.startsWith("{")) {
            try {
                charSpan = new CharSpan(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                charSpan = new CharSpan(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            try {
                if (parts.length == 2) {
                    charSpan = new CharSpan(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } else if (parts.length == 1) {
                    charSpan = new CharSpan(Integer.parseInt(value));
                } else {
                    throw new IllegalArgumentException("Unknown format for CharSpan: " + value);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return charSpan;
    }
}
