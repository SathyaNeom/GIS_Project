package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enbridge.gdsgpscollection.domain.entity.FeatureType
import com.enbridge.gdsgpscollection.domain.usecase.GetFeatureTypesUseCase
import com.enbridge.gdsgpscollection.util.Logger
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

    companion object {
        private const val TAG = "CollectESViewModel"
    }

    init {
        Logger.i(TAG, "CollectESViewModel initialized")
        loadFeatureTypes()
    }

    /**
     * Load feature types from the repository
     */
    fun loadFeatureTypes() {
        Logger.d(TAG, "Loading feature types")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getFeatureTypesUseCase()
                .onSuccess { featureTypes ->
                    Logger.i(TAG, "Feature types loaded successfully - Count: ${featureTypes.size}")
                    featureTypes.forEach { type ->
                        Logger.d(TAG, "  - ${type.name} (${type.geometryType})")
                    }
                    _uiState.value = _uiState.value.copy(
                        featureTypes = featureTypes,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Failed to load feature types", exception)
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
        Logger.d(TAG, "Clearing error state")
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "CollectESViewModel cleared")
    }
}
