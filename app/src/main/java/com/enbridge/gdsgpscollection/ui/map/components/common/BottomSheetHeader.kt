package com.enbridge.gdsgpscollection.ui.map.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Reusable bottom sheet header with title and navigation buttons.
 *
 * @param title Main title of the bottom sheet
 * @param subtitle Optional subtitle text
 * @param onBack Optional back button callback (if null, back button is hidden)
 * @param onClose Close button callback
 * @param modifier Optional modifier
 */
@Composable
fun BottomSheetHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header row with title and navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button (optional)
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Close button
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Subtitle (optional)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetHeaderSimplePreview() {
    GdsGpsCollectionTheme {
        BottomSheetHeader(
            title = "Select Feature Type",
            onClose = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetHeaderWithSubtitlePreview() {
    GdsGpsCollectionTheme {
        BottomSheetHeader(
            title = "Project Settings",
            subtitle = "Configure your project details",
            onClose = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomSheetHeaderWithBackPreview() {
    GdsGpsCollectionTheme {
        BottomSheetHeader(
            title = "Crew Information",
            subtitle = "Step 2 of 2",
            onBack = {},
            onClose = {}
        )
    }
}
