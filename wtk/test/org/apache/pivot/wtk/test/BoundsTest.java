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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Point;


/**
 * Tests the {@link Bounds} class which is used extensively
 * in the "wtk" source, and deserves good tests.
 */
public class BoundsTest {

    @Test
    public void test() {
        Bounds bndMinus1 = new Bounds(-1, -1, 0, 0);
        Bounds bnd0 = new Bounds(0, 0, 0, 0);
        Bounds bnd1 = new Bounds(1, 1, 1, 1);

        Dimensions dim0 = new Dimensions(0, 0);
        Dimensions dim1 = new Dimensions(1, 1);
        Dimensions dim5 = new Dimensions(5);
        Point p10 = new Point(10, 10);
        Bounds bnd10 = new Bounds(p10, dim1);
        Bounds bnd10a = new Bounds(dim1);
        Bounds bnd10b = new Bounds(0, 0, 1, 1);

        Bounds bnd2 = Bounds.decode("[2, 3, 4, 5]");
        Bounds bnd3 = Bounds.decode("{x:2, y:3, width:4, height:5}");
        Bounds bnd3a = new Bounds(2, 3, 4, 5);

        Bounds bnd4 = new Bounds(4, 4, 4, 5);
        Bounds bnd5 = bnd3a.translate(1, 1); // -> {3, 4, 4, 5}
        bnd5 = bnd5.expand(-2, -4);
        Bounds bnd5a = new Bounds(3, 4, 2, 1);
        Bounds bnd5b = new Bounds(4, 3, 1, 2);
        Bounds bndN = new Bounds(0, 0, 8, 9);
        Bounds bndAll = bnd1.union(bnd0).union(bnd2).union(bnd3).union(bnd4);

        Bounds bnd6 = Bounds.decode("2, 3;  4,  5");
        Bounds bnd6a = new Bounds(2, 3, 4, 5);

        Bounds bnd7 = bnd6a.enlarge(2);
        Bounds bnd7a = bnd6a.enlarge(1, 3);

        assertEquals(Bounds.EMPTY, bnd0);
        assertNotEquals(bndMinus1, bnd0);
        assertNotEquals(bnd0, bnd1);
        assertEquals(bnd10a, bnd10b);

        assertEquals(bnd10.getLocation(), p10);
        assertEquals(bnd10.getSize(), dim1);
        assertEquals(bnd2, bnd3);
        assertEquals(bnd3, bnd3a);

        assertEquals(dim0, bndMinus1.getSize());
        assertEquals(dim0, bnd0.getSize());
        assertEquals(dim1, bnd1.getSize());

        assertFalse(bnd1.contains(bnd0));

        assertFalse(bndMinus1.intersects(bnd0));
        assertFalse(bnd0.intersects(bnd1));
        assertEquals(new Bounds(1, 1, -1, -1), bnd0.intersect(bnd1));
        assertTrue(bnd5a.intersects(bnd5b));
        assertTrue(bnd0.union(bnd1).equals(new Bounds(0, 0, 2, 2)));

        assertFalse(bnd0.equals(bnd1));

        assertTrue(bnd5.equals(bnd5a));
        assertEquals(bndN, bndAll);

        assertEquals(bnd6, bnd6a);
        assertEquals("Bounds [2,3;4x5]", bnd6a.toString());

        assertEquals("Bounds [0,1;8x9]", bnd7.toString());
        assertEquals("Bounds [1,0;6x11]", bnd7a.toString());
    }

}
