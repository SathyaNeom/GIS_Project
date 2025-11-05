package com.enbridge.gpsdeviceproj.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gpsdeviceproj.domain.entity.ProjectSettings
import com.enbridge.gpsdeviceproj.domain.entity.WorkOrder
import com.enbridge.gpsdeviceproj.domain.usecase.GetProjectSettingsUseCase
import com.enbridge.gpsdeviceproj.domain.usecase.GetWorkOrdersUseCase
import com.enbridge.gpsdeviceproj.domain.usecase.SaveProjectSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Project Settings Bottom Sheet
 */
data class ProjectSettingsUiState(
    // Screen 1: Work Order Selection
    val workOrders: List<WorkOrder> = emptyList(),
    val filteredWorkOrders: List<WorkOrder> = emptyList(),
    val selectedPoleType: String = "8 Foot Pole", // Default selection
    val selectedWorkOrder: WorkOrder? = null,
    val searchQuery: String = "",
    val isLoadingWorkOrders: Boolean = false,
    val workOrdersError: String? = null,

    // Screen 2: Crew Information
    val projectSettings: ProjectSettings? = null,
    val isLoadingProjectSettings: Boolean = false,
    val projectSettingsError: String? = null,

    // Save operation
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * ViewModel for managing the Project Settings Bottom Sheet state and business logic
 * Follows MVVM architecture with clean separation of concerns
 */
@HiltViewModel
class ProjectSettingsViewModel @Inject constructor(
    private val getWorkOrdersUseCase: GetWorkOrdersUseCase,
    private val getProjectSettingsUseCase: GetProjectSettingsUseCase,
    private val saveProjectSettingsUseCase: SaveProjectSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectSettingsUiState())
    val uiState: StateFlow<ProjectSettingsUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "ProjectSettingsVM"

        // Mock location - Toronto, Canada (same as used in MainMapScreen)
        private const val DEFAULT_LATITUDE = 43.6532
        private const val DEFAULT_LONGITUDE = -79.3832
        private const val DEFAULT_DISTANCE = 500 // meters
    }

    init {
        loadProjectSettings()
    }

    /**
     * Load project settings from repository
     */
    fun loadProjectSettings() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingProjectSettings = true,
                    projectSettingsError = null
                )
            }
            Log.d(TAG, "Loading project settings")

            getProjectSettingsUseCase()
                .onSuccess { settings ->
                    Log.d(TAG, "Project settings loaded successfully: ${settings.crewId}")
                    _uiState.update {
                        it.copy(
                            projectSettings = settings,
                            isLoadingProjectSettings = false,
                            projectSettingsError = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to load project settings", exception)
                    _uiState.update {
                        it.copy(
                            isLoadingProjectSettings = false,
                            projectSettingsError = exception.message
                                ?: "Failed to load project settings"
                        )
                    }
                }
        }
    }

    /**
     * Get work orders based on selected pole type
     * Uses mock GPS location for demonstration
     */
    fun getWorkOrders() {
        viewModelScope.launch {
            val poleType = _uiState.value.selectedPoleType
            _uiState.update {
                it.copy(
                    isLoadingWorkOrders = true,
                    workOrdersError = null,
                    searchQuery = "" // Clear search when fetching new data
                )
            }

            Log.d(TAG, "Fetching work orders for pole type: $poleType")

            getWorkOrdersUseCase(
                poleType = poleType,
                latitude = DEFAULT_LATITUDE,
                longitude = DEFAULT_LONGITUDE,
                distance = DEFAULT_DISTANCE
            )
                .onSuccess { workOrders ->
                    Log.d(TAG, "Work orders loaded successfully: ${workOrders.size} items")
                    _uiState.update {
                        it.copy(
                            workOrders = workOrders,
                            filteredWorkOrders = workOrders,
                            isLoadingWorkOrders = false,
                            workOrdersError = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to load work orders", exception)
                    _uiState.update {
                        it.copy(
                            isLoadingWorkOrders = false,
                            workOrdersError = exception.message ?: "Failed to load work orders"
                        )
                    }
                }
        }
    }

    /**
     * Update selected pole type
     */
    fun selectPoleType(poleType: String) {
        Log.d(TAG, "Pole type selected: $poleType")
        _uiState.update { it.copy(selectedPoleType = poleType) }
    }

    /**
     * Select a work order from the list
     */
    fun selectWorkOrder(workOrder: WorkOrder) {
        Log.d(TAG, "Work order selected: ${workOrder.workOrderNumber}")
        _uiState.update { it.copy(selectedWorkOrder = workOrder) }
    }

    /**
     * Update search query and filter work orders
     * Filters by work order number
     */
    fun updateSearchQuery(query: String) {
        Log.d(TAG, "Search query updated: $query")
        _uiState.update { currentState ->
            val filtered = if (query.isEmpty()) {
                currentState.workOrders
            } else {
                currentState.workOrders.filter { workOrder ->
                    workOrder.workOrderNumber.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredWorkOrders = filtered
            )
        }
    }

    /**
     * Clear search query and show all work orders
     */
    fun clearSearch() {
        Log.d(TAG, "Search cleared")
        _uiState.update {
            it.copy(
                searchQuery = "",
                filteredWorkOrders = it.workOrders
            )
        }
    }

    /**
     * Update project settings (crew information)
     */
    fun updateProjectSettings(updatedSettings: ProjectSettings) {
        Log.d(TAG, "Project settings updated: ${updatedSettings.crewId}")
        _uiState.update { it.copy(projectSettings = updatedSettings) }
    }

    /**
     * Save project settings with selected work order
     */
    fun saveProjectSettings() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val workOrderNumber = currentState.selectedWorkOrder?.workOrderNumber
            val settings = currentState.projectSettings

            if (workOrderNumber == null) {
                Log.w(TAG, "Cannot save: No work order selected")
                _uiState.update {
                    it.copy(saveError = "Please select a work order")
                }
                return@launch
            }

            if (settings == null) {
                Log.w(TAG, "Cannot save: No project settings available")
                _uiState.update {
                    it.copy(saveError = "Project settings not loaded")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveError = null,
                    saveSuccess = false
                )
            }

            Log.d(TAG, "Saving project settings with WO: $workOrderNumber")

            saveProjectSettingsUseCase(workOrderNumber, settings)
                .onSuccess {
                    Log.d(TAG, "Project settings saved successfully")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            saveError = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to save project settings", exception)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = exception.message ?: "Failed to save project settings"
                        )
                    }
                }
        }
    }

    /**
     * Clear any error states
     */
    fun clearErrors() {
        Log.d(TAG, "Clearing errors")
        _uiState.update {
            it.copy(
                workOrdersError = null,
                projectSettingsError = null,
                saveError = null
            )
        }
    }

    /**
     * Reset save success state
     */
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    /**
     * Reset the entire state (when bottom sheet is dismissed)
     */
    fun resetState() {
        Log.d(TAG, "Resetting state")
        _uiState.value = ProjectSettingsUiState(
            selectedPoleType = "8 Foot Pole",
            projectSettings = _uiState.value.projectSettings // Keep loaded settings
        )
    }
}
