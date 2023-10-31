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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.pivot.wtk.util.ColorUtilities;

/**
 * The complete enumeration of the CSS3/X11 color names and values,
 * taken from here:
 * <a href="http://www.w3.org/TR/css3-color/">http://www.w3.org/TR/css3-color/</a>,
 * and including the Java {@link Color} names (with all British/American
 * spelling variants).
 * <p> Ugly note: Several of these colors including (but not limited to), "pink",
 * "green", the "grays", etc. are different between the Java colors and these CSS
 * definitions. So, to support everything, the following conventions have been
 * established:
 * <ol><li>The color names are (more-or-less) case-sensitive.</li>
 * <li>Where there are CSS colors and Java colors with similar names (except for
 * case differences), the Java colors are found using all lower case (e.g., "green")
 * or all UPPER case (e.g., "GREEN") and the CSS value with a mixed-case name
 * (e.g., "Green").</li>
 * <li>If there is no duplicate (e.g., "AliceBlue") then the value can be found
 * using any of these variants.</li>
 * <li>NOTE: this means that other combinations (e.g., "gReEn") which might have
 * worked in the past will now fail and throw an exception.
 * </ol>
 * <p> This list also contains the Java versions of the names (complete with spelling
 * variations) for complete compatibility, and we put the Java variants in first
 * for the sake of the lookup maps.
 * <p> Note: these are available through the
 * {@link GraphicsUtilities#decodeColor GraphicsUtilities.decodeColor()}
 * and {@link org.apache.pivot.wtk.content.ColorItem#allCSSColors} methods.
 */
public enum CSSColor {
               AliceBlue(240, 248, 255),
            AntiqueWhite(250, 235, 215),
                    Aqua(  0, 255, 255),
              Aquamarine(127, 255, 212),
                   Azure(240, 255, 255),
                   Beige(245, 245, 220),
                  Bisque(255, 228, 196),
                   black(Color.black),
                   BLACK(Color.BLACK),
                   Black(  0,   0,   0),
          BlanchedAlmond(255, 235, 205),
                    blue(Color.blue),
                    BLUE(Color.BLUE),
                    Blue(  0,   0, 255),
              BlueViolet(138,  43, 226),
                   Brown(165,  42,  42),
               Burlywood(222, 184, 135),
               CadetBlue( 95, 158, 160),
              Chartreuse(127, 255,   0),
               Chocolate(210, 105,  30),
                   Coral(255, 127,  80),
          CornflowerBlue(100, 149, 237),
                Cornsilk(255, 248, 220),
                 Crimson(220,  20,  60),
                    cyan(Color.cyan),
                    CYAN(Color.CYAN),
                    Cyan(  0, 255, 255),
                DarkBlue(  0,   0, 139),
                DarkCyan(  0, 139, 139),
           DarkGoldenrod(184, 134,  11),
                darkGray(Color.darkGray),
               DARK_GRAY(Color.DARK_GRAY),
                DarkGray(169, 169, 169),
               DarkGreen(  0, 100,   0),
                DarkGrey(169, 169, 169),
               DarkKhaki(189, 183, 107),
             DarkMagenta(139,   0, 139),
          DarkOliveGreen( 85, 107,  47),
              DarkOrange(255, 140,   0),
              DarkOrchid(153,  50, 204),
                 DarkRed(139,   0,   0),
              DarkSalmon(233, 150, 122),
            DarkSeaGreen(143, 188, 143),
           DarkSlateBlue( 72,  61, 139),
           DarkSlateGray( 47,  79,  79),
           DarkSlateGrey( 47,  79,  79),
           DarkTurquoise(  0, 206, 209),
              DarkViolet(148,   0, 211),
                DeepPink(255,  20, 147),
             DeepSkyBlue(  0, 191, 255),
                 DimGray(105, 105, 105),
                 DimGrey(105, 105, 105),
              DodgerBlue( 30, 144, 255),
               FireBrick(178,  34,  34),
             FloralWhite(255, 250, 240),
             ForestGreen( 34, 139,  34),
                 Fuchsia(255,   0, 255),
               Gainsboro(220, 220, 220),
              GhostWhite(248, 248, 255),
                    Gold(255, 215,   0),
               Goldenrod(218, 165,  32),
                    gray(Color.gray),
                    GRAY(Color.GRAY),
                    Gray(128, 128, 128),
                   green(Color.green),
                   GREEN(Color.GREEN),
                   Green(  0, 128,   0),
             GreenYellow(173, 255,  47),
                    Grey(128, 128, 128),
                Honeydew(240, 255, 240),
                 HotPink(255, 105, 180),
               IndianRed(205,  92,  92),
                  Indigo( 75,   0, 130),
                   Ivory(255, 255, 240),
                   Khaki(240, 230, 140),
                Lavender(230, 230, 250),
           LavenderBlush(255, 240, 245),
               LawnGreen(124, 252,   0),
            LemonChiffon(255, 250, 205),
               LightBlue(173, 216, 230),
              LightCoral(240, 128, 128),
               LightCyan(224, 255, 255),
    LightGoldenrodYellow(250, 250, 210),
               lightGray(Color.lightGray),
              LIGHT_GRAY(Color.LIGHT_GRAY),
               LightGray(211, 211, 211),
              LightGreen(144, 238, 144),
               LightGrey(211, 211, 211),
               LightPink(255, 182, 193),
             LightSalmon(255, 160, 122),
           LightSeaGreen( 32, 178, 170),
            LightSkyBlue(135, 206, 250),
          LightSlateGray(119, 136, 153),
          LightSlateGrey(119, 136, 153),
          LightSteelBlue(176, 196, 222),
             LightYellow(255, 255, 224),
                    Lime(  0, 255,   0),
               LimeGreen( 50, 205,  50),
                   Linen(250, 240, 230),
                 magenta(Color.magenta),
                 MAGENTA(Color.MAGENTA),
                 Magenta(255,   0, 255),
                  Maroon(128,   0,   0),
        MediumAquamarine(102, 205, 170),
              MediumBlue(  0,   0, 205),
            MediumOrchid(186,  85, 211),
            MediumPurple(147, 112, 219),
          MediumSeaGreen( 60, 179, 113),
         MediumSlateBlue(123, 104, 238),
       MediumSpringGreen(  0, 250, 154),
         MediumTurquoise( 72, 209, 204),
         MediumVioletRed(199,  21, 133),
            MidnightBlue( 25,  25, 112),
               MintCream(245, 255, 250),
               MistyRose(255, 228, 225),
                Moccasin(255, 228, 181),
             NavajoWhite(255, 222, 173),
                    Navy(  0,   0, 128),
                 OldLace(253, 245, 230),
                   Olive(128, 128,   0),
               OliveDrab(107, 142,  35),
                  orange(Color.orange),
                  ORANGE(Color.ORANGE),
                  Orange(255, 165,   0),
               OrangeRed(255,  69,   0),
                  Orchid(218, 112, 214),
           PaleGoldenrod(238, 232, 170),
               PaleGreen(152, 251, 152),
           PaleTurquoise(175, 238, 238),
           PaleVioletRed(219, 112, 147),
              PapayaWhip(255, 239, 213),
               PeachPuff(255, 218, 185),
                    Peru(205, 133,  63),
                    pink(Color.pink),
                    PINK(Color.PINK),
                    Pink(255, 192, 203),
                    Plum(221, 160, 221),
              PowderBlue(176, 224, 230),
                  Purple(128,   0, 128),
                     red(Color.red),
                     RED(Color.RED),
                     Red(255,   0,   0),
               RosyBrown(188, 143, 143),
               RoyalBlue( 65, 105, 225),
             SaddleBrown(139,  69,  19),
                  Salmon(250, 128, 114),
              SandyBrown(244, 164,  96),
                SeaGreen( 46, 139,  87),
                Seashell(255, 245, 238),
                  Sienna(160,  82,  45),
                  Silver(192, 192, 192),
                 SkyBlue(135, 206, 235),
               SlateBlue(106,  90, 205),
               SlateGray(112, 128, 144),
               SlateGrey(112, 128, 144),
                    Snow(255, 250, 250),
             SpringGreen(  0, 255, 127),
               SteelBlue( 70, 130, 180),
                     Tan(210, 180, 140),
                    Teal(  0, 128, 128),
                 Thistle(216, 191, 216),
                  Tomato(255,  99,  71),
               Turquoise( 64, 224, 208),
                  Violet(238, 130, 238),
                   Wheat(245, 222, 179),
                   white(Color.white),
                   WHITE(Color.WHITE),
                   White(255, 255, 255),
              WhiteSmoke(245, 245, 245),
                  yellow(Color.yellow),
                  YELLOW(Color.YELLOW),
                  Yellow(255, 255,   0),
             YellowGreen(154, 205,  50);

    /** The color value associated with this CSS color name. */
    private Color color;
    /** The enum name for this color for matching (note: this value, plus the upper- or lower-case
     * equivalent can be used). */
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
     * @param awtColor The Java AWT color value this corresponds to.
     */
    CSSColor(final Color awtColor) {
        this.color = awtColor;
        // Okay, this is ugly, BUT Color.GREEN isn't the same color as our "Green"
        // (0,255,0) vs. (0,128,0), so make an ugly hack involving case, such that
        // "Green" is our color, but "green" or "GREEN" is the Java version.
        String cssName = super.toString();
        this.colorName = cssName;
        String lowerName = cssName.toLowerCase(Locale.ENGLISH);
        String upperName = cssName.toUpperCase(Locale.ENGLISH);
        // Put the value in as both the native version and the lower- and
        // upper-case variants, if they differ.
        // Note: this generally means the Java equivalents (in lower- and
        // upper-case) must come before the CSS version in mixed-case in
        // the enum list.
        Lookup.colorNameMap.put(cssName, this);
        Lookup.colorNameMap.putIfAbsent(lowerName, this);
        Lookup.colorNameMap.putIfAbsent(upperName, this);
        // Note: this reverse map WILL have values replaced by later enums
        // since many of these colors have the same RGB values.
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
     * @return The name of this color (as defined in the
     * W3C CSS color spec), or the Java {@link Color} name.
     */
    public String getColorName() {
        return this.colorName;
    }

    /**
     * @return The enum value of the given color name (which can be upper-, lower-,
     * or mixed-case) if it can be found.
     * @param colorName The name of a color to match with one of our values.
     * @throws IllegalArgumentException if the color name cannot be found.
     */
    public static CSSColor fromString(final String colorName) {
        // Note: we entered names as lower-, upper-, or mixed-case values
        // (for the "green" confusion), so one of the variants should match.
        CSSColor color = Lookup.colorNameMap.get(colorName);
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

    /**
     * Return the set of all these colors with the same RGB value as the given color.
     * <p> This could be a bit time-consuming because we have to search all the values.
     * @param color An RGB (solid) color to match with these values.
     * @return The complete set (could be empty) of these colors with the same RGB values.
     */
    public static Set<CSSColor> getMatchingColors(final Color color) {
        Set<CSSColor> matches = EnumSet.noneOf(CSSColor.class);
        Color solidColor = ColorUtilities.toSolidColor(color);
        for (CSSColor cssColor : values()) {
            if (cssColor.color.equals(solidColor)) {
                matches.add(cssColor);
            }
        }
        return matches;
    }

    /**
     * Return the set of all these colors with the same RGB value as the given {@code CSSColor},
     * in other words, the synonyms for this color.
     * <p> This could be a bit time-consuming because we have to search all the values.
     * @param color One of these colors to find the matching enum values.
     * @return The complete set (could be empty) of these colors with the same RGB value (not
     * including the given color).
     */
    public static Set<CSSColor> getMatchingColors(final CSSColor color) {
        Set<CSSColor> matches = EnumSet.noneOf(CSSColor.class);
        Color solidColor = color.color;
        for (CSSColor cssColor : values()) {
            if (cssColor.color.equals(solidColor) && cssColor != color) {
                matches.add(cssColor);
            }
        }
        return matches;
    }

    /**
     * @return The number of colors in this list.
     */
    public static int numberOfColors() {
        return values().length;
    }

}
