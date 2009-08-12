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

import org.apache.pivot.util.ListenerList;

/**
 * Popup class that displays a cascading menu.
 *
 * @author gbrown
 */
public class MenuPopup extends Window {
    private static class MenuPopupListenerList extends ListenerList<MenuPopupListener>
        implements MenuPopupListener {
        public void menuChanged(MenuPopup menuPopup, Menu previousMenu) {
            for (MenuPopupListener listener : this) {
                listener.menuChanged(menuPopup, previousMenu);
            }
        }
    }

    private Menu menu;
    private boolean autoClose = false;

    private MenuPopupListenerList menuPopupListeners = new MenuPopupListenerList();

    public MenuPopup() {
        this(null);
    }

    public MenuPopup(Menu menu) {
        super(true);

        setMenu(menu);
        installSkin(MenuPopup.class);
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        Menu previousMenu = this.menu;

        if (previousMenu != menu) {
            this.menu = menu;
            menuPopupListeners.menuChanged(this, previousMenu);
        }
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void open(Display display, int x, int y) {
        // TODO Determine x, y and width, height based on display bounds

        autoClose = true;

        setLocation(x, y);
        super.open(display);
    }

    public final void open(Display display, Point location) {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        open(display, location.x, location.y);
    }

    public void open(Window owner, int x, int y) {
        // TODO Determine x, y and width, height based on display bounds

        autoClose = true;

        setLocation(x, y);
        super.open(owner);
    }

    public final void open(Window owner, Point location) {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        open(owner, location.x, location.y);
    }

    @Override
    public void close() {
        super.close();

        if (isClosed()) {
            autoClose = false;
        }
    }

    public ListenerList<MenuPopupListener> getMenuPopupListeners() {
        return menuPopupListeners;
    }
}
