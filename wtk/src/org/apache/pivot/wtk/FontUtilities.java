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
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.pivot.collections.Dictionary;
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
    public static final String SANS_SERIF_FONTS =
        "Verdana, Helvetica, Arial, SansSerif";

    /**
     * A list of monospaced fonts, useful for text editing areas for code, where
     * column position must be consistent.
     * <p> Note: this list is adapted from the list at
     * <a href="https://en.wikipedia.org/wiki/List_of_monospaced_typefaces">
     * https://en.wikipedia.org/wiki/List_of_monospaced_typefaces</a> with the most-popular or
     * readily available ones listed here.
     */
    public static final String MONOSPACED_FONTS =
        "Courier New, Andale Mono, Cascadia Code, Consolas, Courier, DejaVu Sans Mono, "
      + "Droid Sans Mono, FreeMono, Inconsolata, Letter Gothic, Liberation Mono, "
      + "Lucida Console, Menlo, Monaco, Noto Mono, Overpass Mono, Monospaced";


    /** The obvious factor needed to convert a number to a percentage value. */
    private static final float PERCENT_SCALE = 100.0f;

    /**
     * Private constructor for utility class.
     */
    private FontUtilities() {
    }

    /**
     * Parse out just the "name" part of a font specification.
     * <p> Note: this logic follows the logic in {@link Font#decode(String)}.
     *
     * @param str The font specification to parse.
     * @return    Just the font name part (which could be a list).
     */
    private static String getFontName(final String str) {
        String fontName = str;
        int lastHyphen  = str.lastIndexOf('-');
        int lastSpace   = str.lastIndexOf(' ');
        char sepChar    = (lastHyphen > lastSpace) ? '-' : ' ';
        int sizeIndex   = str.lastIndexOf(sepChar);
        int styleIndex  = str.lastIndexOf(sepChar, sizeIndex - 1);
        int length      = str.length();

        if (sizeIndex > 0 && sizeIndex + 1 < length) {
            try {
                Integer.valueOf(str.substring(sizeIndex + 1));
            } catch (NumberFormatException nfe) {
                /* Invalid size, maybe this is the style */
                styleIndex = sizeIndex;
                sizeIndex  = length;
                while (sizeIndex > 0 && str.charAt(sizeIndex - 1) == sepChar) {
                    sizeIndex--;
                }
            }
        }

        if (styleIndex >= 0 && styleIndex + 1 < length) {
            String styleName = str.substring(styleIndex + 1, sizeIndex);
            styleName = styleName.toLowerCase(Locale.ENGLISH);
            switch (styleName) {
                case "bolditalic":
                case "italic":
                case "bold":
                case "plain":
                    break;
                default:
                    /* Not a recognized style, must be part of the name */
                    styleIndex = sizeIndex;
                    while (styleIndex > 0 && str.charAt(styleIndex - 1) == sepChar) {
                        styleIndex--;
                    }
                    break;
            }
            fontName = str.substring(0, styleIndex);
        } else {
            int fontEnd = length;
            if (styleIndex > 0) {
                fontEnd = styleIndex;
            } else if (sizeIndex > 0) {
                fontEnd = sizeIndex;
            }
            while (fontEnd > 0 && str.charAt(fontEnd - 1) == sepChar) {
                fontEnd--;
            }
            fontName = str.substring(0, fontEnd);
        }

        return fontName;
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
            String name = getFontName(str);
            int pos     = name.length();
            String spec = pos < str.length() ? str.substring(pos) : "";

            String[] names = name.split("\\s*,\\s*");
            for (String nm : names) {
                Font f = Font.decode(nm + spec);
                if (f.getName().equalsIgnoreCase(nm) || f.getFamily().equalsIgnoreCase(nm)) {
                    return f;
                }
            }

            // No names matched in the list, so use the default name
            return Font.decode(Font.DIALOG + spec);
        }

        return Font.decode(str);
    }

    /**
     * Decode a font specification, choosing the first font in the list capable of displaying
     * the given characters. If only one font name is given, that font will be selected
     * regardless of its ability to display the test string of characters. If no fonts in the
     * list are capable of displaying the characters then the default system font (<code>DIALOG</code>)
     * will be returned.
     * <p> This is the same as {@link Font#decode(String)} except that we will allow multiple
     * font/family names separated by commas as the <code><i>fontname</i></code> part of the
     * spec (much the same as CSS allows), with the proviso that any font chosen from the list must be
     * capable of displaying all the given characters.
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
     * @param str        The font specification as above.
     * @param testString The characters that must have glyphs available to display (can be <code>null</code>
     *                   to skip the "capable" test) (only applicable if multiple font names are given to
     *                   choose from).
     * @return           The font according to the desired specification as much as possible.
     * @see              Font#decode(String)
     */
    public static Font decodeCapable(final String str, final String testString) {
        if (Utils.isNullOrEmpty(str)) {
            return Font.decode(str);
        }

        if (str.indexOf(',') > 0) {
            String name = getFontName(str);
            int pos     = name.length();
            String spec = pos < str.length() ? str.substring(pos) : "";

            String[] names = name.split("\\s*,\\s*");
            for (String nm : names) {
                Font f = Font.decode(nm + spec);
                if (f.getName().equalsIgnoreCase(nm) || f.getFamily().equalsIgnoreCase(nm)) {
                    if (testString == null || testString.isEmpty() || f.canDisplayUpTo(testString) < 0) {
                        return f;
                    }
                }
            }

            // No names matched in the list, so use the default name (either monospaced or not)
            if (str.indexOf("Mono") >= 0) {
                return Font.decode(Font.MONOSPACED + spec);
            } else {
                return Font.decode(Font.DIALOG + spec);
            }
        }

        return Font.decode(str);
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
            String[] names = name.split("\\s*,\\s*");
            for (String nm : names) {
                Font f = new Font(nm, style, size);
                if (f.getName().equalsIgnoreCase(nm) || f.getFamily().equalsIgnoreCase(nm)) {
                    return f;
                }
            }

            // No names matched in the list, so use the default name
            return new Font(Font.DIALOG, style, size);
        }

        return new Font(name, style, size);
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

    /**
     * Convert any object we support into its corresponding font.
     * <p> Uses {@link #decodeFont} or {@link Theme#deriveFont}
     * to do the work.
     *
     * @param fontValue The object to be converted to a font.
     * @return The converted font.
     * @throws IllegalArgumentException if the value is {@code null} or
     * cannot be converted.
     */
    public static Font fromObject(final Object fontValue) {
        Utils.checkNull(fontValue, "font");

        if (fontValue instanceof Font) {
            return (Font) fontValue;
        } else if (fontValue instanceof String) {
            return decodeFont((String) fontValue);
        } else if (fontValue instanceof Dictionary) {
            @SuppressWarnings("unchecked")
            Dictionary<String, ?> fontDictionary = (Dictionary<String, ?>) fontValue;
            return Theme.deriveFont(fontDictionary);
        } else {
            throw new IllegalArgumentException("Unable to convert "
                + fontValue.getClass().getSimpleName() + " to Font!");
        }
    }

    /**
     * Install a new font for use by this program (only).
     *
     * @param fullFontFilePath The complete path (on disk) to the local font file
     *                         (must be a TrueType font in this case).
     * @return The newly created / registered font if successful, <code>null</code> otherwise.
     * @throws IOException if there was a problem reading the font from the file.
     * @throws FontFormatException if the font file itself was malformed.
     */
    public static Font registerFont(final File fullFontFilePath)
        throws IOException, FontFormatException {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font f = Font.createFont(Font.TRUETYPE_FONT, fullFontFilePath);
            if (ge.registerFont(f)) {
                return f;
            }
            return null;
        } catch (IOException | FontFormatException ex) {
            throw ex;
        }
    }

    /**
     * Install a new font for use by this program (only).
     *
     * @param fontInputStream The stream of bytes describing this font
     *                        (must be a TrueType font in this case). Also note that the
     *                        caller is responsible for closing this input stream.
     * @return The newly created / registered font if successful, <code>null</code> otherwise.
     * @throws IOException if there was a problem reading the font from the file.
     * @throws FontFormatException if the font file itself was malformed.
     */
    public static Font registerFont(final InputStream fontInputStream)
        throws IOException, FontFormatException {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font f = Font.createFont(Font.TRUETYPE_FONT, fontInputStream);
            if (ge.registerFont(f)) {
                return f;
            }
            return null;
        } catch (IOException | FontFormatException ex) {
            throw ex;
        }
    }

}
