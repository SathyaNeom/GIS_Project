package com.enbridge.gdsgpscollection.util.formatters

import com.arcgismaps.geometry.Point

/**
 * Utility object for formatting coordinates consistently across the application.
 *
 * Provides standardized formatting methods for coordinate values, points,
 * and other geographic data to ensure consistency in UI display.
 *
 * @author Sathya Narayanan
 */
object CoordinateFormatter {

    /** Default number of decimal places for coordinate display */
    private const val DEFAULT_DECIMAL_PLACES = 4

    /**
     * Formats a single coordinate value to the specified number of decimal places.
     *
     * @param value The coordinate value to format
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted coordinate string
     */
    fun formatCoordinate(value: Double, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): String {
        return String.format("%.${decimalPlaces}f", value)
    }

    /**
     * Formats a Point to "X: [x], Y: [y]" format.
     *
     * @param point The point to format
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted point string
     */
    fun formatPoint(point: Point, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): String {
        return "X: ${formatCoordinate(point.x, decimalPlaces)}, Y: ${
            formatCoordinate(
                point.y,
                decimalPlaces
            )
        }"
    }

    /**
     * Formats a Point to compact format "[x], [y]".
     *
     * @param point The point to format
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Compact formatted point string
     */
    fun formatPointCompact(point: Point, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): String {
        return "${formatCoordinate(point.x, decimalPlaces)}, ${
            formatCoordinate(
                point.y,
                decimalPlaces
            )
        }"
    }

    /**
     * Formats a Point with Z (elevation) to "X: [x], Y: [y], Z: [z]" format.
     *
     * @param point The point to format
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted point string with elevation
     */
    fun formatPointWithZ(point: Point, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): String {
        val z = point.z
        return if (z != null && !z.isNaN()) {
            "${formatPoint(point, decimalPlaces)}, Z: ${formatCoordinate(z, decimalPlaces)}"
        } else {
            formatPoint(point, decimalPlaces)
        }
    }

    /**
     * Formats latitude value with N/S suffix.
     *
     * @param latitude Latitude value in degrees
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted latitude string (e.g., "37.7749°N")
     */
    fun formatLatitude(latitude: Double, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): String {
        val absLat = kotlin.math.abs(latitude)
        val suffix = if (latitude >= 0) "N" else "S"
        return "${formatCoordinate(absLat, decimalPlaces)}°$suffix"
    }

    /**
     * Formats longitude value with E/W suffix.
     *
     * @param longitude Longitude value in degrees
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted longitude string (e.g., "122.4194°W")
     */
    fun formatLongitude(longitude: Double, decimalPlaces: Int = DEFAULT_DECIMAL_PLACES): String {
        val absLon = kotlin.math.abs(longitude)
        val suffix = if (longitude >= 0) "E" else "W"
        return "${formatCoordinate(absLon, decimalPlaces)}°$suffix"
    }

    /**
     * Formats a geographic coordinate (lat/lon) pair.
     *
     * @param latitude Latitude value
     * @param longitude Longitude value
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted coordinate pair (e.g., "37.7749°N, 122.4194°W")
     */
    fun formatLatLon(
        latitude: Double,
        longitude: Double,
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): String {
        return "${formatLatitude(latitude, decimalPlaces)}, ${
            formatLongitude(
                longitude,
                decimalPlaces
            )
        }"
    }

    /**
     * Formats a coordinate value or returns a placeholder if null/invalid.
     *
     * @param value The coordinate value to format
     * @param placeholder Placeholder text for invalid values (default: "--")
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted value or placeholder
     */
    fun formatOrPlaceholder(
        value: Double?,
        placeholder: String = "--",
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): String {
        return if (value != null && value.isFinite()) {
            formatCoordinate(value, decimalPlaces)
        } else {
            placeholder
        }
    }

    /**
     * Formats a Point or returns placeholders if null.
     *
     * @param point The point to format (nullable)
     * @param placeholder Placeholder text (default: "--")
     * @param decimalPlaces Number of decimal places (default: 4)
     * @return Formatted point string or placeholders
     */
    fun formatPointOrPlaceholder(
        point: Point?,
        placeholder: String = "--",
        decimalPlaces: Int = DEFAULT_DECIMAL_PLACES
    ): Pair<String, String> {
        return if (point != null) {
            formatCoordinate(point.x, decimalPlaces) to formatCoordinate(point.y, decimalPlaces)
        } else {
            placeholder to placeholder
        }
    }

    /**
     * Parses a formatted coordinate string back to Double.
     *
     * @param formatted The formatted coordinate string
     * @return Parsed double value or null if parsing fails
     */
    fun parseCoordinate(formatted: String): Double? {
        return formatted.replace(",", "").trim().toDoubleOrNull()
    }
}
