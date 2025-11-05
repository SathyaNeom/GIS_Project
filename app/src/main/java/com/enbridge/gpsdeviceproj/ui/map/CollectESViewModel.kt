package com.enbridge.gpsdeviceproj.ui.map

/**
 * @author Sathya Narayanan
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gpsdeviceproj.domain.entity.FeatureType
import com.enbridge.gpsdeviceproj.domain.usecase.GetFeatureTypesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Collect Electronic Services Bottom Sheet
 */
data class CollectESUiState(
    val featureTypes: List<FeatureType> = emptyList(),
    val isLoading: Boolean = true,  // Start with loading true to prevent initial glitch
    val error: String? = null
)

/**
 * ViewModel for managing the Collect ES Bottom Sheet state and business logic
 */
@HiltViewModel
class CollectESViewModel @Inject constructor(
    private val getFeatureTypesUseCase: GetFeatureTypesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectESUiState())
    val uiState: StateFlow<CollectESUiState> = _uiState.asStateFlow()

    init {
        loadFeatureTypes()
    }

    /**
     * Load feature types from the repository
     */
    fun loadFeatureTypes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getFeatureTypesUseCase()
                .onSuccess { featureTypes ->
                    _uiState.value = _uiState.value.copy(
                        featureTypes = featureTypes,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load feature types"
                    )
                }
        }
    }

    /**
     * Clear any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
