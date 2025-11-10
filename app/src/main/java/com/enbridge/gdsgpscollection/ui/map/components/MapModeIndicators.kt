package com.enbridge.gdsgpscollection.ui.map.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

/**
 * Displays mode indicators for map interactions.
 *
 * Shows visual feedback when the map is in special interaction modes such as
 * identify or measurement mode. Only one mode can be active at a time.
 *
 * ## Usage Example:
 * ```kotlin
 * Box(modifier = Modifier.fillMaxSize()) {
 *     MapView(...)
 *
 *     MapModeIndicators(
 *         identifyMode = isIdentifyModeActive,
 *         measurementMode = isMeasurementModeActive,
 *         modifier = Modifier.align(Alignment.TopCenter)
 *     )
 * }
 * ```
 *
 * ## Design Rationale:
 * - Provides clear visual feedback for active map modes
 * - Positioned at top center for maximum visibility
 * - Uses distinctive colors for different modes
 * - Follows Material 3 surface container patterns
 *
 * @param identifyMode True when identify mode is active (user can tap features)
 * @param measurementMode True when measurement mode is active (user can measure distances)
 * @param modifier Optional modifier for positioning and styling
 */
@Composable
fun MapModeIndicators(
    identifyMode: Boolean,
    measurementMode: Boolean,
    modifier: Modifier = Modifier
) {
    // Show measurement mode indicator
    if (measurementMode) {
        Surface(
            modifier = modifier.padding(top = Spacing.normal),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Text(
                text = stringResource(R.string.msg_measurement_active),
                modifier = Modifier.padding(
                    horizontal = Spacing.normal,
                    vertical = Spacing.small
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    // Show identify mode indicator
    if (identifyMode) {
        Surface(
            modifier = modifier.padding(top = Spacing.normal),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Text(
                text = stringResource(R.string.msg_identify_mode),
                modifier = Modifier.padding(
                    horizontal = Spacing.normal,
                    vertical = Spacing.small
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
