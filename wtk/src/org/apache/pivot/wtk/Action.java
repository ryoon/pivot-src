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

        public Callback(final Action actionToPerform, final Component actionSource) {
            Utils.checkNull(actionToPerform, "action");

            action = actionToPerform;
            source = actionSource;
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
     * @param initialEnable Whether the action is to be initially enabled.
     */
    public Action(final boolean initialEnable) {
        setEnabled(initialEnable);
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

    /**
     * Perform the named action.
     * <p> This is the equivalent of
     * <code>Action.getNamedActions().get(<i>actionName</i>).perform(<i>comp</i>)</code>.
     *
     * @param actionName One of the previously defined action names.
     * @param comp       The component initiating the action.
     * @throws IllegalArgumentException if the actionName is {@code null} or if there is
     * no action with that name.
     */
    public static void performAction(final String actionName, final Component comp) {
        Utils.checkNull(actionName, "action name");

        Action action = namedActionDictionary.get(actionName);
        Utils.checkNull(action, "action");

        action.perform(comp);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabledState) {
        if (enabled != enabledState) {
            enabled = enabledState;
            actionListeners.enabledChanged(this);
        }
    }

    /**
     * Add this action to the named action dictionary.
     * <p> This is equivalent to <code>getNamedActions().put(<i>id</i>, <i>action</i>)</code>
     *
     * @param id     The name to store this action under (can be referenced from button actions, etc.)
     * @param action The action to be performed under this name.
     * @return       The previous action (if any) listed under this name.
     */
    public static Action addNamedAction(final String id, final Action action) {
        return namedActionDictionary.put(id, action);
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
