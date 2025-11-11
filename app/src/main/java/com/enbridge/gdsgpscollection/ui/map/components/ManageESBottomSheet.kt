package com.enbridge.gdsgpscollection.ui.map.components

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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppDialog
import com.enbridge.gdsgpscollection.designsystem.components.AppIconButton
import com.enbridge.gdsgpscollection.designsystem.components.AppSnackbarHost
import com.enbridge.gdsgpscollection.designsystem.components.DialogType
import com.enbridge.gdsgpscollection.designsystem.components.DownloadProgressDialog
import com.enbridge.gdsgpscollection.designsystem.components.PrimaryButton
import com.enbridge.gdsgpscollection.designsystem.components.SecondaryButton
import com.enbridge.gdsgpscollection.designsystem.components.SingleSelectDropdown
import com.enbridge.gdsgpscollection.designsystem.components.SnackbarType
import com.enbridge.gdsgpscollection.designsystem.theme.GdsGpsCollectionTheme
import com.enbridge.gdsgpscollection.designsystem.theme.Spacing
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.ui.map.ManageESUiState
import com.enbridge.gdsgpscollection.ui.map.ManageESViewModel
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
    getCurrentMapExtent: () -> com.arcgismaps.geometry.Envelope?,
    modifier: Modifier = Modifier,
    onDistanceSelected: (ESDataDistance) -> Unit = {},
    onGeodatabasesDownloaded: (List<GeodatabaseInfo>) -> Unit = {},
    viewModel: ManageESViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showDownloadErrorDialog by remember { mutableStateOf(false) }
    var showSyncErrorDialog by remember { mutableStateOf(false) }

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

    // Error detection
    LaunchedEffect(uiState.downloadError) {
        if (uiState.downloadError != null) {
            showDownloadErrorDialog = true
        }
    }
    LaunchedEffect(uiState.postError) {
        if (uiState.postError != null) {
            showSyncErrorDialog = true
        }
    }

    ManageESBottomSheetContent(
        uiState = uiState,
        onDismissRequest = onDismissRequest,
        onDistanceSelected = { distance ->
            viewModel.onDistanceSelected(distance)
            onDistanceSelected(distance)
        },
        onGetDataClicked = {
            val extent = getCurrentMapExtent()
            if (extent != null) {
                viewModel.onGetDataClicked(extent, onGeodatabasesDownloaded)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Unable to get map extent. Please try again.")
                }
            }
        },
        onPostDataClicked = viewModel::onPostDataClicked,
        onDeleteJobCardsClicked = viewModel::onDeleteJobCardsClicked,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onDismissDownloadDialog = viewModel::onDismissDownloadDialog,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )

    // Show download progress dialog
    // Multi-service download (Project environment)
    if (uiState.isMultiServiceDownload && uiState.multiServiceProgress != null) {
        val progress = uiState.multiServiceProgress
        DownloadProgressDialog(
            progress = progress?.overallProgress ?: 0f,
            message = progress?.overallMessage ?: "Downloading data…",
            title = "Downloading Data"
        )
    }
    // Single-service download (Wildfire environment - legacy)
    else if (uiState.isDownloading) {
        // TODO: Remove this block once all environments use multi-service approach
        // This is for backward compatibility with Wildfire environment
        DownloadProgressDialog(
            progress = uiState.downloadProgress,
            message = uiState.downloadMessage,
            title = "Downloading Data"
        )
    }
    // Show upload/sync progress dialog
    if (uiState.isUploading) {
        DownloadProgressDialog(
            progress = uiState.uploadProgress,
            message = "Uploading data…",
            title = "Synchronizing Data"
        )
    }
    // Download error dialog
    if (showDownloadErrorDialog && uiState.downloadError != null) {
        val errorMessage = uiState.downloadError
        AppDialog(
            onDismissRequest = { showDownloadErrorDialog = false },
            title = stringResource(R.string.error_geodatabase_generation_failed),
            type = DialogType.ERROR,
            content = {
                Text(
                    text = when {
                        errorMessage?.contains("network", ignoreCase = true) == true ||
                                errorMessage?.contains("connection", ignoreCase = true) == true ->
                            stringResource(R.string.error_network_unavailable)

                        errorMessage?.contains("service", ignoreCase = true) == true ||
                                errorMessage?.contains("server", ignoreCase = true) == true ->
                            stringResource(R.string.error_feature_service_unreachable)

                        errorMessage?.contains("corrupt", ignoreCase = true) == true ->
                            stringResource(R.string.error_geodatabase_corrupted)

                        else ->
                            stringResource(R.string.error_geodatabase_generation_failed)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = stringResource(android.R.string.ok),
                    onClick = {
                        showDownloadErrorDialog = false
                        viewModel.onDismissDownloadDialog()
                    }
                )
            }
        )
    }
    if (showSyncErrorDialog && uiState.postError != null) {
        val errorMessage = uiState.postError
        AppDialog(
            onDismissRequest = { showSyncErrorDialog = false },
            title = stringResource(R.string.error_geodatabase_sync_failed),
            type = DialogType.ERROR,
            content = {
                Text(
                    text = when {
                        errorMessage?.contains("network", ignoreCase = true) == true ||
                                errorMessage?.contains("connection", ignoreCase = true) == true ->
                            stringResource(R.string.error_network_unavailable)

                        errorMessage?.contains("service", ignoreCase = true) == true ||
                                errorMessage?.contains("server", ignoreCase = true) == true ->
                            stringResource(R.string.error_feature_service_unreachable)

                        else ->
                            stringResource(R.string.error_geodatabase_sync_failed)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = stringResource(android.R.string.ok),
                    onClick = { showSyncErrorDialog = false }
                )
            }
        )
    }
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

                PrimaryButton(
                    text = "Get Data",
                    onClick = onGetDataClicked,
                    icon = Icons.Default.Download,
                    enabled = !uiState.isDownloading && !uiState.isUploading && !uiState.isDownloadInProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
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

                // Post Data Button
                PrimaryButton(
                    text = "Post Data",
                    onClick = onPostDataClicked,
                    icon = Icons.Default.Upload,
                    enabled = !uiState.isDownloading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

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
    GdsGpsCollectionTheme {
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
    GdsGpsCollectionTheme {
        DeleteJobCardsDialog(
            deletedCount = 0,
            onDismiss = {}
        )
    }
}
