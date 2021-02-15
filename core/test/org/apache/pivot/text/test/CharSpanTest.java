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
package org.apache.pivot.text.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.apache.pivot.collections.ArrayAdapter;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.text.CharSpan;


/**
 * Tests the {@link CharSpan} class and its various constructors and
 * utility methods.
 */
public class CharSpanTest {
    @Test
    public void testConstructors() {
        assertEquals(CharSpan.ZERO, new CharSpan());
        assertEquals(CharSpan.ZERO, new CharSpan(0));
        assertEquals(CharSpan.ZERO, new CharSpan(0, 0));

        assertEquals(new CharSpan(10), new CharSpan(10, 0));
        try {
            assertEquals(new CharSpan(3, 4), new CharSpan(JSONSerializer.parseList("[ 3, 4]")));
            assertEquals(new CharSpan(1, 17), new CharSpan(JSONSerializer.parseMap("{start:1,length:17}")));
        } catch (SerializationException se) {
            fail("Test failure: unexpected exception: " + se.getMessage());
        }

        try {
            new CharSpan(-1, 0);
            fail("Negative start value should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            System.out.println("Caught the expected exception for negative start: " + iae.getMessage());
        }
        try {
            new CharSpan(2, -3);
            fail("Negative length value should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            System.out.println("Caught the expected exception for negative length: " + iae.getMessage());
        }
        try {
            new CharSpan(JSONSerializer.parseMap("{span:11}"));
            fail("Missing start value should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            System.out.println("Caught the expected exception for missing start: " + iae.getMessage());
        } catch (SerializationException se) {
            fail("Test failure: unexpected exception: " + se.getMessage());
        }

        HashMap<String, String> stringMap = new HashMap<>();
        stringMap.put("start", "12");
        stringMap.put("length", "3");
        assertEquals(new CharSpan(12, 3), new CharSpan(stringMap));

        HashMap<String, Long> longMap = new HashMap<>();
        longMap.put("start", 23L);
        longMap.put("length", 14L);
        assertEquals(new CharSpan(23, 14), new CharSpan(longMap));

        ArrayAdapter<Integer> intArray = new ArrayAdapter<Integer>(10, 20);
        assertEquals(new CharSpan(10, 20), new CharSpan(intArray));
    }

    @Test
    public void testDecode() {
        assertEquals(new CharSpan(1, 2), CharSpan.decode("[1, 2]"));
        assertEquals(new CharSpan(15), CharSpan.decode("15"));
        assertEquals(new CharSpan(200), CharSpan.decode("[200]"));
        assertEquals(new CharSpan(20), CharSpan.decode("{start:20}"));
        assertEquals(new CharSpan(10, 5), CharSpan.decode("10, 5"));
        assertEquals(new CharSpan(30, 1), CharSpan.decode("{start:30, length:1}"));
    }

    @Test
    public void testUtilityMethods() {
        CharSpan cs1 = new CharSpan(1);
        assertEquals(cs1.getEnd(), 0);
        CharSpan cs2 = new CharSpan(100, 50);
        assertEquals(cs2.getEnd(), 149);
        assertEquals(cs1.offset(0), cs1);
        assertEquals(cs1.offset(99), new CharSpan(100));
        assertEquals(cs2.lengthen(0), cs2);
        assertEquals(cs2.lengthen(-25), new CharSpan(100, 25));
    }
}

