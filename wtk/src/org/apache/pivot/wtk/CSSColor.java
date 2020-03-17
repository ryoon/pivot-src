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

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.pivot.wtk.util.ColorUtilities;

/**
 * The complete enumeration of the CSS3/X11 color names and values,
 * taken from here:
 * <a href="http://www.w3.org/TR/css3-color/">http://www.w3.org/TR/css3-color/</a>,
 * and including the Java {@link Color} names (with all British/American
 * spelling variants).
 * <p> Note: these are available through the
 * {@link GraphicsUtilities#decodeColor GraphicsUtilities.decodeColor()}
 * and {@link org.apache.pivot.wtk.content.ColorItem#allCSSColors} methods.
 */
public enum CSSColor {
    AliceBlue           (240, 248, 255),
    AntiqueWhite        (250, 235, 215),
    Aqua                (  0, 255, 255),
    Aquamarine          (127, 255, 212),
    Azure               (240, 255, 255),
    Beige               (245, 245, 220),
    Bisque              (255, 228, 196),
    Black               (Color.BLACK),
    BlanchedAlmond      (255, 235, 205),
    Blue                (Color.BLUE),
    BlueViolet          (138,  43, 226),
    Brown               (165,  42,  42),
    Burlywood           (222, 184, 135),
    CadetBlue           ( 95, 158, 160),
    Chartreuse          (127, 255,   0),
    Chocolate           (210, 105,  30),
    Coral               (255, 127,  80),
    CornflowerBlue      (100, 149, 237),
    Cornsilk            (255, 248, 220),
    Crimson             (220,  20,  60),
    Cyan                (Color.CYAN),
    DarkBlue            (  0,   0, 139),
    DarkCyan            (  0, 139, 139),
    DarkGoldenrod       (184, 134,  11),
    DarkGray            (Color.DARK_GRAY),
    DarkGreen           (  0, 100,   0),
    DarkGrey            (Color.DARK_GRAY),
    DarkKhaki           (189, 183, 107),
    DarkMagenta         (139,   0, 139),
    DarkOliveGreen      ( 85, 107,  47),
    DarkOrange          (255, 140,   0),
    DarkOrchid          (153,  50, 204),
    DarkRed             (139,   0,   0),
    DarkSalmon          (233, 150, 122),
    DarkSeaGreen        (143, 188, 143),
    DarkSlateBlue       ( 72,  61, 139),
    DarkSlateGray       ( 47,  79,  79),
    DarkSlateGrey       ( 47,  79,  79),
    DarkTurquoise       (  0, 206, 209),
    DarkViolet          (148,   0, 211),
    DeepPink            (255,  20, 147),
    DeepSkyBlue         (  0, 191, 255),
    DimGray             (105, 105, 105),
    DimGrey             (105, 105, 105),
    DodgerBlue          ( 30, 144, 255),
    FireBrick           (178,  34,  34),
    FloralWhite         (255, 250, 240),
    ForestGreen         ( 34, 139,  34),
    Fuchsia             (255,   0, 255),
    Gainsboro           (220, 220, 220),
    GhostWhite          (248, 248, 255),
    Gold                (255, 215,   0),
    Goldenrod           (218, 165,  32),
    Gray                (Color.GRAY),
    Green               (Color.GREEN),
    GreenYellow         (173, 255,  47),
    Grey                (Color.GRAY),
    Honeydew            (240, 255, 240),
    HotPink             (255, 105, 180),
    IndianRed           (205,  92,  92),
    Indigo              ( 75,   0, 130),
    Ivory               (255, 255, 240),
    Khaki               (240, 230, 140),
    Lavender            (230, 230, 250),
    LavenderBlush       (255, 240, 245),
    LawnGreen           (124, 252,   0),
    LemonChiffon        (255, 250, 205),
    LightBlue           (173, 216, 230),
    LightCoral          (240, 128, 128),
    LightCyan           (224, 255, 255),
    LightGoldenrodYellow(250, 250, 210),
    LightGray           (Color.LIGHT_GRAY),
    LightGreen          (144, 238, 144),
    LightGrey           (Color.LIGHT_GRAY),
    LightPink           (255, 182, 193),
    LightSalmon         (255, 160, 122),
    LightSeaGreen       ( 32, 178, 170),
    LightSkyBlue        (135, 206, 250),
    LightSlateGray      (119, 136, 153),
    LightSlateGrey      (119, 136, 153),
    LightSteelBlue      (176, 196, 222),
    LightYellow         (255, 255, 224),
    Lime                (  0, 255,   0),
    LimeGreen           ( 50, 205,  50),
    Linen               (250, 240, 230),
    Magenta             (Color.MAGENTA),
    Maroon              (128,   0,   0),
    MediumAquamarine    (102, 205, 170),
    MediumBlue          (  0,   0, 205),
    MediumOrchid        (186,  85, 211),
    MediumPurple        (147, 112, 219),
    MediumSeaGreen      ( 60, 179, 113),
    MediumSlateBlue     (123, 104, 238),
    MediumSpringGreen   (  0, 250, 154),
    MediumTurquoise     ( 72, 209, 204),
    MediumVioletRed     (199,  21, 133),
    MidnightBlue        ( 25,  25, 112),
    MintCream           (245, 255, 250),
    MistyRose           (255, 228, 225),
    Moccasin            (255, 228, 181),
    NavajoWhite         (255, 222, 173),
    Navy                (  0,   0, 128),
    OldLace             (253, 245, 230),
    Olive               (128, 128,   0),
    OliveDrab           (107, 142,  35),
    Orange              (Color.ORANGE),
    OrangeRed           (255,  69,   0),
    Orchid              (218, 112, 214),
    PaleGoldenrod       (238, 232, 170),
    PaleGreen           (152, 251, 152),
    PaleTurquoise       (175, 238, 238),
    PaleVioletRed       (219, 112, 147),
    PapayaWhip          (255, 239, 213),
    PeachPuff           (255, 218, 185),
    Peru                (205, 133,  63),
    Pink                (Color.PINK),
    Plum                (221, 160, 221),
    PowderBlue          (176, 224, 230),
    Purple              (128,   0, 128),
    Red                 (Color.RED),
    RosyBrown           (188, 143, 143),
    RoyalBlue           ( 65, 105, 225),
    SaddleBrown         (139,  69,  19),
    Salmon              (250, 128, 114),
    SandyBrown          (244, 164,  96),
    SeaGreen            ( 46, 139,  87),
    Seashell            (255, 245, 238),
    Sienna              (160,  82,  45),
    Silver              (192, 192, 192),
    SkyBlue             (135, 206, 235),
    SlateBlue           (106,  90, 205),
    SlateGray           (112, 128, 144),
    SlateGrey           (112, 128, 144),
    Snow                (255, 250, 250),
    SpringGreen         (  0, 255, 127),
    SteelBlue           ( 70, 130, 180),
    Tan                 (210, 180, 140),
    Teal                (  0, 128, 128),
    Thistle             (216, 191, 216),
    Tomato              (255,  99,  71),
    Turquoise           ( 64, 224, 208),
    Violet              (238, 130, 238),
    Wheat               (245, 222, 179),
    White               (Color.WHITE),
    WhiteSmoke          (245, 245, 245),
    Yellow              (Color.YELLOW),
    YellowGreen         (154, 205,  50);

    /** The color value associated with this CSS color name. */
    private Color color;
    /** A standardized (lower-case) name for this color for matching. */
    private String colorName;

    /**
     * Private class that allows us to initialize lookup maps at constructor time,
     * instead of in a static initializer block later.
     */
    private static class Lookup {
        /** A map to translate from a color name to the enum value. */
        private static Map<String, CSSColor> colorNameMap = new HashMap<>();
        /** A map to translate from a color value to the enum value. */
        private static Map<Color, CSSColor> colorValueMap = new HashMap<>();
    }

    /**
     * Construct from integer R,G,B values.
     * @param r The red component of the color.
     * @param g The green component.
     * @param b And finally the blue value.
     */
    CSSColor(final int r, final int g, final int b) {
        this(new Color(r, g, b));
    }

    /**
     * Construct from one of the {@link Color} equivalents.
     * @param color The Java AWT color value this corresponds to.
     */
    CSSColor(final Color color) {
        this.color = color;
        this.colorName = super.toString().toLowerCase(Locale.ENGLISH);
        Lookup.colorNameMap.put(this.colorName, this);
        Lookup.colorValueMap.put(this.color, this);
    }

    /**
     * @return The standard color value (RGB) for this color according
     * to the W3C CSS color spec.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * @return The lowercase name of this color (as defined in the
     * W3C CSS color spec).
     */
    public String getColorName() {
        return this.colorName;
    }

    /**
     * @return The enum value of the given color name (compared in
     * lower case) if it can be found.
     * @param colorName The name of a color to match with one of our values.
     * @throws IllegalArgumentException if the color name cannot be found.
     */
    public static CSSColor fromString(final String colorName) {
        String lowerName = colorName.toLowerCase(Locale.ENGLISH);
        CSSColor color = Lookup.colorNameMap.get(lowerName);
        if (color == null) {
            throw new IllegalArgumentException("Incorrect Color format.  "
                + "Color name \"" + colorName + "\" is not valid.");
        }
        return color;
    }

    /**
     * @return The enum value of the given color value (solid color)
     * if it can be found.  Any transparency in the given color is stripped out
     * before a match is attempted.
     * @param color The solid color to match with one of our values.
     * @throws IllegalArgumentException if the color value cannot be found.
     */
    public static CSSColor fromColor(final Color color) {
        Color solidColor = ColorUtilities.toSolidColor(color);
        CSSColor cssColor = Lookup.colorValueMap.get(solidColor);
        if (cssColor == null) {
            throw new IllegalArgumentException("Incorrect Color value.  "
                + color.toString() + " does not match any CSS color.");
        }
        return cssColor;
    }

}
