package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.Viewpoint
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance

/**
 * Delegate for managing map extent operations.
 *
 * Responsibilities:
 * - Update map's maxExtent based on selected distance
 * - Calculate appropriate viewpoint to display the extent area
 * - Support dynamic center points for user-centric extent calculations
 *
 * @author Sathya Narayanan
 */
interface ExtentManagerDelegate {

    /**
     * Updates the map's maximum extent based on the selected distance and calculates
     * the appropriate viewpoint to display the extent area.
     *
     * This method performs two operations:
     * 1. Sets the map's maxExtent to constrain panning within the distance buffer
     * 2. Calculates a viewpoint that encompasses the entire extent with padding
     *
     * The returned viewpoint should be applied to the MapView by the UI layer to
     * ensure the map zooms to display the selected distance area, regardless of
     * whether the new extent is larger or smaller than the current view.
     *
     * Center Point Priority:
     * 1. User's current location (if provided and valid)
     * 2. Map's current viewport center
     * 3. Default location (San Francisco) as fail-safe
     *
     * @param distance The distance radius for the extent buffer
     * @param map The ArcGIS map instance to update
     * @param centerPoint Optional center point for the extent. If null, uses map's current center or default location.
     * @return A Viewpoint to display the extent area, or null if calculation fails
     */
    suspend fun updateMaxExtent(
        distance: ESDataDistance,
        map: ArcGISMap,
        centerPoint: Point? = null
    ): Viewpoint?
}
