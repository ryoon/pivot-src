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
package org.apache.pivot.util.concurrent;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.apache.pivot.util.Utils;

/**
 * Abstract base class for "tasks". A task is an asynchronous operation that may
 * optionally return a value.
 *
 * @param <V> The type of the value returned by the operation. May be
 * {@link Void} to indicate that the task does not return a value.
 */
public abstract class Task<V> {
    /**
     * Task execution callback that is posted to the executor service; responsible
     * for running the background task from the {@link #run} method, where it
     * invokes the {@link Task#execute} method (abstract here, so it must be
     * implemented in the subclass).
     */
    private class ExecuteCallback implements Runnable {
        @Override
        public void run() {
            V taskResult = null;
            Throwable taskFault = null;

            synchronized (Task.this) {
                Task.this.taskThread = new WeakReference<Thread>(Thread.currentThread());
            }

            try {
                taskResult = execute();
            } catch (Throwable throwable) {
                taskFault = throwable;
            }

            TaskListener<V> localListener;
            synchronized (Task.this) {
                Task.this.result = taskResult;
                Task.this.fault = taskFault;

                abort = false;

                localListener = Task.this.taskListener;
                Task.this.taskListener = null;
            }

            if (taskFault == null) {
                localListener.taskExecuted(Task.this);
            } else {
                localListener.executeFailed(Task.this);
            }
        }
    }

    /**
     * The executor service used to launch this background task.
     */
    private ExecutorService executorService;

    /**
     * The result of this task's execution. Not valid until and unless the task
     * finishes successfully.
     */
    private V result = null;
    /**
     * The reason for the task's failure, if any. Not valid until and unless the
     * task's {@link #execute} method throws an exception.
     */
    private Throwable fault = null;
    /**
     * Listener attached to this task which is notified when the task finishes
     * either successfully, or with an exception.
     */
    private TaskListener<V> taskListener = null;
    /**
     * Weak reference to the thread actually executing the task. Provided as a
     * convenience if needed. Not valid until the task's {@link #execute} method
     * is called. "Weak" so that garbage collection can recover all this task's
     * resources once this thread finishes.
     */
    private WeakReference<Thread> taskThread = null;

    /**
     * Timeout value, which can be used to ensure the task finishes even if something
     * untoward happens. Must be implemented and respected by the subclass.
     */
    protected volatile long timeout = Long.MAX_VALUE;
    /**
     * Flag used to signal that the task should be / has been aborted.  Implemented and
     * respected by the subclass.
     */
    protected volatile boolean abort = false;

    /**
     * Default executor service used to launch tasks if no other is provided. The default is a
     * cached thread pool.
     */
    public static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();


    /**
     * Construct this task using the default executor service to launch it.
     */
    public Task() {
        this(DEFAULT_EXECUTOR_SERVICE);
    }

    /**
     * Construct this task using the given executor service to launch it.
     *
     * @param execService The service to use to execute this background task (must not
     *                    be <code>null</code>).
     * @throws IllegalArgumentException if the executor service is {@code null}.
     */
    public Task(final ExecutorService execService) {
        Utils.checkNull(execService, "executorService");

        executorService = execService;
    }

    /**
     * Synchronously executes the task.
     *
     * @return The result of the task's execution.
     * @throws TaskExecutionException If an error occurs while executing the
     * task.
     */
    public abstract V execute() throws TaskExecutionException;

    /**
     * Asynchronously executes the task using the executor service specified
     * at construction time.. The caller is notified of the task's completion
     * via the listener argument. Note that the listener will be notified on
     * the task's worker thread, not on the thread that requested the task
     * execution.
     *
     * @param taskListenerValue The listener to be notified when the task
     * completes or throws an exception.
     * @throws IllegalThreadStateException if this task is already scheduled / running.
     */
    public synchronized void execute(final TaskListener<V> taskListenerValue) {
        execute(taskListenerValue, executorService);
    }

    /**
     * Asynchronously executes the task. The caller is notified of the task's
     * completion via the listener argument. Note that the listener will be
     * notified on the task's background worker thread, not on the thread that
     * requested the task execution.
     *
     * @param taskListenerValue The listener to be notified when the task
     * completes or throws an exception.
     * @param execServiceOverride The service to submit the task to (which may be
     * an override of the Task's own <code>ExecutorService</code>).
     * @throws IllegalThreadStateException if this task is already scheduled / running.
     */
    public synchronized void execute(final TaskListener<V> taskListenerValue,
        final ExecutorService execServiceOverride) {
        Utils.checkNull(taskListenerValue, "taskListener");
        Utils.checkNull(execServiceOverride, "executorService");

        if (taskListener != null) {
            throw new IllegalThreadStateException("Task is already pending.");
        }

        taskListener = taskListenerValue;

        result = null;
        fault = null;
        taskThread = null;
        abort = false;

        // Create a new execute callback and post it to the executor service
        execServiceOverride.submit(new ExecuteCallback());
    }

    /**
     * @return The executor service used to execute this task.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Returns the result of executing the task.
     *
     * @return The task result, or {@code null} if the task is still executing
     * or has failed. The result itself may also be {@code null}, especially
     * for a {@code Task<Void>}, or just if the task execution resulted in that.
     * Callers should call {@link #isPending()} and {@link #getFault()} to
     * distinguish between these cases.
     */
    public synchronized V getResult() {
        return result;
    }

    /**
     * Returns the fault that occurred while executing the task.
     *
     * @return The task fault, or {@code null} if the task is still executing
     * or has succeeded. Callers should call {@link #isPending()} to distinguish
     * between these cases.
     */
    public synchronized Throwable getFault() {
        return fault;
    }

    /**
     * Returns the thread that was used to execute this task in the background.
     *
     * @return The background thread or {@code null} if the weak reference was
     * already cleared or if the thread hasn't started yet.
     */
    public synchronized Thread getBackgroundThread() {
        return taskThread == null ? null : taskThread.get();
    }

    /**
     * Returns the pending state of the task.
     *
     * @return {@code true} if the task is awaiting execution or currently
     * executing; {@code false}, otherwise.
     */
    public synchronized boolean isPending() {
        return (taskListener != null);
    }

    /**
     * Return the timeout value for this task.
     *
     * @return The timeout value.
     * @see #setTimeout(long)
     */
    public synchronized long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value for this task. It is the responsibility of the
     * implementing class to respect this value.
     *
     * @param timeoutValue The time by which the task must complete execution. If the
     * timeout is exceeded, a {@link TimeoutException} must be thrown (again, the
     * responsibility of the implementing class).
     */
    public synchronized void setTimeout(final long timeoutValue) {
        timeout = timeoutValue;
    }

    /**
     * Sets the abort flag for this task to {@code true}. It is the
     * responsibility of the implementing class to respect this value and throw
     * a {@link AbortException}.
     */
    public synchronized void abort() {
        abort = true;
    }
}
