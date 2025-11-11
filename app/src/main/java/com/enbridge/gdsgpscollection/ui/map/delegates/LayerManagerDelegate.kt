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
 *
 * @author Sathya Narayanan
 */
interface LayerManagerDelegate {

    /**
     * StateFlow containing the list of all layers with their UI states
     */
    val layerInfoList: StateFlow<List<LayerUiState>>

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
}
