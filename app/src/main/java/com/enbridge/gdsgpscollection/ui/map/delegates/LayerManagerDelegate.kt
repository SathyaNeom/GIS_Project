package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.data.Geodatabase
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.ui.map.models.LayerUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Delegate interface for managing map layers.
 *
 * Responsibilities:
 * - Layer visibility management (show/hide/toggle all)
 * - Layer expansion state for legends
 * - Loading geodatabase layers onto the map
 * - Extracting legend items from renderers
 * - Tracking layer metadata for service-aware operations
 *
 * @author Sathya Narayanan
 */
interface LayerManagerDelegate {

    /**
     * StateFlow containing the list of all layers with their UI states
     */
    val layerInfoList: StateFlow<List<LayerUiState>>

    /**
     * StateFlow indicating whether layers are currently being loaded from geodatabase.
     * Used to prevent operations (like OSM toggle) while loading is in progress.
     */
    val isLoadingLayers: StateFlow<Boolean>

    /**
     * Toggle visibility of a specific layer on the map.
     *
     * @param layerId The unique identifier of the layer to toggle
     * @param visible The desired visibility state
     * @param featureLayers List of feature layers from the map to update
     */
    fun toggleVisibility(layerId: String, visible: Boolean, featureLayers: List<FeatureLayer>)

    /**
     * Show all geodatabase layers on the map.
     *
     * @param featureLayers List of feature layers from the map to update
     */
    fun showAllLayers(featureLayers: List<FeatureLayer>)

    /**
     * Hide all geodatabase layers on the map.
     *
     * @param featureLayers List of feature layers from the map to update
     */
    fun hideAllLayers(featureLayers: List<FeatureLayer>)

    /**
     * Toggle the expansion state of a layer's legend section.
     *
     * @param layerId The unique identifier of the layer to toggle
     */
    fun toggleLayerExpanded(layerId: String)

    /**
     * Toggle visibility of all layers based on current master checkbox state.
     * If all layers are visible, hides all. Otherwise, shows all.
     *
     * @param featureLayers List of feature layers from the map to update
     */
    fun toggleSelectAll(featureLayers: List<FeatureLayer>)

    /**
     * Load geodatabase layers and add them to the layer info list.
     * This extracts layer metadata including name, feature count, geometry type, and legend items.
     *
     * @param geodatabase The geodatabase containing feature tables to load
     */
    suspend fun loadGeodatabaseLayers(geodatabase: Geodatabase)

    /**
     * Clear all layers from the layer info list.
     * Called when geodatabase is deleted or cleared.
     */
    fun clearLayers()

    /**
     * Returns metadata for all tracked layers.
     * Used during map recreation to filter and recreate layers appropriately.
     *
     * @return List of LayerMetadata containing service and display configuration
     */
    fun getLayerMetadata(): List<com.enbridge.gdsgpscollection.domain.entity.LayerMetadata>

    /**
     * Checks if a layer is currently visible.
     *
     * @param layerId Unique layer identifier
     * @return true if layer is visible, false otherwise
     */
    fun isLayerVisible(layerId: String): Boolean

    /**
     * Stores metadata for a layer during geodatabase loading.
     * Associates layer with its service configuration for future operations.
     *
     * @param metadata Layer metadata containing service and display configuration
     */
    fun storeLayerMetadata(metadata: com.enbridge.gdsgpscollection.domain.entity.LayerMetadata)

    /**
     * Checks if any layer metadata is currently stored.
     * Used to determine if layers are ready for operations like recreation.
     *
     * @return true if metadata exists, false otherwise
     */
    fun hasMetadata(): Boolean

    /**
     * Applies service prefix-based names to FeatureLayers based on stored metadata.
     * This ensures layer names follow the pattern: {servicePrefix}{tableName}
     * Examples: "GDB_Points_Sync", "OP_Lines_Sync", "BM_Polygons_Sync"
     *
     * @param featureLayers List of FeatureLayers to name
     */
    fun applyLayerNames(featureLayers: List<FeatureLayer>)
}
