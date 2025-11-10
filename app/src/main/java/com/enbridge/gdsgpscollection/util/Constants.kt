package com.enbridge.gdsgpscollection.util

/**
 * @author Sathya Narayanan
 * Application-wide constants
 */
object Constants {

    /**
     * Feature service URL for WildfireSync data
     * Reference: https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer
     */
    const val FEATURE_SERVICE_URL =
        "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer"

    /**
     * Geodatabase file name
     */
    const val GEODATABASE_FILE_NAME = "wildfire_sync.geodatabase"

    /**
     * San Francisco coordinates (Web Mercator)
     * Center point for map initialization
     */
    object SanFrancisco {
        const val CENTER_X = -13630845.0  // Longitude in Web Mercator
        const val CENTER_Y = 4544861.0    // Latitude in Web Mercator
        const val INITIAL_SCALE = 72000.0

        // WGS84 coordinates for reference
        const val LATITUDE = 37.7749
        const val LONGITUDE = -122.4194
    }
}
