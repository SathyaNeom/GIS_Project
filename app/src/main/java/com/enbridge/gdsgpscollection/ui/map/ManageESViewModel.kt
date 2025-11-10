package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.usecase.DeleteJobCardsUseCase
import com.enbridge.gdsgpscollection.domain.usecase.DownloadESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetChangedDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetSelectedDistanceUseCase
import com.enbridge.gdsgpscollection.domain.usecase.PostESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.SaveSelectedDistanceUseCase
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
 * ViewModel for Manage ES Edits feature
 * Manages state for downloading, posting, and managing ES data
 */
@HiltViewModel
class ManageESViewModel @Inject constructor(
    private val downloadESDataUseCase: DownloadESDataUseCase,
    private val postESDataUseCase: PostESDataUseCase,
    private val getChangedDataUseCase: GetChangedDataUseCase,
    private val deleteJobCardsUseCase: DeleteJobCardsUseCase,
    private val getSelectedDistanceUseCase: GetSelectedDistanceUseCase,
    private val saveSelectedDistanceUseCase: SaveSelectedDistanceUseCase
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
            val savedDistance = getSelectedDistanceUseCase()
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
            saveSelectedDistanceUseCase(distance)
            Logger.d(TAG, "Distance preference saved: ${distance.displayText}")
        }
    }

    fun onGetDataClicked(
        extent: Envelope,
        onGeodatabaseDownloaded: (com.arcgismaps.data.Geodatabase) -> Unit = {},
        onSaveTimestamp: () -> Unit = {}
    ) {
        Logger.i(
            TAG,
            "Get Data clicked - Extent: ${extent.xMin}, ${extent.yMin}, ${extent.xMax}, ${extent.yMax}"
        )

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
            Logger.d(TAG, "Starting ES data download...")

            try {
                downloadESDataUseCase(extent).collect { progress ->
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

                        // Pass the geodatabase to the callback if available
                        progress.geodatabase?.let { geodatabase ->
                            Logger.d(TAG, "Notifying geodatabase downloaded")
                            onGeodatabaseDownloaded(geodatabase)
                            // Save timestamp after successful download
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
                val result = postESDataUseCase()
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
                val result = deleteJobCardsUseCase()
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
                downloadMessage = ""
            )
        }
    }

    private fun loadChangedData() {
        Logger.d(TAG, "Loading changed data")
        viewModelScope.launch {
            try {
                val result = getChangedDataUseCase()
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
 * UI State for Manage ES screen
 */
data class ManageESUiState(
    val selectedDistance: ESDataDistance = ESDataDistance.TWO_KILOMETERS,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadMessage: String = "",
    val downloadError: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val isPosting: Boolean = false,
    val postSuccess: Boolean = false,
    val postError: String? = null,
    val changedData: List<JobCard> = emptyList(),
    val isDeletingJobCards: Boolean = false,
    val deletedJobCardsCount: Int = 0,
    val showDeleteDialog: Boolean = false,
    val deleteError: String? = null,
    val isDownloadInProgress: Boolean = false // Prevents concurrent downloads
)
