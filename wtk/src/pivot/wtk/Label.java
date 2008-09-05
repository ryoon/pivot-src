/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.Dictionary;
import pivot.util.ListenerList;

/**
 * Component representing a displayable string of text.
 *
 * @author gbrown
 *
 */
@ComponentInfo(icon="Label.png")
public class Label extends Component {
    private static class LabelListenerList extends ListenerList<LabelListener>
        implements LabelListener {
        public void textChanged(Label label, String previousText) {
            for (LabelListener listener : this) {
                listener.textChanged(label, previousText);
            }
        }

        public void textKeyChanged(Label label, String previousTextKey) {
            for (LabelListener listener : this) {
                listener.textKeyChanged(label, previousTextKey);
            }
        }
    }

    private String text = null;
    private String textKey = null;
    private LabelListenerList labelListeners = new LabelListenerList();

    public Label() {
        this(null);
    }

    public Label(String text) {
        this.text = text;

        installSkin(Label.class);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        // TODO We may not want to pass the previous value here, since we
        // don't actually compare values to determine a change

        String previousText = this.text;
        this.text = text;
        labelListeners.textChanged(this, previousText);
    }

    /**
     * Returns the label's text key.
     *
     * @return
     * The text key, or <tt>null</tt> if no text key is set.
     */
    public String getTextKey() {
        return textKey;
    }

    /**
     * Sets the label's text key.
     *
     * @param textKey
     * The text key, or <tt>null</tt> to clear the binding.
     */
    public void setTextKey(String textKey) {
        String previousTextKey = this.textKey;

        if ((previousTextKey != null
            && textKey != null
            && !previousTextKey.equals(textKey))
            || previousTextKey != textKey) {
            this.textKey = textKey;
            labelListeners.textKeyChanged(this, previousTextKey);
        }
    }

    @Override
    public void load(Dictionary<String, Object> context) {
        if (textKey != null
            && context.containsKey(textKey)) {
            Object value = context.get(textKey);
            if (value != null) {
                setText(value.toString());
            }
        }
    }

    @Override
    public void store(Dictionary<String, Object> context) {
        if (textKey != null) {
            context.put(textKey, getText());
        }
    }

    public ListenerList<LabelListener> getLabelListeners() {
        return labelListeners;
    }
}
