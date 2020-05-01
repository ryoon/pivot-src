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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.RangeSelection;
import org.apache.pivot.wtk.Span;


public class RangeSelectionTest {
    @Test
    public void test1() {
        RangeSelection r1 = new RangeSelection();
        r1.addRange(10, 1);
        Span s1 = new Span(1, 10);
        assertEquals(1, r1.getLength());
        assertEquals(0, r1.indexOf(s1));

        r1.addRange(10, 20);
        Span s2 = new Span(1, 20);
        assertEquals(1, r1.getLength());
        assertEquals(0, r1.indexOf(s2));

        Span s3 = new Span(30, 40);
        r1.addRange(s3);
        assertEquals(2, r1.getLength());
        assertEquals(1, r1.indexOf(s3));
        assertTrue(r1.containsIndex(35));

        int inserted = r1.insertIndex(29);
        assertEquals(1, inserted);
        assertEquals(2, r1.getLength());

        Sequence<Span> removed = r1.removeRange(20, 30);
        assertEquals(1, removed.getLength());
        assertEquals(2, r1.getLength());
        assertEquals(1, r1.getStart());
        assertEquals(41, r1.getEnd());
        assertFalse(r1.containsIndex(25));
    }

}

