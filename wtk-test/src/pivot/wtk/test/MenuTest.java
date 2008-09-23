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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.wtk.Action;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Display;
import pivot.wtk.Keyboard;
import pivot.wtk.Label;
import pivot.wtk.Menu;
import pivot.wtk.MenuBar;
import pivot.wtk.MenuPopup;
import pivot.wtk.Mouse;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class MenuTest implements Application {
    private Window window = null;
    private MenuPopup menuPopup = null;

    public void startup(final Display display, Dictionary<String, String> properties)
        throws Exception {
        Action testAction = new Action("testAction") {
            public String getDescription() {
                return "Test Action";
            }

            public void perform() {
                Alert.alert("Test action performed.", display);
            }
        };

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("menu_test.wtkx")));
        final MenuBar menuBar = (MenuBar)wtkxSerializer.getObjectByName("menuBar");
        Label label = (Label)wtkxSerializer.getObjectByName("label");

        menuPopup = new MenuPopup((Menu)wtkxSerializer.readObject(getClass().getResource("menu_popup.wtkx")));
        menuPopup.getStyles().put("borderColor", "#00ff00");

        label.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener() {
            public void mouseDown(Component component, Mouse.Button button, int x, int y) {
                if (button == Mouse.Button.RIGHT) {
                    menuPopup.open(display, component.mapPointToAncestor(display, x, y));
                }
            }

            public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            }

            public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            }
        });

        window.getActions().put(new Keyboard.KeyStroke(Keyboard.KeyCode.A,
            Keyboard.Modifier.META.getMask()), testAction);
        window.setMaximized(true);
        window.open(display);

        ApplicationContext.queueCallback(new Runnable() {
            public void run() {
                menuBar.requestFocus();
            }
        });
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
