package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BasemapManagerDelegate.
 *
 * Manages basemap style changes and OSM visibility toggling.
 *
 * @author Sathya Narayanan
 */
@Singleton
class BasemapManagerDelegateImpl @Inject constructor() : BasemapManagerDelegate {

    private val _osmVisible = MutableStateFlow(true)
    override val osmVisible: StateFlow<Boolean> = _osmVisible.asStateFlow()

    private var _currentBasemapStyle: BasemapStyle = BasemapStyle.ArcGISTopographic
    override val currentBasemapStyle: BasemapStyle
        get() = _currentBasemapStyle

    companion object {
        private const val TAG = "BasemapManagerDelegate"
    }

    override suspend fun updateBasemapStyle(
        style: BasemapStyle,
        currentMap: ArcGISMap
    ): ArcGISMap {
        return try {
            // Store the new basemap style
            _currentBasemapStyle = style

            val currentViewpoint = currentMap.initialViewpoint
            val currentLayers = currentMap.operationalLayers.toList()
            val currentMaxExtent = currentMap.maxExtent

            // Create new map with new basemap
            // Only create with basemap if OSM is visible
            val newMap = if (_osmVisible.value) {
                ArcGISMap(style).apply {
                    initialViewpoint = currentViewpoint
                    maxExtent = currentMaxExtent
                    operationalLayers.addAll(currentLayers)
                }
            } else {
                // OSM is hidden, create without basemap
                ArcGISMap().apply {
                    initialViewpoint = currentViewpoint
                    maxExtent = currentMaxExtent
                    operationalLayers.addAll(currentLayers)
                }
            }

            Logger.d(TAG, "Updated basemap style to $style")
            newMap

        } catch (e: Exception) {
            Logger.e(TAG, "Error updating basemap style", e)
            currentMap
        }
    }

    override suspend fun toggleOsmVisibility(
        visible: Boolean,
        currentMap: ArcGISMap
    ): ArcGISMap {
        return try {
            // Update state IMMEDIATELY for instant UI feedback
            _osmVisible.value = visible

            // Preserve current map state
            val currentViewpoint = currentMap.initialViewpoint
            val currentLayers = currentMap.operationalLayers.toList()
            val currentMaxExtent = currentMap.maxExtent

            val newMap = if (visible) {
                // Recreate map with basemap
                ArcGISMap(_currentBasemapStyle).apply {
                    initialViewpoint = currentViewpoint
                    maxExtent = currentMaxExtent

                    // Re-add operational layers
                    operationalLayers.addAll(currentLayers)
                }
            } else {
                // Create map without basemap (null basemap)
                ArcGISMap().apply {
                    initialViewpoint = currentViewpoint
                    maxExtent = currentMaxExtent

                    // Re-add operational layers
                    operationalLayers.addAll(currentLayers)
                }
            }

            Logger.d(TAG, "OSM basemap visibility: $visible")
            newMap

        } catch (e: Exception) {
            Logger.e(TAG, "Error toggling OSM visibility", e)
            // Revert state on error
            _osmVisible.value = !visible
            currentMap
        }
    }
}
