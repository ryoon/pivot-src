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
package org.apache.pivot.tutorials.buttons;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class RadioButtons extends Window implements Bindable {
    private PushButton selectButton = null;

    @Override
    public void initialize(Dictionary<String, Object> context, Resources resources) {
        selectButton = (PushButton)context.get("selectButton");

        // Get a reference to the button group
        final ButtonGroup numbersGroup = (ButtonGroup)context.get("numbers");

        // Add a button press listener
        selectButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                String message = "You selected \""
                    + numbersGroup.getSelection().getButtonData()
                    + "\".";
                Alert.alert(MessageType.INFO, message, RadioButtons.this);
            }
        });
    }
}
