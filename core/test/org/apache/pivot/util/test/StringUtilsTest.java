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
package org.apache.pivot.util.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.StringUtils;

/**
 * Test the methods in {@link StringUtils}.
 */
public class StringUtilsTest {
    /**
     * Our (tiny) test case.
     */
    private static String[] values = {
        "b", "c", "d", "e", "f", "g"
    };

    /**
     * Run some basic tests.
     */
    @Test
    public void test1() {
        List<String> list = new ArrayList<>();
        String output = StringUtils.toString(list);
        assertEquals("[]", output);

        list.add("a");
        output = StringUtils.toString(list);
        assertEquals("[a]", output);

        list.addAll(values);
        output = StringUtils.toString(list);
        System.out.println("list " + output);
        assertEquals("[a, b, c, d, e, f, g]", output);
    }

}

