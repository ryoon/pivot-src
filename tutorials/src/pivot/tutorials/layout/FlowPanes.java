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
package pivot.tutorials.layout;

import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Orientation;
import pivot.wtk.RadioButton;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class FlowPanes implements Application, ButtonStateListener {
    private FlowPane flowPane = null;
    private RadioButton horizontalOrientationButton = null;
    private RadioButton verticalOrientationButton = null;
    private RadioButton horizontalAlignmentRightButton = null;
    private RadioButton horizontalAlignmentLeftButton = null;
    private RadioButton horizontalAlignmentCenterButton = null;
    private RadioButton horizontalAlignmentJustifyButton = null;
    private RadioButton verticalAlignmentTopButton = null;
    private RadioButton verticalAlignmentBottomButton = null;
    private RadioButton verticalAlignmentCenterButton = null;
    private RadioButton verticalAlignmentJustifyButton = null;

    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/layout/flowpanes.wtkx");

        flowPane = (FlowPane)componentLoader.getComponent("flowPane");

        // Orientation
        horizontalOrientationButton =
            (RadioButton)componentLoader.getComponent("horizontalOrientationButton");
        horizontalOrientationButton.getButtonStateListeners().add(this);

        verticalOrientationButton =
            (RadioButton)componentLoader.getComponent("verticalOrientationButton");
        verticalOrientationButton.getButtonStateListeners().add(this);

        // Horizontal alignment
        horizontalAlignmentLeftButton =
            (RadioButton)componentLoader.getComponent("horizontalAlignmentLeftButton");
        horizontalAlignmentLeftButton.getButtonStateListeners().add(this);

        horizontalAlignmentRightButton =
            (RadioButton)componentLoader.getComponent("horizontalAlignmentRightButton");
        horizontalAlignmentRightButton.getButtonStateListeners().add(this);

        horizontalAlignmentCenterButton =
            (RadioButton)componentLoader.getComponent("horizontalAlignmentCenterButton");
        horizontalAlignmentCenterButton.getButtonStateListeners().add(this);

        horizontalAlignmentJustifyButton =
            (RadioButton)componentLoader.getComponent("horizontalAlignmentJustifyButton");
        horizontalAlignmentJustifyButton.getButtonStateListeners().add(this);

        // Vertical alignment
        verticalAlignmentTopButton =
            (RadioButton)componentLoader.getComponent("verticalAlignmentTopButton");
        verticalAlignmentTopButton.getButtonStateListeners().add(this);

        verticalAlignmentBottomButton =
            (RadioButton)componentLoader.getComponent("verticalAlignmentBottomButton");
        verticalAlignmentBottomButton.getButtonStateListeners().add(this);

        verticalAlignmentCenterButton =
            (RadioButton)componentLoader.getComponent("verticalAlignmentCenterButton");
        verticalAlignmentCenterButton.getButtonStateListeners().add(this);

        verticalAlignmentJustifyButton =
            (RadioButton)componentLoader.getComponent("verticalAlignmentJustifyButton");
        verticalAlignmentJustifyButton.getButtonStateListeners().add(this);

        stateChanged(null, null);

        window = new Window();
        window.setContent(content);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE,
            Boolean.TRUE);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }

    public void stateChanged(Button button, Button.State previousState) {
        Orientation orientation = null;
        if (horizontalOrientationButton.isSelected()) {
            orientation = Orientation.HORIZONTAL;
        } else if (verticalOrientationButton.isSelected()) {
            orientation = Orientation.VERTICAL;
        }

        flowPane.setOrientation(orientation);

        HorizontalAlignment horizontalAlignment = null;
        if (horizontalAlignmentLeftButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.LEFT;
        } else if (horizontalAlignmentRightButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.RIGHT;
        } else if (horizontalAlignmentCenterButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.CENTER;
        } else if (horizontalAlignmentJustifyButton.isSelected()) {
            horizontalAlignment = HorizontalAlignment.JUSTIFY;
        }

        flowPane.getStyles().put("horizontalAlignment", horizontalAlignment);

        VerticalAlignment verticalAlignment = null;
        if (verticalAlignmentTopButton.isSelected()) {
            verticalAlignment = VerticalAlignment.TOP;
        } else if (verticalAlignmentBottomButton.isSelected()) {
            verticalAlignment = VerticalAlignment.BOTTOM;
        } else if (verticalAlignmentCenterButton.isSelected()) {
            verticalAlignment = VerticalAlignment.CENTER;
        } else if (verticalAlignmentJustifyButton.isSelected()) {
            verticalAlignment = VerticalAlignment.JUSTIFY;
        }

        flowPane.getStyles().put("verticalAlignment", verticalAlignment);
    }
}
