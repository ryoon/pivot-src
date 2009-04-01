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
package pivot.wtk.skin;

import pivot.wtk.StackPane;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;

/**
 * Stack pane skin.
 *
 * @author gbrown
 */
public class StackPaneSkin extends ContainerSkin {
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;
        StackPane stackPane = (StackPane)getComponent();

        for (Component component : stackPane) {
            preferredWidth = Math.max(preferredWidth,
                component.getPreferredWidth(height));
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;
        StackPane stackPane = (StackPane)getComponent();

        for (Component component : stackPane) {
            preferredHeight = Math.max(preferredHeight,
                component.getPreferredHeight(width));
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        StackPane stackPane = (StackPane)getComponent();

        for (Component component : stackPane) {
            Dimensions preferredCardSize = component.getPreferredSize();

            preferredWidth = Math.max(preferredWidth,
                preferredCardSize.width);

            preferredHeight = Math.max(preferredHeight,
                preferredCardSize.height);
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        // Set the size of all components to match the size of the stack pane
        StackPane stackPane = (StackPane)getComponent();

        int width = getWidth();
        int height = getHeight();
        for (Component component : stackPane) {
            component.setSize(width, height);
        }
    }
}
