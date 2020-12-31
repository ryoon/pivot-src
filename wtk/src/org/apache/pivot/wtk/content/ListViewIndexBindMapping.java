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

import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.ListView;


/**
 * A {@link ListView.ItemBindMapping} that loads and stores just
 * the index itself instead of the selected item.  This is a convenience
 * class for users where the data stored is just the index of the item.
 */
public class ListViewIndexBindMapping implements ListView.ItemBindMapping {

    /**
     * Called during {@code load}, and {@code value} is what is
     * stored in our data object (which is the {@code Integer}
     * index value).
     *
     * @param listData The {@code ListView}'s data list.
     * @param value The object value to map to an index in this list
     * (which is an {@code Integer} value).
     * @return The value converted to an integer, or <code>-1</code>
     * if the value is out of range of the list size.
     */
    @Override
    public int indexOf(final List<?> listData, final Object value) {
        if (value instanceof Number) {
            int iValue = ((Number) value).intValue();
            if (iValue >= -1 && iValue < listData.getLength()) {
                return iValue;
            }
        }
        return -1;
    }

    /**
     * Called during {@code store}, and {@code index} is the
     * selected item index.  We are going to just return an
     * {@code Integer} representing the index itself.
     *
     * @param listData The underlying data for the {@code ListView}.
     * @param index The index value to convert to a "storable" value.
     * @return The {@code Integer} value of the index.
     */
    @Override
    public Object get(final List<?> listData, final int index) {
        return Integer.valueOf(index);
    }

}
