package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 * Refactored to use ManageESFacade - Phase 4: ViewModel Dependency Reduction
 * Enhanced for Multi-Service Support - Phase 5: Presentation Layer
 * Enhanced for Location Feature Flags - Phase 6: Location Integration
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.Viewpoint
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.config.LocationFeatureFlags
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import com.enbridge.gdsgpscollection.domain.facade.ManageESFacade
import com.enbridge.gdsgpscollection.ui.map.delegates.LocationManagerDelegate
import com.enbridge.gdsgpscollection.util.Logger
import com.enbridge.gdsgpscollection.util.network.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Manage ES Edits feature.
 *
 * Manages state for downloading, posting, and managing ES data.
 * Supports both single-service (Wildfire) and multi-service (Project) environments.
 *
 * Architecture Evolution:
 * - Phase 4: Refactored to use ManageESFacade (6 use cases → 1 facade, 83% reduction)
 * - Phase 5: Enhanced for multi-service geodatabase support
 * - Phase 6: Enhanced for location-based feature validation
 *
 * @property manageESFacade Facade providing access to all ES data operations
 * @property configuration Environment configuration provider
 * @property networkMonitor Network connectivity monitor for auto-clearing network errors
 * @property locationFeatureFlags Feature flags controlling location behavior (dev/prod)
 * @property locationManager Location manager providing current location and availability
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
@HiltViewModel
class ManageESViewModel @Inject constructor(
    private val manageESFacade: ManageESFacade,
    private val configuration: FeatureServiceConfiguration,
    private val networkMonitor: NetworkMonitor,
    private val locationFeatureFlags: LocationFeatureFlags,
    private val locationManager: LocationManagerDelegate
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageESUiState())
    val uiState: StateFlow<ManageESUiState> = _uiState.asStateFlow()

    /**
     * StateFlow indicating whether location is available for ManageES operations.
     * Used to enable/disable "Get Data" button based on location status.
     */
    val isLocationAvailable: StateFlow<Boolean> = locationManager.isLocationAvailable

    /**
     * StateFlow containing current location for extent calculations.
     */
    val currentLocation: StateFlow<Point?> = locationManager.currentLocation

    /**
     * StateFlow indicating whether "Get Data" button should be enabled.
     *
     * Enabled when:
     * - Distance is selected
     * - Location is available (or not required by feature flags)
     * - No download/upload in progress
     */
    val isGetDataEnabled: StateFlow<Boolean> = combine(
        uiState,
        isLocationAvailable
    ) { state, locationAvailable ->
        val hasDistance = state.selectedDistance != null
        val hasLocation = !locationFeatureFlags.requireLocationForManageES || locationAvailable
        val notBusy = !state.isDownloadInProgress && !state.isUploading

        hasDistance && hasLocation && notBusy
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Stores the original map viewpoint before distance selection.
     * Used to restore map state if user dismisses bottom sheet without committing.
     * Always cleared after dismiss to prevent memory retention.
     */
    private val _originalViewpoint = MutableStateFlow<Viewpoint?>(null)

    /**
     * Tracks whether the distance selection has been committed via "Get Data" action.
     * When true, the map viewpoint change is permanent.
     * When false, dismissing the sheet will restore the original viewpoint.
     */
    private val _isExtentCommitted = MutableStateFlow(false)

    companion object {
        private const val TAG = "ManageESViewModel"
    }

    init {
        Logger.i(TAG, "ManageESViewModel initialized with location support")
        loadChangedData()
        observeNetworkChanges()
    }

    /**
     * Monitors network connectivity changes and automatically clears network-related errors
     * when connection is restored.
     *
     * This provides better UX by removing stale "no internet" errors when the user
     * regains connectivity without requiring manual intervention.
     */
    private fun observeNetworkChanges() {
        Logger.d(TAG, "Starting network connectivity monitoring")
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                if (isConnected) {
                    val currentError = _uiState.value.downloadError
                    if (currentError != null && isNetworkRelatedError(currentError)) {
                        Logger.i(
                            TAG,
                            "Network restored - automatically clearing network-related error"
                        )
                        _uiState.update {
                            it.copy(downloadError = null)
                        }
                    }

                    val currentPostError = _uiState.value.postError
                    if (currentPostError != null && isNetworkRelatedError(currentPostError)) {
                        Logger.i(
                            TAG,
                            "Network restored - automatically clearing network-related post error"
                        )
                        _uiState.update {
                            it.copy(postError = null)
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if an error message is network-related.
     *
     * @param error The error message to check
     * @return true if the error is related to network connectivity
     */
    private fun isNetworkRelatedError(error: String): Boolean {
        val networkKeywords = listOf(
            "network",
            "connection",
            "internet",
            "offline",
            "connectivity",
            "unreachable",
            "timeout"
        )
        return networkKeywords.any { keyword ->
            error.contains(keyword, ignoreCase = true)
        }
    }

    fun onDistanceSelected(distance: ESDataDistance) {
        Logger.i(TAG, "Distance selected: ${distance.displayText}")
        _uiState.update { it.copy(selectedDistance = distance) }
    }

    /**
     * Handles "Get Data" button click - automatically selects single or multi-service download
     * based on current environment configuration.
     *
     * Marks the extent change as committed, preventing viewpoint restoration on dismiss.
     *
     * Environment Routing:
     * - Project Environment: Calls onGetDataMultiService() for parallel downloads
     * - Wildfire Environment: Calls onGetDataSingleService() for legacy download
     *
     * @param extent Geographic extent to download data for
     * @param onGeodatabasesDownloaded Callback with list of downloaded geodatabases
     * @param onSaveTimestamp Callback to save download timestamp
     */
    fun onGetDataClicked(
        extent: Envelope,
        onGeodatabasesDownloaded: (List<GeodatabaseInfo>) -> Unit = {},
        onSaveTimestamp: () -> Unit = {}
    ) {
        if (!isGetDataEnabled.value) {
            Logger.w(TAG, "Get Data clicked but button is not enabled; ignoring request")
            return
        }

        // Mark extent change as committed - prevents viewpoint restoration on dismiss
        _isExtentCommitted.value = true
        Logger.d(TAG, "Map extent change committed via Get Data action")

        val environment = configuration.getCurrentEnvironment()
        val isMultiService = environment.featureServices.size > 1

        Logger.i(
            TAG,
            "Get Data clicked - Environment: ${if (isMultiService) "Multi-Service" else "Single-Service"}"
        )

        if (isMultiService) {
            onGetDataMultiService(extent, onGeodatabasesDownloaded, onSaveTimestamp)
        } else {
            onGetDataSingleService(extent, onGeodatabasesDownloaded, onSaveTimestamp)
        }
    }

    /**
     * Multi-service download for Project environment.
     *
     * Downloads Operations and Basemap geodatabases in parallel with combined progress tracking.
     */
    private fun onGetDataMultiService(
        extent: Envelope,
        onGeodatabasesDownloaded: (List<GeodatabaseInfo>) -> Unit,
        onSaveTimestamp: () -> Unit
    ) {
        Logger.i(TAG, "Starting multi-service download")

        // Validate location if required by feature flags
        if (locationFeatureFlags.requireLocationForManageES && currentLocation.value == null) {
            Logger.w(TAG, "Multi-service download blocked: location is not available")
            return
        }

        viewModelScope.launch {
            if (_uiState.value.isDownloadInProgress) {
                Logger.w(TAG, "Download is already in progress, ignoring request")
                return@launch
            }

            _uiState.update {
                it.copy(
                    isDownloadInProgress = true,
                    isMultiServiceDownload = true,
                    downloadError = null
                )
            }

            try {
                manageESFacade.downloadAllServices(extent).collect { progress ->
                    Logger.d(
                        TAG,
                        "Multi-service progress: ${(progress.overallProgress * 100).toInt()}% - ${progress.overallMessage}"
                    )

                    // Check for errors
                    if (progress.hasError) {
                        Logger.e(TAG, "Multi-service download failed: ${progress.error}")
                        _uiState.update {
                            it.copy(
                                isDownloadInProgress = false,
                                isMultiServiceDownload = false,
                                multiServiceProgress = null,
                                downloadError = progress.error
                            )
                        }
                        return@collect
                    }

                    _uiState.update {
                        it.copy(multiServiceProgress = progress)
                    }

                    if (progress.isComplete && !progress.hasError) {
                        Logger.i(TAG, "Multi-service download completed successfully")

                        // Load all geodatabases and pass to callback
                        val result = manageESFacade.loadAllGeodatabases()
                        result.onSuccess { geodatabaseInfos ->
                            Logger.d(TAG, "Loaded ${geodatabaseInfos.size} geodatabases")
                            onGeodatabasesDownloaded(geodatabaseInfos)
                            onSaveTimestamp()
                        }.onFailure { error ->
                            Logger.e(TAG, "Failed to load geodatabases after download", error)
                        }

                        // Reload changed data
                        loadChangedData()

                        _uiState.update {
                            it.copy(
                                isDownloadInProgress = false,
                                isMultiServiceDownload = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error in multi-service download", e)
                _uiState.update {
                    it.copy(
                        isDownloadInProgress = false,
                        isMultiServiceDownload = false,
                        multiServiceProgress = null,
                        downloadError = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    /**
     * Single-service download for Wildfire environment (legacy).
     *
     * TODO: This method will be deprecated once all environments use multi-service approach.
     */
    private fun onGetDataSingleService(
        extent: Envelope,
        onGeodatabasesDownloaded: (List<GeodatabaseInfo>) -> Unit,
        onSaveTimestamp: () -> Unit
    ) {
        Logger.i(TAG, "Starting single-service download (legacy)")

        // Validate location if required by feature flags
        if (locationFeatureFlags.requireLocationForManageES && currentLocation.value == null) {
            Logger.w(TAG, "Single-service download blocked: location is not available")
            return
        }

        viewModelScope.launch {
            if (_uiState.value.isDownloadInProgress) {
                Logger.w(TAG, "Download is already in progress, ignoring request")
                return@launch
            }

            _uiState.update {
                it.copy(
                    isDownloadInProgress = true,
                    isDownloading = true,
                    downloadError = null
                )
            }

            try {
                manageESFacade.downloadESData(extent).collect { progress ->
                    Logger.d(
                        TAG,
                        "Download progress: ${(progress.progress * 100).toInt()}% - ${progress.message}"
                    )

                    // Check for errors immediately and stop
                    if (progress.error != null) {
                        Logger.e(TAG, "Download failed: ${progress.error}")
                        _uiState.update {
                            it.copy(
                                isDownloadInProgress = false,
                                isDownloading = false,
                                downloadProgress = 0f,
                                downloadMessage = "",
                                downloadError = progress.error
                            )
                        }
                        return@collect
                    }

                    _uiState.update {
                        it.copy(
                            downloadProgress = progress.progress,
                            downloadMessage = progress.message,
                            isDownloading = !progress.isComplete
                        )
                    }

                    if (progress.isComplete && progress.error == null) {
                        Logger.i(TAG, "Download completed successfully")

                        // Load geodatabase and pass to callback
                        progress.geodatabase?.let { geodatabase ->
                            Logger.d(TAG, "Loading geodatabase before notifying callback")

                            // CRITICAL: Load geodatabase first to populate featureTables
                            geodatabase.load().onSuccess {
                                Logger.d(
                                    TAG,
                                    "Geodatabase loaded: ${geodatabase.featureTables.size} tables"
                                )

                                // Wrap single geodatabase in list for consistent callback
                                val geodatabaseInfo = GeodatabaseInfo(
                                    serviceId = "wildfire",
                                    serviceName = "Wildfire",
                                    fileName = "wildfire.geodatabase",
                                    geodatabase = geodatabase,
                                    lastSyncTime = System.currentTimeMillis(),
                                    layerCount = geodatabase.featureTables.size,
                                    fileSizeKB = 0, // Will be calculated by repository
                                    displayOnMap = true
                                )

                                Logger.d(
                                    TAG,
                                    "Notifying geodatabase downloaded with ${geodatabaseInfo.layerCount} layers"
                                )
                                onGeodatabasesDownloaded(listOf(geodatabaseInfo))
                                onSaveTimestamp()

                                // Reload changed data after download
                                loadChangedData()

                                _uiState.update { it.copy(isDownloadInProgress = false) }
                            }.onFailure { error ->
                                Logger.e(TAG, "Failed to load geodatabase", error)
                                _uiState.update {
                                    it.copy(
                                        isDownloadInProgress = false,
                                        isDownloading = false,
                                        downloadError = "Failed to load geodatabase: ${error.message}"
                                    )
                                }
                            }
                        } ?: run {
                            Logger.e(TAG, "Download completed but geodatabase is null")
                            _uiState.update {
                                it.copy(
                                    isDownloadInProgress = false,
                                    isDownloading = false,
                                    downloadError = "Download completed but no geodatabase was created"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error downloading data", e)
                _uiState.update {
                    it.copy(
                        isDownloadInProgress = false,
                        isDownloading = false,
                        downloadError = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onPostDataClicked() {
        Logger.i(TAG, "Post Data clicked - Starting upload process")

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0f, postError = null) }

            try {
                // Simulate 4-second upload with progress
                for (i in 0..100 step 5) {
                    delay(200) // 200ms * 20 steps = 4 seconds
                    _uiState.update { it.copy(uploadProgress = i / 100f) }
                    Logger.v(TAG, "Upload progress: $i%")
                }

                // Call actual use case
                val result = manageESFacade.postESData()
                result.fold(
                    onSuccess = {
                        Logger.i(TAG, "Data posted successfully")
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 1f,
                                postSuccess = true
                            )
                        }
                    },
                    onFailure = { error ->
                        Logger.e(TAG, "Error posting data", error)
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 0f,
                                postError = error.message ?: "Failed to post data"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error posting data", e)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        postError = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onPostDataSuccess() {
        Logger.d(TAG, "Post data success acknowledged")
        _uiState.update { it.copy(postSuccess = false) }
    }

    fun onDeleteJobCardsClicked() {
        Logger.i(TAG, "Delete Job Cards clicked")

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingJobCards = true) }

            try {
                val result = manageESFacade.deleteJobCards()
                result.fold(
                    onSuccess = { count ->
                        Logger.i(TAG, "Deleted $count job cards")
                        _uiState.update {
                            it.copy(
                                isDeletingJobCards = false,
                                deletedJobCardsCount = count,
                                showDeleteDialog = true
                            )
                        }
                    },
                    onFailure = { error ->
                        Logger.e(TAG, "Error deleting job cards", error)
                        _uiState.update {
                            it.copy(
                                isDeletingJobCards = false,
                                deleteError = error.message ?: "Failed to delete job cards"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error deleting job cards", e)
                _uiState.update {
                    it.copy(
                        isDeletingJobCards = false,
                        deleteError = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onDismissDeleteDialog() {
        Logger.d(TAG, "Delete dialog dismissed")
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDismissDownloadDialog() {
        Logger.d(TAG, "Download dialog dismissed - clearing error state")
        _uiState.update {
            it.copy(
                isDownloading = false,
                downloadProgress = 0f,
                downloadMessage = "",
                multiServiceProgress = null,
                downloadError = null  // Clear error state to prevent stale errors
            )
        }
    }

    /**
     * Stores the original map viewpoint when ManageES bottom sheet opens.
     * This enables restoring the map to its previous state if user dismisses
     * without taking action.
     *
     * The stored viewpoint is used for restoration only if the extent change
     * is not committed (i.e., user dismisses without clicking "Get Data").
     *
     * @param viewpoint The current map viewpoint to preserve for potential restoration
     */
    fun storeOriginalViewpoint(viewpoint: Viewpoint?) {
        // Only store if this is a fresh session (no viewpoint currently stored)
        if (_originalViewpoint.value == null) {
            _originalViewpoint.value = viewpoint
            _isExtentCommitted.value = false
            Logger.d(TAG, "Stored original map viewpoint for potential restoration on dismiss")
        } else {
            Logger.v(TAG, "Original viewpoint already stored; skipping duplicate storage")
        }
    }

    /**
     * Handles bottom sheet dismissal with conditional viewpoint restoration.
     *
     * Determines whether to restore the original map viewpoint based on commit status:
     * - If extent was NOT committed (user dismissed without "Get Data"): Restore original viewpoint
     * - If extent was committed (user clicked "Get Data"): Keep current viewpoint
     *
     * Always clears stored state after processing to prevent memory retention,
     * regardless of whether restoration occurred.
     *
     * @return The original viewpoint to restore, or null if committed or no viewpoint stored
     */
    fun onBottomSheetDismissedWithViewpoint(): Viewpoint? {
        val viewpointToRestore =
            if (!_isExtentCommitted.value && _originalViewpoint.value != null) {
                Logger.d(
                    TAG,
                    "Restoring original map viewpoint: extent change not committed by user action"
                )
                _originalViewpoint.value
            } else if (_isExtentCommitted.value) {
                Logger.d(
                    TAG,
                    "Viewpoint not restored: extent change was committed via Get Data action"
                )
                null
            } else {
                Logger.d(TAG, "Viewpoint not restored: no original viewpoint was stored in session")
                null
            }

        // Always clear tracking state to prevent memory retention
        clearViewpointTrackingState()

        return viewpointToRestore
    }

    /**
     * Clears viewpoint tracking state to prevent memory retention.
     * Called after every bottom sheet dismissal regardless of commit status.
     *
     * This ensures:
     * - No memory leaks from retained Viewpoint objects
     * - Each bottom sheet session starts with fresh state
     * - No state pollution between sessions
     */
    private fun clearViewpointTrackingState() {
        _originalViewpoint.value = null
        _isExtentCommitted.value = false
        Logger.v(TAG, "Cleared viewpoint tracking state to prevent memory retention")
    }

    fun onBottomSheetDismissed() {
        Logger.d(TAG, "Bottom sheet dismissed - resetting error states")
        _uiState.update {
            it.copy(
                downloadError = null,
                postError = null,
                deleteError = null
            )
        }
    }

    private fun loadChangedData() {
        Logger.d(TAG, "Loading changed data")
        viewModelScope.launch {
            try {
                val result = manageESFacade.getChangedData()
                result.fold(
                    onSuccess = { changedData ->
                        Logger.i(TAG, "Loaded ${changedData.size} changed items")
                        _uiState.update { it.copy(changedData = changedData) }
                    },
                    onFailure = { error ->
                        Logger.e(TAG, "Error loading changed data", error)
                    }
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error loading changed data", e)
            }
        }
    }

    fun onJobCardSelected(jobCard: JobCard) {
        Logger.d(TAG, "Job card selected: ${jobCard.id}")
        _uiState.update { it.copy(selectedJobCard = jobCard) }
    }

    fun onJobCardDeselected() {
        Logger.d(TAG, "Job card deselected")
        _uiState.update { it.copy(selectedJobCard = null) }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ManageESViewModel cleared")
    }
}

/**
 * UI State for Manage ES screen.
 *
 * Enhanced for multi-service support with separate fields for single and multi-service downloads.
 */
data class ManageESUiState(
    // Distance selection - null indicates no selection
    val selectedDistance: ESDataDistance? = null,

    // Single-service download state (legacy/Wildfire)
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadMessage: String = "",

    // Multi-service download state (Project environment)
    val isMultiServiceDownload: Boolean = false,
    val multiServiceProgress: MultiServiceDownloadProgress? = null,

    // Common download state
    val downloadError: String? = null,
    val isDownloadInProgress: Boolean = false, // Prevents concurrent downloads

    // Upload/Post state
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val isPosting: Boolean = false,
    val postSuccess: Boolean = false,
    val postError: String? = null,

    // Changed data state
    val changedData: List<JobCard> = emptyList(),
    val selectedJobCard: JobCard? = null, // Selected job card for Post/Delete operations

    // Delete job cards state
    val isDeletingJobCards: Boolean = false,
    val deletedJobCardsCount: Int = 0,
    val showDeleteDialog: Boolean = false,
    val deleteError: String? = null
)
