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

/**
 * Calender button binding listener interface.
 */
public interface CalendarButtonBindingListener {
    /**
     * Calendar button binding listener adapter.
     */
    public static class Adapter implements CalendarButtonBindingListener {
        @Override
        public void selectedDateKeyChanged(CalendarButton calendarButton,
            String previousSelectedDateKey) {
        }

        @Override
        public void selectedDateBindTypeChanged(CalendarButton calendarButton,
            BindType previousSelectedDateBindType) {
        }

        @Override
        public void selectedDateBindMappingChanged(CalendarButton calendarButton,
            Calendar.SelectedDateBindMapping previousSelectedDateBindMapping) {
        }
    }

    /**
     * Called when a calendar button's selected date key has changed.
     *
     * @param calendarButton
     * @param previousSelectedDateKey
     */
    public void selectedDateKeyChanged(CalendarButton calendarButton,
        String previousSelectedDateKey);

    /**
     * Called when a calendar button's selected date bind type has changed.
     *
     * @param calendarButton
     * @param previousSelectedDateBindType
     */
    public void selectedDateBindTypeChanged(CalendarButton calendarButton,
        BindType previousSelectedDateBindType);

    /**
     * Called when a calendar button's selected date bind mapping has changed.
     *
     * @param calendarButton
     * @param previousSelectedDateBindMapping
     */
    public void selectedDateBindMappingChanged(CalendarButton calendarButton,
        Calendar.SelectedDateBindMapping previousSelectedDateBindMapping);

}
