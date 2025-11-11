package com.enbridge.gdsgpscollection.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import com.enbridge.gdsgpscollection.domain.facade.ProjectSettingsFacade
import com.enbridge.gdsgpscollection.util.Logger
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
 *
 * Refactored to use [ProjectSettingsFacade] instead of 3 individual use cases,
 * reducing dependencies from 3 → 1 (67% reduction)
 */
@HiltViewModel
class ProjectSettingsViewModel @Inject constructor(
    private val projectSettingsFacade: ProjectSettingsFacade
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
        Logger.i(TAG, "ProjectSettingsViewModel initialized")
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
            Logger.d(TAG, "Loading project settings from repository")

            projectSettingsFacade.getProjectSettings()
                .onSuccess { settings ->
                    Logger.i(
                        TAG,
                        "Project settings loaded successfully - Crew ID: ${settings.crewId}"
                    )
                    _uiState.update {
                        it.copy(
                            projectSettings = settings,
                            isLoadingProjectSettings = false,
                            projectSettingsError = null
                        )
                    }
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Failed to load project settings", exception)
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

            Logger.i(
                TAG,
                "Fetching work orders - Pole Type: $poleType, Location: ($DEFAULT_LATITUDE, $DEFAULT_LONGITUDE)"
            )

            projectSettingsFacade.getWorkOrders(
                poleType = poleType,
                latitude = DEFAULT_LATITUDE,
                longitude = DEFAULT_LONGITUDE,
                distance = DEFAULT_DISTANCE
            )
                .onSuccess { workOrders ->
                    Logger.i(TAG, "Work orders loaded successfully - Count: ${workOrders.size}")
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
                    Logger.e(TAG, "Failed to load work orders", exception)
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
        Logger.i(TAG, "Pole type selected: $poleType")
        _uiState.update { it.copy(selectedPoleType = poleType) }
    }

    /**
     * Select a work order from the list
     */
    fun selectWorkOrder(workOrder: WorkOrder) {
        Logger.i(
            TAG,
            "Work order selected - WO#: ${workOrder.workOrderNumber}, Address: ${workOrder.address}"
        )
        _uiState.update { it.copy(selectedWorkOrder = workOrder) }
    }

    /**
     * Update search query and filter work orders
     * Filters by work order number
     */
    fun updateSearchQuery(query: String) {
        Logger.d(TAG, "Search query updated: '$query'")
        _uiState.update { currentState ->
            val filtered = if (query.isEmpty()) {
                currentState.workOrders
            } else {
                currentState.workOrders.filter { workOrder ->
                    workOrder.workOrderNumber.contains(query, ignoreCase = true)
                }
            }

            Logger.d(TAG, "Filtered work orders: ${filtered.size} results")

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
        Logger.d(TAG, "Search cleared - Showing all work orders")
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
        Logger.d(TAG, "Project settings updated - Crew ID: ${updatedSettings.crewId}")
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
                Logger.w(TAG, "Cannot save: No work order selected")
                _uiState.update {
                    it.copy(saveError = "Please select a work order")
                }
                return@launch
            }

            if (settings == null) {
                Logger.w(TAG, "Cannot save: No project settings available")
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

            Logger.i(
                TAG,
                "Saving project settings - WO#: $workOrderNumber, Crew: ${settings.crewId}"
            )

            projectSettingsFacade.saveProjectSettings(workOrderNumber, settings)
                .onSuccess {
                    Logger.i(TAG, "Project settings saved successfully")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            saveError = null
                        )
                    }
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Failed to save project settings", exception)
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
        Logger.d(TAG, "Clearing all error states")
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
        Logger.d(TAG, "Resetting save success state")
        _uiState.update { it.copy(saveSuccess = false) }
    }

    /**
     * Reset the entire state (when bottom sheet is dismissed)
     */
    fun resetState() {
        Logger.i(TAG, "Resetting ProjectSettingsViewModel state")
        _uiState.value = ProjectSettingsUiState(
            selectedPoleType = "8 Foot Pole",
            projectSettings = _uiState.value.projectSettings // Keep loaded settings
        )
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "ProjectSettingsViewModel cleared")
    }
}
