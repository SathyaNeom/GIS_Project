package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.data.Geodatabase
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.ui.map.GeodatabaseMetadata
import kotlinx.coroutines.flow.StateFlow

/**
 * Delegate interface for managing geodatabase lifecycle.
 *
 * Responsibilities:
 * - Check and load existing geodatabase on startup
 * - Delete geodatabase and clean up resources
 * - Track geodatabase metadata (timestamp, size, layer count)
 * - Health check validation
 * - First-time user guidance
 *
 * @author Sathya Narayanan
 */
interface GeodatabaseManagerDelegate {

    /**
     * StateFlow containing metadata about the currently loaded geodatabase
     */
    val geodatabaseMetadata: StateFlow<GeodatabaseMetadata?>

    /**
     * StateFlow indicating whether to show first-time guidance dialog
     */
    val showFirstTimeGuidance: StateFlow<Boolean>

    /**
     * StateFlow containing any geodatabase load errors
     */
    val geodatabaseLoadError: StateFlow<String?>

    /**
     * Current geodatabase instance (if loaded)
     */
    val currentGeodatabase: Geodatabase?

    /**
     * Checks for and loads existing geodatabase on app startup.
     *
     * Implements:
     * 1. Health Check: Validates geodatabase integrity before loading
     * 2. First-Time Guidance: Shows welcome dialog if no geodatabase exists
     * 3. Metadata Tracking: Stores and displays geodatabase download timestamp
     *
     * @return Geodatabase if successfully loaded, null otherwise
     */
    suspend fun checkAndLoadExisting(): Result<Geodatabase?>

    /**
     * Delete geodatabase file and close connection.
     *
     * @param featureLayers List of feature layers from the map to remove
     */
    suspend fun deleteGeodatabase(featureLayers: List<FeatureLayer>)

    /**
     * Check if geodatabase file exists
     *
     * @return true if geodatabase file exists, false otherwise
     */
    fun geodatabaseExists(): Boolean

    /**
     * Saves the geodatabase download timestamp.
     * Called when a new geodatabase is successfully downloaded.
     */
    fun saveTimestamp()

    /**
     * Dismisses the first-time guidance dialog
     */
    fun dismissFirstTimeGuidance()

    /**
     * Dismisses the geodatabase load error
     */
    fun dismissGeodatabaseLoadError()

    /**
     * Store reference to current geodatabase
     *
     * @param geodatabase The geodatabase to store
     */
    fun setCurrentGeodatabase(geodatabase: Geodatabase?)

    /**
     * Close the current geodatabase connection
     */
    fun closeGeodatabase()
}
