package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.Viewpoint
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ExtentManagerDelegate.
 *
 * Manages map extent restrictions and viewpoint calculations based on distance buffers.
 * This implementation ensures consistent zoom behavior when users change the distance
 * selection, whether zooming in to a smaller area or zooming out to a larger area.
 *
 * @author Sathya Narayanan
 */
@Singleton
class ExtentManagerDelegateImpl @Inject constructor() : ExtentManagerDelegate {

    companion object {
        private const val TAG = "ExtentManagerDelegate"

        /**
         * Padding factor applied to the extent for viewpoint calculation.
         * A value of 1.2 means the viewpoint will show the extent plus 20% padding
         * on all sides, providing better visual context to the user.
         */
        private const val EXTENT_PADDING_FACTOR = 1.2
    }

    /**
     * Updates the map's maximum extent and calculates the appropriate viewpoint
     * to display the distance buffer area.
     *
     * This method addresses a common issue where changing from a smaller to larger
     * distance does not zoom out the map. By returning a viewpoint, the UI layer
     * can explicitly animate the map to show the entire extent area, ensuring
     * consistent zoom behavior in both directions (zoom in and zoom out).
     *
     * Implementation Details:
     * 1. Determines center point using fallback chain: provided → map center → San Francisco
     * 2. Creates a geodetic buffer around the center point based on the distance
     * 3. Sets the map's maxExtent to restrict panning within the buffer area
     * 4. Calculates a viewpoint with padding to provide visual context
     * 5. Returns the viewpoint for the UI layer to apply with animation
     *
     * Center Point Fallback Chain:
     * - Primary: User's current location (if provided via centerPoint parameter)
     * - Secondary: Map's current viewport center (if user has panned)
     * - Tertiary: San Francisco default (fail-safe for edge cases)
     *
     * @param distance The distance radius for the extent buffer
     * @param map The ArcGIS map instance to update
     * @param centerPoint Optional center point (typically user's location) for centering the extent
     * @return A Viewpoint encompassing the extent with padding, or null if calculation fails
     */
    override suspend fun updateMaxExtent(
        distance: ESDataDistance,
        map: ArcGISMap,
        centerPoint: Point?
    ): Viewpoint? {
        try {
            // Determine center point using fallback chain for robust handling
            // Priority: User Location → Map Center → San Francisco (fail-safe)
            val effectiveCenterPoint = when {
                // 1. Use provided center point (typically user's location)
                centerPoint != null -> {
                    Logger.d(
                        TAG,
                        "Using provided center point (user location) for extent calculation"
                    )
                    centerPoint
                }

                // 2. Use map's current viewport center (where user is currently looking)
                map.initialViewpoint?.targetGeometry is Point -> {
                    Logger.d(TAG, "Using map's current viewport center for extent calculation")
                    map.initialViewpoint?.targetGeometry as Point
                }

                // 3. Fallback to San Francisco coordinates (fail-safe default)
                else -> {
                    Logger.w(
                        TAG,
                        "No center point available, falling back to San Francisco coordinates"
                    )
                    Point(
                        Constants.SanFrancisco.CENTER_X,
                        Constants.SanFrancisco.CENTER_Y,
                        SpatialReference.webMercator()
                    )
                }
            }

            // Create geodetic buffer around the determined center point
            val bufferedGeometry = GeometryEngine.bufferGeodeticOrNull(
                geometry = effectiveCenterPoint,
                distance = distance.meters.toDouble(),
                distanceUnit = com.arcgismaps.geometry.LinearUnit(
                    com.arcgismaps.geometry.LinearUnitId.Meters
                ),
                maxDeviation = Double.NaN,
                curveType = com.arcgismaps.geometry.GeodeticCurveType.Geodesic
            )

            val extent = bufferedGeometry?.extent
            if (extent != null) {
                // Update the map's maximum extent constraint
                // This restricts panning to keep the map within the buffer area
                map.maxExtent = extent

                // Calculate expanded extent with padding for better visual context
                // We expand the extent by 20% (EXTENT_PADDING_FACTOR) to provide breathing room
                val centerX = extent.center.x
                val centerY = extent.center.y
                val expandedWidth = extent.width * EXTENT_PADDING_FACTOR
                val expandedHeight = extent.height * EXTENT_PADDING_FACTOR

                val paddedExtent = com.arcgismaps.geometry.Envelope(
                    xMin = centerX - (expandedWidth / 2.0),
                    yMin = centerY - (expandedHeight / 2.0),
                    xMax = centerX + (expandedWidth / 2.0),
                    yMax = centerY + (expandedHeight / 2.0),
                    spatialReference = extent.spatialReference
                )

                val viewpoint = Viewpoint(paddedExtent)

                Logger.d(
                    TAG,
                    "Updated maxExtent and calculated viewpoint for ${distance.displayText} " +
                            "with ${EXTENT_PADDING_FACTOR}x padding"
                )

                return viewpoint
            } else {
                Logger.w(TAG, "Failed to create extent for distance: ${distance.displayText}")
                return null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error updating maxExtent and calculating viewpoint", e)
            return null
        }
    }

}
