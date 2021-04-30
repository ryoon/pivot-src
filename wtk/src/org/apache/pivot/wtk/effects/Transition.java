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
package org.apache.pivot.wtk.effects;

import java.util.Optional;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Theme;

/**
 * Abstract base class for "transitions", which are animated application
 * effects.
 */
public abstract class Transition {
    /**
     * Number of milliseconds per second.
     */
    private static final int MILLIS_PER_SECOND = 1000;

    /**
     * The duration of the transition (milliseconds).
     */
    private int duration;

    /**
     * Rate of the transition in frames per second.
     */
    private int rate;

    /**
     * Whether the transition repeats once it is finished.
     */
    private boolean repeating;

    /**
     * Whether the transition is to be run in reverse.
     */
    private boolean reversed = false;

    /**
     * Optional {@code TransitionListener} for transition events.
     */
    private Optional<TransitionListener> optionalListener = Optional.empty();

    /**
     * The transition start time (in milliseconds).
     */
    private long startTime = 0;
    /**
     * The current millisecond timestamp, set on every callback.
     */
    private long currentTime = 0;
    /**
     * Set during transition operation to be the current callback for updates,
     * then cleared once the transition is over.
     */
    private ApplicationContext.ScheduledCallback transitionCallback = null;

    /**
     * Callback for every interval to call {@link #update} and then {@link #stop}
     * once the transition interval is over.
     */
    private final Runnable updateCallback = () -> {
        currentTime = System.currentTimeMillis();

        long endTime = startTime + duration;
        if (currentTime >= endTime) {
            if (repeating) {
                startTime = endTime;
            } else {
                currentTime = endTime;
                stop();

                optionalListener.ifPresent(listener -> listener.transitionCompleted(Transition.this));
            }
        }

        update();
    };


    /**
     * Creates a new non-repeating transition with the given duration, and rate.
     *
     * @param durationValue Transition duration, in milliseconds.
     * @param rateValue Transition rate, in frames per second.
     */
    public Transition(final int durationValue, final int rateValue) {
        this(durationValue, rateValue, false);
    }

    /**
     * Creates a new transition with the given duration, rate, and repeat.
     *
     * @param durationValue Transition duration, in milliseconds.
     * @param rateValue Transition rate, in frames per second.
     * @param repeat {@code true} if the transition should repeat;
     * {@code false}, otherwise.
     */
    public Transition(final int durationValue, final int rateValue, final boolean repeat) {
        this(durationValue, rateValue, repeat, false);
    }

    /**
     * Creates a new transition with the given duration, rate, and repeat.
     *
     * Note that if the current Theme has transitions not enabled,
     * the duration and rate will both be set to zero, so that the final
     * update is called once, in a minimum amount of time after the start.
     *
     * @param durationValue Transition duration, in milliseconds.
     * @param rateValue Transition rate, in frames per second.
     * @param repeat {@code true} if the transition should repeat;
     * {@code false}, otherwise.
     * @param reverse {@code true} if the transition should run in reverse;
     * {@code false} otherwise.
     */
    public Transition(final int durationValue, final int rateValue, final boolean repeat, final boolean reverse) {
        Utils.checkNonNegative(durationValue, "duration");

        if (!themeHasTransitionEnabled()) {
            // System.out.println("transitions not enabled, overriding transition values");
            duration = 0;
            rate = 0;
        } else {
            duration = durationValue;
            rate = rateValue;
        }

        repeating = repeat;
        reversed = reverse;
    }

    /**
     * Returns the transition duration.
     *
     * @return The duration of the transition, in milliseconds.
     * @see #setDuration(int)
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the transition duration, the length of time the transition is
     * scheduled to run.
     *
     * @param durationValue The duration of the transition, in milliseconds.
     */
    public void setDuration(final int durationValue) {
        Utils.checkNonNegative(durationValue, "duration");

        if (isRunning()) {
            throw new IllegalStateException("Transition is currently running.");
        }

        duration = durationValue;
    }

    /**
     * Returns the transition rate.
     *
     * @return The rate of the transition, in frames per second.
     * @see #setRate(int)
     */
    public int getRate() {
        return rate;
    }

    /**
     * Sets the transition rate, the number of times the transition will be
     * updated within the span of one second.
     *
     * @param rateValue The transition rate, in frames per second.
     */
    public void setRate(final int rateValue) {
        Utils.checkNonNegative(rateValue, "rate");

        if (isRunning()) {
            throw new IllegalStateException("Transition is currently running.");
        }

        rate = rateValue;
    }

    /**
     * Returns the transition interval, the number of milliseconds between
     * updates.
     *
     * @return The transition interval in milliseconds,
     *         or a default minimum value if transitions have been disabled.
     */
    public int getInterval() {
        int interval;

        if (rate != 0) {
            interval = (int) ((1.0f / rate) * MILLIS_PER_SECOND);
        } else {
            interval = 1;
        }
        return interval;
    }

    /**
     * Returns the time at which the transition was started.
     *
     * @return The transition's start time.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the last time the transition was updated.
     *
     * @return The most recent update time.
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Returns the elapsed time since the transition started.
     *
     * @return The amount of time that has passed since the transition
     * was started. If the transition is reversed, this value reflects the
     * amount of time remaining.
     */
    public int getElapsedTime() {
        long endTime = startTime + duration;
        int elapsedTime;

        if (reversed) {
            elapsedTime = (int) (endTime - currentTime);
        } else {
            elapsedTime = (int) (currentTime - startTime);
        }

        return elapsedTime;
    }

    /**
     * Returns the percentage of the transition that has completed.
     *
     * @return A value between 0 and 1, inclusive, representing the transition's
     * percent complete. If the transition is reversed, this value reflects the
     * percent remaining.
     */
    public float getPercentComplete() {
        float percentComplete;

        if (duration != 0) {
            percentComplete = (float) (currentTime - startTime) / (float) duration;
        } else {
            percentComplete = 1.0f;
        }

        if (reversed) {
            percentComplete = 1.0f - percentComplete;
        }

        return percentComplete;
    }

    /**
     * Tells whether or not the transition is currently running.
     *
     * @return {@code true} if the transition is currently running;
     * {@code false} if it is not
     */
    public final boolean isRunning() {
        return (transitionCallback != null);
    }

    /**
     * Starts the transition. Calls {@link #update()} to establish the initial
     * state and starts a time that will repeatedly call {@link #update()} at
     * the current rate. No {@code TransitionListener} will be notified.
     *
     * @see #start(TransitionListener)
     */
    public final void start() {
        start(null);
    }

    /**
     * Starts the transition. Calls {@link #update()} to establish the initial
     * state and starts a timer that will repeatedly call {@link #update()} at
     * the current rate. The specified {@code TransitionListener} will be
     * notified when the transition completes.
     *
     * @param listener The listener to get notified when the transition completes,
     * or {@code null} if no notification is necessary.
     */
    public void start(final TransitionListener listener) {
        if (isRunning()) {
            throw new IllegalStateException("Transition is currently running.");
        }

        optionalListener = Optional.ofNullable(listener);

        startTime = System.currentTimeMillis();
        currentTime = startTime;

        transitionCallback = ApplicationContext.scheduleRecurringCallback(updateCallback,
            getInterval());

        update();
    }

    /**
     * Stops the transition running with no final update, and does not fire a
     * {@link TransitionListener#transitionCompleted(Transition)} event.
     */
    public void stop() {
        if (isRunning()) {
            transitionCallback.cancel();
        }

        transitionCallback = null;
    }

    /**
     * "Fast-forward" to the end of the transition, run {@link #update}
     * the last time, and fire a
     * {@link TransitionListener#transitionCompleted(Transition)} event.
     */
    public void end() {
        if (isRunning()) {
            currentTime = startTime + duration;
            stop();
            update();
            optionalListener.ifPresent(listener -> listener.transitionCompleted(this));
        }
    }

    /**
     * Called repeatedly while the transition is running to update the
     * transition's state.
     */
    protected abstract void update();

    /**
     * Is the transition a repeating one?
     *
     * @return {@code true} if the transition should repeat; {@code false} if not.
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Tests whether the transition is reversed.
     *
     * @return {@code true} if the transition is reversed; {@code false} otherwise.
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * Sets the transition's reversed flag.
     *
     * @param reverse Whether the transition should be reversed.
     */
    public void setReversed(final boolean reverse) {
        reversed = reverse;
    }

    /**
     * Reverses the transition. If the transition is running, updates the start
     * time so the reverse duration is the same as the current elapsed time.
     */
    public void reverse() {
        if (isRunning()) {
            long repeatDuration = currentTime - startTime;
            long endTime = currentTime + repeatDuration;
            startTime = endTime - duration;
        }

        setReversed(!isReversed());
    }

    /**
     * Tell if the theme has transitions enabled.<br> Usually this means that (if false) any
     * effect/transition will not be drawn.
     *
     * @return {@code true} if enabled (default), {@code false} otherwise.
     * @see Theme#isTransitionEnabled
     */
    protected boolean themeHasTransitionEnabled() {
        return Theme.getTheme().isTransitionEnabled();
    }

}
