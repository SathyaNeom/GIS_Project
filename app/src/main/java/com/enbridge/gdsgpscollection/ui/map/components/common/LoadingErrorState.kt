package com.enbridge.gdsgpscollection.ui.map.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme

/**
 * Reusable component for displaying loading or error states in bottom sheets.
 *
 * Shows either a loading indicator or an error message with a retry button.
 * Priority: Loading > Error
 *
 * @param isLoading Whether data is currently being loaded
 * @param error Error message to display, if any
 * @param onRetry Callback to retry the operation after an error
 * @param modifier Optional modifier for the component
 * @param loadingHeight Height of the loading container
 */
@Composable
fun LoadingErrorState(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loadingHeight: Int = 200
) {
    when {
        isLoading -> {
            // Show centered loading indicator
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(loadingHeight.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            // Show error message with retry button
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(Spacing.normal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(Spacing.normal))
                PrimaryButton(
                    text = "Retry",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Simplified version for empty state messages
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.normal),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingErrorStateLoadingPreview() {
    GdsGpsCollectionTheme {
        LoadingErrorState(
            isLoading = true,
            error = null,
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingErrorStateErrorPreview() {
    GdsGpsCollectionTheme {
        LoadingErrorState(
            isLoading = false,
            error = "Failed to load data. Please check your connection and try again.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    GdsGpsCollectionTheme {
        EmptyState(
            message = "No items found"
        )
    }
}
