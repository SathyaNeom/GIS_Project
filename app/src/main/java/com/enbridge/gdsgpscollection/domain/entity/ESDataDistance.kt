package com.enbridge.gdsgpscollection.domain.entity

/**
 * Represents the distance radius options for geodatabase download extent calculation.
 *
 * The selected distance defines the geographic area around a center point (typically user location
 * or map viewport center) from which data will be downloaded into the local geodatabase.
 * Features outside this extent will not be included in the offline geodatabase.
 *
 * Available Options:
 * - 50 Meters: Minimal area for focused data collection
 * - 100 Meters: Small area for localized work
 * - 500 Meters: Standard area for typical field operations
 *
 * @property meters The radius distance in meters
 * @property displayText Human-readable label for UI display
 * @author Sathya Narayanan
 */
enum class ESDataDistance(val meters: Int, val displayText: String) {
    FIFTY_METERS(50, "50 Meters"),
    HUNDRED_METERS(100, "100 Meters"),
    FIVE_HUNDRED_METERS(500, "500 Meters"),
    ONE_KILOMETER(1000, "1 Kilometer"),      // Added for testing
    TWO_KILOMETERS(2000, "2 Kilometers");     // Added for testing

    companion object {
        fun fromMeters(meters: Int): ESDataDistance? {
            return entries.find { it.meters == meters }
        }
    }
}
