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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PromptListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TextDecoration;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

/**
 * Prompt skin.
 */
public class TerraPromptSkin extends TerraSheetSkin implements PromptListener {
    /**
     * The image view that contains the prompt icon.
     */
    private ImageView typeImageView = null;
    /**
     * Label containing the message text.
     */
    private Label messageLabel = null;
    /**
     * Box pane that contains the prompt body.
     */
    private BoxPane messageBoxPane = null;
    /**
     * Box pane containing the option buttons.
     */
    private BoxPane optionButtonBoxPane = null;
    /**
     * The command button style for the option buttons.
     */
    private static final String BUTTON_STYLE_NAME =
            TerraPromptSkin.class.getPackage().getName() + "." + TerraTheme.COMMAND_BUTTON_STYLE;

    /**
     * Button press listener to select the prompt response and close the prompt.
     */
    private ButtonPressListener optionButtonPressListener = new ButtonPressListener() {
        @Override
        public void buttonPressed(final Button button) {
            int optionIndex = optionButtonBoxPane.indexOf(button);

            if (optionIndex >= 0) {
                Prompt prompt = (Prompt) getComponent();
                prompt.setSelectedOptionIndex(optionIndex);
                prompt.close(true);
            }
        }
    };

    /**
     * Default constructor.
     */
    public TerraPromptSkin() {
        setResizable(true);
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        Prompt prompt = (Prompt) component;
        prompt.setPreferredWidth(320);
        prompt.setMinimumWidth(160);

        prompt.getPromptListeners().add(this);

        // Load the prompt content
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        Component content;
        try {
            content = (Component) bxmlSerializer.readObject(TerraPromptSkin.class,
                "terra_prompt_skin.bxml");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        prompt.setContent(content);

        typeImageView = (ImageView) bxmlSerializer.getNamespace().get("typeImageView");
        messageLabel = (Label) bxmlSerializer.getNamespace().get("messageLabel");
        messageBoxPane = (BoxPane) bxmlSerializer.getNamespace().get("messageBoxPane");
        optionButtonBoxPane = (BoxPane) bxmlSerializer.getNamespace().get("optionButtonBoxPane");

        for (Object option : prompt.getOptions()) {
            PushButton optionButton = new PushButton(option);
            optionButton.setStyleName(BUTTON_STYLE_NAME);
            optionButton.getButtonPressListeners().add(optionButtonPressListener);

            optionButtonBoxPane.add(optionButton);
        }

        messageTypeChanged(prompt, null);
        messageChanged(prompt, null);
        bodyChanged(prompt, null);
    }

    @Override
    public void windowOpened(final Window window) {
        super.windowOpened(window);

        Prompt prompt = (Prompt) window;
        int index = prompt.getSelectedOptionIndex();

        if (index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        } else {
            window.requestFocus();
        }
    }

    @Override
    public void messageTypeChanged(final Prompt prompt, final MessageType previousMessageType) {
        TerraTheme theme = (TerraTheme) Theme.getTheme();
        typeImageView.setImage(theme.getMessageIcon(prompt.getMessageType()));
    }

    @Override
    public void messageChanged(final Prompt prompt, final String previousMessage) {
        String message = prompt.getMessage();
        messageLabel.setText(message != null ? message : "");
    }

    @Override
    public void bodyChanged(final Prompt prompt, final Component previousBody) {
        if (previousBody != null) {
            messageBoxPane.remove(previousBody);
        }

        Component body = prompt.getBody();
        if (body != null) {
            messageBoxPane.add(body);
        }
    }

    @Override
    public void optionInserted(final Prompt prompt, final int index) {
        Object option = prompt.getOptions().get(index);

        PushButton optionButton = new PushButton(option);
        optionButton.setStyleName(BUTTON_STYLE_NAME);
        optionButton.getButtonPressListeners().add(optionButtonPressListener);

        optionButtonBoxPane.insert(optionButton, index);
    }

    @Override
    public void optionsRemoved(final Prompt prompt, final int index, final Sequence<?> removed) {
        optionButtonBoxPane.remove(index, removed.getLength());
    }

    @Override
    public void selectedOptionChanged(final Prompt prompt, final int previousSelectedOption) {
        int index = prompt.getSelectedOptionIndex();

        if (prompt.isOpen() && index >= 0) {
            optionButtonBoxPane.get(index).requestFocus();
        }
    }

    @Override
    public Vote previewWindowOpen(final Window window) {
        Vote vote = super.previewWindowOpen(window);
        if (vote == Vote.APPROVE) {
            // If this is the second or subsequent open, then the
            // image view has been cleared, so set it up again
            messageTypeChanged((Prompt) window, null);
        }
        return vote;
    }

    @Override
    public void sheetClosed(final Sheet sheet) {
        super.sheetClosed(sheet);
        typeImageView.clearImage();
    }

    /**
     * @return The font used to render the message label's text.
     */
    public final Font getMessageFont() {
        return messageLabel.getStyles().getFont(Style.font);
    }

    /**
     * Sets the font used to render the message label's text.
     *
     * @param font The new font used to render the message label text.
     */
    public final void setMessageFont(final Font font) {
        messageLabel.getStyles().put(Style.font, font);
    }

    /**
     * Sets the font used in rendering the message label's text.
     *
     * @param font A font specification.
     */
    public final void setMessageFont(final String font) {
        messageLabel.getStyles().put(Style.font, font);
    }

    /**
     * Sets the font used in rendering the message label's text.
     *
     * @param font A dictionary describing a font.
     */
    public final void setMessageFont(final Dictionary<String, ?> font) {
        messageLabel.getStyles().put(Style.font, font);
    }

    /**
     * @return The foreground color of the text of the message label.
     */
    public final Color getMessageColor() {
        return messageLabel.getStyles().getColor(Style.color);
    }

    /**
     * Sets the foreground color of the text of the message label.
     *
     * @param color The new foreground color for the label text.
     */
    public final void setMessageColor(final Color color) {
        messageLabel.getStyles().put(Style.color, color);
    }

    /**
     * Sets the foreground color of the text of the message label.
     *
     * @param color Any of the recognized color values.
     */
    public final void setMessageColor(final String color) {
        messageLabel.getStyles().put(Style.color, color);
    }

    /**
     * @return The background color of the message label.
     */
    public final Color getMessageBackgroundColor() {
        return messageLabel.getStyles().getColor(Style.backgroundColor);
    }

    /**
     * Sets the background color of the message label.
     *
     * @param backgroundColor The new background color for the message label
     * (can be {@code null} to let the parent background show through).
     */
    public final void setMessageBackgroundColor(final Color backgroundColor) {
        messageLabel.getStyles().put(Style.backgroundColor, backgroundColor);
    }

    /**
     * Sets the background color of the message label.
     *
     * @param backgroundColor Any of the recognized color values.
     */
    public final void setMessageBackgroundColor(final String backgroundColor) {
        messageLabel.getStyles().put(Style.backgroundColor, backgroundColor);
    }

    /**
     * @return The text decoration of the message label.
     */
    public final TextDecoration getMessageTextDecoration() {
        return (TextDecoration) messageLabel.getStyles().get(Style.textDecoration);
    }

    /**
     * Set the text decoration for the message label.
     *
     * @param textDecoration The text decoration for the message label.
     */
    public final void setMessageTextDecoration(final TextDecoration textDecoration) {
        messageLabel.getStyles().put(Style.textDecoration, textDecoration);
    }

}
