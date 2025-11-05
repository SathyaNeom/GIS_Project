package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */
import androidx.lifecycle.ViewModel
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MapUiState(
    val gpsStatus: String = "GPS: Off",
    val mapScale: Double = 1e8
)

@HiltViewModel
class MainMapViewModel @Inject constructor(
    // Add use cases here as needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "MainMapViewModel"
    }

    init {
        Logger.i(TAG, "MainMapViewModel initialized")
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "MainMapViewModel cleared")
    }

    // Map control functions will be added here
}
