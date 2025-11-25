package com.enbridge.gdsgpscollection.util.extensions

import com.arcgismaps.geometry.Geometry
import com.arcgismaps.geometry.GeodeticCurveType
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.LinearUnit
import com.arcgismaps.geometry.LinearUnitId
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.Polygon
import com.arcgismaps.geometry.Polyline
import com.arcgismaps.geometry.SpatialReference

/**
 * Extension functions for ArcGIS Geometry operations.
 *
 * Provides convenient utility methods for common geometry operations such as
 * buffering, distance calculations, spatial relationships, and transformations.
 *
 * @author Sathya Narayanan
 */

/**
 * Creates a geodetic buffer around this geometry.
 *
 * Uses geodesic buffering which accounts for the curvature of the Earth,
 * providing accurate buffers for geographic coordinate systems.
 *
 * @param distance Buffer distance
 * @param distanceUnit Unit of measurement for distance (default: Meters)
 * @param maxDeviation Maximum deviation for curve approximation (default: NaN = automatic)
 * @param curveType Type of curve to use (default: Geodesic)
 * @return Buffered geometry or null if operation fails
 */
fun Geometry.bufferGeodetic(
    distance: Double,
    distanceUnit: LinearUnit = LinearUnit(LinearUnitId.Meters),
    maxDeviation: Double = Double.NaN,
    curveType: GeodeticCurveType = GeodeticCurveType.Geodesic
): Geometry? {
    return GeometryEngine.bufferGeodeticOrNull(
        geometry = this,
        distance = distance,
        distanceUnit = distanceUnit,
        maxDeviation = maxDeviation,
        curveType = curveType
    )
}

/**
 * Checks if this geometry contains another geometry.
 *
 * @param other The geometry to test
 * @return true if this geometry contains the other geometry
 */
fun Geometry.contains(other: Geometry): Boolean {
    return GeometryEngine.contains(this, other)
}

/**
 * Checks if this geometry intersects another geometry.
 *
 * @param other The geometry to test
 * @return true if geometries intersect
 */
fun Geometry.intersects(other: Geometry): Boolean {
    return GeometryEngine.intersects(this, other)
}

/**
 * Checks if this geometry is within another geometry.
 *
 * @param other The geometry to test against
 * @return true if this geometry is within the other geometry
 */
fun Geometry.within(other: Geometry): Boolean {
    return GeometryEngine.within(this, other)
}

/**
 * Projects this geometry to a different spatial reference.
 *
 * @param targetSpatialReference The spatial reference to project to
 * @return Projected geometry or null if projection fails
 */
fun Geometry.projectTo(targetSpatialReference: SpatialReference): Geometry? {
    return GeometryEngine.projectOrNull(this, targetSpatialReference)
}

/**
 * Simplifies this geometry to reduce complexity while maintaining shape.
 *
 * @return Simplified geometry or the original if simplification fails
 */
fun Geometry.simplify(): Geometry {
    return GeometryEngine.simplifyOrNull(this) ?: this
}

/**
 * Returns the centroid (center point) of this geometry.
 *
 * @return Center point or null if calculation fails
 */
fun Geometry.centroid(): Point? {
    return when (this) {
        is Point -> this
        is Polygon -> GeometryEngine.labelPointOrNull(this)
        else -> null
    }
}

/**
 * Densifies the geometry by adding vertices along the geometry's segments.
 *
 * @param maxSegmentLength Maximum length between vertices
 * @return Densified geometry or null if operation fails
 */
fun Geometry.densify(maxSegmentLength: Double): Geometry? {
    return GeometryEngine.densifyOrNull(this, maxSegmentLength)
}

/**
 * Returns the boundary of this geometry.
 *
 * For a polygon, returns the outline as a polyline.
 * For a polyline, returns the end points.
 *
 * @return Boundary geometry or null if operation fails
 */
fun Geometry.boundary(): Geometry? {
    return GeometryEngine.boundaryOrNull(this)
}

/**
 * Clips this geometry to the specified envelope.
 *
 * Note: This method is currently unused in the application. The application downloads
 * complete feature geometries for features intersecting the download extent, rather than
 * clipping geometries. This method is retained for potential future use cases where
 * geometry clipping may be required (e.g., custom spatial analysis, reporting features).
 *
 * @param envelope The clipping envelope
 * @return Clipped geometry or null if operation fails
 */
fun Geometry.clip(envelope: com.arcgismaps.geometry.Envelope): Geometry? {
    return GeometryEngine.clipOrNull(this, envelope)
}

/**
 * Returns the convex hull of this geometry.
 *
 * The convex hull is the smallest convex polygon that contains all points.
 *
 * @return Convex hull geometry or null if operation fails
 */
fun Geometry.convexHull(): Geometry? {
    return GeometryEngine.convexHullOrNull(this)
}

/**
 * Checks if this geometry is empty (has no coordinates).
 *
 * @return true if geometry is empty
 */
fun Geometry.isEmptyGeometry(): Boolean {
    return this.isEmpty
}

/**
 * Checks if this geometry is valid according to OGC specifications.
 *
 * @return true if geometry is valid
 */
fun Geometry.isValidGeometry(): Boolean {
    return !this.isEmpty && this.extent != null
}
