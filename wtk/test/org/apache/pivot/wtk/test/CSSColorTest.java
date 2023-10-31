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
package org.apache.pivot.wtk.test;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.apache.pivot.util.ClassUtils;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.util.ColorUtilities;


/**
 * Tests the {@link CSSColor} enum and various {@link ColorUtilities} methods.
 */
public class CSSColorTest {
    private void testColors(final CSSColor original, final CSSColor lookup) {
        // Doing an exact match here is problematic because many of the
        // CSS colors have duplicate color values, even synonyms
        // (vis. DarkGray and DarkGrey), so do some clever checking.
        if (original != lookup) {
            if (!original.getColor().equals(lookup.getColor())) {
                String message = String.format("CSS Color %1$s (%2$s) gets wrong color by lookup %3$s (%4$s)!",
                        original, ClassUtils.defaultToString(original),
                        lookup, ClassUtils.defaultToString(lookup));
                fail(message);
            } else {
                // Log the "failures" for the record
                String message = String.format("Note: CSS Color %1$s matches %2$s by color (%3$s), but not by value.",
                        original, lookup, lookup.getColor());
                System.out.println(message);
            }
        }
    }

    @Test
    public void test() {
        // We're going to do a full round-trip cycle to test everything
        for (CSSColor css : CSSColor.values()) {
            Color underlyingColor = css.getColor();
            String name = css.getColorName();
            CSSColor lookupByName = CSSColor.fromString(name);
            assertEquals(css, lookupByName);

            CSSColor lookupByColor = CSSColor.fromColor(underlyingColor);
            testColors(css, lookupByColor);

            String stringValue = ColorUtilities.toStringValue(css);
            Color decodedColor = GraphicsUtilities.decodeColor(stringValue, name);
            CSSColor lookupByDecodedValue = CSSColor.fromColor(decodedColor);
            testColors(css, lookupByDecodedValue);

            String enumName = ((Object) css).toString();
            CSSColor lookupByEnumName = CSSColor.fromString(enumName);
            assertEquals(css, lookupByEnumName);
        }
    }

    @Test
    public void test2() throws IllegalAccessException {
        // Now, test to make sure the CSS and regular Java colors work "right".
        for (Field f : Color.class.getDeclaredFields()) {
            Class<?> clazz = f.getType();
            if (clazz.equals(Color.class)) {
                String javaColorName = f.getName();
                CSSColor cssEquivalent = CSSColor.fromString(javaColorName);
                assertEquals(f.getName() + " does not match in CSSColor lookup",
                        cssEquivalent.getColor(), (Color) f.get(null));
            }
        }
    }

    @Test
    public void test3() {
        Set<CSSColor> matchingColors = CSSColor.getMatchingColors(Color.black);
        assertEquals(3, matchingColors.size());
        assertTrue(matchingColors.contains(CSSColor.black));
        assertTrue(matchingColors.contains(CSSColor.BLACK));
        assertTrue(matchingColors.contains(CSSColor.Black));

        Set<CSSColor> match2 = CSSColor.getMatchingColors(Color.GREEN);
        assertEquals(3, match2.size());
        assertTrue(match2.contains(CSSColor.green));
        assertTrue(match2.contains(CSSColor.GREEN));
        assertTrue(match2.contains(CSSColor.Lime));
        assertFalse(match2.contains(CSSColor.Green));

        Set<CSSColor> match3 = CSSColor.getMatchingColors(CSSColor.SandyBrown.getColor());
        assertEquals(1, match3.size());
        assertTrue(match3.contains(CSSColor.SandyBrown));

        Set<CSSColor> match4 = CSSColor.getMatchingColors(new Color(255, 239, 213));
        assertEquals(1, match4.size());
        assertTrue(match4.contains(CSSColor.PapayaWhip));

        Set<CSSColor> matchEmpty = CSSColor.getMatchingColors(new Color(3, 3, 3));
        assertEquals(0, matchEmpty.size());
    }
}
