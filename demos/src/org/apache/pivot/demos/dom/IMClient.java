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
package org.apache.pivot.demos.dom;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BrowserApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.FadeTransition;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class IMClient implements Application {
    /**
     * Task for asynchronously logging into Jabber.
     */
    private class LoginTask extends Task<Void> {
        public Void execute() throws TaskExecutionException {
            try {
                String domain = domainTextInput.getText();

                ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(domain);
                xmppConnection = new XMPPConnection(connectionConfiguration);

                String username = usernameTextInput.getText();
                String password = passwordTextInput.getText();
                xmppConnection.connect();
                xmppConnection.login(username, password);
            } catch(XMPPException exception) {
                throw new TaskExecutionException(exception);
            }

            return null;
        }
    }

    private XMPPConnection xmppConnection = null;

    private Window window;

    @WTKX private CardPane cardPane;
    @WTKX private Form loginForm;
    @WTKX private TextInput usernameTextInput;
    @WTKX private TextInput passwordTextInput;
    @WTKX private TextInput domainTextInput;
    @WTKX private PushButton loginButton;
    @WTKX private Label errorMessageLabel;
    @WTKX private Label messageLabel;

    private ApplicationContext.ScheduledCallback scheduledFadeCallback = null;

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "im_client.wtkx");
        wtkxSerializer.bind(this, IMClient.class);

        loginForm.getComponentKeyListeners().add(new ComponentKeyListener() {
            public boolean keyTyped(Component component, char character) {
                return false;
            }

            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    login();
                }

                return false;
            }

            public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                return false;
            }
        });

        loginButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(final Button button) {
                login();
            }
        });

        window.open(display);
    }

    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
        // No-op
    }

    public void resume() {
        // No-op
    }

    private void login() {
        if (usernameTextInput.getText().length() == 0) {
            errorMessageLabel.setText("Username is required.");
        } else if (passwordTextInput.getText().length() == 0) {
            errorMessageLabel.setText("Password is required.");
        } else if (domainTextInput.getText().length() == 0) {
            errorMessageLabel.setText("Domain is required.");
        } else {
            LoginTask loginTask = new LoginTask();
            loginTask.execute(new TaskListener<Void>() {
                public void taskExecuted(Task<Void> task) {
                    loginButton.setEnabled(true);
                    cardPane.setSelectedIndex(1);
                    listenForMessages();
                }

                public void executeFailed(Task<Void> task) {
                    loginButton.setEnabled(true);
                    errorMessageLabel.setText(task.getFault().getMessage());
                }
            });

            errorMessageLabel.setText(null);
            loginButton.setEnabled(false);
        }
    }

    private void listenForMessages() {
        PacketFilter filter = new PacketTypeFilter(Message.class);

        PacketListener packetListener = new PacketListener() {
            public void processPacket(Packet packet) {
                final Message message = (Message)packet;

                ApplicationContext.queueCallback(new Runnable() {
                    public void run() {
                        // Show the message text
                        String body = message.getBody();
                        messageLabel.setText(body);

                        // Notify the page that a message was received
                        BrowserApplicationContext.eval("messageReceived(\"" + body + "\");", IMClient.this);

                        // Cancel any pending fade and schedule a new fade callback
                        if (scheduledFadeCallback != null) {
                            scheduledFadeCallback.cancel();
                        }

                        scheduledFadeCallback = ApplicationContext.scheduleCallback(new Runnable() {
                            public void run() {
                                FadeTransition fadeTransition = new FadeTransition(messageLabel, 500, 30);

                                fadeTransition.start(new TransitionListener() {
                                    public void transitionCompleted(Transition transition) {
                                        messageLabel.setText(null);
                                    }
                                });
                            }
                        }, 2500);
                    }
                });
            }
        };

        xmppConnection.addPacketListener(packetListener, filter);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(IMClient.class, args);
    }
}
