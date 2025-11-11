package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.mapping.ArcGISMap
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance

/**
 * Delegate interface for managing map extent restrictions.
 *
 * Responsibilities:
 * - Update map's maxExtent based on selected distance
 * - Create geodetic buffers around center point
 * - Restrict panning to specific geographic areas
 *
 * @author Sathya Narayanan
 */
interface ExtentManagerDelegate {

    /**
     * Update map's maxExtent based on selected distance.
     * Restricts panning to the buffer area around San Francisco.
     *
     * @param distance The distance to use for buffer calculation
     * @param map The map to update
     */
    suspend fun updateMaxExtent(distance: ESDataDistance, map: ArcGISMap)
}
