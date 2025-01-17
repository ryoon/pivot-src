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

import java.text.DateFormat;
import java.util.Locale;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Style;

/**
 * Default calendar button data renderer.
 */
public class CalendarButtonDataRenderer extends ButtonDataRenderer {
    public CalendarButtonDataRenderer() {
        putStyle(Style.horizontalAlignment, HorizontalAlignment.LEFT);
    }

    @Override
    public void render(final Object data, final Button button, boolean highlight) {
        Object dataMutable = data;
        CalendarButton calendarButton = (CalendarButton) button;
        Locale locale = calendarButton.getLocale();

        if (dataMutable == null) {
            dataMutable = "";
        } else {
            if (dataMutable instanceof CalendarDate) {
                CalendarDate date = (CalendarDate) dataMutable;

                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
                dataMutable = dateFormat.format(date.toCalendar().getTime());
            }
        }

        super.render(dataMutable, button, highlight);
    }
}
