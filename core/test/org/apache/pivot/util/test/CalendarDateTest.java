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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Time;


public class CalendarDateTest {
    private static final String D1 = "1941-12-07";
    private static final String D2 = "1929-10-29";
    private static final String D3 = "2008-09-29";
    private static final String D4 = "1945-08-14";
    private static final String D5 = "2019-12-06";
    private static final int DAYS_FROM_D1_TO_D5 = 28_488;

    @Test
    public void test1() {
        CalendarDate.Range r1 = new CalendarDate.Range(D1);
        CalendarDate.Range r1a = new CalendarDate.Range(D1, D1);
        CalendarDate.Range r2 = new CalendarDate.Range(D2, D3);
        CalendarDate.Range r3 = CalendarDate.Range.decode("{ \"start\" : \"1929-10-29\", \"end\" : \"2008-09-29\"}");
        CalendarDate.Range r3a = CalendarDate.Range.decode("[ \"1929-10-29\", \"2008-09-29\" ]");
        CalendarDate.Range r3b = CalendarDate.Range.decode("1929-10-29, 2008-09-29");

        CalendarDate cd1 = CalendarDate.decode(D1);
        CalendarDate cd2 = CalendarDate.decode(D2);
        CalendarDate cd3 = CalendarDate.decode(D3);

        assertTrue(r2.contains(r1));
        assertEquals(r1, r1a);
        assertEquals(r1.getLength(), 1);
        assertTrue(r2.normalize().equals(r2));
        // TODO: more tests of range methods: intersects, etc.

        assertEquals(r3, r3a);
        assertEquals(r3, r3b);
        assertEquals(r3a, r3b);

        assertEquals(cd1.year, 1941);
        assertEquals(cd1.month, 11);
        assertEquals(cd1.day, 6);
        assertEquals(cd1.toString(), D1);
    }

    @Test
    public void test2() {
        // PIVOT-1010: test interaction with LocalDate, etc. (new Java 8 classes)
        LocalDate ld1 = LocalDate.of(1941, 12, 7);
        CalendarDate cd1 = new CalendarDate(ld1);
        CalendarDate cd1a = CalendarDate.decode(D1);
        LocalDate ld1a = cd1a.toLocalDate();

        assertEquals(cd1, cd1a);
        assertEquals(ld1, ld1a);

        Time t1 = Time.decode("07:48:00");
        LocalDateTime dt1 = LocalDateTime.of(1941, 12, 7, 7, 48, 0);
        LocalDateTime dt1a = cd1.toLocalDateTime(t1);

        assertEquals(dt1, dt1a);
    }

    @Test
    public void test3() {
        // Testing new stuff in CalendarDate that tries to deal with time zones
        // more effectively
        TimeZone gmtZone = CalendarDate.TIMEZONE_GMT;
        TimeZone pstZone = TimeZone.getTimeZone("America/Los_Angeles");
        CalendarDate d1 = new CalendarDate(1941, 11, 6, gmtZone);
        CalendarDate d2 = new CalendarDate(1941, 11, 6, pstZone);
        CalendarDate d3 = new CalendarDate(1945, 7, 13, gmtZone);
        CalendarDate d4 = d1.add(1346);
        CalendarDate d5 = d2.add(1346);

        // First we should establish that dates don't depend on timezone for equality
        // nor do their string representations
        assertTrue(d1.equals(d2));
        assertEquals(d1.toString(), d2.toString());

        // Now, establish whether (or not) timezones might make a difference in durations
        assertEquals(d3.subtract(d1), 1346);
        // Surprise! they do!
        assertEquals(d3.subtract(d2), 1345);
        assertEquals(d4, d5);
    }

    @Test
    public void test4() {
        // Now let's test some of the other duration-related methods
        CalendarDate d1 = CalendarDate.decode(D1);
        CalendarDate d2 = d1.addMonths(44).add(7);
        CalendarDate d3 = d1.addYears(3).addMonths(8).add(7);
        CalendarDate d4 = CalendarDate.decode(D4);
        CalendarDate d5 = CalendarDate.decode(D5);

        assertEquals(d2, d3);
        assertEquals(d2, d4);
        assertEquals(d3, d4);

        assertEquals(d5.subtract(d1), DAYS_FROM_D1_TO_D5);
        assertEquals(d1.add(DAYS_FROM_D1_TO_D5), d5);
        assertEquals(d1.add(DAYS_FROM_D1_TO_D5).add(-DAYS_FROM_D1_TO_D5), d1);
    }
}
