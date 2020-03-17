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

import static org.junit.Assert.assertEquals;
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
    private void testColors(CSSColor original, CSSColor lookup) {
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

            String enumName = ((Object)css).toString();
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
                assertEquals(f.getName() + " does not match in CSSColor lookup", cssEquivalent.getColor(), (Color)f.get(null));
            }
        }
    }
}
