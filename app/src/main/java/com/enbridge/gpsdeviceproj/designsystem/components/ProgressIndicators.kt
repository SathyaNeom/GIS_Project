package com.enbridge.gpsdeviceproj.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.gpsdeviceproj.designsystem.theme.ElectronicServicesTheme
import com.enbridge.gpsdeviceproj.designsystem.theme.Spacing

/**
 * Progress indicator type enum
 */
enum class ProgressIndicatorType {
    CIRCULAR,
    LINEAR
}

/**
 * Unified progress indicator that can render either circular or linear based on type
 * Uses the primary theme color for consistency
 * Supports both determinate and indeterminate progress
 */
@Composable
fun AppProgressIndicator(
    type: ProgressIndicatorType = ProgressIndicatorType.CIRCULAR,
    modifier: Modifier = Modifier,
    progress: Float? = null // null for indeterminate, 0.0-1.0 for determinate
) {
    when (type) {
        ProgressIndicatorType.CIRCULAR -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                if (progress != null) {
                    CircularProgressIndicator(
                        progress = { progress },
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
                        progress = { progress },
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
    ElectronicServicesTheme {
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
