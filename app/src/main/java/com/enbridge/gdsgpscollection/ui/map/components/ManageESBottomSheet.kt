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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.designsystem.components.AppDialog
import com.enbridge.gdsgpscollection.designsystem.components.AppIconButton
import com.enbridge.gdsgpscollection.designsystem.components.AppRadioButton
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
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.geometry.Envelope
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
    getCurrentMapExtent: () -> Envelope?,
    initialViewpoint: Viewpoint?,
    onRestoreViewpoint: (Viewpoint) -> Unit,
    modifier: Modifier = Modifier,
    onDistanceSelected: (ESDataDistance) -> Unit = {},
    onGeodatabasesDownloaded: (List<GeodatabaseInfo>) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: ManageESViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showDownloadErrorDialog by remember { mutableStateOf(false) }
    var showSyncErrorDialog by remember { mutableStateOf(false) }
    var downloadSuccessTriggered by remember { mutableStateOf(false) }
    var downloadErrorTriggered by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Collect location state from ViewModel
    val isLocationAvailable by viewModel.isLocationAvailable.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isGetDataEnabled by viewModel.isGetDataEnabled.collectAsState()

    // Store the pre-captured original viewpoint for potential restoration
    // initialViewpoint is captured synchronously in parent (MainMapScreen) before sheet composition
    // This eliminates race conditions with map animations updating currentViewpoint
    LaunchedEffect(Unit) {
        viewModel.storeOriginalViewpoint(initialViewpoint)
    }

    // Clear error states when the bottom sheet is disposed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onBottomSheetDismissed()
        }
    }

    // Handle dismissal with viewpoint restoration if not committed
    val handleDismiss: () -> Unit = {
        val viewpointToRestore = viewModel.onBottomSheetDismissedWithViewpoint()
        if (viewpointToRestore != null) {
            onRestoreViewpoint(viewpointToRestore)
        }
        onDismissRequest()
    }

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

    // Handle download success - dismiss bottom sheet and show success snackbar
    LaunchedEffect(
        uiState.isDownloadInProgress,
        uiState.downloadError,
        uiState.multiServiceProgress
    ) {
        // Multi-service download completion
        val progress = uiState.multiServiceProgress
        if (!uiState.isDownloadInProgress &&
            uiState.isMultiServiceDownload &&
            progress != null &&
            progress.isComplete &&
            !progress.hasError &&
            !downloadSuccessTriggered
        ) {
            downloadSuccessTriggered = true
            // CRITICAL: Use handleDismiss to properly handle extent commit on success
            // Download success means extent was committed via "Get Data"
            handleDismiss()
            // Then show success snackbar from parent
            onPostDataSnackbar()
        }
        // Single-service download completion
        else if (!uiState.isDownloadInProgress &&
            !uiState.isDownloading &&
            !uiState.isMultiServiceDownload &&
            uiState.downloadError == null &&
            !downloadSuccessTriggered &&
            uiState.downloadProgress > 0f
        ) {
            downloadSuccessTriggered = true
            // CRITICAL: Use handleDismiss to properly handle extent commit on success
            // Download success means extent was committed via "Get Data"
            handleDismiss()
            // Then show success snackbar from parent
            onPostDataSnackbar()
        }
    }

    // Handle download error - show error snackbar without dismissing
    LaunchedEffect(uiState.downloadError) {
        if (uiState.downloadError != null && !downloadErrorTriggered) {
            downloadErrorTriggered = true
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = uiState.downloadError
                        ?: "Failed to download geodatabase. Please try again.",
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    // Reset triggered flags when download starts
    LaunchedEffect(uiState.isDownloadInProgress) {
        if (uiState.isDownloadInProgress) {
            downloadSuccessTriggered = false
            downloadErrorTriggered = false
        }
    }

    // Error detection for dialogs (kept for backward compatibility)
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
        isLocationAvailable = isLocationAvailable,
        currentLocation = currentLocation,
        isGetDataEnabled = isGetDataEnabled,
        onDismissRequest = handleDismiss,  // Pass handleDismiss directly
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
        onJobCardSelected = viewModel::onJobCardSelected,
        onJobCardDeselected = { viewModel.onJobCardDeselected() },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        context = context
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
    // Download error dialog (commented out as we're using snackbar now)
    // Keeping the code for reference in case dialogs are preferred later
    /*
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
    */
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
    isLocationAvailable: Boolean,
    currentLocation: com.arcgismaps.geometry.Point?,
    isGetDataEnabled: Boolean,
    onDismissRequest: () -> Unit,
    onDistanceSelected: (ESDataDistance) -> Unit,
    onGetDataClicked: () -> Unit,
    onPostDataClicked: () -> Unit,
    onDeleteJobCardsClicked: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onDismissDownloadDialog: () -> Unit,
    onJobCardSelected: (com.enbridge.gdsgpscollection.domain.entity.JobCard) -> Unit,
    onJobCardDeselected: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    context: Context
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
            // CRITICAL: This now calls handleDismiss which includes viewpoint restoration logic
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
                    onClick = onDismissRequest,  // This now triggers handleDismiss -> viewpoint restoration
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
                    label = stringResource(R.string.managees_distance),
                    placeholder = stringResource(R.string.managees_distance_placeholder),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp),
                    itemLabel = { it.displayText },
                    enabled = !uiState.isDownloading && !uiState.isUploading
                )

                PrimaryButton(
                    text = stringResource(R.string.managees_get_data),
                    onClick = onGetDataClicked,
                    icon = Icons.Default.Download,
                    enabled = isGetDataEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Location status indicator (only show when location unavailable)
            if (!isLocationAvailable) {
                LocationStatusIndicator(
                    context = context,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            // Data Changed List Section
            Text(
                text = stringResource(R.string.managees_data_changed),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            // Changed Data List with Single Selection
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.changedData.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_data_changes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Single selection radio button list
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Spacing.small),
                            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                        ) {
                            items(
                                count = uiState.changedData.size,
                                key = { index -> uiState.changedData[index].id }
                            ) { index ->
                                val jobCard = uiState.changedData[index]
                                val isSelected = uiState.selectedJobCard?.id == jobCard.id

                                AppRadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        // Toggle selection: deselect if already selected, select if not
                                        if (isSelected) {
                                            onJobCardDeselected()
                                        } else {
                                            onJobCardSelected(jobCard)
                                        }
                                    },
                                    label = "${jobCard.id} - ${jobCard.address}",
                                    enabled = !uiState.isDownloading && !uiState.isUploading
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
                // Delete JC Button - enabled only when a job card is selected
                SecondaryButton(
                    text = stringResource(R.string.managees_delete_jc),
                    onClick = onDeleteJobCardsClicked,
                    icon = Icons.Default.Delete,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.selectedJobCard != null &&
                            !uiState.isDeletingJobCards &&
                            !uiState.isDownloading &&
                            !uiState.isUploading
                )

                // Post Data Button - enabled only when a job card is selected
                PrimaryButton(
                    text = stringResource(R.string.managees_post_data),
                    onClick = onPostDataClicked,
                    icon = Icons.Default.Upload,
                    enabled = uiState.selectedJobCard != null &&
                            !uiState.isDownloading &&
                            !uiState.isUploading,
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
    // Snackbar Host for error messages (shown within bottom sheet)
    // Note: Success messages are shown via parent's snackbar host after dismissal
    Box(modifier = Modifier.padding(Spacing.normal)) {
        AppSnackbarHost(
            hostState = snackbarHostState,
            snackbarType = if (uiState.downloadError != null) SnackbarType.ERROR else SnackbarType.SUCCESS
        )
    }
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
// LocationStatusIndicator: Local implementation (not in design system)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LocationStatusIndicator(
    context: Context,
    modifier: Modifier = Modifier
) {
    // Check for location permission state (assuming ACCESS_FINE_LOCATION for simplicity)
    val hasLocationPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = "Location unavailable",
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Location unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                val instruction = if (!hasLocationPermission) {
                    "Please grant location permission in app settings."
                } else {
                    "Check that location is enabled on your device."
                }
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEWS
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ManageESBottomSheetPreview() {
    GdsGpsCollectionTheme {
        ManageESBottomSheet(
            onDismissRequest = {},
            onPostDataSnackbar = {},
            getCurrentMapExtent = { null },
            initialViewpoint = null,
            onRestoreViewpoint = {},
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
