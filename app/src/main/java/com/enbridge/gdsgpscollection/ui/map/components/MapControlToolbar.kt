package com.enbridge.gdsgpscollection.ui.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

/**
 * Floating map control toolbar with expandable action buttons.
 *
 * Provides quick access to common map operations including zoom, fullscreen,
 * identify, layer management, data clearing, location, and measurement tools.
 *
 * The toolbar layout adapts based on device type:
 * - **Tablets (≥600dp):** Horizontal layout with buttons expanding to the left
 * - **Phones (<600dp):** Vertical layout with buttons expanding upward
 *
 * ## Usage Example:
 * ```kotlin
 * Box(modifier = Modifier.fillMaxSize()) {
 *     MapView(...)
 *
 *     MapControlToolbar(
 *         isTablet = configuration.screenWidthDp >= 600,
 *         isExpanded = isToolbarExpanded,
 *         isFullscreen = isFullscreen,
 *         onToggleExpanded = { isToolbarExpanded = !isToolbarExpanded },
 *         onZoomIn = { /* Handle zoom in */ },
 *         onZoomOut = { /* Handle zoom out */ },
 *         modifier = Modifier.align(Alignment.TopEnd)
 *     )
 * }
 * ```
 *
 * ## Design Rationale:
 * - Single component encapsulates all map control UI logic
 * - Responsive layout adapts to device size
 * - Follows Material 3 floating action button patterns
 * - Clear separation of concerns: UI in component, logic in callbacks
 *
 * @param isTablet True if device screen width is >= 600dp (triggers horizontal layout)
 * @param isExpanded True when toolbar is expanded showing all control buttons
 * @param isFullscreen True when map is in fullscreen mode (changes fullscreen icon)
 * @param onToggleExpanded Callback invoked when main FAB is clicked to toggle expansion
 * @param onZoomIn Callback invoked when zoom in button is clicked
 * @param onZoomOut Callback invoked when zoom out button is clicked
 * @param onToggleFullscreen Callback invoked when fullscreen button is clicked
 * @param onIdentify Callback invoked when identify button is clicked
 * @param onShowLayers Callback invoked when layers button is clicked
 * @param onClear Callback invoked when clear button is clicked
 * @param onMyLocation Callback invoked when my location button is clicked
 * @param onToggleMeasure Callback invoked when measure button is clicked
 * @param modifier Optional modifier for positioning and styling
 */
@Composable
fun MapControlToolbar(
    isTablet: Boolean,
    isExpanded: Boolean,
    isFullscreen: Boolean,
    onToggleExpanded: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onIdentify: () -> Unit,
    onShowLayers: () -> Unit,
    onClear: () -> Unit,
    onMyLocation: () -> Unit,
    onToggleMeasure: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTablet) {
        // Horizontal layout for tablets
        Row(
            modifier = modifier
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.End + WindowInsetsSides.Top
                    )
                )
                .padding(end = Spacing.normal, top = Spacing.normal),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.End)
        ) {
            // Expandable toolbar buttons
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.Top
                ) {
                    MapControlButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        onClick = onZoomIn
                    )

                    MapControlButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        onClick = onZoomOut
                    )

                    MapControlButton(
                        icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                        onClick = onToggleFullscreen
                    )

                    MapControlButton(
                        icon = Icons.Default.Info,
                        contentDescription = "Identify",
                        onClick = onIdentify
                    )

                    MapControlButton(
                        icon = Icons.Default.Layers,
                        contentDescription = "Layers",
                        onClick = onShowLayers
                    )

                    MapControlButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Clear",
                        onClick = onClear
                    )

                    MapControlButton(
                        icon = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        onClick = onMyLocation
                    )

                    MapControlButton(
                        icon = Icons.Default.Straighten,
                        contentDescription = "Measure",
                        onClick = onToggleMeasure
                    )
                }
            }

            // Main FAB
            FloatingActionButton(
                onClick = onToggleExpanded,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Map Controls"
                )
            }
        }
    } else {
        // Vertical layout for phones
        Column(
            modifier = modifier
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.End + WindowInsetsSides.Top
                    )
                )
                .padding(end = Spacing.normal, top = Spacing.normal),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.small, Alignment.Top)
        ) {
            // Expandable toolbar buttons
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    horizontalAlignment = Alignment.End
                ) {
                    MapControlButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        onClick = onZoomIn
                    )

                    MapControlButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        onClick = onZoomOut
                    )

                    MapControlButton(
                        icon = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                        onClick = onToggleFullscreen
                    )

                    MapControlButton(
                        icon = Icons.Default.Info,
                        contentDescription = "Identify",
                        onClick = onIdentify
                    )

                    MapControlButton(
                        icon = Icons.Default.Layers,
                        contentDescription = "Layers",
                        onClick = onShowLayers
                    )

                    MapControlButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Clear",
                        onClick = onClear
                    )

                    MapControlButton(
                        icon = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        onClick = onMyLocation
                    )

                    MapControlButton(
                        icon = Icons.Default.Straighten,
                        contentDescription = "Measure",
                        onClick = onToggleMeasure
                    )
                }
            }

            // Main FAB
            FloatingActionButton(
                onClick = onToggleExpanded,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Map Controls"
                )
            }
        }
    }
}

/**
 * Individual floating action button for map controls.
 *
 * Styled consistently with Material 3 design guidelines for secondary FABs.
 *
 * @param icon The icon to display
 * @param contentDescription Accessibility description for screen readers
 * @param onClick Callback invoked when button is clicked
 * @param modifier Optional modifier for customization
 */
@Composable
private fun MapControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}
