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
package org.apache.pivot.tutorials.effects;

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.effects.FadeDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quadratic;

public class CollapseTransition extends Transition {
    private Component component;
    private int initialWidth;
    private Easing easing = new Quadratic();
    private FadeDecorator fadeDecorator = new FadeDecorator();

    public CollapseTransition(Component component, int duration, int rate) {
        super(duration, rate, false);

        this.component = component;
        initialWidth = component.getWidth();
    }

    public Component getComponent() {
        return component;
    }

    @Override
    public void start(TransitionListener transitionListener) {
        component.getDecorators().add(fadeDecorator);

        super.start(transitionListener);
    }

    @Override
    public void stop() {
        component.getDecorators().remove(fadeDecorator);

        super.stop();
    }

    @Override
    protected void update() {
        float percentComplete = getPercentComplete();

        if (percentComplete < 1.0f) {
            int duration = getDuration();
            int width = (int) (initialWidth * (1.0f - percentComplete));

            width = (int) easing.easeInOut(getElapsedTime(), initialWidth, width - initialWidth,
                duration);

            component.setPreferredWidth(width);

            fadeDecorator.setOpacity(1.0f - percentComplete);
            component.repaint();
        }
    }
}
