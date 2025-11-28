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
import com.enbridge.gdsgpscollection.ui.map.delegates.ExtentManagerDelegate
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
 * @property extentManager Extent manager for calculating download extents based on distance
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
    private val locationManager: LocationManagerDelegate,
    private val extentManager: ExtentManagerDelegate
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
     * StateFlow containing current GPS horizontal accuracy in meters.
     *
     * ## Current Implementation (Phase 1):
     * Sources accuracy from ArcGIS Location.horizontalAccuracy, providing standard GPS
     * accuracy values (typically 5-15 meters). This is used for pre-flight checks before
     * downloading geodatabase data in Project environment.
     *
     * ## Rationale for Accuracy Checks:
     * GPS accuracy validation ensures data quality by:
     * - Preventing downloads when position uncertainty is high
     * - Ensuring features are downloaded for the correct geographic area
     * - Reducing data integrity issues from inaccurate location fixes
     * - Meeting operational requirements for field data collection
     *
     * ## Accuracy Threshold:
     * MIN_REQUIRED_ACCURACY = 7.0 meters chosen because:
     * - Typical consumer GPS: 5-15m accuracy
     * - DGPS systems: 1-5m accuracy
     * - 7m provides reasonable balance between accessibility and precision
     * - Suitable for most utility infrastructure mapping (poles, lines, equipment)
     *
     * ## Future Enhancement: External GNSS (Phase 2):
     * When external GNSS receivers are integrated:
     * - This flow will continue to provide horizontalAccuracy from Location
     * - Additional metadata (DOP values, fix quality, satellite count) available via
     *   separate gnssMetadata flow
     * - Adaptive thresholds based on fix quality:
     *   * RTK Fixed: 2m threshold (cm-level accuracy)
     *   * DGPS: 5m threshold (sub-meter accuracy)
     *   * Standard GPS: 7m threshold (meter-level accuracy)
     * - UI can display rich accuracy indicators (signal bars, satellite view)
     *
     * This phased approach allows immediate GPS validation without blocking on
     * external hardware, while maintaining architectural flexibility for future
     * enhancements.
     */
    val currentAccuracy: StateFlow<Float?> = locationManager.currentAccuracy

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

        /**
         * Minimum required GPS horizontal accuracy in meters for data download.
         *
         * ## Rationale for 7.0 meters:
         * This threshold balances operational requirements with GPS capabilities:
         *
         * **Consumer GPS Performance:**
         * - Standard smartphones: 5-15m typical accuracy
         * - Consumer GPS receivers: 3-10m typical accuracy
         * - 7m is achievable by most devices in good conditions
         *
         * **Use Case Requirements:**
         * - Utility infrastructure mapping (poles, transformers, lines)
         * - Asset location accuracy sufficient for field operations
         * - Prevents downloads with poor GPS fixes (>10m error)
         *
         * **Environmental Factors:**
         * - Open sky: 3-5m accuracy possible
         * - Urban canyon: 10-20m accuracy degradation
         * - Forest canopy: 15-30m accuracy degradation
         * - 7m threshold filters out worst-case scenarios
         *
         * **Future Adaptive Thresholds (Phase 2):**
         * When external GNSS with RTK/DGPS is integrated, thresholds will adapt:
         * - RTK Fixed: 2.0m (centimeter-level positioning)
         * - DGPS: 5.0m (sub-meter positioning)
         * - Standard GPS: 7.0m (meter-level positioning)
         *
         * This allows stricter requirements when better equipment is available,
         * while maintaining accessibility for standard GPS operations.
         */
        const val MIN_REQUIRED_ACCURACY = 7.0f
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
     * ## Workflow Overview:
     * 1. **Environment Detection**: Determines if Project or Wildfire environment
     * 2. **Pre-flight Checks**: Project environment only - validates GPS, files, data, changes
     * 3. **Extent Calculation**: Computes download area based on distance and location
     * 4. **Download Execution**: Routes to appropriate download method
     * 5. **Post-download Validation**: Checks for "No Data" scenario
     *
     * ## Project Environment Pre-flight Checks:
     * - GPS longitude validation
     * - GPS accuracy threshold (7m)
     * - File count analysis
     * - Data content validation
     * - Unsaved changes detection
     * - Internet connectivity
     *
     * ## Wildfire Environment:
     * - Simplified flow (existing logic)
     * - No complex pre-flight checks
     *
     * Marks the extent change as committed, preventing viewpoint restoration on dismiss.
     *
     * Environment Routing:
     * - Project Environment: Calls onGetDataMultiService() for parallel downloads
     * - Wildfire Environment: Calls onGetDataSingleService() for legacy download
     *
     * @param selectedDistance The distance radius for the download extent buffer
     * @param centerPoint Center point for the extent (typically user's current location)
     * @param onGeodatabasesDownloaded Callback with list of downloaded geodatabases
     * @param onSaveTimestamp Callback to save download timestamp
     */
    fun onGetDataClicked(
        selectedDistance: ESDataDistance,
        centerPoint: Point,
        onGeodatabasesDownloaded: (List<GeodatabaseInfo>) -> Unit = {},
        onSaveTimestamp: () -> Unit = {}
    ) {
        Logger.i(
            TAG,
            "onGetDataClicked called with distance=${selectedDistance.displayText}, centerPoint=(${centerPoint.x.toInt()}, ${centerPoint.y.toInt()})"
        )

        if (!isGetDataEnabled.value) {
            Logger.w(TAG, "Get Data clicked but button is not enabled; ignoring request")
            return
        }

        // Mark extent change as committed - prevents viewpoint restoration on dismiss
        _isExtentCommitted.value = true
        Logger.d(TAG, "Map extent change committed via Get Data action")

        viewModelScope.launch {
            // ========== ENVIRONMENT DETECTION ==========
            val environment = configuration.getCurrentEnvironment()
            val isProjectEnvironment =
                environment is com.enbridge.gdsgpscollection.domain.config.AppEnvironment.Project

            Logger.i(
                TAG,
                "Environment detected: ${if (isProjectEnvironment) "Project" else "Wildfire"}"
            )

            // ========== PROJECT ENVIRONMENT: COMPREHENSIVE PRE-FLIGHT CHECKS ==========
            if (isProjectEnvironment) {
                Logger.d(TAG, "Performing Project environment pre-flight checks...")

                val checksPass = performPreFlightChecks()

                if (!checksPass) {
                    Logger.w(TAG, "Pre-flight checks failed - aborting download")
                    return@launch
                }

                Logger.i(TAG, "✓ All pre-flight checks passed - proceeding with download")
            } else {
                Logger.d(TAG, "Wildfire environment - skipping complex pre-flight checks")
            }

            // ========== EXTENT CALCULATION ==========
            Logger.d(
                TAG,
                "Calculating extent for distance=${selectedDistance.displayText}, centerPoint=(${centerPoint.x}, ${centerPoint.y})"
            )

            val extent = extentManager.calculateExtentForDistance(selectedDistance, centerPoint)

            if (extent == null) {
                Logger.e(TAG, "Failed to calculate extent for download")
                _uiState.update {
                    it.copy(downloadError = "Failed to calculate download extent. Please try again.")
                }
                return@launch
            }

            Logger.i(
                TAG,
                "Calculated download extent: width=${extent.width.toInt()}m, height=${extent.height.toInt()}m, " +
                        "xMin=${extent.xMin.toInt()}, yMin=${extent.yMin.toInt()}, xMax=${extent.xMax.toInt()}, yMax=${extent.yMax.toInt()}"
            )

            // ========== DOWNLOAD EXECUTION ==========
            val isMultiService = environment.featureServices.size > 1

            Logger.i(
                TAG,
                "Executing download - Type: ${if (isMultiService) "Multi-Service" else "Single-Service"}"
            )

            if (isMultiService) {
                onGetDataMultiService(extent, onGeodatabasesDownloaded, onSaveTimestamp)
            } else {
                onGetDataSingleService(extent, onGeodatabasesDownloaded, onSaveTimestamp)
            }
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

                        // ========== POST-DOWNLOAD VALIDATION: Check for "No Data" ==========
                        Logger.d(TAG, "Validating downloaded geodatabases contain data...")

                        val hasDataResult = manageESFacade.hasDataToLoad()
                        val hasData =
                            hasDataResult.getOrElse { true } // Assume true on error (safe default)

                        if (!hasData) {
                            Logger.e(
                                TAG,
                                "Downloaded geodatabases are empty - showing No Data dialog"
                            )

                            _uiState.update {
                                it.copy(
                                    isDownloadInProgress = false,
                                    isMultiServiceDownload = false,
                                    multiServiceProgress = null
                                )
                            }

                            // Show No Data dialog (user clicking OK will trigger silent clear)
                            showNoDataDialog()
                            return@collect
                        }

                        Logger.d(TAG, "✓ Data validation passed - geodatabases contain features")

                        // Load all geodatabases and pass to callback
                        val result = manageESFacade.loadAllGeodatabases()
                        result.onSuccess { geodatabaseInfos ->
                            Logger.d(TAG, "Loaded ${geodatabaseInfos.size} geodatabases")

                            // Wrap in try-catch to detect corrupted files during loading
                            try {
                                onGeodatabasesDownloaded(geodatabaseInfos)
                                onSaveTimestamp()
                            } catch (e: Exception) {
                                Logger.e(TAG, "Error loading geodatabases - possibly corrupted", e)
                                showCorruptedFileDialog()
                                return@onSuccess
                            }
                        }.onFailure { error ->
                            Logger.e(TAG, "Failed to load geodatabases after download", error)

                            // Show corrupted file dialog for any load failure
                            showCorruptedFileDialog()
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

                                // ========== POST-DOWNLOAD VALIDATION: Check for "No Data" (Wildfire) ==========
                                // Note: For Wildfire (single-service), this is optional but provides
                                // consistent error handling across environments
                                Logger.d(TAG, "Validating geodatabase contains data...")

                                val hasDataResult = manageESFacade.hasDataToLoad()
                                val hasData =
                                    hasDataResult.getOrElse { true } // Assume true on error

                                if (!hasData) {
                                    Logger.e(
                                        TAG,
                                        "Downloaded geodatabase is empty - showing No Data dialog"
                                    )

                                    _uiState.update {
                                        it.copy(
                                            isDownloadInProgress = false,
                                            isDownloading = false,
                                            downloadProgress = 0f,
                                            downloadMessage = ""
                                        )
                                    }

                                    showNoDataDialog()
                                    return@onSuccess
                                }

                                Logger.d(
                                    TAG,
                                    "✓ Data validation passed - geodatabase contains features"
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

                                // Wrap in try-catch to detect corrupted files
                                try {
                                    onGeodatabasesDownloaded(listOf(geodatabaseInfo))
                                    onSaveTimestamp()

                                    // Reload changed data after download
                                    loadChangedData()

                                    _uiState.update { it.copy(isDownloadInProgress = false) }
                                } catch (e: Exception) {
                                    Logger.e(
                                        TAG,
                                        "Error loading geodatabase - possibly corrupted",
                                        e
                                    )
                                    showCorruptedFileDialog()
                                    _uiState.update {
                                        it.copy(
                                            isDownloadInProgress = false,
                                            isDownloading = false
                                        )
                                    }
                                }
                            }.onFailure { error ->
                                Logger.e(TAG, "Failed to load geodatabase", error)
                                showCorruptedFileDialog()
                                _uiState.update {
                                    it.copy(
                                        isDownloadInProgress = false,
                                        isDownloading = false
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

    /**
     * Clears download state when bottom sheet is opened without active geodatabase.
     * Prevents showing stale success messages from previous downloads.
     *
     * This should be called when:
     * - Bottom sheet opens and no geodatabase is loaded
     * - Geodatabase was cleared but bottom sheet wasn't open during deletion
     *
     * @param hasGeodatabase Whether a geodatabase is currently loaded
     */
    fun onBottomSheetOpened(hasGeodatabase: Boolean) {
        if (!hasGeodatabase) {
            Logger.d(TAG, "Bottom sheet opened without geodatabase - clearing download state")
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    downloadProgress = 0f,
                    downloadMessage = "",
                    isDownloadInProgress = false,
                    multiServiceProgress = null
                )
            }
        } else {
            Logger.d(TAG, "Bottom sheet opened with geodatabase present - preserving state")
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

    /**
     * Checks for unsaved changes before initiating download.
     * Shows warning dialog if changes exist, requiring user to sync first.
     *
     * This method prevents data loss by detecting local edits before downloading
     * new data which would overwrite the existing geodatabase.
     *
     * Flow:
     * 1. Check for unsaved changes via facade
     * 2. If changes exist: Show sync warning dialog
     * 3. If no changes: Allow download to proceed
     */
    fun checkSyncBeforeDownload() {
        Logger.i(TAG, "Checking for unsaved changes before download")

        viewModelScope.launch {
            val result = manageESFacade.hasUnsyncedChanges()

            result.onSuccess { hasChanges ->
                if (hasChanges) {
                    Logger.w(TAG, "Unsaved changes detected - showing sync warning dialog")
                    _uiState.update {
                        it.copy(showSyncWarningBeforeDownload = true)
                    }
                } else {
                    Logger.d(TAG, "No unsaved changes - user can proceed with download")
                    // User can proceed with normal download flow
                    // UI will call onGetDataClicked() after this check passes
                }
            }.onFailure { error ->
                Logger.e(TAG, "Failed to check for unsaved changes", error)
                _uiState.update {
                    it.copy(
                        downloadError = "Failed to check for unsaved changes: ${error.message}"
                    )
                }
            }
        }
    }

    /**
     * Dismisses the sync warning dialog.
     * User cancelled the sync operation.
     */
    fun onDismissSyncWarningDialog() {
        Logger.d(TAG, "Sync warning dialog dismissed")
        _uiState.update {
            it.copy(
                showSyncWarningBeforeDownload = false,
                syncBeforeDownloadError = null
            )
        }
    }

    /**
     * User confirmed to sync changes before download.
     * Performs sync operation, then allows download to proceed.
     *
     * Sync Strategy:
     * - Syncs all geodatabases (Wildfire: 1, Project: 2+)
     * - Shows progress during sync
     * - On success: Dismisses dialog, user can then download
     * - On failure: Shows error, blocks download
     */
    fun onSyncBeforeDownload() {
        Logger.i(TAG, "User confirmed sync before download")

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncingBeforeDownload = true,
                    syncBeforeDownloadError = null
                )
            }

            try {
                // Sync all geodatabases
                val syncResult = manageESFacade.syncAllServices()

                syncResult.onSuccess { results ->
                    val allSuccess = results.values.all { it }

                    if (allSuccess) {
                        Logger.i(TAG, "All geodatabases synced successfully")
                        _uiState.update {
                            it.copy(
                                isSyncingBeforeDownload = false,
                                showSyncWarningBeforeDownload = false
                            )
                        }
                        // Download can now proceed - UI will handle the flow
                    } else {
                        val failedServices = results.filter { !it.value }.keys
                        Logger.e(TAG, "Some geodatabases failed to sync: $failedServices")
                        _uiState.update {
                            it.copy(
                                isSyncingBeforeDownload = false,
                                syncBeforeDownloadError = "Failed to sync: ${failedServices.joinToString()}"
                            )
                        }
                    }
                }.onFailure { error ->
                    Logger.e(TAG, "Sync failed before download", error)
                    _uiState.update {
                        it.copy(
                            isSyncingBeforeDownload = false,
                            syncBeforeDownloadError = error.message ?: "Sync failed"
                        )
                    }
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during sync", e)
                _uiState.update {
                    it.copy(
                        isSyncingBeforeDownload = false,
                        syncBeforeDownloadError = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Performs comprehensive pre-flight checks for Project environment before download.
     *
     * Implements the complete decision tree:
     * 1. GPS Longitude validation
     * 2. GPS Accuracy validation (MIN_REQUIRED_ACCURACY)
     * 3. File count check
     * 4. Data validation (for existing files)
     * 5. Unsaved changes check
     * 6. Internet connectivity check
     *
     * @return true if all checks pass and download can proceed, false otherwise
     */
    private suspend fun performPreFlightChecks(): Boolean {
        Logger.i(TAG, "Starting pre-flight checks for Project environment")

        // ========== CHECK 1: GPS Longitude ==========
        val location = currentLocation.value
        if (location == null || location.x == 0.0) {
            Logger.e(
                TAG,
                "Pre-flight check failed: GPS longitude invalid (${location?.x ?: "null"})"
            )
            _uiState.update {
                it.copy(downloadError = "GPS location unavailable. Please wait for GPS fix.")
            }
            return false
        }
        Logger.d(TAG, "✓ GPS Longitude check passed: ${location.x}")

        // ========== CHECK 2: GPS Accuracy ==========
        val accuracy = currentAccuracy.value
        if (accuracy == null || accuracy > MIN_REQUIRED_ACCURACY) {
            val accuracyStr = accuracy?.let { String.format("%.1f", it) } ?: "N/A"
            Logger.e(
                TAG,
                "Pre-flight check failed: GPS accuracy too low ($accuracyStr m > $MIN_REQUIRED_ACCURACY m)"
            )
            _uiState.update {
                it.copy(downloadError = "GPS accuracy too low (${accuracyStr}m). Required: ${MIN_REQUIRED_ACCURACY}m.")
            }
            return false
        }
        Logger.d(
            TAG,
            "✓ GPS Accuracy check passed: ${accuracy}m (threshold: ${MIN_REQUIRED_ACCURACY}m)"
        )

        // ========== CHECK 3: File Count & Data Validation ==========
        val fileCount = manageESFacade.getGeodatabaseFileCount()
        Logger.d(TAG, "Geodatabase file count: $fileCount")

        when {
            fileCount == 0 -> {
                Logger.d(TAG, "No existing files - will proceed directly to download")
                return checkInternetConnectivity()
            }

            fileCount == 1 -> {
                Logger.d(TAG, "Single file exists - will replace with new download")
                return checkInternetConnectivity()
            }

            fileCount > 1 -> {
                Logger.d(TAG, "Multiple files exist - checking for data...")

                // Check if files contain actual data
                val hasDataResult = manageESFacade.hasDataToLoad()
                val hasData = hasDataResult.getOrElse { false }

                if (!hasData) {
                    Logger.w(TAG, "Existing files are empty - will re-download")
                    return checkInternetConnectivity()
                }

                Logger.d(TAG, "Files contain data - checking for unsaved changes...")

                // Check for unsaved changes
                val hasChangesResult = manageESFacade.hasUnsyncedChanges()
                val hasChanges = hasChangesResult.getOrElse { false }

                if (hasChanges) {
                    Logger.w(TAG, "Unsaved changes detected - showing Proceed/Cancel dialog")
                    showProceedCancelDialog()
                    return false // Block download until user makes choice
                }

                Logger.d(TAG, "No unsaved changes - safe to proceed")
                return checkInternetConnectivity()
            }

            else -> {
                Logger.e(TAG, "Unexpected file count: $fileCount")
                return false
            }
        }
    }

    /**
     * Checks internet connectivity before download.
     *
     * @return true if connected, false if offline (shows error)
     */
    private fun checkInternetConnectivity(): Boolean {
        if (!networkMonitor.isCurrentlyConnected()) {
            Logger.e(TAG, "Pre-flight check failed: No internet connection")
            _uiState.update {
                it.copy(downloadError = "No internet connection. Please check your network.")
            }
            return false
        }
        Logger.d(TAG, "✓ Internet connectivity check passed")
        return true
    }

    /**
     * Shows the Proceed/Cancel dialog when user attempts download with unsaved changes.
     */
    fun showProceedCancelDialog() {
        Logger.d(TAG, "Showing Proceed/Cancel dialog for unsaved changes")
        _uiState.update { it.copy(activeDialog = ManageESDialog.ProceedCancelDownload) }
    }

    /**
     * Shows the No Data error dialog when downloaded geodatabase is empty.
     */
    fun showNoDataDialog() {
        Logger.e(TAG, "Showing No Data error dialog - downloaded geodatabase is empty")
        _uiState.update { it.copy(activeDialog = ManageESDialog.NoData) }
    }

    /**
     * Shows the Corrupted File error dialog when geodatabase cannot be loaded.
     */
    fun showCorruptedFileDialog() {
        Logger.e(TAG, "Showing Corrupted File error dialog - geodatabase load failed")
        _uiState.update { it.copy(activeDialog = ManageESDialog.CorruptedFile) }
    }

    /**
     * Dismisses the currently active dialog.
     */
    fun dismissDialog() {
        val currentDialog = _uiState.value.activeDialog
        Logger.d(TAG, "Dismissing dialog: ${currentDialog::class.simpleName}")
        _uiState.update { it.copy(activeDialog = ManageESDialog.None) }
    }

    /**
     * Handles primary action for the currently active dialog.
     *
     * Delegates to appropriate handler based on dialog type:
     * - NoData: Performs silent clear and dismisses
     * - CorruptedFile: Simply dismisses (admin contact required)
     * - ProceedCancelDownload: Dismisses and proceeds with download
     */
    fun onDialogPrimaryAction() {
        when (val dialog = _uiState.value.activeDialog) {
            is ManageESDialog.NoData -> {
                Logger.i(TAG, "User confirmed No Data dialog - performing silent clear")
                performSilentClearAndDismiss()
            }

            is ManageESDialog.CorruptedFile -> {
                Logger.d(TAG, "User dismissed Corrupted File dialog")
                dismissDialog()
            }

            is ManageESDialog.ProceedCancelDownload -> {
                Logger.i(TAG, "User chose to proceed with download despite unsaved changes")
                dismissDialog()
                // Note: Actual download will be triggered by UI after dialog dismisses
            }

            ManageESDialog.None -> {
                Logger.v(TAG, "Primary action called but no dialog active")
            }
        }
    }

    /**
     * Handles secondary action (Cancel) for dialogs that support it.
     * Currently only ProceedCancelDownload has a secondary action.
     */
    fun onDialogSecondaryAction() {
        when (val dialog = _uiState.value.activeDialog) {
            is ManageESDialog.ProceedCancelDownload -> {
                Logger.d(TAG, "User cancelled download to sync changes first")
                dismissDialog()
            }

            else -> {
                Logger.v(TAG, "Secondary action called but dialog doesn't support it")
            }
        }
    }

    /**
     * Performs silent clear of geodatabases and dismisses dialog.
     *
     * This is called when user clicks OK on the No Data dialog.
     * Geodatabases are deleted without showing "Files deleted" Snackbar,
     * allowing the user to immediately retry the download.
     */
    private fun performSilentClearAndDismiss() {
        viewModelScope.launch {
            Logger.d(TAG, "Starting silent clear operation")

            val result = manageESFacade.clearGeodatabases()

            result.onSuccess { count ->
                Logger.i(TAG, "Silent clear complete: $count file(s) deleted")
            }.onFailure { error ->
                Logger.e(TAG, "Silent clear failed", error)
            }

            // Dismiss dialog regardless of result
            dismissDialog()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ManageESViewModel cleared")
    }
}

/**
 * Sealed class representing dialog states for ManageES feature.
 *
 * ## Design Rationale: Sealed Class vs Separate Booleans
 *
 * **Why Sealed Class:**
 * Using a sealed class provides better state management compared to separate boolean flags:
 *
 * 1. **Type Safety:** Impossible to show multiple dialogs simultaneously
 *    - With booleans: `showDialog1 = true, showDialog2 = true` → undefined behavior
 *    - With sealed class: Only one variant active at a time
 *
 * 2. **Exhaustive When:** Compiler enforces handling all dialog types
 *    ```kotlin
 *    when (activeDialog) {
 *        ManageESDialog.None -> { }
 *        ManageESDialog.ProceedCancelDownload -> { }  // Must handle
 *        ManageESDialog.NoData -> { }                 // Must handle
 *        ManageESDialog.CorruptedFile -> { }          // Must handle
 *    }
 *    ```
 *
 * 3. **Single Source of Truth:** One field instead of multiple booleans
 *    - Cleaner state: `activeDialog = ManageESDialog.NoData`
 *    - Easier debugging: Clear which dialog is active
 *    - Simpler testing: `uiState.activeDialog is ManageESDialog.NoData`
 *
 * 4. **Scalability:** Adding new dialogs is simple
 *    - Add new sealed class variant
 *    - Compiler shows where to add handling
 *    - No risk of conflicting boolean states
 *
 * 5. **Clear Intent:** Dialog state is explicit in code
 *    - `activeDialog = ManageESDialog.ProceedCancelDownload` is self-documenting
 *    - Better than `showDialog1 = true` (which dialog is 1?)
 *
 * **Dialog Types:**
 * - **None:** No dialog displayed (default state)
 * - **ProceedCancelDownload:** Warn user about unsaved changes before download
 * - **NoData:** Error when downloaded geodatabase contains no features
 * - **CorruptedFile:** Error when geodatabase file is corrupted or unreadable
 *
 * **Side Effects Handled in ViewModel:**
 * Each dialog's actions are handled by ViewModel methods, keeping state clean:
 * - NoData → Performs silent clear on dismiss
 * - ProceedCancelDownload → Proceeds with download or cancels
 * - CorruptedFile → Simply dismisses (administrator contact required)
 *
 * @author Sathya Narayanan
 */
sealed class ManageESDialog {
    /**
     * No dialog is currently displayed.
     * This is the default state.
     */
    data object None : ManageESDialog()

    /**
     * Dialog shown when user attempts to download but has unsaved local edits.
     * Allows user to proceed (losing changes) or cancel to sync first.
     *
     * Actions:
     * - Primary (Proceed): Continue with download, overwriting local changes
     * - Secondary (Cancel): Dismiss dialog, user can sync manually
     */
    data object ProceedCancelDownload : ManageESDialog()

    /**
     * Error dialog shown when downloaded geodatabase contains no data.
     * This indicates a server issue or incorrect extent selection.
     *
     * Actions:
     * - Primary (OK): Dismisses dialog and triggers silent clear of empty geodatabase
     *
     * Rationale for Silent Clear:
     * Empty geodatabases serve no purpose and consume storage. Automatically
     * removing them prevents confusion and allows immediate retry without manual cleanup.
     */
    data object NoData : ManageESDialog()

    /**
     * Error dialog shown when geodatabase file is corrupted or cannot be loaded.
     * This requires administrator intervention to investigate root cause.
     *
     * Actions:
     * - Primary (OK): Dismisses dialog
     *
     * Possible Causes:
     * - Network interruption during download
     * - Disk write errors
     * - Insufficient storage during download
     * - File system corruption
     * - Incompatible geodatabase version
     */
    data object CorruptedFile : ManageESDialog()
}

/**
 * UI State for Manage ES screen.
 *
 * Enhanced for multi-service support with separate fields for single and multi-service downloads.
 * Includes sync warning state for data loss prevention.
 * Uses sealed class for type-safe dialog management.
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
    val deleteError: String? = null,

    // Sync warning state (data loss prevention)
    val showSyncWarningBeforeDownload: Boolean = false,
    val isSyncingBeforeDownload: Boolean = false,
    val syncBeforeDownloadError: String? = null,

    // Dialog management using sealed class (type-safe, single source of truth)
    val activeDialog: ManageESDialog = ManageESDialog.None
)
