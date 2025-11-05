package com.enbridge.gdsgpscollection.ui.jobs

/**
 * @author Sathya Narayanan
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry
import com.enbridge.gdsgpscollection.domain.usecase.SaveJobCardEntryUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetJobCardEntryUseCase
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobCardEntryUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val entry: JobCardEntry = JobCardEntry(),
    val selectedTab: Int = 0,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class JobCardEntryViewModel @Inject constructor(
    private val saveJobCardEntryUseCase: SaveJobCardEntryUseCase,
    private val getJobCardEntryUseCase: GetJobCardEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobCardEntryUiState())
    val uiState: StateFlow<JobCardEntryUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "JobCardEntryViewModel"
    }

    init {
        Logger.i(TAG, "JobCardEntryViewModel initialized")
    }

    fun updateField(update: (JobCardEntry) -> JobCardEntry) {
        val updatedEntry = update(_uiState.value.entry)
        Logger.d(TAG, "Job card field updated - Work Order: ${updatedEntry.workOrder}")
        _uiState.update { it.copy(entry = updatedEntry) }
    }

    fun selectTab(index: Int) {
        Logger.i(TAG, "Tab selected: $index (${getTabName(index)})")
        _uiState.update { it.copy(selectedTab = index) }
    }

    private fun getTabName(index: Int): String {
        return when (index) {
            0 -> "Job Card"
            1 -> "Measurements"
            2 -> "Meter Info"
            else -> "Unknown"
        }
    }

    fun saveJobCardEntry() {
        val entry = _uiState.value.entry
        Logger.i(
            TAG,
            "Saving job card entry - WO: ${entry.workOrder}, Address: ${entry.address}"
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            Logger.d(TAG, "Job card save initiated")

            saveJobCardEntryUseCase(_uiState.value.entry)
                .onSuccess {
                    Logger.i(
                        TAG,
                        "Job card entry saved successfully - WO: ${entry.workOrder}"
                    )
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    Logger.e(
                        TAG,
                        "Failed to save job card entry - WO: ${entry.workOrder}",
                        exception
                    )
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = false,
                            error = exception.message ?: "Failed to save job card entry"
                        )
                    }
                }
        }
    }

    fun loadJobCardEntry(id: String) {
        Logger.i(TAG, "Loading job card entry with ID: $id")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            Logger.d(TAG, "Job card load initiated")

            getJobCardEntryUseCase(id)
                .onSuccess { entry ->
                    Logger.i(
                        TAG,
                        "Job card entry loaded successfully - WO: ${entry.workOrder}, Address: ${entry.address}"
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entry = entry,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Failed to load job card entry with ID: $id", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load job card entry"
                        )
                    }
                }
        }
    }

    fun clearError() {
        Logger.d(TAG, "Clearing error state")
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        Logger.d(TAG, "Clearing save success state")
        _uiState.update { it.copy(saveSuccess = false) }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "JobCardEntryViewModel cleared")
    }
}
