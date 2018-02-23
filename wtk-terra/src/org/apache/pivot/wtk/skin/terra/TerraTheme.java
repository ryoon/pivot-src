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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.ColorChooser;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Gauge;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.HyperlinkButton;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.Palette;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VFSBrowser;
import org.apache.pivot.wtk.VFSBrowserSheet;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.util.ColorUtilities;

/**
 * Terra theme.
 */
public final class TerraTheme extends Theme {
    private Font font = null;
    private List<Color> colors = null;
    private int numberOfPaletteColors = 0;
    private Map<MessageType, Image> messageIcons = null;
    private Map<MessageType, Image> smallMessageIcons = null;

    private static float colorMultiplier = 0.1f;
    private static boolean themeIsDark = false;
    private static boolean themeIsFlat = false;
    private static boolean transitionEnabled = true;

    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;

    public static final String LOCATION_PROPERTY = "location";

    public static final String FONT_PROPERTY = "font";
    public static final String COLOR_MULTIPLIER_PROPERTY = "colorMultiplier";
    public static final String THEME_IS_DARK_PROPERTY = "themeIsDark";
    public static final String THEME_IS_FLAT_PROPERTY = "themeIsFlat";
    public static final String TRANSITION_ENABLED_PROPERTY = "transitionEnabled";
    public static final String COLORS_PROPERTY = "colors";
    public static final String DEFAULT_STYLES_PROPERTY = "defaultStylesFile";
    public static final String NAMED_STYLES_PROPERTY = "namedStylesFile";
    public static final String MESSAGE_ICONS_PROPERTY = "messageIcons";
    public static final String SMALL_MESSAGE_ICONS_PROPERTY = "smallMessageIcons";
    public static final String DEFAULT_BACKGROUND_COLOR_PROPERTY = "defaultBackgroundColor";
    public static final String DEFAULT_FOREGROUND_COLOR_PROPERTY = "defaultForegroundColor";

    public static final String COMMAND_BUTTON_STYLE = "commandButton";

    public static final String DEFAULT_STYLES_FILE = "terra_theme_defaults.json";
    public static final String NAMED_STYLES_FILE = "terra_theme_styles.json";

    /** Can be overridden by {@link #DEFAULT_STYLES_PROPERTY} property. */
    private String defaultStylesFile = DEFAULT_STYLES_FILE;
    /** Can be overridden by {@link #NAMED_STYLES_PROPERTY} property. */
    private String namedStylesFile = NAMED_STYLES_FILE;

    private Map<String, Map<String, ?>> themeDefaultStyles = null;


    @SuppressWarnings("unchecked")
    public TerraTheme() {
        // Note: these classes are in addition to the classes listed in Theme's constructor.
        componentSkinMap.put(Accordion.class, TerraAccordionSkin.class);
        componentSkinMap.put(ActivityIndicator.class, TerraActivityIndicatorSkin.class);
        componentSkinMap.put(Alert.class, TerraAlertSkin.class);
        componentSkinMap.put(Border.class, TerraBorderSkin.class);
        componentSkinMap.put(Calendar.class, TerraCalendarSkin.class);
        componentSkinMap.put(CalendarButton.class, TerraCalendarButtonSkin.class);
        componentSkinMap.put(Checkbox.class, TerraCheckboxSkin.class);
        componentSkinMap.put(ColorChooser.class, TerraColorChooserSkin.class);
        componentSkinMap.put(ColorChooserButton.class, TerraColorChooserButtonSkin.class);
        componentSkinMap.put(Dialog.class, TerraDialogSkin.class);
        componentSkinMap.put(Expander.class, TerraExpanderSkin.class);
        componentSkinMap.put(FileBrowser.class, TerraFileBrowserSkin.class);
        componentSkinMap.put(FileBrowserSheet.class, TerraFileBrowserSheetSkin.class);
        componentSkinMap.put(Form.class, TerraFormSkin.class);
        componentSkinMap.put(Frame.class, TerraFrameSkin.class);
        componentSkinMap.put(Gauge.class, TerraGaugeSkin.class);
        componentSkinMap.put(GridPane.class, TerraGridPaneSkin.class);
        componentSkinMap.put(HyperlinkButton.class, TerraLinkButtonSkin.class);
        componentSkinMap.put(Label.class, TerraLabelSkin.class);
        componentSkinMap.put(LinkButton.class, TerraLinkButtonSkin.class);
        componentSkinMap.put(ListButton.class, TerraListButtonSkin.class);
        componentSkinMap.put(ListView.class, TerraListViewSkin.class);
        componentSkinMap.put(Menu.class, TerraMenuSkin.class);
        componentSkinMap.put(Menu.Item.class, TerraMenuItemSkin.class);
        componentSkinMap.put(MenuBar.class, TerraMenuBarSkin.class);
        componentSkinMap.put(MenuBar.Item.class, TerraMenuBarItemSkin.class);
        componentSkinMap.put(MenuButton.class, TerraMenuButtonSkin.class);
        componentSkinMap.put(MenuPopup.class, TerraMenuPopupSkin.class);
        componentSkinMap.put(Meter.class, TerraMeterSkin.class);
        componentSkinMap.put(Palette.class, TerraPaletteSkin.class);
        componentSkinMap.put(Panorama.class, TerraPanoramaSkin.class);
        componentSkinMap.put(Prompt.class, TerraPromptSkin.class);
        componentSkinMap.put(PushButton.class, TerraPushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, TerraRadioButtonSkin.class);
        componentSkinMap.put(Rollup.class, TerraRollupSkin.class);
        componentSkinMap.put(ScrollBar.class, TerraScrollBarSkin.class);
        componentSkinMap.put(ScrollPane.class, TerraScrollPaneSkin.class);
        componentSkinMap.put(ScrollPane.Corner.class, TerraScrollPaneCornerSkin.class);
        componentSkinMap.put(Separator.class, TerraSeparatorSkin.class);
        componentSkinMap.put(Sheet.class, TerraSheetSkin.class);
        componentSkinMap.put(Slider.class, TerraSliderSkin.class);
        componentSkinMap.put(Spinner.class, TerraSpinnerSkin.class);
        componentSkinMap.put(SplitPane.class, TerraSplitPaneSkin.class);
        componentSkinMap.put(SuggestionPopup.class, TerraSuggestionPopupSkin.class);
        componentSkinMap.put(TablePane.class, TerraTablePaneSkin.class);
        componentSkinMap.put(TableViewHeader.class, TerraTableViewHeaderSkin.class);
        componentSkinMap.put(TableView.class, TerraTableViewSkin.class);
        componentSkinMap.put(TabPane.class, TerraTabPaneSkin.class);
        componentSkinMap.put(TextArea.class, TerraTextAreaSkin.class);
        componentSkinMap.put(TextPane.class, TerraTextPaneSkin.class);
        componentSkinMap.put(TextInput.class, TerraTextInputSkin.class);
        componentSkinMap.put(Tooltip.class, TerraTooltipSkin.class);
        componentSkinMap.put(TreeView.class, TerraTreeViewSkin.class);
        componentSkinMap.put(VFSBrowser.class, TerraVFSBrowserSkin.class);
        componentSkinMap.put(VFSBrowserSheet.class, TerraVFSBrowserSheetSkin.class);

        componentSkinMap.put(TerraCalendarSkin.DateButton.class,
            TerraCalendarSkin.DateButtonSkin.class);
        componentSkinMap.put(TerraExpanderSkin.ShadeButton.class,
            TerraExpanderSkin.ShadeButtonSkin.class);
        componentSkinMap.put(TerraFrameSkin.FrameButton.class, TerraFrameSkin.FrameButtonSkin.class);
        componentSkinMap.put(TerraRollupSkin.RollupButton.class,
            TerraRollupSkin.RollupButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.ScrollButton.class,
            TerraScrollBarSkin.ScrollButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.Handle.class, TerraScrollBarSkin.HandleSkin.class);
        componentSkinMap.put(TerraSliderSkin.Thumb.class, TerraSliderSkin.ThumbSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinButton.class,
            TerraSpinnerSkin.SpinButtonSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinnerContent.class,
            TerraSpinnerSkin.SpinnerContentSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.Splitter.class,
            TerraSplitPaneSkin.SplitterSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.SplitterShadow.class,
            TerraSplitPaneSkin.SplitterShadowSkin.class);
        componentSkinMap.put(TerraTabPaneSkin.TabButton.class, TerraTabPaneSkin.TabButtonSkin.class);

        String packageName = getClass().getPackage().getName();

        // Load the color scheme
        String location = null;
        try {
            String locationKey = packageName + "." + LOCATION_PROPERTY;
            location = System.getProperty(locationKey);
        } catch (SecurityException exception) {
            // No-op
        }

        URL locationURL;
        if (location == null) {
            locationURL = getClass().getResource("TerraTheme_default.json");
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (location.startsWith("/")) {
                locationURL = classLoader.getResource(location.substring(1));
            } else {
                locationURL = classLoader.getResource(packageName.replace('.', '/') + "/"
                    + location);
            }
        }

        if (locationURL == null) {
            throw new RuntimeException("Unable to locate color scheme resource \"" + location
                + "\".");
        }

        load(locationURL);

        // Load our theme default styles for each skin class
        themeDefaultStyles = new HashMap<>();
        try (InputStream inputStream = getClass().getResourceAsStream(defaultStylesFile)) {
            JSONSerializer serializer = new JSONSerializer();
            Map<String, ?> terraThemeDefaultStyles = (Map<String, ?>) serializer.readObject(inputStream);

            for (String className : terraThemeDefaultStyles) {
                themeDefaultStyles.put(className, (Map<String, ?>) terraThemeDefaultStyles.get(className));
            }
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // Install named styles
        try (InputStream inputStream = getClass().getResourceAsStream(namedStylesFile)) {
            JSONSerializer serializer = new JSONSerializer();
            Map<String, ?> terraThemeNamedStyles = (Map<String, ?>) serializer.readObject(inputStream);

            for (String name : terraThemeNamedStyles) {
                Component.getNamedStyles().put(packageName + "." + name,
                    (Map<String, ?>) terraThemeNamedStyles.get(name));
            }
        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private Color getColorProperty(Map<String, ?> properties, String colorPropertyName) {
        String colorString = (String) properties.get(colorPropertyName);
        if (colorString != null) {
            return GraphicsUtilities.decodeColor(colorString, colorPropertyName);
        }
        return null;
    }

    private void load(URL location) {
        Utils.checkNull(location, "location");

        try (InputStream inputStream = location.openStream()) {
            JSONSerializer serializer = new JSONSerializer();
            @SuppressWarnings("unchecked")
            Map<String, ?> properties = (Map<String, ?>) serializer.readObject(inputStream);

            font = Font.decode((String) properties.get(FONT_PROPERTY));

            String defaultStylesName = (String) properties.get(DEFAULT_STYLES_PROPERTY);
            if (defaultStylesName != null && !defaultStylesName.isEmpty()) {
                defaultStylesFile = defaultStylesName;
            }
            String namedStylesName = (String) properties.get(NAMED_STYLES_PROPERTY);
            if (namedStylesName != null && !namedStylesName.isEmpty()) {
                namedStylesFile = namedStylesName;
            }

            @SuppressWarnings("unchecked")
            List<String> colorCodes = (List<String>) properties.get(COLORS_PROPERTY);
            numberOfPaletteColors = colorCodes.getLength();
            int numberOfColors = numberOfPaletteColors * 3;
            colors = new ArrayList<>(numberOfColors);

            Double mult = (Double) properties.get(COLOR_MULTIPLIER_PROPERTY);
            if (mult != null) {
                colorMultiplier = mult.floatValue();
            }

            themeIsDark = properties.getBoolean(THEME_IS_DARK_PROPERTY, false);
            themeIsFlat = properties.getBoolean(THEME_IS_FLAT_PROPERTY, false);
            transitionEnabled = properties.getBoolean(TRANSITION_ENABLED_PROPERTY, true);

            for (String colorCode : colorCodes) {
                Color baseColor = GraphicsUtilities.decodeColor(colorCode, "baseColor");
                colors.add(darken(baseColor));
                colors.add(baseColor);
                colors.add(brighten(baseColor));
            }


            @SuppressWarnings("unchecked")
            Map<String, String> messageIconNames = (Map<String, String>) properties.get(MESSAGE_ICONS_PROPERTY);
            messageIcons = new HashMap<>();
            loadMessageIcons(messageIconNames, messageIcons);

            @SuppressWarnings("unchecked")
            Map<String, String> smallMessageIconNames = (Map<String, String>) properties.get(SMALL_MESSAGE_ICONS_PROPERTY);
            smallMessageIcons = new HashMap<>();
            loadMessageIcons(smallMessageIconNames, smallMessageIcons);

            if ((defaultBackgroundColor = getColorProperty(properties, DEFAULT_BACKGROUND_COLOR_PROPERTY)) == null) {
                defaultBackgroundColor = super.getDefaultBackgroundColor();
            }
            if ((defaultForegroundColor = getColorProperty(properties, DEFAULT_FOREGROUND_COLOR_PROPERTY)) == null) {
                defaultForegroundColor = super.getDefaultForegroundColor();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadMessageIcons(Map<String, String> messageIconNames,
        Map<MessageType, Image> messageIconsMap) {
        for (String messageIconType : messageIconNames) {
            String messageIconName = messageIconNames.get(messageIconType);

            Image messageIcon;
            try {
                messageIcon = Image.load(getClass().getResource(messageIconName));
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }

            messageIconsMap.put(MessageType.fromString(messageIconType), messageIcon);
        }
    }

    /**
     * Gets the theme's font.
     */
    @Override
    public Font getFont() {
        return font;
    }

    /**
     * Sets the theme's font.
     *
     * @param font the font
     */
    @Override
    public void setFont(Font font) {
        Utils.checkNull(font, "Font");

        this.font = font;
    }

    private void checkColorIndex(int index, int numberOfColors) {
        if (index < 0 || index >= numberOfColors) {
            throw new IllegalArgumentException("Color index out of range of [0 .. " + (numberOfColors - 1) + "].");
        }
    }

    private void checkColorIndex(int index) {
        checkColorIndex(index, getNumberOfColors());
    }

    /**
     * Gets a value from the theme's complete color palette (including derived
     * colors, if any).
     *
     * @param index the index of the color, starting from 0
     */
    @Override
    public Color getColor(int index) {
        checkColorIndex(index);

        return colors.get(index);
    }

    /**
     * Sets a value in the theme's complete color palette (including derived
     * colors, if any).
     *
     * @param index the index of the color, starting from 0
     * @param color the color to set
     */
    @Override
    public void setColor(int index, Color color) {
        checkColorIndex(index);
        Utils.checkNull(color, "Color");

        colors.update(index, color);
    }

    /**
     * Gets a color from the theme's base color palette.
     *
     * @param index the index of the color, starting from 0
     */
    @Override
    public Color getBaseColor(int index) {
        checkColorIndex(index, numberOfPaletteColors);

        return colors.get(index * 3 + 1);
    }

    /**
     * Sets a color in the theme's base color palette.
     *
     * @param index the index of the color, starting from 0
     * @param baseColor the color to set
     */
    @Override
    public void setBaseColor(int index, Color baseColor) {
        checkColorIndex(index, numberOfPaletteColors);
        Utils.checkNull(baseColor, "Base color");

        int offset = index * 3;
        colors.update(offset, darken(baseColor));
        colors.update(offset + 1, baseColor);
        colors.update(offset + 2, brighten(baseColor));
    }

    /**
     * Gets the number of Palette Colors.
     *
     * @return the number of colors in the base palette
     */
    @Override
    public int getNumberOfPaletteColors() {
        return numberOfPaletteColors;
    }

    /**
     * Gets the total number of Colors (including derived colors, if any).
     *
     * @return the number
     */
    @Override
    public int getNumberOfColors() {
        return colors == null ? 0 : colors.getLength();
    }

    /**
     * Tell if the theme is dark.<br> Usually this means that (if true) any
     * color will be transformed in the opposite way (brightening instead of
     * darkening, and darkening instead of brightening).
     *
     * @return true if dark, false otherwise (default)
     */
    @Override
    public boolean isThemeDark() {
        return themeIsDark;
    }

    /**
     * Tell if the theme is flat.<br> Usually this means that (if true) any
     * border/shadow will not be drawn.
     *
     * @return true if flat, false otherwise (default)
     */
    @Override
    public boolean isThemeFlat() {
        return themeIsFlat;
    }

    /**
     * Tell if the theme has transitions enabled.<br> Usually this means that (if false) any
     * effect/transition will not be drawn.
     *
     * @return true if enabled (default), false otherwise
     */
    @Override
    public boolean isTransitionEnabled() {
        return transitionEnabled;
    }

    /**
     * Gets the image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType The desired message type.
     * @return The icon image for this message type.
     */
    public Image getMessageIcon(MessageType messageType) {
        return messageIcons.get(messageType);
    }

    /**
     * Sets the image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType The message type to change.
     * @param messageIcon The new icon image for this type.
     */
    public void setMessageIcon(MessageType messageType, Image messageIcon) {
        Utils.checkNull(messageType, "Message type");
        Utils.checkNull(messageIcon, "Message icon");

        messageIcons.put(messageType, messageIcon);
    }

    /**
     * Gets the small image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType The message type to query.
     * @return The small image.
     */
    public Image getSmallMessageIcon(MessageType messageType) {
        return smallMessageIcons.get(messageType);
    }

    /**
     * Sets the small image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType      The message type to change.
     * @param smallMessageIcon The new small icon for this type.
     */
    public void setSmallMessageIcon(MessageType messageType, Image smallMessageIcon) {
        Utils.checkNull(messageType, "Message type");
        Utils.checkNull(smallMessageIcon, "Small message icon");

        smallMessageIcons.put(messageType, smallMessageIcon);
    }

    /**
     * Gets the theme's default background color.
     *
     * @return The color if set, or White if the theme is not dark (default), or Black.
     */
    @Override
    public Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    /**
     * Gets the theme's default foreground color.
     *
     * @return The color if set, or Black if the theme is not dark (default), or White.
     */
    @Override
    public Color getDefaultForegroundColor() {
        return defaultForegroundColor;
    }

    /**
     * @return A brighter version of the specified color. Specifically, it
     * increases the brightness (in the HSB color model) by the
     * <tt>colorMultiplier</tt> factor and <tt>themeIsDark</tt> flag already
     * set.
     * @param color The color to brighten.
     */
    public static Color brighten(Color color) {
        if (!themeIsDark) {
            return ColorUtilities.adjustBrightness(color, colorMultiplier);
        }
        return ColorUtilities.adjustBrightness(color, (colorMultiplier * -1.0f));
    }

    /**
     * @return A darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by the
     * <tt>colorMultiplier</tt> factor and <tt>themeDark</tt> flag already set.
     * @param color The color to darken.
     */
    public static Color darken(Color color) {
        if (!themeIsDark) {
            return ColorUtilities.adjustBrightness(color, (colorMultiplier * -1.0f));
        }
        return ColorUtilities.adjustBrightness(color, colorMultiplier);
    }

    /**
     * Set appropriate default styles for the given skin object, specified by the
     * current theme.
     *
     * @param <T>  The skin class whose type we are dealing with.
     * @param skin The skin object of that type whose styles are to be set.
     */
    public <T extends ComponentSkin> void setDefaultStyles(T skin) {
        String className = skin.getClass().getSimpleName();
        @SuppressWarnings("unchecked")
        Map<String, Object> styleMap = (Map<String, Object>)themeDefaultStyles.get(className);
        if (styleMap == null) {
            throw new IllegalArgumentException("Cannot find default styles for class " + className);
        }
        Component component = skin.getComponent();
        Component.StyleDictionary styles = component.getStyles();
        styles.putAll(styleMap);
    }

}
