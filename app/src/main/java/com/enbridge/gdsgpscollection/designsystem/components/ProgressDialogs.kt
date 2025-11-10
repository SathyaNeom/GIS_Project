package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing

/**
 * Progress dialog for geodatabase download/sync operations
 * Shows a linear progress bar with percentage and custom message
 * Non-dismissible during operations to prevent accidental cancellation
 *
 * @param progress Progress value between 0.0 and 1.0
 * @param message Status message to display
 * @param title Dialog title
 * @param onDismissRequest Callback when dialog is dismissed (no-op by default)
 */
@Composable
fun DownloadProgressDialog(
    progress: Float,
    message: String,
    title: String,
    onDismissRequest: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false, // Prevent dismissal during operation
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.normal),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    // Status message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Progress bar
                    AppProgressIndicator(
                        progress = progress,
                        type = ProgressIndicatorType.LINEAR,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Percentage text
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Sync progress dialog for geodatabase synchronization
 * Simplified version for sync operations
 */
@Composable
fun SyncProgressDialog(
    progress: Float,
    message: String,
    onDismissRequest: () -> Unit = {}
) {
    DownloadProgressDialog(
        progress = progress,
        message = message,
        title = "Synchronizing Geodatabase",
        onDismissRequest = onDismissRequest
    )
}

@Preview(showBackground = true)
@Composable
private fun DownloadProgressDialogPreview() {
    GdsGpsCollectionTheme {
        DownloadProgressDialog(
            progress = 0.65f,
            message = "Downloading data…",
            title = "Generating Geodatabase"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncProgressDialogPreview() {
    GdsGpsCollectionTheme {
        SyncProgressDialog(
            progress = 0.35f,
            message = "Synchronizing changes…"
        )
    }
}
