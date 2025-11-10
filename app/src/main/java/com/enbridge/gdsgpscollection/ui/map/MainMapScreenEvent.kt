package com.enbridge.gdsgpscollection.ui.map

import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance

/**
 * Sealed interface defining all possible events that can occur in MainMapScreen.
 *
 * ## EVENT SYSTEM ARCHITECTURE GUIDE
 *
 * ### What is the Event System?
 * The event system is a unidirectional data flow pattern that separates **user actions** from
 * **business logic**. Instead of passing multiple callback functions down the component tree,
 * we pass a single event handler that processes all events.
 *
 * ### Why Use Events Instead of Callbacks?
 *
 * **❌ Problem with Multiple Callbacks:**
 * ```kotlin
 * @Composable
 * fun MyScreen(
 *     onButtonClick: () -> Unit,
 *     onItemSelected: (Item) -> Unit,
 *     onDelete: (Int) -> Unit,
 *     onUpdate: (Data) -> Unit,
 *     onNavigate: (Route) -> Unit,
 *     // ... 20 more callbacks
 * )
 * ```
 * **Issues:**
 * - Too many parameters (violates Clean Code principles)
 * - Difficult to test and mock
 * - Hard to trace event flow
 * - Parameter drilling through component hierarchy
 *
 * **✅ Solution with Events:**
 * ```kotlin
 * @Composable
 * fun MyScreen(
 *     onEvent: (MyScreenEvent) -> Unit
 * )
 * ```
 * **Benefits:**
 * - Single parameter (follows Interface Segregation Principle)
 * - Easy to test and mock
 * - Clear event tracking and logging
 * - Type-safe event handling
 * - Extensible without changing function signatures
 *
 * ### How to Use This Event System
 *
 * #### 1. Trigger Events from UI Components
 * ```kotlin
 * @Composable
 * fun MainMapScreen(onEvent: (MainMapScreenEvent) -> Unit) {
 *     Button(onClick = {
 *         onEvent(MainMapScreenEvent.ShowCollectESSheet)
 *     }) {
 *         Text("Collect ES")
 *     }
 * }
 * ```
 *
 * #### 2. Handle Events in Parent Composable or Activity
 * ```kotlin
 * MainMapScreen(
 *     onEvent = { event ->
 *         when (event) {
 *             is MainMapScreenEvent.ShowCollectESSheet -> {
 *                 // Handle the event
 *                 screenState.showCollectESSheet()
 *             }
 *             is MainMapScreenEvent.ShowSnackbar -> {
 *                 scope.launch {
 *                     snackbarHostState.showSnackbar(event.message)
 *                 }
 *             }
 *             // Handle all other events
 *         }
 *     }
 * )
 * ```
 *
 * #### 3. Events with Data
 * ```kotlin
 * // Trigger event with data
 * onEvent(MainMapScreenEvent.OnGeodatabaseDownloaded(geodatabase))
 *
 * // Handle event with data
 * is MainMapScreenEvent.OnGeodatabaseDownloaded -> {
 *     viewModel.loadLayers(event.geodatabase)
 * }
 * ```
 *
 * ### Adding New Events (Developer Guide)
 *
 * When adding new features to MainMapScreen, follow these steps:
 *
 * **Step 1: Define the Event**
 * ```kotlin
 * sealed interface MainMapScreenEvent {
 *     // Existing events...
 *
 *     // Add your new event
 *     data class OnNewFeature(val data: String) : MainMapScreenEvent
 * }
 * ```
 *
 * **Step 2: Trigger the Event**
 * ```kotlin
 * Button(onClick = {
 *     onEvent(MainMapScreenEvent.OnNewFeature("data"))
 * })
 * ```
 *
 * **Step 3: Handle the Event**
 * ```kotlin
 * when (event) {
 *     is MainMapScreenEvent.OnNewFeature -> {
 *         // Implement handling logic
 *     }
 * }
 * ```
 *
 * ### Event Naming Conventions
 *
 * - **UI Actions (User triggers):** Use imperative verbs
 *   - `ShowCollectESSheet`, `DismissDialog`, `ToggleLayer`
 *
 * - **Data Events (Callbacks):** Use "On" prefix
 *   - `OnGeodatabaseDownloaded`, `OnDistanceSelected`, `OnFeatureSaved`
 *
 * - **Navigation:** Use "Navigate" prefix
 *   - `NavigateToJobCardEntry`, `NavigateBack`
 *
 * ### Design Principles Applied
 *
 * 1. **Single Responsibility:** Each event represents one specific action
 * 2. **Open/Closed:** Add new events without modifying existing code
 * 3. **Liskov Substitution:** All events implement the same interface
 * 4. **Interface Segregation:** Single event handler instead of many callbacks
 * 5. **Dependency Inversion:** UI depends on event abstraction, not concrete handlers
 *
 * ### Performance Considerations
 *
 * - **No Performance Impact:** Events are lightweight sealed classes (compile-time optimization)
 * - **Memory Efficient:** No additional allocations compared to lambda callbacks
 * - **Recomposition Safe:** Event handlers are stable references
 *
 * ### Testing Benefits
 *
 * ```kotlin
 * @Test
 * fun `when button clicked should emit ShowCollectESSheet event`() {
 *     val events = mutableListOf<MainMapScreenEvent>()
 *
 *     composeTestRule.setContent {
 *         MainMapScreen(onEvent = { events.add(it) })
 *     }
 *
 *     composeTestRule.onNodeWithText("Collect ES").performClick()
 *
 *     assert(events.contains(MainMapScreenEvent.ShowCollectESSheet))
 * }
 * ```
 *
 * @see MainMapScreen
 * @see MainMapScreenState
 */
sealed interface MainMapScreenEvent {

    // Bottom Sheet Events

    /**
     * User requested to show the Collect ES bottom sheet.
     */
    data object ShowCollectESSheet : MainMapScreenEvent

    /**
     * User requested to show the Manage ES Edits bottom sheet.
     */
    data object ShowManageESSheet : MainMapScreenEvent

    /**
     * User requested to show the Project Settings bottom sheet.
     */
    data object ShowProjectSettingsSheet : MainMapScreenEvent

    /**
     * User requested to show the Table of Contents bottom sheet.
     */
    data object ShowTableOfContentsSheet : MainMapScreenEvent

    /**
     * User dismissed a bottom sheet.
     */
    data object DismissBottomSheet : MainMapScreenEvent

    // Dialog Events

    /**
     * User requested to show the basemap selector dialog.
     */
    data object ShowBasemapDialog : MainMapScreenEvent

    /**
     * User dismissed the basemap selector dialog.
     */
    data object DismissBasemapDialog : MainMapScreenEvent

    /**
     * User requested to show the logout confirmation dialog.
     */
    data object ShowLogoutDialog : MainMapScreenEvent

    /**
     * User confirmed logout action.
     */
    data object ConfirmLogout : MainMapScreenEvent

    /**
     * User cancelled logout action.
     */
    data object CancelLogout : MainMapScreenEvent

    /**
     * User requested to show the clear data confirmation dialog.
     */
    data object ShowClearDialog : MainMapScreenEvent

    /**
     * User confirmed clear data action.
     */
    data object ConfirmClearData : MainMapScreenEvent

    /**
     * User cancelled clear data action.
     */
    data object CancelClearData : MainMapScreenEvent

    // Map Interaction Events

    /**
     * User toggled identify mode.
     *
     * @property enabled True to enable identify mode, false to disable
     */
    data class ToggleIdentifyMode(val enabled: Boolean) : MainMapScreenEvent

    /**
     * User toggled measurement mode.
     *
     * @property enabled True to enable measurement mode, false to disable
     */
    data class ToggleMeasurementMode(val enabled: Boolean) : MainMapScreenEvent

    /**
     * User toggled fullscreen mode.
     *
     * @property enabled True to enable fullscreen, false to exit
     */
    data class ToggleFullscreen(val enabled: Boolean) : MainMapScreenEvent

    /**
     * User expanded or collapsed the floating toolbar.
     *
     * @property expanded True to expand, false to collapse
     */
    data class ToggleToolbar(val expanded: Boolean) : MainMapScreenEvent

    /**
     * User selected a new basemap style.
     *
     * @property basemapStyle The selected basemap style
     */
    data class OnBasemapSelected(val basemapStyle: com.arcgismaps.mapping.BasemapStyle) :
        MainMapScreenEvent

    // Geodatabase Events

    /**
     * Geodatabase download completed successfully.
     *
     * @property geodatabase The downloaded geodatabase instance
     */
    data class OnGeodatabaseDownloaded(val geodatabase: Geodatabase) : MainMapScreenEvent

    /**
     * User selected a distance for geodatabase extent.
     *
     * @property distance The selected distance
     */
    data class OnDistanceSelected(val distance: ESDataDistance) : MainMapScreenEvent

    /**
     * User requested to get the current map extent.
     *
     * @property onExtentRetrieved Callback with the current extent
     */
    data class GetCurrentMapExtent(val onExtentRetrieved: (Envelope?) -> Unit) : MainMapScreenEvent

    // Snackbar Events

    /**
     * Request to show a snackbar message.
     *
     * @property message The message to display
     */
    data class ShowSnackbar(val message: String) : MainMapScreenEvent

    // Navigation Events

    /**
     * User requested to navigate to Job Card Entry screen.
     */
    data object NavigateToJobCardEntry : MainMapScreenEvent

    /**
     * User requested to perform a search.
     */
    data object OnSearchClick : MainMapScreenEvent
}
