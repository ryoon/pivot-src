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
package org.apache.pivot.collections.test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.pivot.collections.EnumList;
import org.junit.Test;

public class EnumListTest {
    public enum TestEnum {
        A, B, C
    }

    @Test
    public void basicTest() {
        EnumList<TestEnum> enumList = new EnumList<>(TestEnum.class);

        assertEquals(enumList.get(0), TestEnum.A);
        assertEquals(enumList.get(1), TestEnum.B);
        assertEquals(enumList.getLength(), 3);

        // Test some of the "immutable" properties of the enum list
        assertNull(enumList.getComparator());
        try {
            enumList.setComparator((o1, o2) -> o1.ordinal() - o2.ordinal());
            fail("Should have thrown an UnsupportedOperationException before this!");
        } catch (UnsupportedOperationException uoe) {
            System.out.println("Caught expected exception: " + uoe.getMessage());
            uoe.printStackTrace();
        }
        for (Iterator<TestEnum> iter = enumList.iterator(); iter.hasNext();) {
            TestEnum e = iter.next();
            try {
                iter.remove();
                fail("Should have thrown UnsupportedOperationException before this!");
            } catch (UnsupportedOperationException uoe) {
                System.out.println("Caught expected exception from iter.remove()");
            }
        }
    }
}
