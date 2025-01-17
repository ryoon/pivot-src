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
import org.apache.pivot.wtk.FontUtilities;
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
import org.apache.pivot.wtk.ThemeNotFoundException;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VFSBrowser;
import org.apache.pivot.wtk.VFSBrowserSheet;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.util.ColorUtilities;

/**
 * The default (and only) Pivot theme ("Terra"), which is highly customizable
 * as far as color and style using configuration files and runtime attributes.
 */
public final class TerraTheme extends Theme {
    /** The current font used by this theme. */
    private Font font = null;
    /** The current color palette for the theme. */
    private Color[] colors = null;
    /** The number of base colors in the theme palette. */
    private int numberOfPaletteColors = 0;
    /** Map of message type to icon images. */
    private Map<MessageType, Image> messageIcons = null;
    /** Map of message types to small icon images. */
    private Map<MessageType, Image> smallMessageIcons = null;

    /** The default color multiplier (in HSV space) to use for {@link #brighten} and {@link #darken}. */
    private static final float DEFAULT_COLOR_MULTIPLIER = 0.1f;
    /** Number of columns in the theme's color table (darker, base, lighter). */
    private static final int COLOR_TABLE_COLUMNS = 3;

    /** Current multiplier (HSV space) for brightening colors. */
    private static float brightenColorMultiplier = DEFAULT_COLOR_MULTIPLIER;
    /** Current multiplier (HSV space) for darkening colors. */
    private static float darkenColorMultiplier;
    /** Whether this theme is a "dark" theme -- meaning bright colors on a dark background. */
    private static boolean themeIsDark = false;
    /** Whether this theme is "flat" -- meaning no shadows or depth effect for buttons, etc. */
    private static boolean themeIsFlat = false;
    /** Whether this theme allows animations during transitions. */
    private static boolean transitionEnabled = true;
    /** Whether this theme draws "thick" focus rectangles. */
    private static boolean thickFocusRectangle = true;

    /** What the default background of a component should be according to this theme. */
    private Color defaultBackgroundColor;
    /** What the default foreground color of a component should be in this theme. */
    private Color defaultForegroundColor;

    /** Property map key for the location of the theme classes. */
    public static final String LOCATION_PROPERTY = "location";

    /** Property map key for the default font for the theme. */
    public static final String FONT_PROPERTY = "font";
    /** Property map key for the default color multiplier to use for the theme. */
    public static final String COLOR_MULTIPLIER_PROPERTY = "colorMultiplier";
    /** Property map key for the "isDark" flag for the theme. */
    public static final String THEME_IS_DARK_PROPERTY = "themeIsDark";
    /** Property map key for the "isFlat" flag for this theme. */
    public static final String THEME_IS_FLAT_PROPERTY = "themeIsFlat";
    /** Property map key for the "transitionEnabled" flag for the theme. */
    public static final String TRANSITION_ENABLED_PROPERTY = "transitionEnabled";
    /** Property map key for the "thickFocusRectangle" flag for the theme. */
    public static final String THICK_FOCUS_RECTANGLE_PROPERTY = "thickFocusRectangle";
    /** Property map key for the list of base colors for the theme. */
    public static final String COLORS_PROPERTY = "colors";
    /** Property map key for the name of the default styles file. */
    public static final String DEFAULT_STYLES_PROPERTY = "defaultStylesFile";
    /** Property map key for the name of the named styles file. */
    public static final String NAMED_STYLES_PROPERTY = "namedStylesFile";
    /** Property map key for the message icons. */
    public static final String MESSAGE_ICONS_PROPERTY = "messageIcons";
    /** Property map key for the small message icons. */
    public static final String SMALL_MESSAGE_ICONS_PROPERTY = "smallMessageIcons";
    /** Property map key for the default background color for this theme. */
    public static final String DEFAULT_BACKGROUND_COLOR_PROPERTY = "defaultBackgroundColor";
    /** Property map key for the default foreground color for the theme. */
    public static final String DEFAULT_FOREGROUND_COLOR_PROPERTY = "defaultForegroundColor";

    /** The style name for our "command" buttons (such as "OK" and "Cancel"). */
    public static final String COMMAND_BUTTON_STYLE = "commandButton";

    /** Name of the file to read for setting default styles for each component skin. */
    public static final String DEFAULT_STYLES_FILE = "terra_theme_defaults.json";
    /** Name of the file containing our theme's default style definitions. */
    public static final String NAMED_STYLES_FILE = "terra_theme_styles.json";

    /** Can be overridden by {@link #DEFAULT_STYLES_PROPERTY} property. */
    private String defaultStylesFile = DEFAULT_STYLES_FILE;
    /** Can be overridden by {@link #NAMED_STYLES_PROPERTY} property. */
    private String namedStylesFile = NAMED_STYLES_FILE;

    /** The loaded default styles map (key is the skin's simple name). */
    private Map<String, Map<String, ?>> themeDefaultStyles = null;


    /**
     * Construct the "Terra" theme and set the default skin classes for each component.
     */
    @SuppressWarnings("unchecked")
    public TerraTheme() {
        // Note: these classes are in addition to the classes listed in Theme's constructor.
        set(Accordion.class, TerraAccordionSkin.class);
        set(ActivityIndicator.class, TerraActivityIndicatorSkin.class);
        set(Alert.class, TerraAlertSkin.class);
        set(Border.class, TerraBorderSkin.class);
        set(Calendar.class, TerraCalendarSkin.class);
        set(CalendarButton.class, TerraCalendarButtonSkin.class);
        set(Checkbox.class, TerraCheckboxSkin.class);
        set(ColorChooser.class, TerraColorChooserSkin.class);
        set(ColorChooserButton.class, TerraColorChooserButtonSkin.class);
        set(Dialog.class, TerraDialogSkin.class);
        set(Expander.class, TerraExpanderSkin.class);
        set(FileBrowser.class, TerraFileBrowserSkin.class);
        set(FileBrowserSheet.class, TerraFileBrowserSheetSkin.class);
        set(Form.class, TerraFormSkin.class);
        set(Frame.class, TerraFrameSkin.class);
        set(Gauge.class, TerraGaugeSkin.class);
        set(GridPane.class, TerraGridPaneSkin.class);
        set(HyperlinkButton.class, TerraLinkButtonSkin.class);
        set(Label.class, TerraLabelSkin.class);
        set(LinkButton.class, TerraLinkButtonSkin.class);
        set(ListButton.class, TerraListButtonSkin.class);
        set(ListView.class, TerraListViewSkin.class);
        set(Menu.class, TerraMenuSkin.class);
        set(Menu.Item.class, TerraMenuItemSkin.class);
        set(MenuBar.class, TerraMenuBarSkin.class);
        set(MenuBar.Item.class, TerraMenuBarItemSkin.class);
        set(MenuButton.class, TerraMenuButtonSkin.class);
        set(MenuPopup.class, TerraMenuPopupSkin.class);
        set(Meter.class, TerraMeterSkin.class);
        set(Palette.class, TerraPaletteSkin.class);
        set(Panorama.class, TerraPanoramaSkin.class);
        set(Prompt.class, TerraPromptSkin.class);
        set(PushButton.class, TerraPushButtonSkin.class);
        set(RadioButton.class, TerraRadioButtonSkin.class);
        set(Rollup.class, TerraRollupSkin.class);
        set(ScrollBar.class, TerraScrollBarSkin.class);
        set(ScrollPane.class, TerraScrollPaneSkin.class);
        set(ScrollPane.Corner.class, TerraScrollPaneCornerSkin.class);
        set(Separator.class, TerraSeparatorSkin.class);
        set(Sheet.class, TerraSheetSkin.class);
        set(Slider.class, TerraSliderSkin.class);
        set(Spinner.class, TerraSpinnerSkin.class);
        set(SplitPane.class, TerraSplitPaneSkin.class);
        set(SuggestionPopup.class, TerraSuggestionPopupSkin.class);
        set(TablePane.class, TerraTablePaneSkin.class);
        set(TableViewHeader.class, TerraTableViewHeaderSkin.class);
        set(TableView.class, TerraTableViewSkin.class);
        set(TabPane.class, TerraTabPaneSkin.class);
        set(TextArea.class, TerraTextAreaSkin.class);
        set(TextPane.class, TerraTextPaneSkin.class);
        set(TextInput.class, TerraTextInputSkin.class);
        set(Tooltip.class, TerraTooltipSkin.class);
        set(TreeView.class, TerraTreeViewSkin.class);
        set(VFSBrowser.class, TerraVFSBrowserSkin.class);
        set(VFSBrowserSheet.class, TerraVFSBrowserSheetSkin.class);

        set(TerraCalendarSkin.DateButton.class, TerraCalendarSkin.DateButtonSkin.class);
        set(TerraExpanderSkin.ShadeButton.class, TerraExpanderSkin.ShadeButtonSkin.class);
        set(TerraFrameSkin.FrameButton.class, TerraFrameSkin.FrameButtonSkin.class);
        set(TerraRollupSkin.RollupButton.class, TerraRollupSkin.RollupButtonSkin.class);
        set(TerraScrollBarSkin.ScrollButton.class, TerraScrollBarSkin.ScrollButtonSkin.class);
        set(TerraScrollBarSkin.Handle.class, TerraScrollBarSkin.HandleSkin.class);
        set(TerraSliderSkin.Thumb.class, TerraSliderSkin.ThumbSkin.class);
        set(TerraSpinnerSkin.SpinButton.class, TerraSpinnerSkin.SpinButtonSkin.class);
        set(TerraSpinnerSkin.SpinnerContent.class, TerraSpinnerSkin.SpinnerContentSkin.class);
        set(TerraSplitPaneSkin.Splitter.class, TerraSplitPaneSkin.SplitterSkin.class);
        set(TerraSplitPaneSkin.SplitterShadow.class, TerraSplitPaneSkin.SplitterShadowSkin.class);
        set(TerraTabPaneSkin.TabButton.class, TerraTabPaneSkin.TabButtonSkin.class);

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
                locationURL = classLoader.getResource(packageName.replace('.', '/') + "/" + location);
            }
        }

        if (locationURL == null) {
            throw new ThemeNotFoundException("Unable to locate color scheme resource \"" + location + "\".");
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

    /**
     * Get the given color property from the color properties map, doing the color decode from
     * the string property value. If the property is not found, return the given default value.
     *
     * @param properties The color properties map.
     * @param colorPropertyName Name of the color property to get.
     * @param defaultColor The value to use if the property is not found in the map.
     * @return The decoded color value or the default value if not found.
     * @see GraphicsUtilities#decodeColor
     */
    private Color getColorProperty(final Map<String, ?> properties, final String colorPropertyName,
        final Color defaultColor) {
        Color propertyColor = getColorProperty(properties, colorPropertyName);
        return propertyColor == null ? defaultColor : propertyColor;
    }

    /**
     * Get the given color property from the color properties map, doing the color decode from
     * the string property value.
     *
     * @param properties The color properties map.
     * @param colorPropertyName Name of the color property to get.
     * @return The decoded color value or {@code null} if the property by that name cannot be found.
     * @see GraphicsUtilities#decodeColor
     */
    private Color getColorProperty(final Map<String, ?> properties, final String colorPropertyName) {
        String colorString = (String) properties.get(colorPropertyName);
        if (colorString != null) {
            return GraphicsUtilities.decodeColor(colorString, colorPropertyName);
        }
        return null;
    }

    /**
     * Load the theme from the configuration file at the given URL location.
     *
     * @param location Where to load the theme configuration from.
     */
    private void load(final URL location) {
        Utils.checkNull(location, "location");

        try (InputStream inputStream = location.openStream()) {
            JSONSerializer serializer = new JSONSerializer();
            @SuppressWarnings("unchecked")
            Map<String, ?> properties = (Map<String, ?>) serializer.readObject(inputStream);

            font = FontUtilities.decodeFont((String) properties.get(FONT_PROPERTY));

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
            int numberOfColors = numberOfPaletteColors * COLOR_TABLE_COLUMNS;
            colors = new Color[numberOfColors];

            Double mult = (Double) properties.get(COLOR_MULTIPLIER_PROPERTY);
            if (mult != null) {
                brightenColorMultiplier = mult.floatValue();
            }

            themeIsDark = properties.getBoolean(THEME_IS_DARK_PROPERTY, false);
            themeIsFlat = properties.getBoolean(THEME_IS_FLAT_PROPERTY, false);
            transitionEnabled = properties.getBoolean(TRANSITION_ENABLED_PROPERTY, true);
            thickFocusRectangle = properties.getBoolean(THICK_FOCUS_RECTANGLE_PROPERTY, true);

            // For dark themes, reverse the brighten/darken multipliers so "darker" -> brighter, and vice-versa
            if (themeIsDark) {
                brightenColorMultiplier = -brightenColorMultiplier;
            }
            // So, "darken" is always the reverse direction of "brighten" (whichever way that goes)
            darkenColorMultiplier = -brightenColorMultiplier;

            int colorIndex = 0;
            for (String colorCode : colorCodes) {
                Color baseColor = GraphicsUtilities.decodeColor(colorCode, "baseColor");
                setBaseColor(colorIndex++, baseColor);
            }

            messageIcons = loadMessageIcons(properties, MESSAGE_ICONS_PROPERTY);
            smallMessageIcons = loadMessageIcons(properties, SMALL_MESSAGE_ICONS_PROPERTY);

            defaultBackgroundColor = getColorProperty(properties, DEFAULT_BACKGROUND_COLOR_PROPERTY,
                super.getDefaultBackgroundColor());
            defaultForegroundColor = getColorProperty(properties, DEFAULT_FOREGROUND_COLOR_PROPERTY,
                super.getDefaultForegroundColor());

        } catch (IOException | SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Load the default set of message icons for the given type.
     *
     * @param properties The theme properties map.
     * @param propertyName The name of the property containing the icons.
     * @return The map of icons for each {@link MessageType}.
     */
    private Map<MessageType, Image> loadMessageIcons(final Map<String, ?> properties, final String propertyName) {
        @SuppressWarnings("unchecked")
        Map<String, String> messageIconNames = (Map<String, String>) properties.get(propertyName);
        Map<MessageType, Image> messageIconsMap = new HashMap<>();

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
        return messageIconsMap;
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
     * @param newFont The new font to use for this theme.
     */
    @Override
    public void setFont(final Font newFont) {
        Utils.checkNull(newFont, "Font");

        this.font = newFont;
    }

    /**
     * Check the given color index against the total number of colors.
     * @param index Index into the theme color chart.
     * @param numberOfColors the total size of the color chart.
     * @throws IllegalArgumentException if the index is out of range.
     */
    private void checkColorIndex(final int index, final int numberOfColors) {
        if (index < 0 || index >= numberOfColors) {
            throw new IllegalArgumentException("Color index out of range of [0 .. " + (numberOfColors - 1) + "].");
        }
    }

    /**
     * Check the given color index against the {@link #getNumberOfColors} value.
     * @param index Index into the theme color chart.
     * @throws IllegalArgumentException if the index is out of range.
     */
    private void checkColorIndex(final int index) {
        checkColorIndex(index, getNumberOfColors());
    }

    /**
     * Gets a value from the theme's complete color palette (including derived
     * colors, if any).
     *
     * @param index the index of the color, starting from 0
     */
    @Override
    public Color getColor(final int index) {
        checkColorIndex(index);

        return colors[index];
    }

    /**
     * Sets a value in the theme's complete color palette (including derived
     * colors, if any).
     *
     * @param index the index of the color, starting from 0
     * @param color the color to set
     */
    @Override
    public void setColor(final int index, final Color color) {
        checkColorIndex(index);
        Utils.checkNull(color, "Color");

        colors[index] = color;
    }

    /**
     * Gets a color from the theme's base color palette.
     *
     * @param index the index of the color, starting from 0
     */
    @Override
    public Color getBaseColor(final int index) {
        checkColorIndex(index, numberOfPaletteColors);

        return colors[index * COLOR_TABLE_COLUMNS + 1];
    }

    /**
     * Sets a color in the theme's base color palette.
     *
     * @param index the index of the color, starting from 0
     * @param baseColor the color to set
     */
    @Override
    public void setBaseColor(final int index, final Color baseColor) {
        checkColorIndex(index, numberOfPaletteColors);
        Utils.checkNull(baseColor, "Base color");

        int offset = index * COLOR_TABLE_COLUMNS + 1;
        colors[offset - 1] = darken(baseColor);
        colors[offset] = baseColor;
        colors[offset + 1] = brighten(baseColor);
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
     * @return Total number of theme colors.
     */
    @Override
    public int getNumberOfColors() {
        return colors == null ? 0 : colors.length;
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
     * Tell if the theme has thick focus rectangles, which may be more visible in some
     * color schemes, or some screen resolutions.
     *
     * @return {@code true} if the focus rectangle should be thick, or {@code false}
     * for thin (one-pixel) ones (the default for earlier versions).
     */
    @Override
    public boolean isThickFocusRectangle() {
        return thickFocusRectangle;
    }

    /**
     * Gets the image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType The desired message type.
     * @return The icon image for this message type.
     */
    public Image getMessageIcon(final MessageType messageType) {
        return messageIcons.get(messageType);
    }

    /**
     * Sets the image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType The message type to change.
     * @param messageIcon The new icon image for this type.
     */
    public void setMessageIcon(final MessageType messageType, final Image messageIcon) {
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
    public Image getSmallMessageIcon(final MessageType messageType) {
        return smallMessageIcons.get(messageType);
    }

    /**
     * Sets the small image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType      The message type to change.
     * @param smallMessageIcon The new small icon for this type.
     */
    public void setSmallMessageIcon(final MessageType messageType, final Image smallMessageIcon) {
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
     * {@link #brightenColorMultiplier} factor already set.
     * @param color The color to brighten.
     */
    public static Color brighten(final Color color) {
        return ColorUtilities.adjustBrightness(color, brightenColorMultiplier);
    }

    /**
     * @return A darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by the
     * {@link #darkenColorMultiplier} factor already set.
     * @param color The color to darken.
     */
    public static Color darken(final Color color) {
        return ColorUtilities.adjustBrightness(color, darkenColorMultiplier);
    }

    /**
     * Set appropriate default styles for the given skin object, specified by the
     * current theme.
     *
     * @param <T>  The skin class whose type we are dealing with.
     * @param skin The skin object of that type whose styles are to be set.
     * @throws ThemeNotFoundException if the default styles cannot be located for the skin.
     */
    public <T extends ComponentSkin> void setDefaultStyles(final T skin) {
        String className = skin.getClass().getSimpleName();
        @SuppressWarnings("unchecked")
        Map<String, Object> defaultStyleMap = (Map<String, Object>) themeDefaultStyles.get(className);
        if (defaultStyleMap == null) {
            throw new ThemeNotFoundException("Cannot find default styles for class " + className);
        }
        skin.getComponent().getStyles().putAll(defaultStyleMap);
    }

}
