package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.Viewpoint
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance

/**
 * Delegate for managing map extent operations.
 *
 * Responsibilities:
 * - Calculate appropriate viewpoint to display the distance area
 * - Support dynamic center points for user-centric extent calculations
 * - Provide visual feedback for download extent without restricting map panning
 *
 * Note: As of the latest update, this delegate NO LONGER restricts map panning
 * via maxExtent. Users can freely pan/zoom across the entire map. The distance
 * selection only affects:
 * 1. What data is downloaded from the server (extent parameter)
 * 2. What features are displayed (spatial filtering in presentation layer)
 * 3. Visual boundary indicator showing the active filter area
 *
 * @author Sathya Narayanan
 */
interface ExtentManagerDelegate {

    /**
     * Calculates the appropriate viewpoint to display the distance buffer area
     * without restricting map panning.
     *
     * This method performs the following operations:
     * 1. Creates a geodetic buffer around the center point based on the distance
     * 2. Calculates a viewpoint that encompasses the buffer area with padding
     * 3. Returns the viewpoint for the UI layer to animate to
     *
     * IMPORTANT: Unlike the previous implementation, this method does NOT set
     * map.maxExtent. Users can freely pan outside the calculated extent. The
     * extent is used only for:
     * - Geodatabase download scope (what data to fetch from server)
     * - Layer display filtering (what features to show)
     * - Visual boundary indicator (circle overlay on map)
     *
     * The returned viewpoint should be applied to the MapView by the UI layer to
     * ensure the map zooms to display the selected distance area, providing
     * visual context for the download/filter operation.
     *
     * Center Point Priority:
     * 1. User's current location (if provided and valid)
     * 2. Map's current viewport center
     * 3. Default location (San Francisco) as fail-safe
     *
     * @param distance The distance radius for the extent buffer
     * @param map The ArcGIS map instance (used for center point fallback only)
     * @param centerPoint Optional center point for the extent. If null, uses map's current center or default location.
     * @return A Viewpoint to display the extent area, or null if calculation fails
     */
    suspend fun calculateViewpointForDistance(
        distance: ESDataDistance,
        map: ArcGISMap,
        centerPoint: Point? = null
    ): Viewpoint?

    /**
     * Calculates the extent (Envelope) for the given distance and center point.
     * This extent is used for geodatabase download parameters.
     *
     * @param distance The distance radius for the extent buffer
     * @param centerPoint Center point for the buffer
     * @return Envelope representing the buffered area, or null if calculation fails
     */
    suspend fun calculateExtentForDistance(
        distance: ESDataDistance,
        centerPoint: Point
    ): com.arcgismaps.geometry.Envelope?
}
