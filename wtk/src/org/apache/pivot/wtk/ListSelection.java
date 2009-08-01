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
package org.apache.pivot.wtk;

import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;

/**
 * Class for managing a set of indexed range selections.
 *
 * @author gbrown
 */
class ListSelection {
    private ArrayList<Span> selectedRanges = new ArrayList<Span>();

    /**
     * Comparator that determines the index of the first intersecting range.
     */
    public static final Comparator<Span> START_COMPARATOR = new Comparator<Span>() {
        public int compare(Span range1, Span range2) {
            return (range1.end - range2.start);
        }
    };

    /**
     * Comparator that determines the index of the last intersecting range.
     */
    public static final Comparator<Span> END_COMPARATOR = new Comparator<Span>() {
        public int compare(Span range1, Span range2) {
            return (range1.start - range2.end);
        }
    };

    /**
     * Comparator that determines if two ranges intersect.
     */
    public static final Comparator<Span> INTERSECTION_COMPARATOR = new Comparator<Span>() {
        public int compare(Span range1, Span range2) {
            return (range1.start > range2.end) ? 1 : (range2.start > range1.end) ? -1 : 0;
        }
    };

    /**
     * Adds a range to the selection, merging and removing intersecting ranges
     * as needed.
     *
     * @param start
     * @param end
     *
     * @return
     * A sequence containing the ranges that were added.
     */
    public Sequence<Span> addRange(int start, int end) {
        assert(start >= 0);
        assert(end >= start);

        ArrayList<Span> added = new ArrayList<Span>();

        Span range = new Span(start, end);

        if (range.getLength() > 0) {
            int n = selectedRanges.getLength();

            if (n == 0) {
                // The selection is currently empty; append the new range
                selectedRanges.add(range);
                added.add(range);
            } else {
                // Locate the lower bound of the intersection
                int i = ArrayList.binarySearch(selectedRanges, range, START_COMPARATOR);
                if (i < 0) {
                    i = -(i + 1);
                }

                // Merge the selection with the previous range, if necessary
                if (i > 0) {
                    Span previousRange = selectedRanges.get(i - 1);
                    if (range.start == previousRange.end + 1) {
                        i--;
                    }
                }

                if (i == n) {
                    // The new range starts after the last existing selection
                    // ends; append
                    selectedRanges.add(range);
                    added.add(range);
                } else {
                    // Locate the upper bound of the intersection
                    int j = ArrayList.binarySearch(selectedRanges, range, END_COMPARATOR);
                    if (j < 0) {
                        j = -(j + 1);
                    } else {
                        j++;
                    }

                    // Merge the selection with the next range, if necessary
                    if (j < n) {
                        Span nextRange = selectedRanges.get(j);
                        if (range.end == nextRange.start - 1) {
                            j++;
                        }
                    }

                    if (i == j) {
                        selectedRanges.insert(range, i);
                        added.add(range);
                    } else {
                        Span lowerRange = selectedRanges.get(i);
                        Span upperRange = selectedRanges.get(j - 1);

                        // Remove all redundant ranges
                        // TODO Add the gaps to the added list
                        if (i < j) {
                            selectedRanges.remove(i + 1, j - i - 1);
                        }

                        // Create a new range representing the union of the intersecting ranges
                        range = new Span(Math.min(range.start, lowerRange.start),
                            Math.max(range.end, upperRange.end));

                        selectedRanges.update(i, range);
                    }
                }
            }
        }

        return added;
    }

    /**
     * Removes a range from the selection, truncating and removing intersecting
     * ranges as needed.
     *
     * @param start
     * @param end
     *
     * @return
     * A sequence containing the ranges that were removed.
     */
    public Sequence<Span> removeRange(int start, int end) {
        assert(start >= 0);
        assert(end >= start);

        ArrayList<Span> removed = new ArrayList<Span>();

        Span range = new Span(start, end);

        if (range.getLength() > 0) {
            int n = selectedRanges.getLength();

            if (n > 0) {
                // Locate the lower bound of the intersection
                int i = ArrayList.binarySearch(selectedRanges, range, START_COMPARATOR);
                if (i < 0) {
                    i = -(i + 1);
                }

                Span lowerRange = selectedRanges.get(i);

                if (lowerRange.start < range.start
                    && lowerRange.end > range.end) {
                    // Removing the range will split the intersecting selection
                    // into two ranges
                    selectedRanges.update(i, new Span(lowerRange.start, range.start - 1));
                    selectedRanges.insert(new Span(range.end + 1, lowerRange.end), i + 1);
                    removed.add(range);
                } else {
                    if (range.start > lowerRange.start) {
                        // Remove the tail of this range
                        // TODO Add removed tail to removed list
                        selectedRanges.update(i, new Span(lowerRange.start, range.start - 1));
                        i++;
                    }

                    // Locate the upper bound of the intersection
                    int j = ArrayList.binarySearch(selectedRanges, range, END_COMPARATOR);
                    if (j < 0) {
                        j = -(j + 1);
                    } else {
                        j++;
                    }

                    Span upperRange = selectedRanges.get(j - 1);

                    if (range.end < upperRange.end) {
                        // Remove the head of this range;
                        // TODO Add removed head to removed list
                        selectedRanges.update(j, new Span(range.end + 1, upperRange.end));
                        j--;
                    }

                    // Remove all cleared ranges
                    // TODO Add the removed ranges to the removed list
                    selectedRanges.remove(i, j - i);
                }
            }
        }

        return removed;
    }

    public void clear() {
        selectedRanges.clear();
    }

    /**
     * Returns the range at a given index.
     *
     * @param index
     */
    public Span get(int index) {
        return selectedRanges.get(index);
    }

    /**
     * Returns the number of ranges in the selection.
     */
    public int getLength() {
        return selectedRanges.getLength();
    }

    /**
     * Determines the index of a range in the selection.
     *
     * @param range
     *
     * @return
     * The index of the range, if it exists in the selection; <tt>-1</tt>,
     * otherwise.
     */
    public int indexOf(Span range) {
        assert (range != null);

        int index = -1;
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);

        if (i >= 0) {
            index = (range.equals(selectedRanges.get(i))) ? i : -1;
        }

        return index;
    }

    /**
     * Tests for the presence of an index in the selection.
     *
     * @param index
     *
     * @return
     * <tt>true</tt> if the index is selected; <tt>false</tt>, otherwise.
     */
    public boolean containsIndex(int index) {
        Span range = new Span(index, index);
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);

        return (i >= 0);
    }

    /**
     * Inserts an index into the span sequence (e.g. when items are inserted
     * into the model data).
     *
     * @param index
     */
    public void insertIndex(int index) {
        // Get the insertion point for the range corresponding to the given index
        Span range = new Span(index, index);
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);

        if (i < 0) {
            // The inserted index does not intersect with a selected range
            i = -(i + 1);
        } else {
            // The inserted index intersects with a currently selected range
            Span selectedRange = selectedRanges.get(i);

            // If the inserted index falls within the current range, increment
            // the endpoint only
            if (selectedRange.start < index) {
                selectedRanges.update(i, new Span(selectedRange.start, selectedRange.end + 1));

                // Start incrementing range bounds beginning at the next range
                i++;
            }
        }

        // Increment any subsequent selection indexes
        int n = selectedRanges.getLength();
        while (i < n) {
            Span selectedRange = selectedRanges.get(i);
            selectedRanges.update(i, new Span(selectedRange.start + 1, selectedRange.end + 1));
            i++;
        }
    }

    /**
     * Removes a range of indexes from the span sequence (e.g. when items
     * are removed from the model data).
     *
     * @param index
     * @param count
     */
    public void removeIndexes(int index, int count) {
        // Clear any selections in the given range
        removeRange(index, (index + count) - 1);

        // Decrement any subsequent selection indexes
        Span range = new Span(index, index);
        int i = ArrayList.binarySearch(selectedRanges, range, INTERSECTION_COMPARATOR);
        assert (i < 0) : "i should be negative, since index should no longer be selected";

        i = -(i + 1);

        // Determine the number of ranges to modify
        int n = selectedRanges.getLength();
        while (i < n) {
            Span selectedRange = selectedRanges.get(i);
            selectedRanges.update(i, new Span(selectedRange.start - count, selectedRange.end - count));
            i++;
        }
    }
}
