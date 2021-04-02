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

import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;


/**
 * Default implementation of the {@link TaskListener} interface
 * with default implementations of the methods.
 *
 * @param <V> Return value type for the task.
 */
public class WTKTaskListener<V> implements TaskListener<V> {
    @Override
    public void taskExecuted(final Task<V> task) {
        // Empty block
    }

    /**
     * Calls the default {@link ApplicationContext#handleUncaughtException(Thread,Throwable)}
     * with the {@link Task#getBackgroundThread} and {@link Task#getFault}.
     */
    @Override
    public void executeFailed(final Task<V> task) {
        ApplicationContext.handleUncaughtException(task.getBackgroundThread(), task.getFault());
    }
}
