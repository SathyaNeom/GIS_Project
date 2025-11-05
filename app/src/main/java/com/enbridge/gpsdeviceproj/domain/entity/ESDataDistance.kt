package com.enbridge.gpsdeviceproj.domain.entity

/**
 * @author Sathya Narayanan
 * Represents the distance radius for downloading ES data
 * Used in Manage ES Edits feature to specify the area around the user's location
 */
enum class ESDataDistance(val meters: Int, val displayText: String) {
    FIFTY_METERS(50, "50 Meters"),
    HUNDRED_METERS(100, "100 Meters"),
    FIVE_HUNDRED_METERS(500, "500 Meters");

    companion object {
        fun fromMeters(meters: Int): ESDataDistance? {
            return entries.find { it.meters == meters }
        }
    }
}
