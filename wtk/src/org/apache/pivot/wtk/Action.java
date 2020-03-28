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

import java.util.Iterator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Abstract base class for "actions". Actions are common application behaviors
 * generally triggered by buttons and keyboard shortcuts.
 */
public abstract class Action {
    /**
     * Action dictionary implementation.
     */
    public static final class NamedActionDictionary implements Dictionary<String, Action>,
        Iterable<String> {
        private NamedActionDictionary() {
        }

        @Override
        public Action get(final String id) {
            return namedActions.get(id);
        }

        @Override
        public Action put(final String id, final Action action) {
            Utils.checkNull(action, "action");

            boolean update = containsKey(id);
            Action previousAction = namedActions.put(id, action);

            if (update) {
                actionClassListeners.actionUpdated(id, previousAction);
            } else {
                actionClassListeners.actionAdded(id);
            }

            return previousAction;
        }

        @Override
        public Action remove(final String id) {
            Action action = null;

            if (containsKey(id)) {
                action = namedActions.remove(id);
                actionClassListeners.actionRemoved(id, action);
            }

            return action;
        }

        @Override
        public boolean containsKey(final String id) {
            return namedActions.containsKey(id);
        }

        @Override
        public Iterator<String> iterator() {
            return new ImmutableIterator<>(namedActions.iterator());
        }
    }

    /**
     * A callback for the GUI thread to perform the given action there.
     */
    public static class Callback implements Runnable {
        private Action action;
        private Component source;

        public Callback(final Action action, final Component source) {
            Utils.checkNull(action, "action");

            this.action = action;
            this.source = source;
        }

        @Override
        public void run() {
            action.perform(source);
        }
    }

    private boolean enabled = true;

    private ActionListener.Listeners actionListeners = new ActionListener.Listeners();

    private static HashMap<String, Action> namedActions = new HashMap<>();
    private static NamedActionDictionary namedActionDictionary = new NamedActionDictionary();

    private static ActionClassListener.Listeners actionClassListeners = new ActionClassListener.Listeners();

    /**
     * Constructor which builds the action and sets it enabled to begin with.
     */
    public Action() {
        this(true);
    }

    /**
     * Constructor to build the action and set the enabled state at the beginning.
     * @param enabled Whether the action is to be initially enabled.
     */
    public Action(final boolean enabled) {
        setEnabled(enabled);
    }

    /**
     * Returns a text description of the action. Subclasses should override this
     * to return a meaningful description if one is needed.
     *
     * @return The text description of the action.
     */
    public String getDescription() {
        return null;
    }

    /**
     * Performs the action.
     *
     * @param source The component that initiated the action.
     */
    public abstract void perform(Component source);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            actionListeners.enabledChanged(this);
        }
    }

    public static NamedActionDictionary getNamedActions() {
        return namedActionDictionary;
    }

    public ListenerList<ActionListener> getActionListeners() {
        return actionListeners;
    }

    public static ListenerList<ActionClassListener> getActionClassListeners() {
        return actionClassListeners;
    }
}
