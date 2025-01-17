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

/**
 * An enumeration of oft-used style names in various components (esp. renderers).
 * <p> Use of this is tied to the {@link Component.StyleDictionary#get(Style)}
 * and {@link Component.StyleDictionary#put(Style,Object)} methods which specifically
 * reference this enumeration.
 * <p> Of course, these values must also correspond to bean methods in the component
 * skins as appropriate.
 */
public enum Style {
    activeBackgroundColor,
    activeColor,
    alignment,
    alignToBaseline,
    alternateRowBackgroundColor,
    alwaysShowScrollButtons,
    backgroundColor,
    borderColor,
    buttonBackgroundColor,
    buttonPadding,
    candlestick,
    checkmarkImage,
    closeTransitionDuration,
    closeTransitionRate,
    color,
    criticalColor,
    disabledColor,
    editOnMouseDown,
    fill,
    font,
    gridFrequency,
    headingColor,
    hideDisabledFiles,
    highlightBackgroundColor,
    highlightColor,
    horizontalAlignment,
    horizontalSpacing,
    inactiveSelectionBackgroundColor,
    inactiveSelectionColor,
    includeTrailingHorizontalGridLine,
    includeTrailingVerticalGridLine,
    keyboardFolderTraversalEnabled,
    margin,
    markerSpacing,
    minimumAspectRatio,
    onlyMaxColor,
    opacity,
    padding,
    resizable,
    selectionBackgroundColor,
    selectionChangeDuration,
    selectionChangeEffect,
    selectionChangeRate,
    selectionColor,
    showBranchControls,
    showEmptyBranchControls,
    showGridLines,
    showHiddenFiles,
    showHighlight,
    showKeyboardShortcuts,
    showVerticalGridLines,
    sizeToContent,
    sizeToSelection,
    spacing,
    tabOrientation,
    tabWidth,
    textColor,
    textDecoration,
    toolbar,
    units,
    useShadow,
    variableItemHeight,
    verticalAlignment,
    verticalGridColor,
    verticalSpacing,
    warningColor,
    wrapText
}
