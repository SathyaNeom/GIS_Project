package com.enbridge.gpsdeviceproj.ui.map

/**
 * @author Sathya Narayanan
 */
import androidx.lifecycle.ViewModel
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

    // Map control functions will be added here
}
