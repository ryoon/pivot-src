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

import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Menu;
import pivot.wtk.MenuButton;
import pivot.wtk.MenuButtonListener;
import pivot.wtk.MenuPopup;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Window;
import pivot.wtk.WindowStateListener;

/**
 * Abstract base class for menu button skins.
 *
 * @author gbrown
 */
public abstract class MenuButtonSkin extends ButtonSkin
    implements MenuButtonListener {
    protected boolean pressed = false;
    protected MenuPopup menuPopup = new MenuPopup();

    public MenuButtonSkin() {
        menuPopup.getWindowStateListeners().add(new WindowStateListener() {
            public boolean previewWindowOpen(Window window, Display display) {
                return true;
            }

            public void windowOpenVetoed(Window window) {
                // No-op
            }

            public void windowOpened(Window window) {
                // No-op
            }

            public boolean previewWindowClose(Window window) {
                return true;
            }

            public void windowCloseVetoed(Window window) {
                // No-op
            }

            public void windowClosed(Window window, Display display) {
                getComponent().requestFocus();
            }
        });
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuButton menuButton = (MenuButton)getComponent();
        menuButton.getMenuButtonListeners().add(this);
    }

    @Override
    public void uninstall() {
        MenuButton menuButton = (MenuButton)getComponent();
        menuButton.getMenuButtonListeners().remove(this);

        super.uninstall();
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        menuPopup.close();
        pressed = false;
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()
            && !menuPopup.containsFocus()) {
            menuPopup.close();
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

        // TODO Consume the event if the menu button is repeatable and the event
        // occurs over the trigger

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
        MenuButton menuButton = (MenuButton)getComponent();

        menuButton.requestFocus();
        menuButton.press();

        if (menuPopup.isShowing()) {
            menuPopup.requestFocus();
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

        MenuButton menuButton = (MenuButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            menuButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // Button events
    public void buttonPressed(Button button) {
        if (menuPopup.isOpen()) {
            menuPopup.close();
        } else {
            MenuButton menuButton = (MenuButton)getComponent();
            Component content = menuPopup.getContent();

            // Determine the popup's location and preferred size, relative
            // to the button
            Window window = menuButton.getWindow();

            if (window != null) {
                Display display = menuButton.getWindow().getDisplay();
                Point buttonLocation = menuButton.mapPointToAncestor(display, 0, 0);

                // Ensure that the popup remains within the bounds of the display
                int displayHeight = display.getHeight();

                int y = buttonLocation.y + getHeight() - 1;
                int preferredPopupHeight = content.getPreferredHeight();

                if (y + preferredPopupHeight > displayHeight) {
                    if (buttonLocation.y - preferredPopupHeight > 0) {
                        y = buttonLocation.y - preferredPopupHeight + 1;
                    } else {
                        preferredPopupHeight = displayHeight - y;
                    }
                } else {
                    preferredPopupHeight = -1;
                }

                menuPopup.setLocation(buttonLocation.x, y);
                menuPopup.setPreferredHeight(preferredPopupHeight);
                menuPopup.open(menuButton);

                menuPopup.requestFocus();
            }
        }
    }

    public void menuChanged(MenuButton menuButton, Menu previousMenu) {
        menuPopup.setMenu(menuButton.getMenu());
    }

    public void repeatableChanged(MenuButton menuButton) {
        invalidateComponent();
    }
}
