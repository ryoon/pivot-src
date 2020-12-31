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
package org.apache.pivot.wtk;

import java.awt.Font;

import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Utility class for dealing with fonts.
 */
public final class FontUtilities {

    /** The standard name for the {@code Arial} font. */
    public static final String ARIAL = "Arial";

    /** The obvious factor needed to convert a number to a percentage factor. */
    private static final float PERCENT_SCALE = 100.0f;

    /**
     * Private constructor for utility class.
     */
    private FontUtilities() {
    }

    /**
     * Interpret a string as a font specification.
     *
     * @param value Either a JSON dictionary {@link Theme#deriveFont describing
     * a font relative to the current theme}, or one of the
     * {@link Font#decode(String) standard Java font specifications}.
     * @return The font corresponding to the specification.
     * @throws IllegalArgumentException if the given string is {@code null}
     * or empty or the font specification cannot be decoded.
     */
    public static Font decodeFont(final String value) {
        Utils.checkNullOrEmpty(value, "font");

        Font font;
        if (value.startsWith("{")) {
            try {
                font = Theme.deriveFont(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            font = Font.decode(value);
        }

        return font;
    }

    /**
     * Decode a font size specification, taking into account "nnn%" form, and the existing size.
     *
     * @param sizeValue The input size value, could be a number or a numeric string, or a number
     * followed by "%".
     * @param existingSize The existing font size, which will be adjusted by the percentage.
     * Can be null, in which case the original size is returned. Otherwise it is unused.
     * @return The new font size value.
     * @throws IllegalArgumentException if the sizeValue cannot be decoded.
     */
    public static int decodeFontSize(final Object sizeValue, final int existingSize) {
        int adjustedSize = existingSize;

        if (sizeValue != null) {
            if (sizeValue instanceof String) {
                String string = (String) sizeValue;

                try {
                    if (string.endsWith("%")) {
                        float percentage = Float.parseFloat(string.substring(0, string.length() - 1)) / PERCENT_SCALE;
                        adjustedSize = Math.round(existingSize * percentage);
                    } else {
                        adjustedSize = Float.valueOf(string).intValue();
                    }
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("\"" + sizeValue + "\" is not a valid font size!");
                }
            } else if (sizeValue instanceof Number) {
                adjustedSize = ((Number) sizeValue).intValue();
            } else {
                throw new IllegalArgumentException("\"" + sizeValue + "\" is not a valid font size!");
            }
        }

        return adjustedSize;
    }

}
