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
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.FadeTransition;

/**
 * Transition for fading a window, including the drop shadow.
 */
public class FadeWindowTransition extends FadeTransition {
    private DropShadowDecorator dropShadowDecorator;
    private float initialShadowOpacity;

    public FadeWindowTransition(Component component, int duration, int rate,
        DropShadowDecorator dropShadowDecorator) {
        super(component, duration, rate);

        this.dropShadowDecorator = dropShadowDecorator;
        if (dropShadowDecorator != null) {
            initialShadowOpacity = dropShadowDecorator.getShadowOpacity();
        }
    }

    @Override
    protected void update() {
        super.update();

        if (dropShadowDecorator != null) {
            dropShadowDecorator.setShadowOpacity(initialShadowOpacity * (1.0f - getPercentComplete()));
        }

        Component component = getComponent();
        Container parent = component.getParent();
        if (parent != null) {
            parent.repaint(component.getDecoratedBounds());
        }
    }
}
