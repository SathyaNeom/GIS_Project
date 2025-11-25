package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import kotlinx.coroutines.flow.StateFlow

/**
 * Delegate interface for managing basemap operations.
 *
 * Responsibilities:
 * - Basemap style management
 * - OSM basemap visibility toggling
 * - Map recreation with/without basemap
 * - Layer recreation progress tracking
 *
 * @author Sathya Narayanan
 */
interface BasemapManagerDelegate {

    /**
     * StateFlow indicating whether the OSM basemap is visible
     */
    val osmVisible: StateFlow<Boolean>

    /**
     * StateFlow indicating whether layer recreation is in progress.
     * Used to show progress indicator for operations taking >500ms.
     */
    val isRecreating: StateFlow<Boolean>

    /**
     * Current basemap style being used
     */
    val currentBasemapStyle: BasemapStyle

    /**
     * Update the map's basemap style.
     *
     * Creates a new map with the selected basemap while preserving the current
     * viewpoint, operational layers, and extent restrictions.
     *
     * @param style The new basemap style to apply
     * @param currentMap The current map instance
     * @return New map with updated basemap style
     */
    suspend fun updateBasemapStyle(style: BasemapStyle, currentMap: ArcGISMap): ArcGISMap

    /**
     * Toggle OpenStreetMap basemap visibility.
     *
     * When enabled, shows the basemap. When disabled, hides the basemap
     * by recreating the map without basemap.
     *
     * @param visible Desired visibility state
     * @param currentMap The current map instance
     * @return New map with updated basemap visibility
     */
    suspend fun toggleOsmVisibility(visible: Boolean, currentMap: ArcGISMap): ArcGISMap

    /**
     * Create a new map with or without basemap (NO operational layers).
     * The caller is responsible for adding layers to the returned map.
     *
     * @param visible Whether to include the basemap
     * @param currentMap The current map to copy settings from
     * @return New map without operational layers
     */
    suspend fun createMapWithBasemapVisibility(visible: Boolean, currentMap: ArcGISMap): ArcGISMap

    /**
     * Clears the layer cache.
     * Should be called when geodatabase is deleted or reloaded.
     */
    fun clearLayerCache()

    /**
     * Initializes OSM visibility from persisted preference.
     * Should be called during app initialization.
     */
    fun initializeOsmVisibility()

    /**
     * Persists the current OSM visibility state to preferences.
     *
     * @param visible The visibility state to save
     */
    fun saveOsmVisibility(visible: Boolean)
}
