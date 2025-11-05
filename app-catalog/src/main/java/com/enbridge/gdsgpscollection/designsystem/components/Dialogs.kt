package com.enbridge.gdsgpscollection.designsystem.components

/**
 * @author Sathya Narayanan
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.ErrorRed
import com.enbridge.gdsgpscollection.designsystem.theme.InfoBlue
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.designsystem.theme.SuccessGreen
import com.enbridge.gdsgpscollection.designsystem.theme.WarningAmber

/**
 * Dialog type enum for different states
 */
enum class DialogType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

/**
 * Get the icon for the dialog type
 */
private fun getDialogIcon(type: DialogType): ImageVector {
    return when (type) {
        DialogType.INFO -> Icons.Default.Info
        DialogType.WARNING -> Icons.Default.Warning
        DialogType.ERROR -> Icons.Default.Error
        DialogType.SUCCESS -> Icons.Default.CheckCircle
    }
}

/**
 * Get the color for the dialog type
 */
@Composable
private fun getDialogColor(type: DialogType): Color {
    return when (type) {
        DialogType.INFO -> InfoBlue
        DialogType.WARNING -> WarningAmber
        DialogType.ERROR -> ErrorRed
        DialogType.SUCCESS -> SuccessGreen
    }
}

/**
 * Custom dialog with type-based styling
 * Shows relevant icon and color bar based on type
 * Provides slots for action buttons
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    title: String,
    type: DialogType = DialogType.INFO,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    val dialogColor = getDialogColor(type)
    val dialogIcon = getDialogIcon(type)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
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
                modifier = modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 24.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Colored vertical bar on the left
                    Spacer(
                        modifier = Modifier
                            .width(4.dp)
                            .height(200.dp)
                            .background(dialogColor)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.large)
                    ) {
                        // Icon at the top
                        Icon(
                            imageVector = dialogIcon,
                            contentDescription = null,
                            tint = dialogColor,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(Spacing.normal))

                        // Title
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(Spacing.normal))

                        // Content
                        content()

                        Spacer(modifier = Modifier.height(Spacing.large))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (dismissButton != null) {
                                dismissButton()
                                Spacer(modifier = Modifier.width(Spacing.small))
                            }
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Simple alert dialog for basic confirmations
 */
@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    dismissButtonText: String? = null,
    onDismissClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            PrimaryButton(
                text = confirmButtonText,
                onClick = onConfirmClick,
                modifier = Modifier.padding(horizontal = Spacing.small)
            )
        },
        dismissButton = if (dismissButtonText != null && onDismissClick != null) {
            {
                AppTextButton(
                    text = dismissButtonText,
                    onClick = onDismissClick
                )
            }
        } else null,
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    )
}

@Preview(showBackground = true)
@Composable
private fun AppDialogPreview() {
    GdsGpsCollectionTheme {
        AppDialog(
            onDismissRequest = { },
            title = "Information",
            type = DialogType.INFO,
            content = {
                Text("This is an informational dialog with helpful content.")
            },
            confirmButton = {
                PrimaryButton(text = "OK", onClick = { })
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppDialogWarningPreview() {
    GdsGpsCollectionTheme {
        AppDialog(
            onDismissRequest = { },
            title = "Warning",
            type = DialogType.WARNING,
            content = {
                Text("This action requires your attention. Are you sure?")
            },
            confirmButton = {
                PrimaryButton(text = "Proceed", onClick = { })
            },
            dismissButton = {
                AppTextButton(text = "Cancel", onClick = { })
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppDialogErrorPreview() {
    GdsGpsCollectionTheme {
        AppDialog(
            onDismissRequest = { },
            title = "Error",
            type = DialogType.ERROR,
            content = {
                Text("An error occurred. Please try again.")
            },
            confirmButton = {
                PrimaryButton(text = "Retry", onClick = { })
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppDialogSuccessPreview() {
    GdsGpsCollectionTheme {
        AppDialog(
            onDismissRequest = { },
            title = "Success",
            type = DialogType.SUCCESS,
            content = {
                Text("Your action was completed successfully!")
            },
            confirmButton = {
                PrimaryButton(text = "Great!", onClick = { })
            }
        )
    }
}
