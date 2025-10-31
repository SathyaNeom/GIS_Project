package com.enbridge.electronicservices.feature.map

/**
 * @author Sathya Narayanan
 */

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.electronicservices.domain.entity.ESDataDistance
import com.enbridge.electronicservices.domain.entity.JobCard
import com.enbridge.electronicservices.domain.usecase.DeleteJobCardsUseCase
import com.enbridge.electronicservices.domain.usecase.DownloadESDataUseCase
import com.enbridge.electronicservices.domain.usecase.GetChangedDataUseCase
import com.enbridge.electronicservices.domain.usecase.GetSelectedDistanceUseCase
import com.enbridge.electronicservices.domain.usecase.PostESDataUseCase
import com.enbridge.electronicservices.domain.usecase.SaveSelectedDistanceUseCase
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
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load saved distance preference
            val savedDistance = getSelectedDistanceUseCase()
            _uiState.update { it.copy(selectedDistance = savedDistance) }

            // Load changed data
            loadChangedData()
        }
    }

    fun onDistanceSelected(distance: ESDataDistance) {
        Log.d(TAG, "Distance selected: ${distance.displayText}")
        _uiState.update { it.copy(selectedDistance = distance) }

        viewModelScope.launch {
            saveSelectedDistanceUseCase(distance)
        }
    }

    fun onGetDataClicked(latitude: Double, longitude: Double) {
        Log.d(TAG, "Get Data clicked - Lat: $latitude, Lon: $longitude")

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, downloadError = null) }

            try {
                downloadESDataUseCase(
                    distance = _uiState.value.selectedDistance,
                    latitude = latitude,
                    longitude = longitude
                ).collect { progress ->
                    _uiState.update {
                        it.copy(
                            downloadProgress = progress.progress,
                            downloadMessage = progress.message,
                            isDownloading = !progress.isComplete
                        )
                    }

                    if (progress.isComplete) {
                        Log.d(TAG, "Download completed successfully")
                        // Reload changed data after download
                        loadChangedData()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading data", e)
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        downloadError = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun onPostDataClicked() {
        Log.d(TAG, "Post Data clicked")

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, uploadProgress = 0f, postError = null) }

            try {
                // Simulate 4-second upload with progress
                for (i in 0..100 step 5) {
                    delay(200) // 200ms * 20 steps = 4 seconds
                    _uiState.update { it.copy(uploadProgress = i / 100f) }
                }

                // Call actual use case
                val result = postESDataUseCase()
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Data posted successfully")
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 1f,
                                postSuccess = true
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error posting data", error)
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
                Log.e(TAG, "Unexpected error posting data", e)
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
        _uiState.update { it.copy(postSuccess = false) }
    }

    fun onDeleteJobCardsClicked() {
        Log.d(TAG, "Delete Job Cards clicked")

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingJobCards = true) }

            try {
                val result = deleteJobCardsUseCase()
                result.fold(
                    onSuccess = { count ->
                        Log.d(TAG, "Deleted $count job cards")
                        _uiState.update {
                            it.copy(
                                isDeletingJobCards = false,
                                deletedJobCardsCount = count,
                                showDeleteDialog = true
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error deleting job cards", error)
                        _uiState.update {
                            it.copy(
                                isDeletingJobCards = false,
                                deleteError = error.message ?: "Failed to delete job cards"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error deleting job cards", e)
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
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDismissDownloadDialog() {
        _uiState.update {
            it.copy(
                isDownloading = false,
                downloadProgress = 0f,
                downloadMessage = ""
            )
        }
    }

    private fun loadChangedData() {
        viewModelScope.launch {
            try {
                val result = getChangedDataUseCase()
                result.fold(
                    onSuccess = { changedData ->
                        Log.d(TAG, "Loaded ${changedData.size} changed items")
                        _uiState.update { it.copy(changedData = changedData) }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading changed data", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading changed data", e)
            }
        }
    }
}

/**
 * UI State for Manage ES screen
 */
data class ManageESUiState(
    val selectedDistance: ESDataDistance = ESDataDistance.HUNDRED_METERS,
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
    val deleteError: String? = null
)
