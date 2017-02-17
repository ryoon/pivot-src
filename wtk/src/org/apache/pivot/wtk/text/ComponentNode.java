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
package org.apache.pivot.wtk.text;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.content.ButtonData;

/**
 * Node representing a live pivot component.
 */
public class ComponentNode extends Node {

    private static class ComponentNodeListenerList extends ListenerList<ComponentNodeListener> implements
        ComponentNodeListener {
        @Override
        public void componentChanged(ComponentNode componentNode, Component previousComponent) {
            for (ComponentNodeListener listener : this) {
                listener.componentChanged(componentNode, previousComponent);
            }
        }
    }

    private Component component = null;

    private ComponentNodeListenerList componentNodeListeners = new ComponentNodeListenerList();

    public ComponentNode() {
    }

    public ComponentNode(ComponentNode componentNode) {
        setComponent(componentNode.getComponent());
    }

    public ComponentNode(Component component) {
        setComponent(component);
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(Component component) {
        Component previousComponent = this.component;

        if (previousComponent != component) {
            this.component = component;
            componentNodeListeners.componentChanged(this, previousComponent);
        }
    }

    public String getText() {
        return getText(this.component);
    }

    private String getText(Component comp) {
        if (comp instanceof TextInput) {
            return ((TextInput)comp).getText();
        } else if (comp instanceof TextArea) {
            return ((TextArea)comp).getText();
        } else if (comp instanceof TextPane) {
            return ((TextPane)comp).getText();
        } else if (comp instanceof Label) {
            return ((Label)comp).getText();
        } else if (comp instanceof Button) {
            Object buttonData = ((Button)comp).getButtonData();
            if (buttonData instanceof ButtonData) {
                return ((ButtonData)buttonData).getText();
            } else if (buttonData instanceof String) {
                return (String)buttonData;
            } else {
                return buttonData.toString();
            }
        } else if (comp instanceof Container) {
            StringBuilder buf = new StringBuilder();
            for (Component child : (Container)comp) {
                buf.append(getText(child));
            }
            return buf.toString();
        }
        return "";
    }

    public String getSubstring(Span range) {
        return getText(this.component).substring(range.start, range.end + 1);
    }

    public String getSubstring(int start, int end) {
        return getText(this.component).substring(start, end);
    }

    public CharSequence getCharacters(Span range) {
        return getText(this.component).subSequence(range.start, range.end + 1);
    }

    public CharSequence getCharacters(int start, int end) {
        return getText(this.component).subSequence(start, end);
    }

    @Override
    public char getCharacterAt(int offset) {
        return getText(this.component).charAt(offset);
    }

    @Override
    public int getCharacterCount() {
        return getText(this.component).length();
    }

    @Override
    public void insertRange(Node range, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node removeRange(int offset, int span) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        // Note: only supports getting the complete range of text
        String componentText = getText();
        if (offset < 0 || offset >= componentText.length()) {
            throw new IndexOutOfBoundsException();
        }

        if (characterCount != 1) {
            throw new IllegalArgumentException("Invalid characterCount.");
        }

        return new ComponentNode(this);
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new ComponentNode(this);
    }

    public ListenerList<ComponentNodeListener> getComponentNodeListeners() {
        return componentNodeListeners;
    }
}
