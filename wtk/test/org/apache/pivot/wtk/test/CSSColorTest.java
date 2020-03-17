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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

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
        // (vis. DarkGray and DarkGrey), so do some clever checking
        if (!original.equals(lookup)) {
            if (!original.getColor().equals(lookup.getColor())) {
                fail("CSSColor " + original.toString() + " gets wrong color by lookup " + lookup.toString());
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
}
