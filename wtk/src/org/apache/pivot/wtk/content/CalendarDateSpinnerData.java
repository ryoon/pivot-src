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
package org.apache.pivot.wtk.content;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.ReadOnlySequence;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Spinner data model that presents a bounded list of calendar dates. <p> This
 * is a lightweight class that spoofs the actual list data by using an internal
 * calendar instance from which <tt>CalendarDate</tt> instances are created on
 * demand.
 */
public class CalendarDateSpinnerData extends ReadOnlySequence<CalendarDate> implements List<CalendarDate> {
    private static final long serialVersionUID = 8422963466022375674L;

    /**
     * Iterator that simply wraps calls to the list. Since the internal list
     * data is spoofed, each accessor runs in constant time, so there's no
     * performance hit in making the iterator delegate its implementation to the
     * list.
     */
    private class DataIterator implements Iterator<CalendarDate> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return (index < length);
        }

        @Override
        public CalendarDate next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return get(index++);
        }

        @UnsupportedOperation
        @Override
        public void remove() {
            throw defaultException();
        }
    }

    /** The current date of this set of data (offset by {@link #calendarIndex} from the base date of the range). */
    private CalendarDate currentDate;
    /** The current offset from the base date. */
    private int calendarIndex;

    // Calculated during construction
    private transient int length;

    private transient ListListenerList<CalendarDate> listListeners = new ListListenerList<>();

    /**
     * Creates a new <tt>CalendarDateSpinnerData</tt> bounded from
     * <tt>1900-01-01</tt> to <tt>2099-12-31</tt>.
     */
    public CalendarDateSpinnerData() {
        this(new CalendarDate(1900, 0, 0), new CalendarDate(2099, 11, 30));
    }

    /**
     * Creates a new <tt>CalendarDateSpinnerData</tt> bounded by the specified
     * calendar dates (inclusive).
     *
     * @param lowerBound The earliest date to include in this spinner data.
     * @param upperBound The latest date to include in this spinner data.
     */
    public CalendarDateSpinnerData(final CalendarDate lowerBound, final CalendarDate upperBound) {
        Utils.checkNull(lowerBound, "lowerBound");
        Utils.checkNull(upperBound, "upperBound");

        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lowerBound is after upperBound.");
        }

        currentDate = lowerBound;
        calendarIndex = 0;

        // Calculate our length and cache it, since it is guaranteed to remain fixed
        length = upperBound.subtract(lowerBound) + 1;
    }

    /**
     * Gets the calendar date at the specified index.
     *
     * @param index The index of the calendar date to retrieve.
     */
    @Override
    public final CalendarDate get(final int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        // Move the calendar's fields to match the specified index
        currentDate = currentDate.add(index - calendarIndex);
        calendarIndex = index;

        return currentDate;
    }

    @Override
    public final int indexOf(final CalendarDate item) {
        int indexDiffDays = item.subtract(this.currentDate);
        int index = calendarIndex + indexDiffDays;

        return (index < 0 || index >= length) ? -1 : index;
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @UnsupportedOperation
    @Override
    public void clear() {
        throw defaultException();
    }

    @Override
    public final boolean isEmpty() {
        return (length == 0);
    }

    /**
     * Gets the number of entries in this list.
     *
     * @return The number of calendar dates in this list.
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * Gets the comparator for this list, which is guaranteed to always be
     * <tt>null</tt>. The generated data is inherently in date order, thus
     * sorting doesn't make sense.
     */
    @Override
    public Comparator<CalendarDate> getComparator() {
        return null;
    }

    /**
     * Throws {@link UnsupportedOperationException} because the generated data
     * is inherently in date order, thus sorting makes no sense.
     */
    @UnsupportedOperation
    @Override
    public final void setComparator(final Comparator<CalendarDate> comparator) {
        throw defaultException();
    }

    @Override
    public final Iterator<CalendarDate> iterator() {
        return new DataIterator();
    }

    @Override
    public final ListenerList<ListListener<CalendarDate>> getListListeners() {
        return listListeners;
    }
}
