package com.enbridge.gdsgpscollection.domain.entity

import com.arcgismaps.data.Geodatabase

/**
 * Metadata and information about a downloaded geodatabase file.
 *
 * This data class encapsulates all relevant information about a geodatabase,
 * including its source service, file details, and display configuration.
 * It serves as a unified container for managing multiple geodatabases within
 * the application.
 *
 * Design Rationale:
 * - Combines geodatabase instance with its metadata for easier management
 * - Tracks which service the geodatabase originated from
 * - Determines whether layers should be displayed on map or only in Table of Contents
 *
 * @property serviceId Unique identifier of the source feature service (e.g., "operations", "basemap", "wildfire").
 *                     Used for tracking, file naming, and service-specific operations.
 * @property serviceName Human-readable name of the source service (e.g., "Operations", "Basemap").
 *                       Displayed in UI elements like Table of Contents groupings.
 * @property fileName Name of the geodatabase file on disk (e.g., "Operations.geodatabase").
 *                    Generated dynamically based on serviceId.
 * @property geodatabase Loaded ArcGIS Geodatabase instance ready for use.
 *                       Contains feature tables that can be queried and displayed.
 * @property lastSyncTime Unix timestamp (milliseconds) of last successful sync or download.
 *                        Used for tracking data freshness and sync scheduling.
 * @property layerCount Number of feature tables (layers) contained in this geodatabase.
 *                      Used for validation and UI display.
 * @property fileSizeKB Size of the geodatabase file in kilobytes.
 *                      Used for storage management and progress estimation.
 * @property displayOnMap Flag indicating whether layers should be rendered on the map.
 *                        - true: Layers are added to map's operational layers (e.g., Basemap)
 *                        - false: Layers only appear in Table of Contents (e.g., Operations)
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * val basemapInfo = GeodatabaseInfo(
 *     serviceId = "basemap",
 *     serviceName = "Basemap",
 *     fileName = "basemap.geodatabase",
 *     geodatabase = loadedGeodatabase,
 *     lastSyncTime = System.currentTimeMillis(),
 *     layerCount = 5,
 *     fileSizeKB = 2048,
 *     displayOnMap = true
 * )
 * ```
 */
data class GeodatabaseInfo(
    val serviceId: String,
    val serviceName: String,
    val fileName: String,
    val geodatabase: Geodatabase,
    val lastSyncTime: Long,
    val layerCount: Int,
    val fileSizeKB: Long,
    val displayOnMap: Boolean
)