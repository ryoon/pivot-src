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

    /**
     * A list of "standard" sans-serif fonts, useful when cross-platform
     * support is necessary.
     */
    public static final String SANS_SERIF_FONTS = "Arial,Verdana,Helvetica,SansSerif";

    /** The obvious factor needed to convert a number to a percentage value. */
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
            font = decode(value);
        }

        return font;
    }

    /**
     * Decode a font specification.
     * <p> This is the same as {@link Font#decode(String)} except that we will allow multiple
     * font/family names separated by commas as the <code><i>fontname</i></code> part of the
     * spec (much the same as CSS allows).
     * <p>The list of allowed formats is:
     * <ul><li><i>fontname-style-pointsize</i></li>
     * <li><i>fontname-pointsize</i></li>
     * <li><i>fontname-style</i></li>
     * <li><i>fontname</i></li>
     * <li><i>fontname style pointsize</i></li>
     * <li><i>fontname pointsize</i></li>
     * <li><i>fontname style</i></li>
     * <li><i>fontname</i></li>
     * </ul>
     * where <i>fontname</i> can be <i>fontname</i>[,<i>fontname</i>]*.
     *
     * @param str The font specification as above.
     * @return    The font according to the desired specification as much as possible.
     * @see       Font#decode(String)
     */
    public static Font decode(final String str) {
        if (Utils.isNullOrEmpty(str)) {
            return Font.decode(str);
        }

        if (str.indexOf(',') > 0) {
            // Search from the end to find the separator (either ' ' or '-')
            int len = str.length();
            char sep = ' ';
            while (--len >= 0) {
                char ch = str.charAt(len);
                if (ch == ' ' || ch == '-') {
                    sep = ch;
                    break;
                }
            }
            int pos = str.indexOf(sep);
            String name = pos < 0 ? str : str.substring(0, pos);
            String spec = pos < 0 ? "" : str.substring(pos);
            String[] names = name.split(",");
            for (String nm : names) {
                Font f = Font.decode(nm + spec);
System.out.println("getName = " + f.getName() + ", getFontName = " + f.getFontName() + ", getFamily = " + f.getFamily());
            }
        } else {
            return Font.decode(str);
        }
    }

    /**
     * Get a new font with the given name, style, and size.
     * <p> The {@code name} can be a comma-separated list of names, and the first one matched will be used.
     *
     * @param name  The font name, which can be a list (such as <code>Arial,Verdana,SansSerif</code> with
     *              no spaces).
     * @param style The integer font style (as in {@link Font#PLAIN} or {@link Font#ITALIC}).
     * @param size  The integer font size (in points).
     * @return      The newly created font with these attributes.
     */
    public static Font getFont(final String name, final int style, final int size) {
        if (Utils.isNullOrEmpty(name)) {
            return new Font(name, style, size);
        }

        if (name.indexOf(',') > 0) {
            String[] names = name.split(",");
            for (String nm : names) {
                Font f = new Font(nm, style, size);
System.out.println("getName = " + f.getName() + ", getFontName = " + f.getFontName() + ", getFamily = " + f.getFamily());
            }
        } else {
            return new Font(name, style, size);
        }
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
