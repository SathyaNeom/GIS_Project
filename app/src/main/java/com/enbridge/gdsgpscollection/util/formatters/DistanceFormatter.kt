package com.enbridge.gdsgpscollection.util.formatters

/**
 * Utility object for formatting distance values consistently across the application.
 *
 * Provides standardized formatting methods for distance measurements with
 * appropriate unit conversions and display formats.
 *
 * @author Sathya Narayanan
 */
object DistanceFormatter {

    /** Conversion factor: meters to kilometers */
    private const val METERS_TO_KM = 1000.0

    /** Conversion factor: meters to miles */
    private const val METERS_TO_MILES = 1609.34

    /** Conversion factor: meters to feet */
    private const val METERS_TO_FEET = 0.3048

    /**
     * Formats distance in meters with automatic unit selection.
     *
     * Automatically chooses between meters and kilometers based on distance.
     * - < 1000m: displays in meters
     * - >= 1000m: displays in kilometers
     *
     * @param meters Distance in meters
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted distance string (e.g., "150.0 m" or "5.2 km")
     */
    fun formatMeters(meters: Double, decimalPlaces: Int = 1): String {
        return if (meters < METERS_TO_KM) {
            "${String.format("%.${decimalPlaces}f", meters)} m"
        } else {
            val km = meters / METERS_TO_KM
            "${String.format("%.${decimalPlaces}f", km)} km"
        }
    }

    /**
     * Formats distance in meters to kilometers.
     *
     * @param meters Distance in meters
     * @param decimalPlaces Number of decimal places (default: 2)
     * @return Formatted distance string (e.g., "5.25 km")
     */
    fun formatToKilometers(meters: Double, decimalPlaces: Int = 2): String {
        val km = meters / METERS_TO_KM
        return "${String.format("%.${decimalPlaces}f", km)} km"
    }

    /**
     * Formats distance in meters to miles.
     *
     * @param meters Distance in meters
     * @param decimalPlaces Number of decimal places (default: 2)
     * @return Formatted distance string (e.g., "3.28 mi")
     */
    fun formatToMiles(meters: Double, decimalPlaces: Int = 2): String {
        val miles = meters / METERS_TO_MILES
        return "${String.format("%.${decimalPlaces}f", miles)} mi"
    }

    /**
     * Formats distance in meters to feet.
     *
     * @param meters Distance in meters
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted distance string (e.g., "164.0 ft")
     */
    fun formatToFeet(meters: Double, decimalPlaces: Int = 1): String {
        val feet = meters / METERS_TO_FEET
        return "${String.format("%.${decimalPlaces}f", feet)} ft"
    }

    /**
     * Formats distance with automatic imperial unit selection.
     *
     * Automatically chooses between feet and miles based on distance.
     * - < 1 mile: displays in feet
     * - >= 1 mile: displays in miles
     *
     * @param meters Distance in meters
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted distance string (e.g., "500.0 ft" or "3.2 mi")
     */
    fun formatImperial(meters: Double, decimalPlaces: Int = 1): String {
        val miles = meters / METERS_TO_MILES
        return if (miles < 1.0) {
            formatToFeet(meters, decimalPlaces)
        } else {
            formatToMiles(meters, decimalPlaces)
        }
    }

    /**
     * Formats distance or returns a placeholder if null/invalid.
     *
     * @param meters Distance in meters (nullable)
     * @param placeholder Placeholder text for invalid values (default: "--")
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted distance or placeholder
     */
    fun formatOrPlaceholder(
        meters: Double?,
        placeholder: String = "--",
        decimalPlaces: Int = 1
    ): String {
        return if (meters != null && meters.isFinite() && meters >= 0) {
            formatMeters(meters, decimalPlaces)
        } else {
            placeholder
        }
    }

    /**
     * Formats area in square meters with automatic unit selection.
     *
     * Automatically chooses between square meters and square kilometers.
     *
     * @param squareMeters Area in square meters
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted area string (e.g., "500.0 m²" or "5.2 km²")
     */
    fun formatArea(squareMeters: Double, decimalPlaces: Int = 1): String {
        return if (squareMeters < 1_000_000) {
            "${String.format("%.${decimalPlaces}f", squareMeters)} m²"
        } else {
            val sqKm = squareMeters / 1_000_000
            "${String.format("%.${decimalPlaces}f", sqKm)} km²"
        }
    }

    /**
     * Formats area in square meters to acres.
     *
     * @param squareMeters Area in square meters
     * @param decimalPlaces Number of decimal places (default: 2)
     * @return Formatted area string (e.g., "2.47 acres")
     */
    fun formatToAcres(squareMeters: Double, decimalPlaces: Int = 2): String {
        val acres = squareMeters / 4046.86
        return "${String.format("%.${decimalPlaces}f", acres)} acres"
    }

    /**
     * Converts kilometers to meters.
     *
     * @param kilometers Distance in kilometers
     * @return Distance in meters
     */
    fun kilometersToMeters(kilometers: Double): Double = kilometers * METERS_TO_KM

    /**
     * Converts miles to meters.
     *
     * @param miles Distance in miles
     * @return Distance in meters
     */
    fun milesToMeters(miles: Double): Double = miles * METERS_TO_MILES

    /**
     * Converts feet to meters.
     *
     * @param feet Distance in feet
     * @return Distance in meters
     */
    fun feetToMeters(feet: Double): Double = feet * METERS_TO_FEET
}
