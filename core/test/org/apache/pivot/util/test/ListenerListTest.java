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

import org.apache.pivot.util.ListenerList;

public class ListenerListTest {
    private interface TestListener {
        void changed();
    }

    private class NumberedListener implements TestListener {
        private int which;

        public NumberedListener(int w) {
            this.which = w;
        }

        public int whichOne() {
            return this.which;
        }

        @Override
        public void changed() {
            System.out.println("NumberedListener(" + which + ").changed() called");
        }
    };

    private class TestListenerList extends ListenerList<TestListener>
                implements TestListener {
        @Override
        public void changed() {
            forEach(listener -> listener.changed());
        }
    }

    @Test
    public void test1() {
        TestListenerList listeners = new TestListenerList();
        assertEquals(listeners.isEmpty(), true);

        TestListener l1 = new NumberedListener(1);
        listeners.add(l1);
        assertEquals(listeners.getLength(), 1);
        assertEquals(l1, listeners.get(0));

        TestListener l2 = new NumberedListener(2);
        listeners.add(0, l2);
        assertEquals(listeners.getLength(), 2);
        assertEquals(l2, listeners.get(0));
        assertEquals(l1, listeners.get(1));

        TestListener l3 = new NumberedListener(3);
        listeners.add(1, l3);
        assertEquals(listeners.getLength(), 3);
        assertEquals(l2, listeners.get(0));
        assertEquals(l3, listeners.get(1));
        assertEquals(l1, listeners.get(2));

        TestListener l4 = new NumberedListener(4);
        listeners.add(l4);
        assertEquals(listeners.getLength(), 4);
        assertEquals(l2, listeners.get(0));
        assertEquals(l3, listeners.get(1));
        assertEquals(l1, listeners.get(2));
        assertEquals(l4, listeners.get(3));

        listeners.changed();

        listeners.remove(l1);
        assertEquals(listeners.getLength(), 3);
        assertEquals(l2, listeners.get(0));
        assertEquals(l3, listeners.get(1));
        assertEquals(l4, listeners.get(2));

        listeners.changed();
    }

    private static final int LIMIT = 30;
    private static final int[] EXPECTED_VALUES = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 32, 17, 34, 31,
        36, 33, 38, 35, 40, 37, 42, 39, 44, 41,
        46, 43, 48, 45, 50, 47, 52, 49, 54, 51,
        56, 53, 58, 55, 60, 57, 59, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30
    };

    @Test
    public void test2() {
        TestListenerList listeners = new TestListenerList();

        for (int i = 1; i <= LIMIT; i++) {
            TestListener listener = new NumberedListener(i);
            listeners.add(listener);
        }
        assertEquals(listeners.getLength(), LIMIT);

        for (int i = 1; i <= LIMIT; i++) {
            TestListener listener = new NumberedListener(i + LIMIT);
            int index = (((i % 2) == 0) ? i - 1 : i + 1) + (LIMIT / 2);
            listeners.add(index, listener);
        }
        assertEquals(listeners.getLength(), LIMIT * 2);

        listeners.changed();

        for (int i = 0; i < EXPECTED_VALUES.length; i++) {
            NumberedListener listener = (NumberedListener) listeners.get(i);
            assertEquals(listener.whichOne(), EXPECTED_VALUES[i]);
        }
    }

}

