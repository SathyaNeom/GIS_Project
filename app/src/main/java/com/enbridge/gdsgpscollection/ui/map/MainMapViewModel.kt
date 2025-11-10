package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.ui.map.models.LayerUiState
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MapUiState(
    val gpsStatus: String = "GPS: Off",
    val mapScale: Double = 1e8,
    val geodatabaseLoadError: String? = null,
    val showFirstTimeGuidance: Boolean = false,
    val geodatabaseMetadata: GeodatabaseMetadata? = null
)

/**
 * Metadata about the currently loaded geodatabase
 */
data class GeodatabaseMetadata(
    val lastDownloadTime: Long,
    val layerCount: Int,
    val fileSizeKB: Long
)

@HiltViewModel
class MainMapViewModel @Inject constructor(
    private val application: Application,
    private val loadExistingGeodatabaseUseCase: com.enbridge.gdsgpscollection.domain.usecase.LoadExistingGeodatabaseUseCase,
    private val getSelectedDistanceUseCase: com.enbridge.gdsgpscollection.domain.usecase.GetSelectedDistanceUseCase,
    private val networkMonitor: com.enbridge.gdsgpscollection.util.network.NetworkMonitor
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _showFirstTimeGuidance = MutableStateFlow(false)
    val showFirstTimeGuidance: StateFlow<Boolean> = _showFirstTimeGuidance.asStateFlow()

    private val _geodatabaseLoadError = MutableStateFlow<String?>(null)
    val geodatabaseLoadError: StateFlow<String?> = _geodatabaseLoadError.asStateFlow()

    // Current basemap style for recreation (must be initialized before _map)
    private var currentBasemapStyle: BasemapStyle = BasemapStyle.ArcGISTopographic

    // ArcGIS Map owned by ViewModel
    private val _map = MutableStateFlow<ArcGISMap>(createInitialMap())
    val map: StateFlow<ArcGISMap> = _map.asStateFlow()

    // Layer visibility state
    private val _layerInfoList = MutableStateFlow<List<LayerUiState>>(emptyList())
    val layerInfoList: StateFlow<List<LayerUiState>> = _layerInfoList.asStateFlow()

    // OSM basemap visibility state
    private val _osmVisible = MutableStateFlow(true)
    val osmVisible: StateFlow<Boolean> = _osmVisible.asStateFlow()

    // Network connectivity state - exposed to UI for offline banner
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // Current geodatabase instance
    private var currentGeodatabase: Geodatabase? = null

    // SharedPreferences key for first-time user flag
    private val sharedPrefs by lazy {
        application.getSharedPreferences("map_prefs", android.content.Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "MainMapViewModel"
        private const val PREF_FIRST_LAUNCH = "is_first_launch"
        private const val PREF_GEODATABASE_TIMESTAMP = "geodatabase_timestamp"
    }

    init {
        Logger.i(TAG, "MainMapViewModel initialized")
        observeNetworkConnectivity()
        checkAndLoadExistingGeodatabase()
    }

    /**
     * Observes network connectivity changes and updates the offline state.
     *
     * Collects network state from NetworkMonitor and updates the isOffline state flow.
     * This allows the UI to reactively display offline indicators when connectivity is lost.
     */
    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _isOffline.value = !isConnected

                // Log connectivity state changes
                if (isConnected) {
                    Logger.i(TAG, "Network connection restored")
                } else {
                    Logger.w(TAG, "Network connection lost - Operating in offline mode")
                }
            }
        }
    }

    /**
     * Creates the initial ArcGIS Map with San Francisco viewpoint
     */
    private fun createInitialMap(): ArcGISMap {
        return ArcGISMap(currentBasemapStyle).apply {
            initialViewpoint = Viewpoint(
                center = Point(
                    Constants.SanFrancisco.CENTER_X,
                    Constants.SanFrancisco.CENTER_Y,
                    SpatialReference.webMercator()
                ),
                scale = Constants.SanFrancisco.INITIAL_SCALE
            )
        }
    }

    /**
     * Path to the geodatabase file in internal storage
     */
    private val geodatabaseFilePath: String
        get() = File(application.filesDir, Constants.GEODATABASE_FILE_NAME).absolutePath

    /**
     * Check if geodatabase file exists
     */
    fun geodatabaseExists(): Boolean {
        return File(geodatabaseFilePath).exists()
    }

    /**
     * Update map's maxExtent based on selected distance
     * Restricts panning to the buffer area around San Francisco
     */
    fun updateMaxExtent(distance: ESDataDistance) {
        viewModelScope.launch {
            try {
                val centerPoint = Point(
                    Constants.SanFrancisco.CENTER_X,
                    Constants.SanFrancisco.CENTER_Y,
                    SpatialReference.webMercator()
                )

                // Create geodetic buffer
                val bufferedGeometry = GeometryEngine.bufferGeodeticOrNull(
                    geometry = centerPoint,
                    distance = distance.meters.toDouble(),
                    distanceUnit = com.arcgismaps.geometry.LinearUnit(
                        com.arcgismaps.geometry.LinearUnitId.Meters
                    ),
                    maxDeviation = Double.NaN,
                    curveType = com.arcgismaps.geometry.GeodeticCurveType.Geodesic
                )

                val extent = bufferedGeometry?.extent
                if (extent != null) {
                    _map.value.maxExtent = extent
                    Logger.d(
                        TAG,
                        "Updated maxExtent to ${distance.displayText} around San Francisco"
                    )
                } else {
                    Logger.w(TAG, "Failed to create extent for distance: ${distance.displayText}")
                }
            } catch (e: Exception) {
                Logger.e(TAG, "Error updating maxExtent", e)
            }
        }
    }

    /**
     * Load geodatabase layers and add them to the map.
     *
     * Called after geodatabase generation completes. This method loads all feature tables
     * from the geodatabase and creates feature layers for display on the map.
     * Layer metadata (name, feature count, geometry type) is stored for UI management.
     *
     * @param geodatabase The geodatabase containing feature tables to load
     */
    fun loadGeodatabaseLayers(geodatabase: Geodatabase) {
        viewModelScope.launch {
            try {
                Logger.d(TAG, "loadGeodatabaseLayers called with geodatabase: ${geodatabase.path}")

                // Store reference to current geodatabase
                currentGeodatabase = geodatabase

                // Check if geodatabase is already loaded
                if (geodatabase.loadStatus.value != com.arcgismaps.LoadStatus.Loaded) {
                    Logger.d(TAG, "Geodatabase not loaded, loading now...")
                    geodatabase.load().onFailure { error ->
                        Logger.e(TAG, "Failed to load geodatabase", error)
                        return@launch
                    }
                } else {
                    Logger.d(TAG, "Geodatabase already loaded")
                }

                Logger.d(TAG, "Geodatabase has ${geodatabase.featureTables.size} feature tables")

                // Remove existing geodatabase layers (those prefixed with "GDB_")
                val existingLayerCount = _map.value.operationalLayers
                    .filterIsInstance<FeatureLayer>()
                    .count { it.name?.startsWith("GDB_") == true }

                Logger.d(TAG, "Removing $existingLayerCount existing geodatabase layers")

                _map.value.operationalLayers.removeAll { layer ->
                    (layer as? FeatureLayer)?.name?.startsWith("GDB_") == true
                }

                // Check if there are any feature tables
                if (geodatabase.featureTables.isEmpty()) {
                    Logger.w(TAG, "Geodatabase has no feature tables!")
                    return@launch
                }

                // Add new layers from geodatabase and collect their metadata
                val layerUiStates = mutableListOf<LayerUiState>()

                geodatabase.featureTables.forEachIndexed { index, featureTable ->
                    Logger.d(TAG, "Processing feature table $index: ${featureTable.tableName}")

                    // Load the feature table
                    featureTable.load().onSuccess {
                        Logger.d(TAG, "Feature table ${featureTable.tableName} loaded successfully")

                        // Create feature layer and add to map
                        val featureLayer = FeatureLayer.createWithFeatureTable(featureTable)
                        featureLayer.name = "GDB_${featureTable.tableName}"

                        _map.value.operationalLayers.add(featureLayer)
                        Logger.d(
                            TAG,
                            "Added layer ${featureLayer.name} to map. Total layers: ${_map.value.operationalLayers.size}"
                        )

                        // Get feature count for display
                        // Extract legend items from the feature layer's renderer
                        viewModelScope.launch {
                            val count = try {
                                featureTable.numberOfFeatures
                            } catch (e: Exception) {
                                Logger.w(TAG, "Failed to get feature count: ${e.message}")
                                0L
                            }

                            Logger.d(
                                TAG,
                                "Feature table ${featureTable.tableName} has $count features"
                            )

                            // Determine geometry type by examining the first feature (if available)
                            // Extract legend items from renderer
                            val legendItems = extractLegendItemsFromRenderer(featureLayer)

                            val geometryType = if (count > 0) {
                                try {
                                    // Query just one feature to determine geometry type
                                    val queryParams = com.arcgismaps.data.QueryParameters().apply {
                                        maxFeatures = 1
                                    }
                                    val queryResult =
                                        featureTable.queryFeatures(queryParams).getOrNull()
                                    val firstFeature = queryResult?.firstOrNull()

                                    firstFeature?.let { feature ->
                                        getGeometryType(feature.geometry)
                                    }
                                } catch (e: Exception) {
                                    Logger.w(TAG, "Failed to determine geometry type: ${e.message}")
                                    null
                                }
                            } else {
                                null
                            }

                            // Create layer UI state
                            // Create layer UI state with legend items
                            layerUiStates.add(
                                LayerUiState(
                                    id = featureLayer.name ?: featureTable.tableName,
                                    name = featureTable.tableName,
                                    isVisible = true,
                                    geometryType = geometryType,
                                    legendItems = legendItems,
                                    isExpanded = false
                                )
                            )

                            // Update layer info list after all layers are processed
                            if (layerUiStates.size == geodatabase.featureTables.size) {
                                _layerInfoList.value = layerUiStates
                                Logger.i(
                                    TAG,
                                    "Successfully loaded ${layerUiStates.size} layers from geodatabase"
                                )
                                Logger.d(
                                    TAG,
                                    "Layer info: ${layerUiStates.map { it.name   }}"
                                )
                            }
                        }
                    }.onFailure { error ->
                        Logger.e(
                            TAG,
                            "Failed to load feature table: ${featureTable.tableName}",
                            error
                        )
                    }
                }

            } catch (e: Exception) {
                Logger.e(TAG, "Error loading geodatabase layers", e)
            }
        }
    }

    /**
     * Determine geometry type from an ArcGIS geometry object.
     *
     * @param geometry The geometry to examine
     * @return The corresponding GeometryType enum value
     */
    private fun getGeometryType(geometry: com.arcgismaps.geometry.Geometry?): GeometryType {
        return when (geometry) {
            is com.arcgismaps.geometry.Point,
            is com.arcgismaps.geometry.Multipoint ->
                GeometryType.POINT

            is com.arcgismaps.geometry.Polyline ->
                GeometryType.POLYLINE

            is com.arcgismaps.geometry.Polygon,
            is com.arcgismaps.geometry.Envelope ->
                GeometryType.POLYGON

            else -> GeometryType.UNKNOWN
        }
    }

    /**
     * Toggle visibility of a specific layer on the map.
     *
     * Updates both the ArcGIS feature layer visibility and the UI state.
     *
     * @param layerId The unique identifier of the layer to toggle
     * @param visible The desired visibility state
     */
    fun toggleLayerVisibility(layerId: String, visible: Boolean) {
        // Update the feature layer visibility
        _map.value.operationalLayers
            .filterIsInstance<FeatureLayer>()
            .find { it.name == layerId }
            ?.let { it.isVisible = visible }

        // Update the UI state
        _layerInfoList.update { layers ->
            layers.map { layer ->
                if (layer.id == layerId) layer.copy(isVisible = visible)
                else layer
            }
        }

        Logger.d(TAG, "Toggled layer $layerId visibility to $visible")
    }

    /**
     * Show all geodatabase layers on the map.
     */
    fun showAllLayers() {
        // Show all geodatabase layers
        _map.value.operationalLayers
            .filterIsInstance<FeatureLayer>()
            .filter { it.name?.startsWith("GDB_") == true }
            .forEach { it.isVisible = true }

        // Update UI state
        _layerInfoList.update { layers ->
            layers.map { it.copy(isVisible = true) }
        }

        Logger.d(TAG, "Showing all layers")
    }

    /**
     * Hide all geodatabase layers on the map.
     */
    fun hideAllLayers() {
        // Hide all geodatabase layers
        _map.value.operationalLayers
            .filterIsInstance<FeatureLayer>()
            .filter { it.name?.startsWith("GDB_") == true }
            .forEach { it.isVisible = false }

        // Update UI state
        _layerInfoList.update { layers ->
            layers.map { it.copy(isVisible = false) }
        }

        Logger.d(TAG, "Hiding all layers")
    }

    /**
     * Toggle the expansion state of a layer's legend section.
     *
     * @param layerId The unique identifier of the layer to toggle
     */
    fun toggleLayerExpanded(layerId: String) {
        _layerInfoList.update { layers ->
            layers.map { layer ->
                if (layer.id == layerId) {
                    layer.copy(isExpanded = !layer.isExpanded)
                } else {
                    layer
                }
            }
        }
        Logger.d(TAG, "Toggled layer $layerId expansion")
    }

    /**
     * Toggle visibility of all layers based on current master checkbox state.
     *
     * If all layers are visible, hides all. Otherwise, shows all.
     */
    fun toggleSelectAll() {
        val allVisible = _layerInfoList.value.all { it.isVisible }

        if (allVisible) {
            hideAllLayers()
        } else {
            showAllLayers()
        }
    }

    /**
     * Toggle OpenStreetMap basemap visibility.
     *
     * When enabled, shows the basemap. When disabled, hides the basemap
     * (sets map.basemap to null) showing only operational layers.
     * by recreating the map without basemap.
     *
     * @param visible Desired visibility state
     */
    fun toggleOsmVisibility(visible: Boolean) {
        viewModelScope.launch {
            try {
                // Update state IMMEDIATELY for instant UI feedback
                _osmVisible.value = visible

                // Preserve current map state
                val currentViewpoint = _map.value.initialViewpoint
                val currentLayers = _map.value.operationalLayers.toList()
                val currentMaxExtent = _map.value.maxExtent

                if (visible) {
                    // Recreate map with basemap
                    val newMap = ArcGISMap(currentBasemapStyle).apply {
                        initialViewpoint = currentViewpoint
                        maxExtent = currentMaxExtent

                        // Re-add operational layers
                        operationalLayers.addAll(currentLayers)
                    }
                    _map.value = newMap
                    Logger.d(TAG, "Showing basemap")
                } else {
                    // Create map without basemap (null basemap)
                    val newMap = ArcGISMap().apply {
                        initialViewpoint = currentViewpoint
                        maxExtent = currentMaxExtent

                        // Re-add operational layers
                        operationalLayers.addAll(currentLayers)
                    }
                    _map.value = newMap
                    Logger.d(TAG, "Hiding basemap")
                }

                Logger.d(TAG, "OSM basemap visibility: $visible")
            } catch (e: Exception) {
                Logger.e(TAG, "Error toggling OSM visibility", e)
                // Revert state on error
                _osmVisible.value = !visible
            }
        }
    }

    /**
     * Extract legend items from a feature layer's renderer.
     *
     * Creates simple text-based legend items based on renderer type.
     * For now, this creates placeholder items without symbol images.
     * Full symbol rendering can be implemented later using ArcGIS createSwatch().
     *
     * @param featureLayer The feature layer to extract legend from
     * @return List of legend items
     */
    private suspend fun extractLegendItemsFromRenderer(
        featureLayer: FeatureLayer
    ): List<com.enbridge.gdsgpscollection.domain.entity.LegendItem> {
        return try {
            // Load the layer to access renderer
            featureLayer.load().getOrNull() ?: return emptyList()

            val renderer = featureLayer.renderer
            if (renderer == null) {
                Logger.w(TAG, "Layer ${featureLayer.name} has no renderer")
                return emptyList()
            }

            val legendItems =
                mutableListOf<com.enbridge.gdsgpscollection.domain.entity.LegendItem>()

            // For now, create simple placeholder legend items based on renderer type
            // TODO: Implement full symbol rendering to PNG cache as documented
            when (renderer) {
                is com.arcgismaps.mapping.symbology.SimpleRenderer -> {
                    // Single symbol for all features
                    legendItems.add(
                        com.enbridge.gdsgpscollection.domain.entity.LegendItem(
                            label = featureLayer.name ?: "Features",
                            symbolImagePath = "", // Empty path for now
                            value = null
                        )
                    )
                }

                is com.arcgismaps.mapping.symbology.UniqueValueRenderer -> {
                    // Multiple symbols based on attribute values
                    val uniqueValueRenderer =
                        renderer as com.arcgismaps.mapping.symbology.UniqueValueRenderer
                    uniqueValueRenderer.uniqueValues.forEach { uniqueValue ->
                        val label = uniqueValue.label ?: uniqueValue.values.joinToString(", ")
                        legendItems.add(
                            com.enbridge.gdsgpscollection.domain.entity.LegendItem(
                                label = label,
                                symbolImagePath = "", // Empty path for now
                                value = uniqueValue.values.firstOrNull()?.toString()
                            )
                        )
                    }
                    // Add default symbol if exists
                    if (uniqueValueRenderer.defaultSymbol != null) {
                        legendItems.add(
                            com.enbridge.gdsgpscollection.domain.entity.LegendItem(
                                label = uniqueValueRenderer.defaultLabel ?: "Other",
                                symbolImagePath = "",
                                value = null
                            )
                        )
                    }
                }

                is com.arcgismaps.mapping.symbology.ClassBreaksRenderer -> {
                    // Symbols based on numeric ranges
                    val classBreaksRenderer =
                        renderer as com.arcgismaps.mapping.symbology.ClassBreaksRenderer
                    classBreaksRenderer.classBreaks.forEach { classBreak ->
                        val label =
                            classBreak.label ?: "${classBreak.minValue} - ${classBreak.maxValue}"
                        legendItems.add(
                            com.enbridge.gdsgpscollection.domain.entity.LegendItem(
                                label = label,
                                symbolImagePath = "",
                                value = "${classBreak.minValue}-${classBreak.maxValue}"
                            )
                        )
                    }
                    // Add default symbol if exists
                    if (classBreaksRenderer.defaultSymbol != null) {
                        legendItems.add(
                            com.enbridge.gdsgpscollection.domain.entity.LegendItem(
                                label = classBreaksRenderer.defaultLabel ?: "Other",
                                symbolImagePath = "",
                                value = null
                            )
                        )
                    }
                }

                else -> {
                    Logger.w(TAG, "Unsupported renderer type for layer ${featureLayer.name}")
                }
            }

            Logger.d(
                TAG,
                "Extracted ${legendItems.size} legend items for layer ${featureLayer.name}"
            )
            legendItems
        } catch (e: Exception) {
            Logger.e(TAG, "Error extracting legend items", e)
            emptyList()
        }
    }

    /**
     * Delete geodatabase file and remove layers from map.
     *
     * Closes the current geodatabase connection, deletes the file from storage,
     * removes all layers from the map, and clears the UI state.
     */
    fun deleteGeodatabase() {
        viewModelScope.launch {
            try {
                // Close current geodatabase if open
                currentGeodatabase?.close()
                currentGeodatabase = null

                // Delete file
                val geodatabaseFile = File(geodatabaseFilePath)
                if (geodatabaseFile.exists()) {
                    val deleted = geodatabaseFile.delete()
                    Logger.d(TAG, "Geodatabase file deleted: $deleted")
                }

                // Remove layers from map
                _map.value.operationalLayers.removeAll { layer ->
                    (layer as? FeatureLayer)?.name?.startsWith("GDB_") == true
                }

                // Clear layer info list
                _layerInfoList.value = emptyList()

                // Reset maxExtent
                _map.value.maxExtent = null

                Logger.i(TAG, "Geodatabase deleted successfully")

            } catch (e: Exception) {
                Logger.e(TAG, "Error deleting geodatabase", e)
            }
        }
    }

    /**
     * Update the map's basemap style.
     *
     * Creates a new map with the selected basemap while preserving the current
     * viewpoint, operational layers, and extent restrictions.
     *
     * @param basemapStyle The new basemap style to apply
     */
    fun updateBasemapStyle(basemapStyle: BasemapStyle) {
        viewModelScope.launch {
            try {
                // Get current viewpoint before changing basemap
                // Store the new basemap style
                currentBasemapStyle = basemapStyle

                val currentViewpoint = _map.value.initialViewpoint
                val currentLayers = _map.value.operationalLayers.toList()
                val currentMaxExtent = _map.value.maxExtent

                // Create new map with new basemap
                // Only create with basemap if OSM is visible
                val newMap = if (_osmVisible.value) {
                    ArcGISMap(basemapStyle).apply {
                        initialViewpoint = currentViewpoint
                        maxExtent = currentMaxExtent
                        operationalLayers.addAll(currentLayers)
                    }
                } else {
                    // OSM is hidden, create without basemap
                    ArcGISMap().apply {
                        initialViewpoint = currentViewpoint
                        maxExtent = currentMaxExtent

                    // Re-add operational layers
                        operationalLayers.addAll(currentLayers)
                    }
                }

                _map.value = newMap

                Logger.d(TAG, "Updated basemap style to $basemapStyle")

            } catch (e: Exception) {
                Logger.e(TAG, "Error updating basemap style", e)
            }
        }
    }

    /**
     * Checks for and loads existing geodatabase on app startup.
     *
     * Implements the following UX improvements:
     * 1. Health Check: Validates geodatabase integrity before loading
     * 2. First-Time Guidance: Shows welcome dialog if no geodatabase exists
     * 3. Auto-apply Distance: Applies saved distance preference to map extent
     * 4. Metadata Tracking: Stores and displays geodatabase download timestamp
     */
    private fun checkAndLoadExistingGeodatabase() {
        viewModelScope.launch {
            try {
                Logger.i(TAG, "Checking for existing geodatabase on startup")

                // Load existing geodatabase with health check
                val result = loadExistingGeodatabaseUseCase()

                result.onSuccess { geodatabase ->
                    if (geodatabase != null) {
                        // Geodatabase found and loaded successfully
                        Logger.i(TAG, "Existing geodatabase found and validated")

                        // Store metadata
                        val fileSize = File(geodatabaseFilePath).length()
                        val timestamp = sharedPrefs.getLong(
                            PREF_GEODATABASE_TIMESTAMP,
                            System.currentTimeMillis()
                        )
                        val metadata = GeodatabaseMetadata(
                            lastDownloadTime = timestamp,
                            layerCount = geodatabase.featureTables.size,
                            fileSizeKB = fileSize / 1024
                        )

                        _uiState.update {
                            it.copy(
                                geodatabaseMetadata = metadata
                            )
                        }

                        // Load layers onto map
                        loadGeodatabaseLayers(geodatabase)

                        // Apply saved distance preference to map extent
                        Logger.d(TAG, "Applying saved distance preference to map extent")
                        val savedDistance = getSelectedDistanceUseCase()
                        updateMaxExtent(savedDistance)
                        Logger.d(TAG, "Applied saved distance: ${savedDistance.displayText}")

                    } else {
                        // No geodatabase found - check if first-time user
                        Logger.i(TAG, "No geodatabase found on startup")

                        val isFirstLaunch = sharedPrefs.getBoolean(PREF_FIRST_LAUNCH, true)
                        if (isFirstLaunch) {
                            Logger.i(TAG, "First-time user detected, showing guidance")
                            _showFirstTimeGuidance.value = true
                            sharedPrefs.edit().putBoolean(PREF_FIRST_LAUNCH, false).apply()
                        }
                    }
                }.onFailure { error ->
                    // Geodatabase exists but is corrupted or invalid
                    Logger.e(TAG, "Geodatabase health check failed", error)
                    _geodatabaseLoadError.value = error.message ?: "Failed to load existing data"
                }

            } catch (e: Exception) {
                Logger.e(TAG, "Unexpected error during geodatabase auto-load", e)
                _geodatabaseLoadError.value = "An unexpected error occurred"
            }
        }
    }

    /**
     * Dismisses the first-time guidance dialog
     */
    fun dismissFirstTimeGuidance() {
        _showFirstTimeGuidance.value = false
        Logger.d(TAG, "First-time guidance dismissed")
    }

    /**
     * Dismisses the geodatabase load error
     */
    fun dismissGeodatabaseLoadError() {
        _geodatabaseLoadError.value = null
        Logger.d(TAG, "Geodatabase load error dismissed")
    }

    /**
     * Saves the geodatabase download timestamp
     * Called when a new geodatabase is successfully downloaded
     */
    fun saveGeodatabaseTimestamp() {
        val timestamp = System.currentTimeMillis()
        sharedPrefs.edit().putLong(PREF_GEODATABASE_TIMESTAMP, timestamp).apply()
        Logger.d(TAG, "Saved geodatabase timestamp: $timestamp")
    }

    override fun onCleared() {
        super.onCleared()

        // Close geodatabase connection
        currentGeodatabase?.close()

        Logger.d(TAG, "MainMapViewModel cleared")
    }
}
