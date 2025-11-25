package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 * Refactored to use Delegation Pattern for better separation of concerns
 */
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.data.local.preferences.PreferencesManager
import com.enbridge.gdsgpscollection.domain.config.LocationFeatureFlags
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.domain.usecase.GetSelectedDistanceUseCase
import com.enbridge.gdsgpscollection.data.repository.GeodatabaseMigrationHelper
import com.enbridge.gdsgpscollection.ui.map.delegates.BasemapManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.ExtentManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.GeodatabaseManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.LayerManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.LocationManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.NetworkConnectivityDelegate
import com.enbridge.gdsgpscollection.ui.map.models.LayerUiState
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import javax.inject.Inject

/**
 * UI state for the map screen
 */
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

/**
 * MainMapViewModel - Orchestrates map functionality through specialized delegates.
 *
 * This ViewModel coordinates map operations using the Delegation Pattern for separation of concerns.
 * Each delegate handles a specific responsibility:
 *
 * - LayerManagerDelegate: Layer visibility, TOC filtering, and metadata management
 * - BasemapManagerDelegate: Basemap style selection and OSM layer visibility
 * - GeodatabaseManagerDelegate: Geodatabase lifecycle and persistence
 * - ExtentManagerDelegate: Viewpoint calculation for distance-based extent visualization
 * - NetworkConnectivityDelegate: Network state monitoring for offline awareness
 * - LocationManagerDelegate: Location display and user position tracking
 *
 * Spatial Filtering Architecture:
 * Feature visibility is controlled through geodatabase download extent. When a user downloads
 * data with a selected distance (e.g., 500 meters), only features within that geographic extent
 * are included in the offline geodatabase. Features outside the extent do not exist locally,
 * providing natural spatial filtering without runtime processing.
 *
 * Additional filtering is available through the Table of Contents (TOC), which applies
 * attribute-based filters using SQL WHERE clauses on the loaded geodatabase layers.
 *
 * Benefits of this architecture:
 * - Single Responsibility: Each delegate has one clear purpose
 * - Testability: Delegates can be tested independently with mocks
 * - Maintainability: Changes to one feature domain do not affect others
 * - Clarity: Feature implementation is localized within its delegate
 *
 * @property application Application context for file system operations
 * @property layerManager Manages feature layer visibility and attribute filtering
 * @property basemapManager Manages basemap styles and OSM layer visibility
 * @property geodatabaseManager Manages geodatabase loading, persistence, and metadata
 * @property extentManager Calculates viewpoints for distance-based extent display
 * @property networkConnectivity Monitors network state for offline mode awareness
 * @property locationManager Manages location display and user position
 * @property getSelectedDistanceUseCase Retrieves persisted distance preference
 * @property featureFlags Configuration flags for location behavior (dev/prod modes)
 * @property geodatabaseMigrationHelper Handles backward compatibility for data migration
 */
@HiltViewModel
class MainMapViewModel @Inject constructor(
    private val application: Application,
    private val layerManager: LayerManagerDelegate,
    private val basemapManager: BasemapManagerDelegate,
    private val geodatabaseManager: GeodatabaseManagerDelegate,
    private val extentManager: ExtentManagerDelegate,
    private val networkConnectivity: NetworkConnectivityDelegate,
    private val locationManager: LocationManagerDelegate,
    private val getSelectedDistanceUseCase: GetSelectedDistanceUseCase,
    private val featureFlags: LocationFeatureFlags,
    private val geodatabaseMigrationHelper: GeodatabaseMigrationHelper,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // ArcGIS Map owned by ViewModel
    private val _map = MutableStateFlow<ArcGISMap>(createInitialMap())
    val map: StateFlow<ArcGISMap> = _map.asStateFlow()

    // Expose delegate state flows
    val layerInfoList: StateFlow<List<LayerUiState>> = layerManager.layerInfoList
    val osmVisible: StateFlow<Boolean> = basemapManager.osmVisible
    val isOffline: StateFlow<Boolean> = networkConnectivity.isOffline
    val geodatabaseMetadata: StateFlow<GeodatabaseMetadata?> =
        geodatabaseManager.geodatabaseMetadata
    val showFirstTimeGuidance: StateFlow<Boolean> = geodatabaseManager.showFirstTimeGuidance
    val geodatabaseLoadError: StateFlow<String?> = geodatabaseManager.geodatabaseLoadError
    val isLocationEnabled: StateFlow<Boolean> = locationManager.isLocationEnabled
    val isLocationAvailable: StateFlow<Boolean> = locationManager.isLocationAvailable
    val currentAutoPanMode: StateFlow<com.arcgismaps.location.LocationDisplayAutoPanMode> =
        locationManager.currentAutoPanMode
    val currentLocation: StateFlow<Point?> = locationManager.currentLocation

    /**
     * Location feature flags for controlling location behavior (dev/prod) and debug menu visibility.
     */
    val locationFeatureFlags: LocationFeatureFlags = featureFlags

    /**
     * StateFlow that emits the target viewpoint when the user changes the distance selection.
     * The UI layer should observe this flow and animate the map to the new viewpoint.
     *
     * This ensures consistent zoom behavior whether the user selects a larger or smaller
     * distance. Without explicit viewpoint setting, the map would only adjust when zooming
     * in (smaller extent) due to maxExtent constraints, but would not zoom out when
     * selecting a larger extent.
     */
    private val _targetViewpoint = MutableStateFlow<Viewpoint?>(null)
    val targetViewpoint: StateFlow<Viewpoint?> = _targetViewpoint.asStateFlow()

    /**
     * StateFlow indicating whether map layers are being recreated.
     * Used to show progress indicator during basemap toggle operations.
     */
    val isRecreatingLayers: StateFlow<Boolean> = basemapManager.isRecreating

    /**
     * StateFlow indicating whether layers are currently being loaded from geodatabase.
     * Used to prevent OSM toggle while loading is in progress.
     */
    val isLoadingLayers: StateFlow<Boolean> = layerManager.isLoadingLayers

    companion object {
        private const val TAG = "MainMapViewModel"
        private const val PREF_FIRST_LAUNCH = "is_first_launch"
        private const val PREF_GEODATABASE_TIMESTAMP = "geodatabase_timestamp"
    }

    init {
        Logger.i(TAG, "MainMapViewModel initialized (Refactored with Delegates)")

        // Perform migration if needed for backward compatibility
        geodatabaseMigrationHelper.checkAndMigrate()

        // Initialize OSM visibility from persisted preference
        // This must be called before creating the initial map to ensure correct basemap state
        basemapManager.initializeOsmVisibility()
        Logger.d(TAG, "OSM visibility initialized from preferences")

        networkConnectivity.startObserving()
        checkAndLoadExistingGeodatabase()
    }

    /**
     * Creates the initial ArcGIS Map with San Francisco viewpoint.
     *
     * Default Behavior (Changed):
     * - OSM basemap is HIDDEN by default (no basemap parameter)
     * - Only operational layers (from geodatabase) are visible initially
     * - Users can enable basemap via "Open Street Map" checkbox in Table of Contents
     * - User preference is persisted and restored on subsequent app launches
     *
     * This provides a cleaner initial map view focused on feature data,
     * with optional basemap context available when needed.
     */
    private fun createInitialMap(): ArcGISMap {
        // Create map WITHOUT basemap by default (OSM hidden)
        // BasemapManager will handle adding basemap if user preference indicates visibility
        return ArcGISMap().apply {
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
        return geodatabaseManager.geodatabaseExists()
    }

    /**
     * Updates the map's viewpoint based on the selected distance and emits
     * a target viewpoint for the UI layer to animate to.
     *
     * This method coordinates with the ExtentManagerDelegate to:
     * 1. Calculate an appropriate viewpoint with padding
     * 2. Emit the viewpoint via StateFlow for the UI to observe
     *
     * IMPORTANT: This method does NOT restrict map panning. Users can freely
     * navigate the entire map. The distance selection affects only:
     * - Geodatabase download scope (what data to fetch)
     * - Layer display filtering (what features to show)
     * - Visual boundary indicator (circle overlay)
     *
     * The emitted viewpoint ensures the map zooms to display the selected distance
     * area, providing consistent behavior whether zooming in or out.
     *
     * @param distance The distance radius for the extent buffer
     */
    fun updateMaxExtent(distance: ESDataDistance) {
        viewModelScope.launch {
            val viewpoint = extentManager.calculateViewpointForDistance(
                distance,
                _map.value,
                locationManager.currentLocation.value
            )
            _targetViewpoint.value = viewpoint
        }
    }

    /**
     * Restores a previously stored viewpoint with animated transition.
     *
     * Used to reset the map to its original state when the user dismisses
     * the ManageES bottom sheet without committing the extent change via "Get Data".
     * This provides a non-destructive preview experience where distance selection
     * changes are temporary until explicitly committed.
     *
     * The viewpoint restoration uses the same animation parameters as the
     * preview zoom (1.5 seconds duration, smooth transition) to maintain
     * consistent UX.
     *
     * @param viewpoint The viewpoint to restore (typically the original pre-selection state)
     */
    fun restoreViewpoint(viewpoint: Viewpoint) {
        viewModelScope.launch {
            _targetViewpoint.value = viewpoint
            Logger.d(TAG, "Emitted viewpoint for restoration with animated transition")
        }
    }

    /**
     * Load layers from multiple geodatabases at once.
     * This is more efficient than calling loadGeodatabaseLayers multiple times,
     * as it creates the new map only once with all layers from all geodatabases.
     *
     * @param geodatabases List of geodatabases to load layers from
     */
    fun loadMultipleGeodatabases(geodatabases: List<Geodatabase>) {
        viewModelScope.launch {
            Logger.d(TAG, "loadMultipleGeodatabases called with ${geodatabases.size} geodatabases")

            // Store reference to first geodatabase (for backward compatibility)
            if (geodatabases.isNotEmpty()) {
                geodatabaseManager.setCurrentGeodatabase(geodatabases.first())
            }

            // Preserve current map configuration
            val currentViewpoint = _map.value.initialViewpoint
            val currentMaxExtent = _map.value.maxExtent

            // Remove existing geodatabase layers but keep other layers (like OSM)
            val existingNonGdbLayers = _map.value.operationalLayers
                .filterNot { layer ->
                    (layer as? FeatureLayer)?.name?.startsWith("GDB_") == true
                }

            val existingGdbLayerCount =
                _map.value.operationalLayers.size - existingNonGdbLayers.size
            Logger.d(TAG, "Removing $existingGdbLayerCount existing geodatabase layers")

            // CRITICAL: Load all geodatabases first before accessing their featureTables
            val loadedGeodatabases = mutableListOf<Geodatabase>()
            var geodatabasesLoaded = 0

            geodatabases.forEachIndexed { index, geodatabase ->
                Logger.d(
                    TAG,
                    "Loading geodatabase ${index + 1}/${geodatabases.size}: ${geodatabase.path}"
                )
                Logger.d(
                    TAG,
                    "Geodatabase current loadStatus before load(): ${geodatabase.loadStatus.value}"
                )

                geodatabase.load().onSuccess {
                    Logger.i(TAG, "SUCCESS: Geodatabase loaded successfully!")
                    Logger.d(
                        TAG,
                        "Geodatabase loadStatus after load(): ${geodatabase.loadStatus.value}"
                    )
                    Logger.d(TAG, "Geodatabase has ${geodatabase.featureTables.size} tables")

                    loadedGeodatabases.add(geodatabase)
                    geodatabasesLoaded++

                    // When all geodatabases are loaded, proceed to load their layers
                    if (geodatabasesLoaded == geodatabases.size) {
                        loadLayersFromGeodatabases(
                            loadedGeodatabases,
                            currentViewpoint,
                            currentMaxExtent,
                            existingNonGdbLayers
                        )
                    }
                }.onFailure { error ->
                    Logger.e(
                        TAG,
                        "FAILURE: Failed to load geodatabase: ${geodatabase.path}",
                        error
                    )
                    Logger.e(TAG, "Error type: ${error::class.simpleName}")
                    Logger.e(TAG, "Error message: ${error.message}")
                    geodatabasesLoaded++

                    // Still proceed even if some geodatabases failed
                    if (geodatabasesLoaded == geodatabases.size) {
                        if (loadedGeodatabases.isNotEmpty()) {
                            loadLayersFromGeodatabases(
                                loadedGeodatabases,
                                currentViewpoint,
                                currentMaxExtent,
                                existingNonGdbLayers
                            )
                        } else {
                            Logger.e(TAG, "No geodatabases could be loaded")
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper function to load feature layers from already-loaded geodatabases.
     */
    private suspend fun loadLayersFromGeodatabases(
        geodatabases: List<Geodatabase>,
        currentViewpoint: Viewpoint?,
        currentMaxExtent: com.arcgismaps.geometry.Envelope?,
        existingNonGdbLayers: List<com.arcgismaps.mapping.layers.Layer>
    ) {
        // CRITICAL FIX: Load metadata FIRST before creating map
        // This prevents race condition where user toggles OSM before metadata is ready
        Logger.d(
            TAG,
            "Loading metadata for ${geodatabases.size} geodatabase(s) BEFORE creating map"
        )

        geodatabases.forEach { geodatabase ->
            // Load metadata synchronously - this must complete first
            layerManager.loadGeodatabaseLayers(geodatabase)
        }

        Logger.d(TAG, "Metadata loading complete, now creating feature layers")

        // CRITICAL FIX: Use coroutineScope with async/await to properly wait for all layer loading
        val allNewFeatureLayers = coroutineScope {
            val layerJobs = mutableListOf<kotlinx.coroutines.Deferred<FeatureLayer?>>()

            geodatabases.forEachIndexed { gdbIndex, geodatabase ->
                Logger.d(
                    TAG,
                    "Processing geodatabase ${gdbIndex + 1}/${geodatabases.size}: ${geodatabase.path}"
                )

                geodatabase.featureTables.forEach { featureTable ->
                    // Launch async job for each feature table
                    val job = async {
                        try {
                            // Wait for feature table to load
                            featureTable.load().getOrThrow()

                            val featureLayer = FeatureLayer.createWithFeatureTable(featureTable)
                            // Layer name will be set by LayerManager after creation
                            // using the service prefix pattern

                            // Wait for feature layer to load
                            featureLayer.load().getOrThrow()

                            Logger.d(
                                TAG,
                                "Loaded layer from table ${featureTable.tableName} successfully"
                            )
                            featureLayer
                        } catch (e: Exception) {
                            Logger.e(
                                TAG,
                                "Failed to load feature layer: ${featureTable.tableName}",
                                e
                            )
                            null
                        }
                    }
                    layerJobs.add(job)
                }
            }

            // Wait for ALL jobs to complete
            val results = layerJobs.awaitAll()
            Logger.i(TAG, "All ${layerJobs.size} layer loading jobs completed")

            results.filterNotNull()
        }

        Logger.i(TAG, "Successfully loaded ${allNewFeatureLayers.size} feature layers")

        // Apply layer names from metadata (using service prefix pattern)
        layerManager.applyLayerNames(allNewFeatureLayers)

        // Now create the map with all loaded layers
        createNewMapWithLayers(
            currentViewpoint,
            currentMaxExtent,
            existingNonGdbLayers,
            allNewFeatureLayers
        )
    }

    /**
     * Load geodatabase layers and add them to the map.
     *
     * IMPORTANT: This function creates a NEW ArcGISMap instance after loading layers
     * to ensure the UI (MapView) detects the change and recomposes. Simply mutating
     * operationalLayers on the existing map doesn't trigger Compose recomposition.
     *
     * NOTE: If loading multiple geodatabases, use loadMultipleGeodatabases() instead
     * to avoid race conditions where multiple calls overwrite each other's layers.
     *
     * @param geodatabase The geodatabase containing feature tables to load
     */
    fun loadGeodatabaseLayers(geodatabase: Geodatabase) {
        viewModelScope.launch {
            Logger.d(TAG, "loadGeodatabaseLayers called with geodatabase: ${geodatabase.path}")

            // Store reference to current geodatabase
            geodatabaseManager.setCurrentGeodatabase(geodatabase)

            // CRITICAL FIX: Load metadata FIRST before creating map
            // This prevents race condition where user toggles OSM before metadata is ready
            Logger.d(TAG, "Loading metadata BEFORE creating map")
            layerManager.loadGeodatabaseLayers(geodatabase)
            Logger.d(TAG, "Metadata loading complete")

            // Get all layer metadata to find correct layerIds
            val allMetadata = layerManager.getLayerMetadata()

            // Preserve current map configuration
            val currentViewpoint = _map.value.initialViewpoint
            val currentMaxExtent = _map.value.maxExtent

            // Get known service prefixes from environment
            val environment =
                com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration.getCurrentEnvironment()
            val knownPrefixes = environment.featureServices.map { it.prefix }

            // Remove existing geodatabase layers but keep other layers (like OSM)
            val existingNonGdbLayers = _map.value.operationalLayers
                .filterNot { layer ->
                    (layer as? FeatureLayer)?.let { featureLayer ->
                        // Check if layer name starts with any known service prefix
                        featureLayer.name?.let { name ->
                            knownPrefixes.any { prefix -> name.startsWith(prefix) }
                        } ?: false
                    } ?: false
                }

            val existingGdbLayerCount =
                _map.value.operationalLayers.size - existingNonGdbLayers.size
            Logger.d(TAG, "Removing $existingGdbLayerCount existing geodatabase layers")

            // CRITICAL FIX: Use coroutineScope with async/await to properly wait for all layer loading
            val newFeatureLayers = coroutineScope {
                val layerJobs = mutableListOf<kotlinx.coroutines.Deferred<FeatureLayer?>>()

                geodatabase.featureTables.forEach { featureTable ->
                    // Launch async job for each feature table
                    val job = async {
                        try {
                            // Wait for feature table to load
                            featureTable.load().getOrThrow()

                            val featureLayer = FeatureLayer.createWithFeatureTable(featureTable)
                            // Layer name will be set by LayerManager after creation
                            // using the service prefix pattern

                            // Wait for feature layer to load
                            featureLayer.load().getOrThrow()

                            Logger.d(
                                TAG,
                                "Loaded layer from table ${featureTable.tableName} successfully"
                            )
                            featureLayer
                        } catch (e: Exception) {
                            Logger.e(
                                TAG,
                                "Failed to load feature layer: ${featureTable.tableName}",
                                e
                            )
                            null
                        }
                    }
                    layerJobs.add(job)
                }

                // Wait for ALL jobs to complete
                val results = layerJobs.awaitAll()
                Logger.i(TAG, "All ${layerJobs.size} layer loading jobs completed")

                results.filterNotNull()
            }

            Logger.i(TAG, "Successfully loaded ${newFeatureLayers.size} feature layers")

            // Apply layer names from metadata (using service prefix pattern)
            layerManager.applyLayerNames(newFeatureLayers)

            // Now create the map with all loaded layers
            createNewMapWithLayers(
                currentViewpoint,
                currentMaxExtent,
                existingNonGdbLayers,
                newFeatureLayers
            )
        }
    }

    /**
     * Creates a new ArcGISMap instance with updated layers.
     * This ensures Compose detects the change and triggers MapView recomposition.
     *
     * @param loadedCount Optional parameter for logging (not used in new implementation)
     * @param totalCount Optional parameter for logging (not used in new implementation)
     */
    private fun createNewMapWithLayers(
        currentViewpoint: Viewpoint?,
        currentMaxExtent: com.arcgismaps.geometry.Envelope?,
        existingNonGdbLayers: List<com.arcgismaps.mapping.layers.Layer>,
        newFeatureLayers: List<FeatureLayer>,
        loadedCount: Int = newFeatureLayers.size,
        totalCount: Int = newFeatureLayers.size
    ) {
        Logger.i(
            TAG,
            "Creating new map with ${newFeatureLayers.size} geodatabase layers loaded"
        )

        // Create a completely new ArcGISMap instance using current basemap style
        val newMap = ArcGISMap(basemapManager.currentBasemapStyle).apply {
            initialViewpoint = currentViewpoint
            maxExtent = currentMaxExtent

            // First, add back non-GDB layers (like OSM basemap layers)
            operationalLayers.addAll(existingNonGdbLayers)

            // Then, add the new geodatabase layers
            operationalLayers.addAll(newFeatureLayers)
        }

        // CRITICAL: Assign the new map instance to trigger StateFlow emission
        // This creates a new object reference that Compose can detect
        _map.value = newMap

        Logger.i(
            TAG,
            "New map created with ${newMap.operationalLayers.size} total layers " +
                    "(${existingNonGdbLayers.size} existing + ${newFeatureLayers.size} new GDB layers)"
        )
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
     * Toggle visibility of a specific layer
     */
    fun toggleLayerVisibility(layerId: String, visible: Boolean) {
        val featureLayers = _map.value.operationalLayers.filterIsInstance<FeatureLayer>()
        layerManager.toggleVisibility(layerId, visible, featureLayers)
    }

    /**
     * Show all geodatabase layers
     */
    fun showAllLayers() {
        val featureLayers = _map.value.operationalLayers.filterIsInstance<FeatureLayer>()
        layerManager.showAllLayers(featureLayers)
    }

    /**
     * Hide all geodatabase layers
     */
    fun hideAllLayers() {
        val featureLayers = _map.value.operationalLayers.filterIsInstance<FeatureLayer>()
        layerManager.hideAllLayers(featureLayers)
    }

    /**
     * Toggle layer legend expansion
     */
    fun toggleLayerExpanded(layerId: String) {
        layerManager.toggleLayerExpanded(layerId)
    }

    /**
     * Toggle all layers visibility
     */
    fun toggleSelectAll() {
        val featureLayers = _map.value.operationalLayers.filterIsInstance<FeatureLayer>()
        layerManager.toggleSelectAll(featureLayers)
    }

    /**
     * Toggle OSM basemap visibility
     */
    fun toggleOsmVisibility(visible: Boolean) {
        viewModelScope.launch {
            // Check if layers are still loading
            if (layerManager.isLoadingLayers.value) {
                Logger.w(TAG, "Cannot toggle OSM visibility while layers are loading")
                return@launch
            }

            Logger.d(TAG, "Toggling OSM visibility to: $visible")

            val oldMap = _map.value

            // Extract existing FeatureLayers BEFORE clearing
            val existingLayers = oldMap.operationalLayers.filterIsInstance<FeatureLayer>().toList()
            Logger.d(TAG, "Extracted ${existingLayers.size} FeatureLayers from current map")

            // CRITICAL: Clear the old map's layers to release ownership
            oldMap.operationalLayers.clear()
            Logger.d(TAG, "Cleared old map's operational layers to release ownership")

            // Get new map from delegate (without layers)
            val newMapWithoutLayers = basemapManager.createMapWithBasemapVisibility(visible, oldMap)

            // Add the extracted layers to the new map
            newMapWithoutLayers.operationalLayers.addAll(existingLayers)
            Logger.d(TAG, "Added ${existingLayers.size} layers to new map")

            // Verify map instance changed
            if (oldMap === newMapWithoutLayers) {
                Logger.w(TAG, "Map instance didn't change during OSM toggle")
            } else {
                Logger.d(TAG, "Map instance successfully changed")
            }

            _map.value = newMapWithoutLayers

            Logger.d(
                TAG,
                "OSM visibility toggled successfully. New map has ${newMapWithoutLayers.operationalLayers.size} operational layers"
            )
        }
    }

    /**
     * Update basemap style
     */
    fun updateBasemapStyle(basemapStyle: BasemapStyle) {
        viewModelScope.launch {
            val newMap = basemapManager.updateBasemapStyle(basemapStyle, _map.value)
            _map.value = newMap
            Logger.d(TAG, "Updated basemap style to $basemapStyle")
        }
    }

    /**
     * Delete geodatabase and clear layers
     */
    fun deleteGeodatabase() {
        viewModelScope.launch {
            Logger.i(TAG, "Starting geodatabase deletion")

            // Preserve current viewpoint before clearing
            val currentViewpoint = _map.value.initialViewpoint

            // Create a completely new map without geodatabase layers
            // This ensures Compose detects the change and triggers recomposition
            val newMap = ArcGISMap(basemapManager.currentBasemapStyle).apply {
                initialViewpoint = currentViewpoint
                // Note: maxExtent is not used - map panning is unrestricted
                // Intentionally not adding any operational layers - geodatabase is being deleted
            }

            Logger.d(TAG, "Created new map without geodatabase layers")

            // Update map StateFlow - this triggers MapView recomposition
            _map.value = newMap

            // Delegate geodatabase deletion to GeodatabaseManagerDelegate
            // This closes file handles and deletes the physical file
            val featureLayers = emptyList<FeatureLayer>() // No layers to pass since map is new
            geodatabaseManager.deleteGeodatabase(featureLayers)

            // Clear layer metadata from LayerManagerDelegate
            layerManager.clearLayers()

            // Clear layer cache in BasemapManagerDelegate
            basemapManager.clearLayerCache()

            Logger.i(
                TAG,
                "Geodatabase deleted successfully. New map has ${newMap.operationalLayers.size} layers (should be 0)"
            )
        }
    }

    /**
     * Check and load existing geodatabase on startup
     */
    private fun checkAndLoadExistingGeodatabase() {
        viewModelScope.launch {
            val result = geodatabaseManager.checkAndLoadExisting()

            result.onSuccess { geodatabase ->
                if (geodatabase != null) {
                    // Load layers onto map
                    loadGeodatabaseLayers(geodatabase)

                    // Apply saved distance preference to map extent
                    Logger.d(TAG, "Applying saved distance preference to map extent")
                    val savedDistance = getSelectedDistanceUseCase()
                    updateMaxExtent(savedDistance)
                    Logger.d(TAG, "Applied saved distance: ${savedDistance.displayText}")
                }
            }
        }
    }

    /**
     * Creates a LocationDataSource for displaying user location on the map.
     *
     * ## Current Implementation:
     * Returns a SimulatedLocationDataSource with San Francisco coordinates for testing.
     *
     * ## Future Bluetooth GPS Integration:
     * This method will return a CustomLocationDataSource that connects to a Bluetooth GPS device.
     * The GPS device will provide:
     * - Higher accuracy position (lat, long)
     * - Elevation data
     * - Horizontal and vertical accuracy percentages
     * - Real-time location updates
     *
     * To integrate Bluetooth GPS:
     * 1. Implement BluetoothGpsRepository in the data layer
     * 2. Update LocationManagerDelegateImpl to use CustomLocationDataSource
     * 3. Pass the Bluetooth GPS repository to LocationManagerDelegate via DI
     * 4. Handle connection state and errors in the UI layer
     *
     * @return LocationDataSource configured for the current location provider
     */
    fun createLocationDataSource() = locationManager.createLocationDataSource()

    /**
     * Enables location display on the map.
     */
    fun enableLocationDisplay() {
        locationManager.enableLocation()
        Logger.d(TAG, "Location display enabled")
    }

    /**
     * Disables location display on the map.
     */
    fun disableLocationDisplay() {
        locationManager.disableLocation()
        Logger.d(TAG, "Location display disabled")
    }

    /**
     * Dismiss first-time guidance dialog
     */
    fun dismissFirstTimeGuidance() {
        geodatabaseManager.dismissFirstTimeGuidance()
    }

    /**
     * Dismiss geodatabase load error
     */
    fun dismissGeodatabaseLoadError() {
        geodatabaseManager.dismissGeodatabaseLoadError()
    }

    /**
     * Save geodatabase download timestamp
     */
    fun saveGeodatabaseTimestamp() {
        geodatabaseManager.saveTimestamp()
    }

    /**
     * Toggles location following mode on the map.
     *
     * This method switches between following the user's location and allowing
     * free map navigation. It coordinates with the LocationManagerDelegate to
     * update the auto-pan mode.
     *
     * Behavior:
     * - When Off: Enables CompassNavigation (following with compass rotation)
     * - When Following: Disables to Off (free navigation)
     *
     * ## Current Implementation:
     * Works with SimulatedLocationDataSource at San Francisco coordinates.
     *
     * ## Future Bluetooth GPS Integration:
     * Will automatically work with real GPS data once CustomLocationDataSource
     * is implemented. The auto-pan behavior will smoothly follow actual movement.
     *
     * @return The new auto-pan mode after toggling
     */
    fun toggleLocationFollowing() = locationManager.toggleLocationFollowing()

    /**
     * Sets the auto-pan mode for location display.
     *
     * @param mode The desired auto-pan mode
     */
    fun setAutoPanMode(mode: com.arcgismaps.location.LocationDisplayAutoPanMode) {
        locationManager.setAutoPanMode(mode)
    }

    /**
     * Checks if the OSM basemap guidance should be shown to the user.
     * Returns true if this is the first time, false if already shown.
     *
     * @return true if guidance should be displayed, false otherwise
     */
    fun shouldShowOsmGuidance(): Boolean {
        return !preferencesManager.hasShownOsmGuidance()
    }

    /**
     * Marks the OSM basemap guidance as shown.
     * Prevents the guidance from appearing on subsequent app launches.
     */
    fun markOsmGuidanceShown() {
        preferencesManager.setOsmGuidanceShown(true)
        Logger.d(TAG, "OSM guidance marked as shown")
    }

    /**
     * Updates the user's current location.
     * This should be called by the UI layer when location updates are received from LocationDisplay.
     *
     * The current location is used for operations that need to center on the user,
     * such as extent calculations when selecting distances in ManageES feature.
     *
     * @param location The user's current location point, or null if unavailable
     */
    fun updateCurrentLocation(location: Point?) {
        locationManager.updateCurrentLocation(location)
    }

    override fun onCleared() {
        super.onCleared()

        // Stop network connectivity observation
        networkConnectivity.stopObserving()

        // Close geodatabase connection
        geodatabaseManager.closeGeodatabase()

        Logger.d(TAG, "MainMapViewModel cleared")
    }
}
