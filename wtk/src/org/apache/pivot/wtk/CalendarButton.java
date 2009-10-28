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

import java.util.Locale;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.CalendarButtonDataRenderer;


/**
 * A component that allows a user to select a calendar date. The calendar
 * is hidden until the user pushes the button.
 */
public class CalendarButton extends Button {
    /**
     * Calendar button listener list.
     */
    private static class CalendarButtonListenerList
        extends ListenerList<CalendarButtonListener>
        implements CalendarButtonListener {
        @Override
        public void localeChanged(CalendarButton calendarButton, Locale previousLocale) {
            for (CalendarButtonListener listener : this) {
                listener.localeChanged(calendarButton, previousLocale);
            }
        }

        @Override
        public void disabledDateFilterChanged(CalendarButton calendarButton,
            Filter<CalendarDate> previousDisabledDateFilter) {
            for (CalendarButtonListener listener : this) {
                listener.disabledDateFilterChanged(calendarButton, previousDisabledDateFilter);
            }
        }

        @Override
        public void selectedDateKeyChanged(CalendarButton calendarButton,
            String previousSelectedDateKey) {
            for (CalendarButtonListener listener : this) {
                listener.selectedDateKeyChanged(calendarButton, previousSelectedDateKey);
            }
        }
    }

    /**
     * Calendar button selection listener list.
     */
    private static class CalendarButtonSelectionListenerList
        extends ListenerList<CalendarButtonSelectionListener>
        implements CalendarButtonSelectionListener {

        @Override
        public void selectedDateChanged(CalendarButton calendarButton,
            CalendarDate previousSelectedDate) {
            for (CalendarButtonSelectionListener listener : this) {
                listener.selectedDateChanged(calendarButton, previousSelectedDate);
            }
        }
    }

    private CalendarDate selectedDate = null;
    private Locale locale = Locale.getDefault();
    private Filter<CalendarDate> disabledDateFilter = null;
    private String selectedDateKey = null;

    private CalendarButtonListenerList calendarButtonListeners =
        new CalendarButtonListenerList();
    private CalendarButtonSelectionListenerList calendarButtonSelectionListeners =
        new CalendarButtonSelectionListenerList();

    public static final String LANGUAGE_KEY = "language";
    public static final String COUNTRY_KEY = "country";
    public static final String VARIANT_KEY = "variant";

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new CalendarButtonDataRenderer();

    public CalendarButton() {
        this(null);
    }

    public CalendarButton(Object buttonData) {
        super(buttonData);

        setDataRenderer(DEFAULT_DATA_RENDERER);
        installThemeSkin(CalendarButton.class);

        setSelectedDate(new CalendarDate());
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported by CalendarButton.
     */
    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Calendar buttons cannot be toggle buttons.");
    }

    /**
     * Returns the currently selected date.
     *
     * @return
     * The currently selected date, or <tt>null</tt> if nothing is selected.
     */
    public CalendarDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets the selected date.
     *
     * @param selectedDate
     * The date to select, or <tt>null</tt> to clear the selection.
     */
    public void setSelectedDate(CalendarDate selectedDate) {
        CalendarDate previousSelectedDate = this.selectedDate;

        if (((previousSelectedDate == null) ^ (selectedDate == null))
            || (previousSelectedDate != null
                && !previousSelectedDate.equals(selectedDate))) {
            this.selectedDate = selectedDate;
            calendarButtonSelectionListeners.selectedDateChanged(this,
                previousSelectedDate);
        }
    }

    /**
     * Sets the selected date to the date represented by the specified date
     * string. The date string must be in the <tt>ISO 8601</tt> "calendar date"
     * format, which is <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param selectedDate
     * A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g. 2008-07-23)
     */
    public final void setSelectedDate(String selectedDate) {
        if (selectedDate == null) {
            throw new IllegalArgumentException("selectedDate is null.");
        }

        setSelectedDate(CalendarDate.decode(selectedDate));
    }

    /**
     * Returns the locale used to present calendar data.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        Locale previousLocale = this.locale;
        if (previousLocale != locale) {
            this.locale = locale;
            calendarButtonListeners.localeChanged(this, previousLocale);
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale
     * An dictionary containing values for language, country, and variant.
     * Country and variant are optional but the must adhere to the following
     * rules:
     *
     * <ul>
     * <li>If variant is specified, language and country are required;</li>
     * <li>Otherwise, if country is specified, language is required;</li>
     * <li>Otherwise, language is required.</li>
     * </ul>
     */
    public void setLocale(Dictionary<String, ?> locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        String language = (String)locale.get(LANGUAGE_KEY);
        String country = (String)locale.get(COUNTRY_KEY);
        String variant = (String)locale.get(VARIANT_KEY);

        if (variant != null) {
            setLocale(new Locale(language, country, variant));
        } else if (country != null) {
            setLocale(new Locale(language, country));
        } else {
            setLocale(new Locale(language));
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale
     * A JSON map containing values for language, country, and variant.
     *
     * @see #setLocale(Dictionary)
     */
    public void setLocale(String locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        try {
            setLocale(JSONSerializer.parseMap(locale));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public Filter<CalendarDate> getDisabledDateFilter() {
        return disabledDateFilter;
    }

    public void setDisabledDateFilter(Filter<CalendarDate> disabledDateFilter) {
        Filter<CalendarDate> previousDisabledDateFilter = this.disabledDateFilter;

        if (previousDisabledDateFilter != disabledDateFilter) {
            this.disabledDateFilter = disabledDateFilter;
            calendarButtonListeners.disabledDateFilterChanged(this, previousDisabledDateFilter);
        }
    }

    /**
     * Gets the data binding key that is set on this calendar button.
     */
    public String getSelectedDateKey() {
        return selectedDateKey;
    }

    /**
     * Sets this calendar button's data binding key.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        String previousSelectedDateKey = this.selectedDateKey;

        if (previousSelectedDateKey != selectedDateKey) {
            this.selectedDateKey = selectedDateKey;
            calendarButtonListeners.selectedDateKeyChanged(this, previousSelectedDateKey);
        }
    }

    /**
     * Loads the selected date from the specified bind context using this date
     * picker button's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, ?> context) {
        String selectedDateKey = getSelectedDateKey();

        if (selectedDateKey != null
            && JSONSerializer.containsKey(context, selectedDateKey)) {
            Object value = JSONSerializer.get(context, selectedDateKey);

            if (value instanceof CalendarDate) {
                setSelectedDate((CalendarDate)value);
            } else if (value instanceof String) {
                setSelectedDate((String)value);
            } else {
                throw new IllegalArgumentException("Invalid date type: " +
                    value.getClass().getName());
            }
        }
    }

    /**
     * Stores the selected date into the specified bind context using this date
     * picker button's bind key, if one is set.
     */
    @Override
    public void store(Dictionary<String, ?> context) {
        if (isEnabled()
            && selectedDateKey != null) {
            JSONSerializer.put(context, selectedDateKey, selectedDate);
        }
    }

    /**
     * If a bind key is set, clears the selected date.
     */
    @Override
    public void clear() {
        if (selectedDateKey != null) {
            setSelectedDate((CalendarDate)null);
        }
    }

    /**
     * Returns the calendar button listener list.
     */
    public ListenerList<CalendarButtonListener> getCalendarButtonListeners() {
        return calendarButtonListeners;
    }

    /**
     * Returns the calendar button selection listener list.
     */
    public ListenerList<CalendarButtonSelectionListener> getCalendarButtonSelectionListeners() {
        return calendarButtonSelectionListeners;
    }
}
