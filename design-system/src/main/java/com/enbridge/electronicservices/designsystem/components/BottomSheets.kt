package com.enbridge.electronicservices.designsystem.components

/**
 * @author Sathya Narayanan
 */
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enbridge.electronicservices.designsystem.theme.Spacing
import androidx.compose.ui.tooling.preview.Preview
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme

/**
 * Custom Modal Bottom Sheet with app theme styling
 * Ensures content uses defined typography and components
 * Drag handle is clearly visible
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.extraLarge,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.small))
                // Custom drag handle that's more visible
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .padding(horizontal = Spacing.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp),
                        thickness = 4.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.large)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(Spacing.normal))
            }

            content()
        }
    }
}

/**
 * Bottom Sheet with action buttons
 * Convenience wrapper for common use case with confirm/dismiss actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    dismissButtonText: String? = "Cancel",
    onDismissClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        title = title
    ) {
        content()

        Spacer(modifier = Modifier.height(Spacing.large))

        // Action buttons
        PrimaryButton(
            text = confirmButtonText,
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth()
        )

        if (dismissButtonText != null) {
            Spacer(modifier = Modifier.height(Spacing.small))
            SecondaryButton(
                text = dismissButtonText,
                onClick = onDismissClick ?: onDismissRequest,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun AppBottomSheetPreview() {
    ElectronicServicesTheme {
        Surface {
            Text(
                text = "Preview: Bottom sheet would appear here",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ActionBottomSheetPreview() {
    ElectronicServicesTheme {
        Surface {
            Text(
                text = "Preview: Action bottom sheet would appear here",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
