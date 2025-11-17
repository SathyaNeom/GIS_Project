package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

/**
 * Progress indicator type enum
 */
enum class ProgressIndicatorType {
    CIRCULAR,
    LINEAR
}

/**
 * Unified progress indicator that can render either circular or linear based on type.
 * Uses the primary theme color for consistency.
 * Supports both determinate and indeterminate progress.
 *
 * **Progress Smoothing:**
 * When progress updates are received, this component animates the transition using
 * `animateFloatAsState` with a 300ms duration. This creates a smooth, visually pleasing
 * progression rather than abrupt jumps, especially important during geodatabase downloads
 * where progress updates may arrive at irregular intervals.
 *
 * **Animation Rationale:**
 * - **Duration**: 300ms provides perceptible but not sluggish animation
 * - **Easing**: Default LinearOutSlowIn for natural deceleration
 * - **Performance**: Compose's state-driven animation is efficient and doesn't block UI thread
 *
 * @param type Type of progress indicator (CIRCULAR or LINEAR)
 * @param modifier Modifier for customizing the component's layout
 * @param progress Progress value (null for indeterminate, 0.0-1.0 for determinate)
 */
@Composable
fun AppProgressIndicator(
    type: ProgressIndicatorType = ProgressIndicatorType.CIRCULAR,
    modifier: Modifier = Modifier,
    progress: Float? = null // null for indeterminate, 0.0-1.0 for determinate
) {
    // Animate progress changes for smooth transitions
    // This prevents jarring jumps when progress updates arrive at irregular intervals
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(
            durationMillis = 300, // 300ms provides smooth but responsive animation
            delayMillis = 0
        ),
        label = "progress_animation"
    )

    when (type) {
        ProgressIndicatorType.CIRCULAR -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                if (progress != null) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        ProgressIndicatorType.LINEAR -> {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppProgressIndicatorPreview() {
    GdsGpsCollectionTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.Text(
                "Circular Indeterminate:",
                style = MaterialTheme.typography.labelLarge
            )
            AppProgressIndicator(type = ProgressIndicatorType.CIRCULAR)

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Text(
                "Circular Determinate (75%):",
                style = MaterialTheme.typography.labelLarge
            )
            AppProgressIndicator(
                type = ProgressIndicatorType.CIRCULAR,
                progress = 0.75f
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Text(
                "Linear Indeterminate:",
                style = MaterialTheme.typography.labelLarge
            )
            AppProgressIndicator(
                type = ProgressIndicatorType.LINEAR,
                modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Text(
                "Linear Determinate (50%):",
                style = MaterialTheme.typography.labelLarge
            )
            AppProgressIndicator(
                type = ProgressIndicatorType.LINEAR,
                progress = 0.5f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
