package com.enbridge.gdsgpscollection.util.extensions

import com.arcgismaps.geometry.AngularUnit
import com.arcgismaps.geometry.AngularUnitId
import com.arcgismaps.geometry.GeodeticCurveType
import com.arcgismaps.geometry.GeodeticDistanceResult
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.LinearUnit
import com.arcgismaps.geometry.LinearUnitId
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference

/**
 * Extension functions for ArcGIS Point operations.
 *
 * Provides convenient utility methods for point-specific operations such as
 * distance calculations, coordinate conversions, and spatial relationships.
 *
 * @author Sathya Narayanan
 */

/**
 * Calculates the geodetic distance between this point and another point.
 *
 * Uses geodesic calculation which accounts for the Earth's curvature,
 * providing accurate distance for geographic coordinates.
 *
 * @param other The target point
 * @param distanceUnit Unit for the distance result (default: Meters)
 * @return Distance in specified units, or null if calculation fails
 */
fun Point.distanceTo(
    other: Point,
    distanceUnit: LinearUnit = LinearUnit(LinearUnitId.Meters)
): Double? {
    return GeometryEngine.distanceGeodeticOrNull(
        point1 = this,
        point2 = other,
        distanceUnit = distanceUnit,
        azimuthUnit = null,
        curveType = GeodeticCurveType.Geodesic
    )?.distance
}

/**
 * Calculates the geodetic distance and azimuth between this point and another.
 *
 * @param other The target point
 * @param distanceUnit Unit for distance (default: Meters)
 * @param azimuthUnit Unit for azimuth (default: Degrees)
 * @return GeodeticDistanceResult containing distance and azimuth, or null if calculation fails
 */
fun Point.distanceAndAzimuthTo(
    other: Point,
    distanceUnit: LinearUnit = LinearUnit(LinearUnitId.Meters),
    azimuthUnit: AngularUnit = AngularUnit(AngularUnitId.Degrees)
): GeodeticDistanceResult? {
    return GeometryEngine.distanceGeodeticOrNull(
        point1 = this,
        point2 = other,
        distanceUnit = distanceUnit,
        azimuthUnit = azimuthUnit,
        curveType = GeodeticCurveType.Geodesic
    )
}

/**
 * Checks if this point is within the specified distance of another point.
 *
 * @param other The point to check against
 * @param distance The threshold distance
 * @param distanceUnit Unit for distance (default: Meters)
 * @return true if points are within specified distance
 */
fun Point.isNear(
    other: Point,
    distance: Double,
    distanceUnit: LinearUnit = LinearUnit(LinearUnitId.Meters)
): Boolean {
    val actualDistance = distanceTo(other, distanceUnit) ?: return false
    return actualDistance <= distance
}

/**
 * Returns a copy of this point with the specified offset.
 *
 * @param xOffset Offset in X direction
 * @param yOffset Offset in Y direction
 * @return New point with offset applied
 */
fun Point.offset(xOffset: Double, yOffset: Double): Point {
    return Point(
        x = this.x + xOffset,
        y = this.y + yOffset,
        z = this.z,
        spatialReference = this.spatialReference
    )
}

/**
 * Returns a copy of this point with the specified Z value.
 *
 * @param z New Z coordinate value
 * @return New point with Z value
 */
fun Point.withZ(z: Double): Point {
    return Point(
        x = this.x,
        y = this.y,
        z = z,
        spatialReference = this.spatialReference
    )
}

/**
 * Converts this point to Web Mercator spatial reference (WKID 3857).
 *
 * @return Projected point or null if projection fails
 */
fun Point.toWebMercator(): Point? {
    return projectTo(SpatialReference.webMercator()) as? Point
}

/**
 * Converts this point to WGS84 spatial reference (WKID 4326).
 *
 * @return Projected point or null if projection fails
 */
fun Point.toWgs84(): Point? {
    return projectTo(SpatialReference.wgs84()) as? Point
}

/**
 * Checks if this point has a Z (elevation) value.
 *
 * @return true if point has Z coordinate
 */
fun Point.hasZ(): Boolean {
    return this.z?.isNaN() == false
}

/**
 * Returns a formatted string representation of this point.
 *
 * @param decimalPlaces Number of decimal places (default: 4)
 * @return Formatted string "X: [x], Y: [y]"
 */
fun Point.toFormattedString(decimalPlaces: Int = 4): String {
    val format = "%.${decimalPlaces}f"
    return "X: ${String.format(format, x)}, Y: ${String.format(format, y)}"
}

/**
 * Returns a compact formatted string of coordinates.
 *
 * @param decimalPlaces Number of decimal places (default: 4)
 * @return Formatted string "[x], [y]"
 */
fun Point.toCompactString(decimalPlaces: Int = 4): String {
    val format = "%.${decimalPlaces}f"
    return "${String.format(format, x)}, ${String.format(format, y)}"
}
