package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExtentManagerDelegate.
 *
 * Manages map extent restrictions based on distance buffers.
 *
 * @author Sathya Narayanan
 */
@Singleton
class ExtentManagerDelegateImpl @Inject constructor() : ExtentManagerDelegate {

    companion object {
        private const val TAG = "ExtentManagerDelegate"
    }

    override suspend fun updateMaxExtent(distance: ESDataDistance, map: ArcGISMap) {
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
                map.maxExtent = extent
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
