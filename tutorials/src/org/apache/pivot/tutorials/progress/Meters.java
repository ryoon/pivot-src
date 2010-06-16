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
package org.apache.pivot.tutorials.progress;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;

public class Meters implements Application {
    public class SampleTask extends Task<Void> {
        private int percentage = 0;

        @Override
        public Void execute() throws TaskExecutionException {
            // Simulate a long-running operation
            percentage = 0;

            while (percentage < 100
                && !abort) {
                try {
                    Thread.sleep(100);
                    percentage++;

                    // Update the meter on the UI thread
                    ApplicationContext.queueCallback(new Runnable() {
                        @Override
                        public void run() {
                            meter.setPercentage((double)percentage / 100);
                        }
                    });
                } catch(InterruptedException exception) {
                    throw new TaskExecutionException(exception);
                }
            }

            return null;
        }
    }

    private Window window = null;
    private Meter meter = null;
    private PushButton progressButton = null;

    private SampleTask sampleTask = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        BeanSerializer beanSerializer = new BeanSerializer();
        window = (Window)beanSerializer.readObject(this, "meters.bxml");
        meter = (Meter)beanSerializer.get("meter");
        progressButton = (PushButton)beanSerializer.get("progressButton");

        progressButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                if (sampleTask == null) {
                    // Create and start the simulated task; wrap it in a
                    // task adapter so the result handlers are called on the
                    // UI thread
                    sampleTask = new SampleTask();
                    sampleTask.execute(new TaskAdapter<Void>(new TaskListener<Void>() {
                        @Override
                        public void taskExecuted(Task<Void> task) {
                            reset();
                        }

                        @Override
                        public void executeFailed(Task<Void> task) {
                            reset();
                        }

                        private void reset() {
                            // Reset the meter and button
                            sampleTask = null;
                            meter.setPercentage(0);
                            updateProgressButton();
                        }
                    }));
                } else {
                    // Cancel the task
                    sampleTask.abort();
                }

                updateProgressButton();
            }
        });

        updateProgressButton();

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    private void updateProgressButton() {
        if (sampleTask == null) {
            progressButton.setButtonData("Start");
        } else {
            progressButton.setButtonData("Cancel");
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Meters.class, args);
    }
}
