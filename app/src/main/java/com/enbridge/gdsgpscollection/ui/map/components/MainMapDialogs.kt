package com.enbridge.gdsgpscollection.ui.map.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arcgismaps.mapping.BasemapStyle
import com.enbridge.gdsgpscollection.designsystem.components.AppDialog
import com.enbridge.gdsgpscollection.designsystem.components.AppTextButton
import com.enbridge.gdsgpscollection.designsystem.components.DialogType
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton

/**
 * Consolidated dialog management for MainMapScreen.
 *
 * Renders all modal dialogs used in the map screen including logout confirmation,
 * clear data confirmation, basemap selection, first-time guidance, and error dialogs.
 *
 * ## Usage Example:
 * ```kotlin
 * MainMapDialogs(
 *     showLogoutDialog = state.dialogState.showLogoutDialog,
 *     showClearDialog = state.dialogState.showClearDialog,
 *     showBasemapDialog = state.dialogState.showBasemapDialog,
 *     showFirstTimeGuidance = showFirstTimeGuidance,
 *     geodatabaseLoadError = geodatabaseLoadError,
 *     currentBasemap = selectedBasemapStyle,
 *     onLogout = { /* Handle logout */ },
 *     onClearData = { /* Clear geodatabase */ },
 *     onDismissLogout = { state.dismissLogoutDialog() },
 *     onDismissClear = { state.dismissClearDialog() },
 *     onBasemapSelected = { basemap -> /* Update basemap */ },
 *     onDismissBasemap = { state.dismissBasemapDialog() },
 *     onDismissFirstTimeGuidance = { /* Dismiss guidance */ },
 *     onDismissGeodatabaseError = { /* Dismiss error */ }
 * )
 * ```
 *
 * ## Design Rationale:
 * - Centralizes all dialog logic in one component
 * - Reduces clutter in main screen composable
 * - Follows Single Responsibility Principle
 * - Easy to test dialog rendering in isolation
 *
 * @param showLogoutDialog True to show logout confirmation dialog
 * @param showClearDialog True to show clear data confirmation dialog
 * @param showBasemapDialog True to show basemap selector dialog
 * @param showFirstTimeGuidance True to show first-time user guidance dialog
 * @param geodatabaseLoadError Error message for geodatabase load failure (null if no error)
 * @param currentBasemap Currently selected basemap style
 * @param onLogout Callback invoked when user confirms logout
 * @param onClearData Callback invoked when user confirms clear data
 * @param onDismissLogout Callback invoked when logout dialog is dismissed
 * @param onDismissClear Callback invoked when clear dialog is dismissed
 * @param onBasemapSelected Callback invoked when user selects a new basemap
 * @param onDismissBasemap Callback invoked when basemap dialog is dismissed
 * @param onDismissFirstTimeGuidance Callback invoked when guidance dialog is dismissed
 * @param onDismissGeodatabaseError Callback invoked when geodatabase error dialog is dismissed
 * @param modifier Optional modifier for customization
 */
@Composable
fun MainMapDialogs(
    showLogoutDialog: Boolean,
    showClearDialog: Boolean,
    showBasemapDialog: Boolean,
    showFirstTimeGuidance: Boolean,
    geodatabaseLoadError: String?,
    currentBasemap: BasemapStyle,
    onLogout: () -> Unit,
    onClearData: () -> Unit,
    onDismissLogout: () -> Unit,
    onDismissClear: () -> Unit,
    onBasemapSelected: (BasemapStyle) -> Unit,
    onDismissBasemap: () -> Unit,
    onDismissFirstTimeGuidance: () -> Unit,
    onDismissGeodatabaseError: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AppDialog(
            onDismissRequest = onDismissLogout,
            title = "Logout Confirmation",
            type = DialogType.INFO,
            content = {
                Text(
                    text = "Are you sure you want to log out?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Logout",
                    onClick = {
                        onDismissLogout()
                        onLogout()
                    }
                )
            },
            dismissButton = {
                AppTextButton(
                    text = "Cancel",
                    onClick = onDismissLogout
                )
            }
        )
    }

    // Basemap Selector Dialog
    if (showBasemapDialog) {
        BasemapSelectorDialog(
            currentBasemap = currentBasemap,
            onBasemapSelected = { basemap ->
                onBasemapSelected(basemap)
                onDismissBasemap()
            },
            onDismiss = onDismissBasemap
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDialog) {
        AppDialog(
            onDismissRequest = onDismissClear,
            title = "Clear Data",
            type = DialogType.INFO,
            content = {
                Text(
                    text = "Are you sure you want to delete the geodatabase?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Clear",
                    onClick = {
                        onClearData()
                        onDismissClear()
                    }
                )
            },
            dismissButton = {
                AppTextButton(
                    text = "Cancel",
                    onClick = onDismissClear
                )
            }
        )
    }

    // First-Time Guidance Dialog
    if (showFirstTimeGuidance) {
        AppDialog(
            onDismissRequest = onDismissFirstTimeGuidance,
            title = "Welcome to Electronic Services",
            type = DialogType.INFO,
            content = {
                Text(
                    text = "To begin, download the geodatabase for your area using 'Manage ES Edits'.\n\nOnce completed, you can begin data collection.\n\nTap 'Collect ES' to add new features on the map.",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                AppTextButton(
                    text = "Got it",
                    onClick = onDismissFirstTimeGuidance
                )
            }
        )
    }

    // Geodatabase Load Error Dialog
    if (geodatabaseLoadError != null) {
        AppDialog(
            onDismissRequest = onDismissGeodatabaseError,
            title = "Geodatabase Load Error",
            type = DialogType.ERROR,
            content = {
                Text(
                    text = geodatabaseLoadError,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                AppTextButton(
                    text = "OK",
                    onClick = onDismissGeodatabaseError
                )
            }
        )
    }
}
