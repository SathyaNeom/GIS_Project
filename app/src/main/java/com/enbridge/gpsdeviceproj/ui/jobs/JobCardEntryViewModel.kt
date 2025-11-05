package com.enbridge.gpsdeviceproj.ui.jobs

/**
 * @author Sathya Narayanan
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gpsdeviceproj.domain.entity.JobCardEntry
import com.enbridge.gpsdeviceproj.domain.usecase.SaveJobCardEntryUseCase
import com.enbridge.gpsdeviceproj.domain.usecase.GetJobCardEntryUseCase
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

    fun updateField(update: (JobCardEntry) -> JobCardEntry) {
        _uiState.update { it.copy(entry = update(it.entry)) }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun saveJobCardEntry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }

            saveJobCardEntryUseCase(_uiState.value.entry)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getJobCardEntryUseCase(id)
                .onSuccess { entry ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entry = entry,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
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
        _uiState.update { it.copy(error = null) }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
