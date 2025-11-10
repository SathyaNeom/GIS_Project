package com.enbridge.gdsgpscollection.util.extensions

import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.Point
import com.arcgismaps.mapping.Viewpoint

/**
 * Extension functions for ArcGIS Viewpoint operations.
 *
 * Provides utility methods for converting viewpoints to envelopes and
 * calculating map extents based on scale and screen dimensions.
 */

/**
 * Converts a Viewpoint to an Envelope based on the current scale.
 *
 * This calculates the visible map extent by using the center point and scale,
 * approximating the screen dimensions in map units.
 *
 * @param screenWidthRatio Ratio of scale to map width (default 0.3)
 * @param screenHeightRatio Ratio of scale to map height (default 0.2)
 * @return Envelope representing the visible extent, or null if conversion fails
 */
fun Viewpoint.toEnvelope(
    screenWidthRatio: Double = 0.3,
    screenHeightRatio: Double = 0.2
): Envelope? {
    val centerPoint = targetGeometry as? Point ?: return null
    val scale = targetScale ?: return null

    if (scale <= 0) return null

    // Calculate extent from center point and scale
    // Screen dimensions in map units (approximation)
    val mapWidthInMeters = scale * screenWidthRatio
    val mapHeightInMeters = scale * screenHeightRatio

    return Envelope(
        xMin = centerPoint.x - mapWidthInMeters / 2,
        yMin = centerPoint.y - mapHeightInMeters / 2,
        xMax = centerPoint.x + mapWidthInMeters / 2,
        yMax = centerPoint.y + mapHeightInMeters / 2,
        spatialReference = centerPoint.spatialReference
    )
}
