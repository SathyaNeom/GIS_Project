package com.enbridge.gdsgpscollection.ui.map.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R

/**
 * Displays a floating badge showing the count of visible features after filtering.
 *
 * This component provides visual feedback to users about how many features are
 * currently visible within the selected distance radius from the viewport center.
 *
 * Design:
 * - Semi-transparent background with rounded corners
 * - Filter icon + feature count text
 * - Positioned at top-right corner of map (below toolbar)
 * - Fades in/out based on filter active state
 *
 * UX Rationale:
 * - Helps users understand data availability in current view
 * - Indicates when re-download might be needed (0 features)
 * - Professional GIS app convention (similar to ArcGIS Field Maps)
 *
 * @param visibleFeatureCount Number of features currently visible after filtering
 * @param isVisible Whether the badge should be displayed
 * @param modifier Modifier for positioning and styling
 *
 * @author Sathya Narayanan
 */
@Composable
fun FeatureCountBadge(
    visibleFeatureCount: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            tonalElevation = 4.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.filter_feature_count, visibleFeatureCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Displays the distance radius label on the circle boundary.
 *
 * This component shows the selected distance (e.g., "500m radius") to provide
 * context for the filter area being displayed.
 *
 * @param distanceText Distance display text (e.g., "500 Meters")
 * @param modifier Modifier for positioning and styling
 *
 * @author Sathya Narayanan
 */
@Composable
fun DistanceLabel(
    distanceText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.filter_boundary_label, distanceText),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * NOTE: Circle boundary rendering on the map requires ArcGIS GraphicsOverlay.
 *
 * Since Compose cannot directly draw on MapView, we need to use the ArcGIS
 * Maps SDK's GraphicsOverlay API. The actual circle rendering is handled in
 * MainMapViewModel using:
 *
 * 1. Create a GraphicsOverlay
 * 2. Create a circle geometry using GeometryEngine.buffer()
 * 3. Create a Graphic with the circle and a SimpleLineSymbol
 * 4. Add the Graphic to the GraphicsOverlay
 * 5. Add the GraphicsOverlay to MapView
 *
 * The MainMapViewModel will expose StateFlows for:
 * - isBoundaryVisible: Boolean
 * - boundaryGraphicsOverlay: GraphicsOverlay?
 *
 * The MainMapScreen will observe these flows and update the MapView accordingly.
 *
 * Implementation will be added in the MainMapViewModel integration step.
 */

/**
 * Container composable that will be used to position the feature count badge
 * and trigger boundary visibility updates.
 *
 * This is a placeholder for UI layer composition. The actual map boundary
 * rendering happens through ArcGIS GraphicsOverlay in the ViewModel.
 *
 * @param visibleFeatureCount Number of features visible after filtering
 * @param distanceText Selected distance display text
 * @param isBoundaryVisible Whether the boundary should be shown
 * @param isFilterActive Whether the filter is currently active
 * @param modifier Modifier for this composable
 *
 * @author Sathya Narayanan
 */
@Composable
fun FilterOverlayControls(
    visibleFeatureCount: Int,
    distanceText: String,
    isBoundaryVisible: Boolean,
    isFilterActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Feature count badge - always show when filter is active
        FeatureCountBadge(
            visibleFeatureCount = visibleFeatureCount,
            isVisible = isFilterActive,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Distance label - only show when boundary is visible
        if (isBoundaryVisible && isFilterActive) {
            DistanceLabel(
                distanceText = distanceText,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Position above coordinate bar
            )
        }
    }
}
