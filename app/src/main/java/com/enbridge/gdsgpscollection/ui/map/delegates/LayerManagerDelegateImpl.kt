package com.enbridge.gdsgpscollection.ui.map.delegates

import android.app.Application
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.domain.config.AppEnvironment
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfig
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.domain.entity.LayerMetadata
import com.enbridge.gdsgpscollection.ui.map.models.LayerUiState
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LayerManagerDelegate.
 *
 * Manages all layer-related operations including visibility, expansion state,
 * legend extraction from renderers, and service-aware metadata tracking.
 *
 * Enhanced Features:
 * - Service-aware layer tracking (decoupled from naming conventions)
 * - Environment-based configuration (Project vs. Wildfire)
 * - Metadata storage for layer recreation during basemap toggles
 *
 * @author Sathya Narayanan
 */
@Singleton
class LayerManagerDelegateImpl @Inject constructor(
    private val application: Application
) : LayerManagerDelegate {

    private val _layerInfoList = MutableStateFlow<List<LayerUiState>>(emptyList())
    override val layerInfoList: StateFlow<List<LayerUiState>> = _layerInfoList.asStateFlow()

    // NEW: Layer metadata storage (service-aware)
    private val layerMetadataMap = mutableMapOf<String, LayerMetadata>()

    // Loading state tracking
    private val _isLoadingLayers = MutableStateFlow(false)
    override val isLoadingLayers: StateFlow<Boolean> = _isLoadingLayers.asStateFlow()

    companion object {
        private const val TAG = "LayerManagerDelegate"
    }

    override fun getLayerMetadata(): List<LayerMetadata> {
        return layerMetadataMap.values.toList()
    }

    override fun isLayerVisible(layerId: String): Boolean {
        return _layerInfoList.value.find { it.id == layerId }?.isVisible ?: true
    }

    override fun storeLayerMetadata(metadata: LayerMetadata) {
        layerMetadataMap[metadata.layerId] = metadata
        Logger.d(
            TAG,
            "Stored metadata for layer: ${metadata.layerName} (service: ${metadata.serviceName}, displayOnMap: ${metadata.displayOnMap})"
        )
    }

    override fun hasMetadata(): Boolean {
        return layerMetadataMap.isNotEmpty()
    }

    override fun applyLayerNames(featureLayers: List<FeatureLayer>) {
        // Get current environment to access service configurations
        val environment = FeatureServiceConfiguration.getCurrentEnvironment()

        featureLayers.forEach { featureLayer ->
            val featureTable = featureLayer.featureTable
            if (featureTable != null) {
                // Find metadata for this feature table
                val metadata = layerMetadataMap.values.find { it.featureTable == featureTable }

                if (metadata != null) {
                    // Find the service config to get the prefix
                    val serviceConfig = environment.featureServices.find {
                        it.id == metadata.serviceId
                    }

                    if (serviceConfig != null) {
                        // Apply naming pattern: {servicePrefix}{tableName}
                        val layerName = "${serviceConfig.prefix}${featureTable.tableName}"
                        featureLayer.name = layerName

                        Logger.d(
                            TAG,
                            "Applied name '$layerName' to layer (service: ${serviceConfig.name}, table: ${featureTable.tableName})"
                        )
                    } else {
                        Logger.w(
                            TAG,
                            "Service config not found for serviceId: ${metadata.serviceId}, using default naming"
                        )
                        featureLayer.name = "GDB_${featureTable.tableName}"
                    }
                } else {
                    Logger.w(
                        TAG,
                        "Metadata not found for feature table: ${featureTable.tableName}, using default naming"
                    )
                    featureLayer.name = "GDB_${featureTable.tableName}"
                }
            }
        }
    }

    override fun toggleVisibility(
        layerId: String,
        visible: Boolean,
        featureLayers: List<FeatureLayer>
    ) {
        // LayerId format: {serviceId}_{tableName} (e.g., "wildfire_Points_Sync")
        // Layer name format: {prefix}{tableName} (e.g., "GDB_Points_Sync")
        // We need to match by extracting tableName from layerId

        val tableName = layerId.substringAfter("_", layerId)  // Extract tableName from layerId

        // Find the layer whose feature table name matches
        val matchingLayer = featureLayers.find { layer ->
            layer.featureTable?.tableName == tableName
        }

        if (matchingLayer != null) {
            matchingLayer.isVisible = visible
            Logger.d(TAG, "Toggled layer $layerId (${matchingLayer.name}) visibility to $visible")
        } else {
            Logger.w(TAG, "Layer not found for layerId: $layerId (tableName: $tableName)")
        }

        // Update the UI state
        _layerInfoList.update { layers ->
            layers.map { layer ->
                if (layer.id == layerId) layer.copy(isVisible = visible)
                else layer
            }
        }
    }

    override fun showAllLayers(featureLayers: List<FeatureLayer>) {
        // Show all geodatabase layers that have metadata
        featureLayers
            .filter { layer ->
                layer.featureTable?.let { featureTable ->
                    layerMetadataMap.values.any { it.featureTable == featureTable }
                } ?: false
            }
            .forEach { it.isVisible = true }

        // Update UI state
        _layerInfoList.update { layers ->
            layers.map { it.copy(isVisible = true) }
        }

        Logger.d(TAG, "Showing all layers")
    }

    override fun hideAllLayers(featureLayers: List<FeatureLayer>) {
        // Hide all geodatabase layers that have metadata
        featureLayers
            .filter { layer ->
                layer.featureTable?.let { featureTable ->
                    layerMetadataMap.values.any { it.featureTable == featureTable }
                } ?: false
            }
            .forEach { it.isVisible = false }

        // Update UI state
        _layerInfoList.update { layers ->
            layers.map { it.copy(isVisible = false) }
        }

        Logger.d(TAG, "Hiding all layers")
    }

    override fun toggleLayerExpanded(layerId: String) {
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

    override fun toggleSelectAll(featureLayers: List<FeatureLayer>) {
        val allVisible = _layerInfoList.value.all { it.isVisible }

        if (allVisible) {
            hideAllLayers(featureLayers)
        } else {
            showAllLayers(featureLayers)
        }
    }

    override suspend fun loadGeodatabaseLayers(geodatabase: Geodatabase) = coroutineScope {
        try {
            Logger.d(TAG, "loadGeodatabaseLayers called with geodatabase: ${geodatabase.path}")

            // Set loading state to true
            _isLoadingLayers.value = true

            // Check if geodatabase is already loaded
            if (geodatabase.loadStatus.value != com.arcgismaps.LoadStatus.Loaded) {
                Logger.d(TAG, "Geodatabase not loaded, loading now...")
                try {
                    geodatabase.load()
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to load geodatabase", e)
                    _isLoadingLayers.value = false
                    return@coroutineScope
                }
            } else {
                Logger.d(TAG, "Geodatabase already loaded")
            }

            Logger.d(TAG, "Geodatabase has ${geodatabase.featureTables.size} feature tables")

            // Check if there are any feature tables
            if (geodatabase.featureTables.isEmpty()) {
                Logger.w(TAG, "Geodatabase has no feature tables!")
                _isLoadingLayers.value = false
                return@coroutineScope
            }

            // Get current environment configuration
            val environment = FeatureServiceConfiguration.getCurrentEnvironment()

            // Match geodatabase to service configuration
            val serviceConfig = matchGeodatabaseToService(geodatabase, environment)

            if (serviceConfig == null) {
                Logger.w(
                    TAG,
                    "Could not match geodatabase to any service configuration: ${geodatabase.path}"
                )
                _isLoadingLayers.value = false
                return@coroutineScope
            }

            Logger.i(
                TAG,
                "Matched geodatabase to service: ${serviceConfig.name} (displayOnMap: ${serviceConfig.displayOnMap})"
            )

            // Collect layer metadata
            val layerUiStates = mutableListOf<LayerUiState>()

            // Convert to list for iteration
            val featureTables = geodatabase.featureTables.toList()

            // Await loading all feature tables in parallel
            val tableResults = featureTables.map { featureTable ->
                async {
                    Logger.d(TAG, "Processing feature table: ${featureTable.tableName}")
                    try {
                        featureTable.load()
                        Logger.d(TAG, "Feature table ${featureTable.tableName} loaded successfully")

                        // Generate stable layer ID
                        val layerId = generateLayerId(featureTable, serviceConfig)
                        val layerName = featureTable.tableName

                        // Store metadata with service configuration
                        val metadata = LayerMetadata(
                            layerId = layerId,
                            layerName = layerName,
                            serviceId = serviceConfig.id,
                            serviceName = serviceConfig.name,
                            displayOnMap = serviceConfig.displayOnMap,
                            featureTable = featureTable,
                            geodatabasePath = geodatabase.path,
                            isVisible = true
                        )
                        storeLayerMetadata(metadata)

                        // Get feature count for display
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

                        // Create layer UI state (ALL layers appear in Table of Contents)
                        val uiState = LayerUiState(
                            id = layerId,
                            name = layerName,
                            isVisible = true,
                            geometryType = geometryType,
                            legendItems = emptyList(), // Legend extraction can be added later
                            isExpanded = false
                        )
                        Logger.d(
                            TAG,
                            "Created UI state for layer: $layerName (displayOnMap: ${serviceConfig.displayOnMap})"
                        )
                        uiState
                    } catch (e: Exception) {
                        Logger.e(TAG, "Failed to load feature table: ${featureTable.tableName}", e)
                        null
                    }
                }
            }.awaitAll()

            val loadedUiStates = tableResults.filterNotNull()
            _layerInfoList.value = loadedUiStates

            Logger.i(
                TAG,
                "Successfully loaded ${loadedUiStates.size} layers from ${serviceConfig.name} service"
            )
            Logger.d(
                TAG,
                "Layer info: ${loadedUiStates.map { it.name }}"
            )
            _isLoadingLayers.value = false

        } catch (e: Exception) {
            Logger.e(TAG, "Error loading geodatabase layers", e)
            _isLoadingLayers.value = false
        }
    }

    override fun clearLayers() {
        _layerInfoList.value = emptyList()
        layerMetadataMap.clear()
        Logger.d(TAG, "Cleared all layers and metadata")
    }

    /**
     * Matches a geodatabase to its service configuration based on file path and environment.
     *
     * Matching Strategy:
     * 1. Extract filename from geodatabase path
     * 2. Match by service ID in filename (e.g., "wildfire.geodatabase", "operations_1234.geodatabase")
     * 3. Fallback: If single service environment, use that service
     *
     * @param geodatabase The geodatabase to match
     * @param environment Current application environment
     * @return Matching FeatureServiceConfig or null if no match found
     */
    private fun matchGeodatabaseToService(
        geodatabase: Geodatabase,
        environment: AppEnvironment
    ): FeatureServiceConfig? {
        // Extract filename from path
        val fileName = geodatabase.path.substringAfterLast("/").substringAfterLast("\\")

        Logger.d(TAG, "Matching geodatabase file: $fileName")

        // Match by service ID in filename (e.g., "wildfire.geodatabase", "operations_1234.geodatabase")
        return environment.featureServices.firstOrNull { service ->
            fileName.contains(service.id, ignoreCase = true)
        } ?: run {
            // Fallback: If single service environment, use that service
            if (environment.featureServices.size == 1) {
                Logger.d(
                    TAG,
                    "Single service environment, using: ${environment.featureServices.first().name}"
                )
                environment.featureServices.first()
            } else {
                null
            }
        }
    }

    /**
     * Generates a stable, unique layer ID that doesn't depend on naming conventions.
     *
     * Format: {serviceId}_{tableName}
     * Examples:
     * - "wildfire_gas_pipeline"
     * - "operations_meter_stations"
     * - "basemap_streets"
     *
     * @param featureTable The feature table
     * @param serviceConfig Service configuration for the geodatabase
     * @return Unique layer identifier
     */
    private fun generateLayerId(
        featureTable: com.arcgismaps.data.FeatureTable,
        serviceConfig: FeatureServiceConfig
    ): String {
        return "${serviceConfig.id}_${featureTable.tableName}"
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
}
