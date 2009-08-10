package org.apache.pivot.collections.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.junit.Test;

public class HashMapTest {

    @Test
    public void basicTest() {

        HashMap<String, Integer> map = new HashMap<String, Integer>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.count());
        assertNull(map.getComparator());
        assertFalse(map.containsKey("a"));
        assertNotNull(map.getMapListeners());
        assertNotNull(map.toString());

        assertNull(map.put("a", Integer.valueOf(1)));

        assertEquals(1, map.put("a", 2));

        assertEquals(2, map.get("a"));

        assertEquals(1, map.count());

        assertEquals(2, map.remove("a"));

        assertEquals(0, map.count());

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));

        assertEquals(3, map.count());

        Iterator<String> iter = map.iterator();
        int count = 0;
        while (iter.hasNext()) {
            String s = iter.next();
            if (!map.containsKey(s)) {
                fail("Unknown element in map " + s);
            }

            count++;
        }
        assertEquals(3, count);

        try {
            iter.remove();
            fail("Expecting " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException ex) {
            // ignore, we're expecting this as part of the test
        }

        map.clear();

        assertEquals(0, map.count());

        assertEquals(null, map.get("a"));
        assertEquals(null, map.get("b"));
        assertEquals(null, map.get("c"));
    }

    @Test
    public void constructorTests() throws Exception {
        // don't like the warning generated here ...
        HashMap<String, Integer> map = new HashMap<String, Integer>(
                new Map.Pair<String, Integer>("a", 1),
                new Map.Pair<String, Integer>("b", 2));
        assertEquals(2, map.count());

        map = new HashMap<String, Integer>(true, map);
        assertEquals(2, map.count());

        map = new HashMap<String, Integer>(map);
        assertEquals(2, map.count());

    }

    @Test
    public void exceptionTests() throws Exception {
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };

        try {
            new HashMap<String, Integer>(comparator);
            fail("Expected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            // ignore, we're expecting this as part of the test
        }

        try {
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            map.setComparator(comparator);
            fail("Expected " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            // ignore, we're expecting this as part of the test
        }
    }

}
