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
package org.apache.pivot.tests;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;

public class PromptTest extends Application.Adapter {
    private Window window = null;
    private PushButton helloButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BoxPane boxPane = new BoxPane();
        boxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        boxPane.getStyles().put("verticalAlignment", VerticalAlignment.BOTTOM);

        helloButton = new PushButton("Say Hello");
        boxPane.add(helloButton);

        helloButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                String text = button.getButtonData().toString();
                if (window.isBlocked()) {
                    System.out.println("I'm already saying \"" + text + "\" !");
                } else {
                    Prompt.prompt(text, window);
                }
            }
        });

        Border border = new Border(boxPane);
        border.getStyles().put("color", 7);
        border.getStyles().put("padding", 5);
        window = new Window(border);
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(PromptTest.class, args);
    }

}
