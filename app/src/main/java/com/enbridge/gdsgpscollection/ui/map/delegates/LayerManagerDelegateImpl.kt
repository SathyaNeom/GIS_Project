package com.enbridge.gdsgpscollection.ui.map.delegates

import android.app.Application
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.ui.map.models.LayerUiState
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LayerManagerDelegate.
 *
 * Manages all layer-related operations including visibility, expansion state,
 * and legend extraction from renderers.
 *
 * @author Sathya Narayanan
 */
@Singleton
class LayerManagerDelegateImpl @Inject constructor(
    private val application: Application
) : LayerManagerDelegate {

    private val _layerInfoList = MutableStateFlow<List<LayerUiState>>(emptyList())
    override val layerInfoList: StateFlow<List<LayerUiState>> = _layerInfoList.asStateFlow()

    companion object {
        private const val TAG = "LayerManagerDelegate"
    }

    override fun toggleVisibility(
        layerId: String,
        visible: Boolean,
        featureLayers: List<FeatureLayer>
    ) {
        // Update the feature layer visibility
        featureLayers
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

    override fun showAllLayers(featureLayers: List<FeatureLayer>) {
        // Show all geodatabase layers
        featureLayers
            .filter { it.name?.startsWith("GDB_") == true }
            .forEach { it.isVisible = true }

        // Update UI state
        _layerInfoList.update { layers ->
            layers.map { it.copy(isVisible = true) }
        }

        Logger.d(TAG, "Showing all layers")
    }

    override fun hideAllLayers(featureLayers: List<FeatureLayer>) {
        // Hide all geodatabase layers
        featureLayers
            .filter { it.name?.startsWith("GDB_") == true }
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

    override suspend fun loadGeodatabaseLayers(geodatabase: Geodatabase) {
        try {
            Logger.d(TAG, "loadGeodatabaseLayers called with geodatabase: ${geodatabase.path}")

            // Check if geodatabase is already loaded
            if (geodatabase.loadStatus.value != com.arcgismaps.LoadStatus.Loaded) {
                Logger.d(TAG, "Geodatabase not loaded, loading now...")
                geodatabase.load().onFailure { error ->
                    Logger.e(TAG, "Failed to load geodatabase", error)
                    return
                }
            } else {
                Logger.d(TAG, "Geodatabase already loaded")
            }

            Logger.d(TAG, "Geodatabase has ${geodatabase.featureTables.size} feature tables")

            // Check if there are any feature tables
            if (geodatabase.featureTables.isEmpty()) {
                Logger.w(TAG, "Geodatabase has no feature tables!")
                return
            }

            // Collect layer metadata
            val layerUiStates = mutableListOf<LayerUiState>()

            geodatabase.featureTables.forEachIndexed { index, featureTable ->
                Logger.d(TAG, "Processing feature table $index: ${featureTable.tableName}")

                // Load the feature table
                featureTable.load().onSuccess {
                    Logger.d(TAG, "Feature table ${featureTable.tableName} loaded successfully")

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

                    // Create layer UI state
                    layerUiStates.add(
                        LayerUiState(
                            id = "GDB_${featureTable.tableName}",
                            name = featureTable.tableName,
                            isVisible = true,
                            geometryType = geometryType,
                            legendItems = emptyList(), // Legend extraction can be added later
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
                            "Layer info: ${layerUiStates.map { it.name }}"
                        )
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

    override fun clearLayers() {
        _layerInfoList.value = emptyList()
        Logger.d(TAG, "Cleared all layers")
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
