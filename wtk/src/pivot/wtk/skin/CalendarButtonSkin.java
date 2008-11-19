/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin;

import java.util.Locale;

import pivot.util.CalendarDate;
import pivot.wtk.Button;
import pivot.wtk.Calendar;
import pivot.wtk.CalendarButton;
import pivot.wtk.CalendarButtonListener;
import pivot.wtk.CalendarButtonSelectionListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Direction;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;

/**
 * Abstract base class for calendar button skins.
 * <p>
 * TODO Rather than blindly closing when a mouse down is received, we could
 * instead cache the selection state in the popup's container mouse down event
 * and compare it to the current state in component mouse down. If different,
 * we close the popup. This would also tie this base class less tightly to its
 * concrete subclasses.
 *
 * @author gbrown
 */
public abstract class CalendarButtonSkin extends ButtonSkin
    implements CalendarButtonListener, CalendarButtonSelectionListener {
    protected Calendar calendar;
    protected Popup calendarPopup;

    protected boolean pressed = false;

    private ComponentKeyListener calendarPopupKeyListener = new ComponentKeyListener() {
        public void keyTyped(Component component, char character) {
            // No-op
        }

        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            switch (keyCode) {
                case Keyboard.KeyCode.ESCAPE: {
                    calendarPopup.close();
                    getComponent().requestFocus();
                    break;
                }

                case Keyboard.KeyCode.TAB:
                case Keyboard.KeyCode.ENTER: {
                    CalendarButton calendarButton = (CalendarButton)getComponent();

                    CalendarDate date = calendar.getSelectedDate();

                    calendar.setSelectedDate((CalendarDate)null);
                    calendarButton.setSelectedDate(date);

                    calendarPopup.close();

                    if (keyCode == Keyboard.KeyCode.TAB) {
                        Direction direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                            Direction.BACKWARD : Direction.FORWARD;
                        calendarButton.transferFocus(direction);
                    } else {
                        calendarButton.requestFocus();
                    }

                    break;
                }
            }

            return false;
        }

        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }
    };

    private ComponentMouseButtonListener calendarPopupMouseListener = new ComponentMouseButtonListener() {
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            CalendarButton calendarButton = (CalendarButton)getComponent();

            CalendarDate date = calendar.getSelectedDate();

            calendar.setSelectedDate((CalendarDate)null);
            calendarButton.setSelectedDate(date);

            calendarPopup.close();
            getComponent().requestFocus();
        }
    };

    public CalendarButtonSkin() {
        calendar = new Calendar();

        calendarPopup = new Popup();
        calendarPopup.getComponentMouseButtonListeners().add(calendarPopupMouseListener);
        calendarPopup.getComponentKeyListeners().add(calendarPopupKeyListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        CalendarButton calendarButton = (CalendarButton)component;
        calendarButton.getCalendarButtonListeners().add(this);
        calendarButton.getCalendarButtonSelectionListeners().add(this);

        calendar.setYear(calendarButton.getYear());
        calendar.setMonth(calendarButton.getMonth());
        calendar.setLocale(calendarButton.getLocale());
    }

    @Override
    public void uninstall() {
        calendarPopup.close();

        CalendarButton calendarButton = (CalendarButton)getComponent();
        calendarButton.getCalendarButtonListeners().remove(this);
        calendarButton.getCalendarButtonSelectionListeners().remove(this);

        super.uninstall();
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        calendarPopup.close();
        pressed = false;
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()
            && !calendarPopup.containsFocus()) {
            calendarPopup.close();
        }

        pressed = false;
    }

    // Component mouse events
    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        pressed = false;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        CalendarButton calendarButton = (CalendarButton)getComponent();

        calendarButton.requestFocus();
        calendarButton.press();

        if (calendar.isShowing()) {
            calendar.requestFocus();
        }
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        CalendarButton calendarButton = (CalendarButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            calendarButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // Button events
    @Override
    public void buttonPressed(Button button) {
        if (calendarPopup.isOpen()) {
            calendarPopup.close();
        } else {
            CalendarButton calendarButton = (CalendarButton)button;

            // Determine the popup's location and preferred size, relative
            // to the button
            Display display = calendarButton.getDisplay();

            if (display != null) {
                int width = getWidth();
                int height = getHeight();

                Component content = calendarPopup.getContent();

                // Ensure that the popup remains within the bounds of the display
                Point buttonLocation = calendarButton.mapPointToAncestor(display, 0, 0);

                Dimensions displaySize = display.getSize();
                Dimensions popupSize = content.getPreferredSize();

                int x = buttonLocation.x;
                if (popupSize.width > width
                    && x + popupSize.width > displaySize.width) {
                    x = buttonLocation.x + width - popupSize.width;
                }

                int y = buttonLocation.y + height - 1;
                if (y + popupSize.height > displaySize.height) {
                    if (buttonLocation.y - popupSize.height > 0) {
                        y = buttonLocation.y - popupSize.height + 1;
                    } else {
                        popupSize.height = displaySize.height - y;
                    }
                } else {
                    popupSize.height = -1;
                }

                calendarPopup.setLocation(x, y);
                calendarPopup.setPreferredSize(popupSize);
                calendarPopup.open(calendarButton);

                calendar.requestFocus();
            }
        }
    }

    // Calendar button events
    public void yearChanged(CalendarButton calendarButton, int previousYear) {
        calendar.setYear(calendarButton.getYear());
    }

    public void monthChanged(CalendarButton calendarButton, int previousMonth) {
        calendar.setMonth(calendarButton.getMonth());
    }

    public void selectedDateKeyChanged(CalendarButton calendarButton,
        String previousSelectedDateKey) {
        // No-op
    }

    public void localeChanged(CalendarButton calendarButton, Locale previousLocale) {
        calendar.setLocale(calendarButton.getLocale());
        invalidateComponent();
    }

    // Calendar button selection events
    public void selectedDateChanged(CalendarButton calendarButton,
        CalendarDate previousSelectedDate) {
        // Set the selected date as the button data
        CalendarDate date = calendarButton.getSelectedDate();
        calendarButton.setButtonData(date);

        calendar.setSelectedDate(date);
    }
}
