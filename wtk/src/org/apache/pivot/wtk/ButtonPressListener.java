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
 * Button press listener interface.
 */
@FunctionalInterface
public interface ButtonPressListener {
    /**
     * Button press listeners.
     */
    final class Listeners extends ListenerList<ButtonPressListener>
        implements ButtonPressListener {
        @Override
        public void buttonPressed(final Button button) {
            forEach(listener -> listener.buttonPressed(button));
        }
    }

    /**
     * Called when a button is pressed.
     *
     * @param button The button that was just pressed.
     */
    void buttonPressed(Button button);
}
