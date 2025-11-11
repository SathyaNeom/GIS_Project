package com.enbridge.gdsgpscollection.domain.config

/**
 * Configuration for a single feature service endpoint.
 *
 * This data class represents the configuration required to connect to and download
 * data from an ArcGIS Feature Service. Each service can be configured with specific
 * parameters that control how it is displayed and synchronized.
 *
 * @property id Unique identifier for this service (e.g., "operations", "basemap", "wildfire").
 *              Used for internal tracking and file naming.
 * @property name Display name for this service shown in the UI (e.g., "Operations", "Basemap").
 *              This name is extracted from the service URL or provided explicitly.
 * @property url Full URL to the ArcGIS Feature Service endpoint.
 *              Must end with "/FeatureServer" for offline-capable services.
 * @property prefix Layer name prefix used to distinguish layers from this service (e.g., "OP_", "BM_", "GDB_").
 *              This prefix is prepended to all layer names from this service.
 * @property displayOnMap Whether layers from this service should be rendered on the map.
 *              Set to false for reference-only services (e.g., Operations data for Table of Contents only).
 * @property syncEnabled Whether this service supports offline synchronization.
 *              Set to false for read-only or online-only services.
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * val operationsService = FeatureServiceConfig(
 *     id = "operations",
 *     name = "Operations",
 *     url = "https://server.com/arcgis/rest/services/Operations/FeatureServer",
 *     prefix = "OP_",
 *     displayOnMap = false,
 *     syncEnabled = true
 * )
 * ```
 */
data class FeatureServiceConfig(
    val id: String,
    val name: String,
    val url: String,
    val prefix: String,
    val displayOnMap: Boolean = true,
    val syncEnabled: Boolean = true
)