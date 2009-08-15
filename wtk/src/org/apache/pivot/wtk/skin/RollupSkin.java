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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.RollupListener;
import org.apache.pivot.wtk.RollupStateListener;


/**
 * Abstract base class for rollup skins.
 *
 */
public abstract class RollupSkin extends ContainerSkin
    implements RollupListener, RollupStateListener {
    @Override
    public void install(Component component) {
        super.install(component);

        Rollup rollup = (Rollup)component;
        rollup.getRollupListeners().add(this);
        rollup.getRollupStateListeners().add(this);
    }

    @Override
    public void uninstall() {
        Rollup rollup = (Rollup)getComponent();
        rollup.getRollupListeners().remove(this);
        rollup.getRollupStateListeners().remove(this);

        super.uninstall();
    }

    // RollupListener methods

    public void headingChanged(Rollup rollup, Component previousHeading) {
        // No-op
    }

    public void contentChanged(Rollup rollup, Component previousContent) {
        // No-op
    }

    // RollupStateListener methods

    public Vote previewExpandedChange(Rollup rollup) {
        // No-op
        return Vote.APPROVE;
    }

    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
        // No-op
    }

    public void expandedChanged(Rollup rollup) {
        // No-op
    }
}
