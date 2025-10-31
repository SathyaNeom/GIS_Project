package com.enbridge.electronicservices.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme
import com.enbridge.electronicservices.designsystem.theme.ErrorRed
import com.enbridge.electronicservices.designsystem.theme.InfoBlue
import com.enbridge.electronicservices.designsystem.theme.Spacing
import com.enbridge.electronicservices.designsystem.theme.SuccessGreen
import com.enbridge.electronicservices.designsystem.theme.WarningAmber

/**
 * Snackbar type enum for different states
 */
enum class SnackbarType {
    INFO,
    WARNING,
    ERROR,
    SUCCESS
}

/**
 * Get the icon for the snackbar type
 */
private fun getSnackbarIcon(type: SnackbarType): ImageVector {
    return when (type) {
        SnackbarType.INFO -> Icons.Default.Info
        SnackbarType.WARNING -> Icons.Default.Warning
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
    }
}

/**
 * Get the background color for the snackbar type
 */
@Composable
private fun getSnackbarBackgroundColor(type: SnackbarType): Color {
    return when (type) {
        SnackbarType.INFO -> InfoBlue
        SnackbarType.WARNING -> WarningAmber
        SnackbarType.ERROR -> ErrorRed
        SnackbarType.SUCCESS -> SuccessGreen
    }
}

/**
 * Get the content color for the snackbar type
 */
@Composable
private fun getSnackbarContentColor(type: SnackbarType): Color {
    return when (type) {
        SnackbarType.INFO -> Color.White
        SnackbarType.WARNING -> Color.Black
        SnackbarType.ERROR -> Color.White
        SnackbarType.SUCCESS -> Color.White
    }
}

/**
 * Custom Snackbar with type-based styling
 * Background color, icon, and text color change based on type
 * Provides much richer feedback than default snackbar
 */
@Composable
fun AppSnackbar(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val backgroundColor = getSnackbarBackgroundColor(type)
    val contentColor = getSnackbarContentColor(type)
    val icon = getSnackbarIcon(type)

    Snackbar(
        modifier = modifier.padding(Spacing.normal),
        containerColor = backgroundColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        action = if (actionLabel != null && onActionClick != null) {
            {
                TextButton(onClick = onActionClick) {
                    Text(
                        text = actionLabel,
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        } else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

/**
 * Custom SnackbarHost that displays AppSnackbar
 */
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarType: SnackbarType = SnackbarType.INFO
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            AppSnackbar(
                message = data.visuals.message,
                type = snackbarType,
                actionLabel = data.visuals.actionLabel,
                onActionClick = { data.performAction() }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AppSnackbarPreview() {
    ElectronicServicesTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppSnackbar(
                message = "Information message",
                type = SnackbarType.INFO
            )
            AppSnackbar(
                message = "Warning message",
                type = SnackbarType.WARNING
            )
            AppSnackbar(
                message = "Error message",
                type = SnackbarType.ERROR
            )
            AppSnackbar(
                message = "Success message",
                type = SnackbarType.SUCCESS
            )
            AppSnackbar(
                message = "With action",
                type = SnackbarType.INFO,
                actionLabel = "UNDO",
                onActionClick = { }
            )
        }
    }
}
