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

import pivot.util.Vote;
import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Dimensions;
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

    private WindowStateListener menuPopupWindowStateListener = new WindowStateListener() {
        public Vote previewWindowOpen(Window window, Display display) {
            return Vote.APPROVE;
        }

        public void windowOpenVetoed(Window window, Vote reason) {
            // No-op
        }

        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);
        }

        public Vote previewWindowClose(Window window) {
            return Vote.APPROVE;
        }

        public void windowCloseVetoed(Window window, Vote reason) {
            // No-op
        }

        public void windowClosed(Window window, Display display) {
            display.getContainerMouseListeners().remove(displayMouseListener);

            MenuButton menuButton = (MenuButton)getComponent();

            if (menuButton.isFocusable()) {
                menuButton.requestFocus();
            }

            repaintComponent();
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener() {
        public boolean mouseMove(Container container, int x, int y) {
            return false;
        }

        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!menuPopup.isAncestor(descendant)
                && !menuPopup.isOwner(descendant.getWindow())
                && descendant != MenuButtonSkin.this.getComponent()) {
                menuPopup.close();
            }

            return false;
        }

        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window != menuPopup
                && !menuPopup.isOwner(window)) {
                consumed = true;
            }

            return consumed;
        }
    };

    public MenuButtonSkin() {
        menuPopup.getWindowStateListeners().add(menuPopupWindowStateListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuButton menuButton = (MenuButton)getComponent();
        menuButton.getMenuButtonListeners().add(this);

        menuPopup.setMenu(menuButton.getMenu());
    }

    @Override
    public void uninstall() {
        MenuButton menuButton = (MenuButton)getComponent();
        menuButton.getMenuButtonListeners().remove(this);

        menuPopup.setMenu(null);

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
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        MenuButton menuButton = (MenuButton)getComponent();

        if (menuButton.isFocusable()) {
            menuButton.requestFocus();
        }

        menuButton.press();

        if (menuPopup.isShowing()) {
            menuPopup.requestFocus();
        }

        return true;
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
                int width = getWidth();
                int height = getHeight();

                Display display = menuButton.getDisplay();

                // Ensure that the popup remains within the bounds of the display
                Point buttonLocation = menuButton.mapPointToAncestor(display, 0, 0);

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

                menuPopup.setLocation(x, y);
                menuPopup.setPreferredSize(popupSize);
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
