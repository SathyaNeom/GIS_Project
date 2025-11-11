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
 *
 * @author Sathya Narayanan
 */
interface BasemapManagerDelegate {

    /**
     * StateFlow indicating whether the OSM basemap is visible
     */
    val osmVisible: StateFlow<Boolean>

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
}
