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
 * Manages map extent calculations and viewpoint generation based on distance buffers.
 * This implementation ensures consistent zoom behavior when users change the distance
 * selection, whether zooming in to a smaller area or zooming out to a larger area.
 *
 * IMPORTANT: This implementation does NOT restrict map panning. Users can freely
 * navigate the entire map. The distance-based extent is used only for:
 * - Calculating appropriate zoom viewpoints
 * - Determining geodatabase download scope
 * - Defining spatial filter boundaries for layer display
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
     * Calculates the appropriate viewpoint to display the distance buffer area
     * without restricting map panning.
     *
     * This method addresses the requirement for free map navigation while still
     * providing visual context for the download/filter extent. By returning a
     * viewpoint without setting maxExtent, the UI layer can animate to show the
     * relevant area without preventing users from panning elsewhere.
     *
     * Implementation Details:
     * 1. Determines center point using fallback chain: provided → map center → San Francisco
     * 2. Creates a geodetic buffer around the center point based on the distance
     * 3. Calculates a viewpoint with padding to provide visual context
     * 4. Returns the viewpoint for the UI layer to apply with animation
     *
     * NOTE: Unlike the previous implementation, this method does NOT call
     * map.maxExtent = extent. Map panning is completely unrestricted.
     *
     * Center Point Fallback Chain:
     * - Primary: User's current location (if provided via centerPoint parameter)
     * - Secondary: Map's current viewport center (if user has panned)
     * - Tertiary: San Francisco default (fail-safe for edge cases)
     *
     * @param distance The distance radius for the extent buffer
     * @param map The ArcGIS map instance (used only for center point fallback)
     * @param centerPoint Optional center point (typically user's location) for centering the extent
     * @return A Viewpoint encompassing the extent with padding, or null if calculation fails
     */
    override suspend fun calculateViewpointForDistance(
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
                // CRITICAL CHANGE: Do NOT set map.maxExtent
                // Users can now freely pan outside the calculated extent
                // The extent is used only for:
                // 1. Viewpoint calculation (visual feedback)
                // 2. Geodatabase download scope (data fetching)
                // 3. Layer display filtering (feature visibility)

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
                    "Calculated viewpoint for ${distance.displayText} with ${EXTENT_PADDING_FACTOR}x padding " +
                            "(map panning unrestricted)"
                )

                return viewpoint
            } else {
                Logger.w(TAG, "Failed to create extent for distance: ${distance.displayText}")
                return null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error calculating viewpoint for distance", e)
            return null
        }
    }

    /**
     * Calculates the extent (Envelope) for the given distance and center point.
     * This extent is used for geodatabase download parameters.
     *
     * Unlike calculateViewpointForDistance, this method returns the raw extent
     * without padding, suitable for use in GeodatabaseSyncTask parameters.
     *
     * @param distance The distance radius for the extent buffer
     * @param centerPoint Center point for the buffer
     * @return Envelope representing the buffered area, or null if calculation fails
     */
    override suspend fun calculateExtentForDistance(
        distance: ESDataDistance,
        centerPoint: Point
    ): com.arcgismaps.geometry.Envelope? {
        return try {
            Logger.d(TAG, "Calculating extent for ${distance.displayText}")

            // Create geodetic buffer around the center point
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
                Logger.d(
                    TAG,
                    "Successfully calculated extent for ${distance.displayText}: " +
                            "width=${extent.width.toInt()}m, height=${extent.height.toInt()}m"
                )
            } else {
                Logger.w(TAG, "Failed to create extent for distance: ${distance.displayText}")
            }

            extent
        } catch (e: Exception) {
            Logger.e(TAG, "Error calculating extent for distance", e)
            null
        }
    }
}
