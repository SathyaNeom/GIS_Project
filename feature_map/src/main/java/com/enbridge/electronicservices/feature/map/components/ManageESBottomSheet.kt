package com.enbridge.electronicservices.feature.map.components

/**
 * @author Sathya Narayanan
 */

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * PROGRESS UI VERSION: Option 3 - Button State Change with Visual Progress Fill
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Current: Progress shown inside the "Get Data" button itself using ProgressButton.
 * Button transforms to show visual progress fill (primary color fills button background
 * from left to right) and percentage text overlay during download.
 *
 * TO REVERT TO FULL-SCREEN DIALOG:
 * 1. Find and uncomment the block marked "ORIGINAL FULL-SCREEN DIALOG"
 * 2. Find and comment out ProgressButton component usage
 * 3. Uncomment the original PrimaryButton in the Row
 * 4. Build and run!
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enbridge.electronicservices.designsystem.components.*
import com.enbridge.electronicservices.designsystem.theme.ElectronicServicesTheme
import com.enbridge.electronicservices.designsystem.theme.Spacing
import com.enbridge.electronicservices.domain.entity.ESDataDistance
import com.enbridge.electronicservices.feature.map.ManageESUiState
import com.enbridge.electronicservices.feature.map.ManageESViewModel
import kotlinx.coroutines.launch

/**
 * Bottom sheet for managing ES (Electronic Services) edits
 * Provides functionality to download, post, and manage ES data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageESBottomSheet(
    onDismissRequest: () -> Unit,
    onPostDataSnackbar: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManageESViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show success snackbar when post data completes
    LaunchedEffect(uiState.postSuccess) {
        if (uiState.postSuccess) {
            snackbarHostState.showSnackbar(
                message = "Successfully posted Data",
                duration = SnackbarDuration.Short
            )
            viewModel.onPostDataSuccess()
        }
    }

    // Note: Mock coordinates for development and testing purposes
    // Integration Point: Replace with actual device location from LocationManager
    // Requirements: Location permissions and GPS/Network location provider
    val mockLatitude = 43.6532   // Toronto latitude
    val mockLongitude = -79.3832 // Toronto longitude

    ManageESBottomSheetContent(
        uiState = uiState,
        onDismissRequest = onDismissRequest,
        onDistanceSelected = viewModel::onDistanceSelected,
        onGetDataClicked = {
            viewModel.onGetDataClicked(mockLatitude, mockLongitude)
        },
        onPostDataClicked = viewModel::onPostDataClicked,
        onDeleteJobCardsClicked = viewModel::onDeleteJobCardsClicked,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onDismissDownloadDialog = viewModel::onDismissDownloadDialog,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageESBottomSheetContent(
    uiState: ManageESUiState,
    onDismissRequest: () -> Unit,
    onDistanceSelected: (ESDataDistance) -> Unit,
    onGetDataClicked: () -> Unit,
    onPostDataClicked: () -> Unit,
    onDeleteJobCardsClicked: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onDismissDownloadDialog: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Prevent sheet from closing during async operations
    LaunchedEffect(uiState.isDownloading, uiState.isUploading, sheetState.isVisible) {
        if ((uiState.isDownloading || uiState.isUploading) && !sheetState.isVisible) {
            // If an operation is running and sheet tries to hide, show it again
            sheetState.show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            // Only allow dismissal when no async operations are running
            if (!uiState.isDownloading && !uiState.isUploading) {
                onDismissRequest()
            }
        },
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large)
                .padding(bottom = Spacing.large)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Get data or Post Data",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        text = "Get Data - Select a Distance around where you are standing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                }

                AppIconButton(
                    icon = Icons.Default.Close,
                    contentDescription = "Close",
                    onClick = onDismissRequest,
                    enabled = !uiState.isDownloading && !uiState.isUploading
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Distance Dropdown with Get Data Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleSelectDropdown(
                    items = ESDataDistance.entries,
                    selectedItem = uiState.selectedDistance,
                    onItemSelected = onDistanceSelected,
                    label = "Distance",
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp),
                    itemLabel = { it.displayText },
                    enabled = !uiState.isDownloading && !uiState.isUploading
                )

                // OPTION 3: Button with inline progress
                ProgressButton(
                    text = "Get Data",
                    onClick = onGetDataClicked,
                    isLoading = uiState.isDownloading,
                    progress = uiState.downloadProgress,
                    icon = Icons.Default.Download,
                    enabled = !uiState.isUploading,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )

                // TO REVERT: Uncomment this and comment out ProgressButton above
                /*
                PrimaryButton(
                    text = "Get Data",
                    onClick = onGetDataClicked,
                    icon = Icons.Default.Download,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = Spacing.extraSmall)
                )
                */
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Data Changed List Section
            Text(
                text = "Data Changed:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            // Empty/Disabled List Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.changedData.isEmpty()) {
                        Text(
                            text = "No data changes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Future: Display list of changed data
                        Column(
                            modifier = Modifier.padding(Spacing.normal),
                            verticalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            uiState.changedData.forEach { jobCard ->
                                Text(
                                    text = "${jobCard.id} - ${jobCard.address}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                // Delete JC Button
                SecondaryButton(
                    text = "Delete JC",
                    onClick = onDeleteJobCardsClicked,
                    icon = Icons.Default.Delete,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isDeletingJobCards && !uiState.isDownloading && !uiState.isUploading
                )

                // Post Data Button with Progress
                ProgressButton(
                    text = "Post Data",
                    onClick = onPostDataClicked,
                    isLoading = uiState.isUploading,
                    progress = uiState.uploadProgress,
                    icon = Icons.Default.Upload,
                    enabled = !uiState.isDownloading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // TO REVERT: Uncomment this block to restore full-screen dialog
    /*
    // ORIGINAL FULL-SCREEN DIALOG: START
    if (uiState.isDownloading) {
        DownloadProgressDialog(
            progress = uiState.downloadProgress,
            message = uiState.downloadMessage,
            onDismissRequest = {}
        )
    }
    // ORIGINAL FULL-SCREEN DIALOG: END
    */

    // Delete Job Cards Dialog
    if (uiState.showDeleteDialog) {
        DeleteJobCardsDialog(
            deletedCount = uiState.deletedJobCardsCount,
            onDismiss = onDismissDeleteDialog
        )
    }
    // Snackbar Host for success messages
    AppSnackbarHost(
        hostState = snackbarHostState,
        snackbarType = SnackbarType.SUCCESS
    )
}

/**
 * Dialog for Delete Job Cards result
 */
@Composable
private fun DeleteJobCardsDialog(
    deletedCount: Int,
    onDismiss: () -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = "Delete Job Card Info",
        type = DialogType.INFO,
        content = {
            Text(
                text = if (deletedCount == 0) {
                    "No Job Card has been saved"
                } else {
                    "$deletedCount Job Card(s) deleted successfully"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            PrimaryButton(
                text = "OK",
                onClick = onDismiss
            )
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ManageESBottomSheetPreview() {
    ElectronicServicesTheme {
        ManageESBottomSheetContent(
            uiState = ManageESUiState(),
            onDismissRequest = {},
            onDistanceSelected = {},
            onGetDataClicked = {},
            onPostDataClicked = {},
            onDeleteJobCardsClicked = {},
            onDismissDeleteDialog = {},
            onDismissDownloadDialog = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeleteJobCardsDialogPreview() {
    ElectronicServicesTheme {
        DeleteJobCardsDialog(
            deletedCount = 0,
            onDismiss = {}
        )
    }
}
