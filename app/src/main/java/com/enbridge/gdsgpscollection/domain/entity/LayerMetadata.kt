package com.enbridge.gdsgpscollection.domain.entity

import com.arcgismaps.data.FeatureTable

/**
 * Metadata for tracking geodatabase layers and their service configuration.
 *
 * This class decouples layer identity from naming conventions, enabling flexible
 * layer management across different environments and future schema changes.
 *
 * Architecture Pattern: Value Object
 * - Immutable data class containing layer metadata
 * - Enables service-aware layer management
 * - Supports environment-based filtering (displayOnMap)
 *
 * @property layerId Unique identifier for the layer (service-based, not dependent on name)
 * @property layerName Display name of the layer (e.g., "Gas Pipeline", "Meter Stations")
 * @property serviceId Service identifier from FeatureServiceConfig (e.g., "wildfire", "operations", "basemap")
 * @property serviceName Human-readable service name (e.g., "Wildfire", "Operations")
 * @property displayOnMap Whether this layer should be rendered on the map (from FeatureServiceConfig)
 * @property featureTable Reference to underlying FeatureTable for layer recreation
 * @property geodatabasePath Path to the source geodatabase file
 * @property isVisible Current visibility state of the layer
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
data class LayerMetadata(
    val layerId: String,
    val layerName: String,
    val serviceId: String,
    val serviceName: String,
    val displayOnMap: Boolean,
    val featureTable: FeatureTable?,
    val geodatabasePath: String,
    val isVisible: Boolean = true
)
