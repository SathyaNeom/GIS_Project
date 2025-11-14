package com.enbridge.gdsgpscollection.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint

/**
 * State holder for coordinate display information.
 *
 * Manages the display of geographic coordinates, scale, elevation, and accuracy
 * information shown in the coordinate info bar at the bottom of the map.
 *
 * @property x Longitude coordinate as formatted string
 * @property y Latitude coordinate as formatted string
 * @property scale Map scale as formatted string (e.g., "1:50000")
 * @property scaleValue Numeric scale value for programmatic use
 * @property accuracy GPS accuracy in meters
 * @property elevation Elevation above sea level in meters
 * @property currentViewpoint Current map viewpoint for extent calculations
 */
data class CoordinateDisplayState(
    val x: String = "--",
    val y: String = "--",
    val scale: String = "--",
    val scaleValue: Double = 1e8,
    val accuracy: String = "--",
    val elevation: String = "--",
    val currentViewpoint: Viewpoint? = null
)

/**
 * State holder for dialog visibility.
 *
 * Manages the visibility state of all modal dialogs in the map screen.
 * Each dialog should be mutually exclusive to avoid overlapping modals.
 *
 * @property showBasemapDialog True when basemap selector dialog is visible
 * @property showLogoutDialog True when logout confirmation dialog is visible
 * @property showClearDialog True when geodatabase clear confirmation dialog is visible
 */
data class DialogState(
    val showBasemapDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showClearDialog: Boolean = false
)

/**
 * State holder for bottom sheet visibility.
 *
 * Manages the visibility state of all bottom sheets in the map screen.
 * Only one bottom sheet should be visible at a time to maintain proper UX.
 *
 * @property showCollectES True when Collect ES bottom sheet is visible
 * @property showManageES True when Manage ES Edits bottom sheet is visible
 * @property showProjectSettings True when Project Settings bottom sheet is visible
 * @property showTableOfContents True when Table of Contents bottom sheet is visible
 * @property manageESCapturedViewpoint Viewpoint captured at the moment ManageES sheet is opened (for restoration)
 */
data class BottomSheetState(
    val showCollectES: Boolean = false,
    val showManageES: Boolean = false,
    val showProjectSettings: Boolean = false,
    val showTableOfContents: Boolean = false,
    val manageESCapturedViewpoint: Viewpoint? = null
)

/**
 * State holder for map interaction modes.
 *
 * Manages various map interaction states and display modes.
 * These states control how the user interacts with the map and what
 * additional UI elements are displayed.
 *
 * @property identifyMode True when identify mode is active (tap to identify features)
 * @property measurementMode True when measurement mode is active
 * @property isFullscreen True when map is in fullscreen mode (hides coordinate bar)
 * @property isToolbarExpanded True when floating action button toolbar is expanded
 * @property selectedBasemapStyle Currently selected basemap style
 */
data class MapInteractionState(
    val identifyMode: Boolean = false,
    val measurementMode: Boolean = false,
    val isFullscreen: Boolean = false,
    val isToolbarExpanded: Boolean = false,
    val selectedBasemapStyle: BasemapStyle = BasemapStyle.ArcGISTopographic
)

/**
 * Centralized state holder for MainMapScreen.
 *
 * This class consolidates all screen-level state into a single, stable holder
 * following Compose best practices. It groups related state and provides
 * convenience methods for common state updates.
 *
 * ## Usage Example:
 * ```kotlin
 * @Composable
 * fun MainMapScreen() {
 *     val screenState = rememberMainMapScreenState()
 *
 *     // Access state
 *     if (screenState.dialogState.showBasemapDialog) {
 *         BasemapDialog()
 *     }
 *
 *     // Update state
 *     Button(onClick = { screenState.showBasemapDialog() }) {
 *         Text("Choose Basemap")
 *     }
 * }
 * ```
 *
 * ## Design Rationale:
 * - **@Stable annotation**: Ensures Compose can optimize recomposition
 * - **Grouped state**: Related state variables are grouped into logical categories
 * - **Convenience methods**: Simplify common state update patterns
 * - **Immutable data classes**: Nested state is immutable, updates create new copies
 *
 * @property coordinateState State for coordinate display information
 * @property dialogState State for dialog visibility
 * @property bottomSheetState State for bottom sheet visibility
 * @property interactionState State for map interaction modes
 */
@Stable
class MainMapScreenState(
    coordinateState: CoordinateDisplayState = CoordinateDisplayState(),
    dialogState: DialogState = DialogState(),
    bottomSheetState: BottomSheetState = BottomSheetState(),
    interactionState: MapInteractionState = MapInteractionState()
) {
    var coordinateState by mutableStateOf(coordinateState)
        private set

    var dialogState by mutableStateOf(dialogState)
        private set

    var bottomSheetState by mutableStateOf(bottomSheetState)
        private set

    var interactionState by mutableStateOf(interactionState)
        private set

    // Coordinate state updates

    /**
     * Updates coordinate display information.
     *
     * @param x Longitude coordinate
     * @param y Latitude coordinate
     * @param scale Map scale as string
     * @param scaleValue Numeric scale value
     * @param viewpoint Optional viewpoint update
     */
    fun updateCoordinates(
        x: String,
        y: String,
        scale: String,
        scaleValue: Double,
        viewpoint: Viewpoint? = null
    ) {
        coordinateState = coordinateState.copy(
            x = x,
            y = y,
            scale = scale,
            scaleValue = scaleValue,
            currentViewpoint = viewpoint ?: coordinateState.currentViewpoint
        )
    }

    /**
     * Updates the current map viewpoint.
     *
     * @param viewpoint New viewpoint to store
     */
    fun updateViewpoint(viewpoint: Viewpoint?) {
        coordinateState = coordinateState.copy(currentViewpoint = viewpoint)
    }

    // Dialog state management

    /**
     * Shows the basemap selector dialog.
     */
    fun showBasemapDialog() {
        dialogState = dialogState.copy(showBasemapDialog = true)
    }

    /**
     * Dismisses the basemap selector dialog.
     */
    fun dismissBasemapDialog() {
        dialogState = dialogState.copy(showBasemapDialog = false)
    }

    /**
     * Shows the logout confirmation dialog.
     */
    fun showLogoutDialog() {
        dialogState = dialogState.copy(showLogoutDialog = true)
    }

    /**
     * Dismisses the logout confirmation dialog.
     */
    fun dismissLogoutDialog() {
        dialogState = dialogState.copy(showLogoutDialog = false)
    }

    /**
     * Shows the clear data confirmation dialog.
     */
    fun showClearDialog() {
        dialogState = dialogState.copy(showClearDialog = true)
    }

    /**
     * Dismisses the clear data confirmation dialog.
     */
    fun dismissClearDialog() {
        dialogState = dialogState.copy(showClearDialog = false)
    }

    // Bottom sheet state management

    /**
     * Shows the Collect ES bottom sheet.
     * Automatically closes other bottom sheets to maintain single-sheet UX.
     */
    fun showCollectESSheet() {
        bottomSheetState = BottomSheetState(showCollectES = true)
    }

    /**
     * Shows the Manage ES Edits bottom sheet.
     * Automatically closes other bottom sheets to maintain single-sheet UX.
     */
    fun showManageESSheet(viewpoint: Viewpoint? = null) {
        bottomSheetState =
            BottomSheetState(showManageES = true, manageESCapturedViewpoint = viewpoint)
    }

    /**
     * Shows the Project Settings bottom sheet.
     * Automatically closes other bottom sheets to maintain single-sheet UX.
     */
    fun showProjectSettingsSheet() {
        bottomSheetState = BottomSheetState(showProjectSettings = true)
    }

    /**
     * Shows the Table of Contents bottom sheet.
     * Automatically closes other bottom sheets to maintain single-sheet UX.
     */
    fun showTableOfContentsSheet() {
        bottomSheetState = BottomSheetState(showTableOfContents = true)
    }

    /**
     * Dismisses all bottom sheets.
     */
    fun dismissAllBottomSheets() {
        bottomSheetState = BottomSheetState()
    }

    // Map interaction state management

    /**
     * Toggles identify mode on/off.
     * When enabled, user can tap features to identify them.
     *
     * @param enabled True to enable identify mode
     */
    fun setIdentifyMode(enabled: Boolean) {
        interactionState = interactionState.copy(identifyMode = enabled)
    }

    /**
     * Toggles measurement mode on/off.
     * When enabled, user can measure distances on the map.
     *
     * @param enabled True to enable measurement mode
     */
    fun setMeasurementMode(enabled: Boolean) {
        interactionState = interactionState.copy(measurementMode = enabled)
    }

    /**
     * Toggles fullscreen mode on/off.
     * Hides the coordinate info bar when enabled.
     *
     * @param enabled True to enable fullscreen mode
     */
    fun setFullscreen(enabled: Boolean) {
        interactionState = interactionState.copy(isFullscreen = enabled)
    }

    /**
     * Toggles the floating action button toolbar expansion.
     *
     * @param expanded True to expand the toolbar
     */
    fun setToolbarExpanded(expanded: Boolean) {
        interactionState = interactionState.copy(isToolbarExpanded = expanded)
    }

    /**
     * Updates the selected basemap style.
     *
     * @param style New basemap style to apply
     */
    fun updateBasemapStyle(style: BasemapStyle) {
        interactionState = interactionState.copy(selectedBasemapStyle = style)
    }
}

/**
 * Creates and remembers a MainMapScreenState instance.
 *
 * This function should be called at the top of the MainMapScreen composable
 * to create a stable state holder that survives recomposition.
 *
 * ## Usage:
 * ```kotlin
 * @Composable
 * fun MainMapScreen() {
 *     val screenState = rememberMainMapScreenState()
 *     // Use screenState throughout the composable
 * }
 * ```
 *
 * @return A remembered MainMapScreenState instance
 */
@Composable
fun rememberMainMapScreenState(): MainMapScreenState {
    return remember { MainMapScreenState() }
}