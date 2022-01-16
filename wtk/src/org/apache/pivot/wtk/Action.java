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
 * generally triggered by buttons, menu items, and keyboard shortcuts.
 */
public abstract class Action {
    /**
     * Action dictionary implementation.
     * <p> We wrap the underlying {@code Map<>} implementation so that only a few methods
     * are available for use.
     */
    public static final class NamedActionDictionary implements Dictionary<String, Action>,
        Iterable<String> {
        /**
         * Private constructor so only we can construct the singleton instance.
         */
        private NamedActionDictionary() {
        }

        @Override
        public Action get(final String actionName) {
            return namedActions.get(actionName);
        }

        @Override
        public Action put(final String actionName, final Action action) {
            Utils.checkNull(action, "action");

            boolean update = containsKey(actionName);
            Action previousAction = namedActions.put(actionName, action);

            if (update) {
                actionClassListeners.actionUpdated(actionName, previousAction);
            } else {
                actionClassListeners.actionAdded(actionName);
            }

            return previousAction;
        }

        @Override
        public Action remove(final String actionName) {
            Action removedAction = null;

            if (containsKey(actionName)) {
                removedAction = namedActions.remove(actionName);
                actionClassListeners.actionRemoved(actionName, removedAction);
            }

            return removedAction;
        }

        @Override
        public boolean containsKey(final String actionName) {
            return namedActions.containsKey(actionName);
        }

        @Override
        public Iterator<String> iterator() {
            return new ImmutableIterator<>(namedActions.iterator());
        }
    }

    /**
     * A callback for the GUI thread to perform the given action there, unless the
     * action is disabled at the time {@code run()} is called.
     */
    public static final class Callback implements Runnable {
        /**
         * The action to be performed in this callback.
         */
        private Action action;
        /**
         * The source component that initiated the action.
         */
        private Component source;

        /**
         * Construct a callback to perform the action on the GUI (EDT) thread.
         *
         * @param actionToPerform The action.
         * @param actionSource    The source component.
         */
        public Callback(final Action actionToPerform, final Component actionSource) {
            Utils.checkNull(actionToPerform, "action");

            action = actionToPerform;
            source = actionSource;
        }

        @Override
        public void run() {
            if (action.isEnabled()) {
                action.perform(source);
            }
        }
    }

    /**
     * Flag as to whether this action is currently enabled or not.
     */
    private boolean enabled = true;

    /**
     * List per action of the listeners for activity on that action.
     */
    private ActionListener.Listeners actionListeners = new ActionListener.Listeners();

    /**
     * The backing map for the named action dictionary.
     */
    private static HashMap<String, Action> namedActions = new HashMap<>();
    /**
     * The global dictionary associating action names with their implementations.
     */
    private static NamedActionDictionary namedActionDictionary = new NamedActionDictionary();

    /**
     * Global list of listeners for all action activity.
     */
    private static ActionClassListener.Listeners actionClassListeners = new ActionClassListener.Listeners();


    /**
     * Constructor which builds the action and sets it enabled to begin with.
     */
    public Action() {
        this(true);
    }

    /**
     * Constructor to build the action and set the enabled state at the beginning.
     *
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
     * Perform the named action, unless the action is disabled.
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

        if (action.isEnabled()) {
            action.perform(comp);
        }
    }

    /**
     * Perform the named action, unless the action is disabled.
     * <p> This is the equivalent of
     * <code>Action.getNamedActions().get(<i>actionName</i>).perform(<i>comp</i>)</code>.
     *
     * @param <E>        Enum type that gives the action name.
     * @param actionName An enum value whose <code>toString()</code> value is used as the action name.
     * @param comp       The component initiating the action.
     * @throws IllegalArgumentException if the actionName is {@code null} or if there is
     * no action with that name.
     */
    public static <E extends Enum<E>> void performAction(final E actionName, final Component comp) {
        Utils.checkNull(actionName, "action name");
        performAction(actionName.toString(), comp);
    }

    /**
     * Check if this action is currently enabled.
     *
     * @return Whether or not this action is currently enabled.
     * @see #setEnabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set this action enabled or disabled.
     * <p> Note: in general, the {@link #perform} method can be called whether or not
     * this flag is set, so it will be incumbent on the caller to determine if this is
     * appropriate. However, using the {@link Callback} class or the {@link #performAction}
     * method WILL check this flag before calling {@link #perform}.
     * <p> Also note: buttons and menu items that invoke this action will automatically
     * be enabled/disabled by setting this flag.
     * <p> If the enabled state changes, the associated {@link ActionListener#enabledChanged}
     * method(s) will be called.
     *
     * @param enabledState The new enabled state for the action.
     */
    public void setEnabled(final boolean enabledState) {
        if (enabled != enabledState) {
            enabled = enabledState;
            actionListeners.enabledChanged(this);
        }
    }

    /**
     * Add this action to the named action dictionary.
     * <p> This is equivalent to <code>getNamedActions().put(<i>actionName</i>, <i>action</i>)</code>
     *
     * @param actionName The name to store this action under (can be referenced from button actions, etc.)
     * @param action     The action to be performed under this name.
     * @return           The previous action (if any) listed under this name.
     */
    public static Action addNamedAction(final String actionName, final Action action) {
        return namedActionDictionary.put(actionName, action);
    }

    /**
     * Add this action to the named action dictionary.
     * <p> This is equivalent to <code>getNamedActions().put(<i>actionName</i>, <i>action</i>)</code>
     *
     * @param <E>        Enum type that gives the action name.
     * @param actionName An enum whose <code>toString()</code> value is used as the ID for the action.
     * @param action     The action to be performed under this name.
     * @return           The previous action (if any) listed under this name.
     * @throws IllegalArgumentException if the actionName is {@code null}.
     */
    public static <E extends Enum<E>> Action addNamedAction(final E actionName, final Action action) {
        Utils.checkNull(actionName, "action name");
        return addNamedAction(actionName.toString(), action);
    }

    /**
     * Get the named action from the dictionary.
     * <p> This is the equivalent of <code>getNamedActions().get(<i>actionName</i>)</code>
     *
     * @param actionName The name this action was stored under in the dictionary.
     * @return           The action currently associated with this name (or {@code null} if
     *                   there is no saved action with that name value).
     */
    public static Action getNamedAction(final String actionName) {
        return namedActionDictionary.get(actionName);
    }

    /**
     * Get the named action from the dictionary.
     * <p> This is the equivalent of <code>getNamedActions().get(<i>actionName</i>)</code>
     *
     * @param <E>        Enum type that gives the action name.
     * @param actionName An enum whose <code>toString()</code> method is used as the ID for the action.
     * @return           The action currently associated with this name (or {@code null} if
     *                   there is no saved action with that name value).
     * @throws IllegalArgumentException if the actionName is {@code null}.
     */
    public static <E extends Enum<E>> Action getNamedAction(final E actionName) {
        Utils.checkNull(actionName, "action name");
        return getNamedAction(actionName.toString());
    }

    /**
     * @return The global named action dictionary.
     */
    public static NamedActionDictionary getNamedActions() {
        return namedActionDictionary;
    }

    /**
     * @return The list of listeners for this action.
     */
    public ListenerList<ActionListener> getActionListeners() {
        return actionListeners;
    }

    /**
     * @return The list of listeners for all action activities.
     */
    public static ListenerList<ActionClassListener> getActionClassListeners() {
        return actionClassListeners;
    }
}
