package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 * Refactored to use ManageESFacade - Phase 4: ViewModel Dependency Reduction
 * Enhanced for Multi-Service Support - Phase 5: Presentation Layer
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import com.enbridge.gdsgpscollection.domain.facade.ManageESFacade
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 *
 * @property manageESFacade Facade providing access to all ES data operations
 * @property configuration Environment configuration provider
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
@HiltViewModel
class ManageESViewModel @Inject constructor(
    private val manageESFacade: ManageESFacade,
    private val configuration: FeatureServiceConfiguration
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageESUiState())
    val uiState: StateFlow<ManageESUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "ManageESViewModel"
    }

    init {
        Logger.i(TAG, "ManageESViewModel initialized")
        loadInitialData()
    }

    private fun loadInitialData() {
        Logger.d(TAG, "Loading initial data")
        viewModelScope.launch {
            // Load saved distance preference
            val savedDistance = manageESFacade.getSelectedDistance()
            Logger.d(TAG, "Loaded saved distance: ${savedDistance.displayText}")
            _uiState.update { it.copy(selectedDistance = savedDistance) }

            // Load changed data
            loadChangedData()
        }
    }

    fun onDistanceSelected(distance: ESDataDistance) {
        Logger.i(TAG, "Distance selected: ${distance.displayText}")
        _uiState.update { it.copy(selectedDistance = distance) }

        viewModelScope.launch {
            manageESFacade.saveSelectedDistance(distance)
            Logger.d(TAG, "Distance preference saved: ${distance.displayText}")
        }
    }

    /**
     * Handles "Get Data" button click - automatically selects single or multi-service download
     * based on current environment configuration.
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
                            Logger.d(TAG, "Notifying geodatabase downloaded")

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

                            onGeodatabasesDownloaded(listOf(geodatabaseInfo))
                            onSaveTimestamp()
                        }

                        // Reload changed data after download
                        loadChangedData()

                        _uiState.update { it.copy(isDownloadInProgress = false) }
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
        Logger.d(TAG, "Download dialog dismissed")
        _uiState.update {
            it.copy(
                isDownloading = false,
                downloadProgress = 0f,
                downloadMessage = "",
                multiServiceProgress = null
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
    val selectedDistance: ESDataDistance = ESDataDistance.TWO_KILOMETERS,

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

    // Delete job cards state
    val isDeletingJobCards: Boolean = false,
    val deletedJobCardsCount: Int = 0,
    val showDeleteDialog: Boolean = false,
    val deleteError: String? = null
)
