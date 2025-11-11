package com.enbridge.gdsgpscollection.ui.map.models

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gdsgpscollection.domain.entity.GeometryType
import com.enbridge.gdsgpscollection.domain.entity.LegendItem

/**
 * UI state model representing layer information for display in the Table of Contents bottom sheet.
 *
 * This is a presentation layer model that holds the visibility state, legend information,
 * and metadata for a single map layer. It supports expandable legend sections for layers
 * with multiple symbols.
 *
 * Enhanced for Multi-Service Support:
 * - Tracks which service the layer originated from
 * - Distinguishes between layers displayed on map vs. reference-only layers
 * - Supports consolidated Table of Contents from multiple geodatabases
 *
 * @property id Unique identifier for the layer (e.g., "OP_PowerLines", "BM_Roads", "GDB_Wildfire")
 * @property name Display name of the layer (without prefix)
 * @property serviceId Identifier of the source service (e.g., "operations", "basemap", "wildfire")
 * @property serviceName Human-readable name of the source service (e.g., "Operations", "Basemap")
 * @property isVisible Current visibility state of the layer on the map
 * @property isOnMap Whether this layer is rendered on the map (false for reference-only layers)
 * @property geometryType The geometry type of features in this layer (for categorization)
 * @property legendItems List of legend items representing the symbols in this layer's renderer
 * @property isExpanded UI state indicating whether the legend section is expanded
 *
 * Usage Examples:
 * - Operations Layer: id="OP_PowerLines", serviceId="operations", isOnMap=false (TOC only)
 * - Basemap Layer: id="BM_Roads", serviceId="basemap", isOnMap=true (displayed on map)
 * - Wildfire Layer: id="GDB_Wildfire", serviceId="wildfire", isOnMap=true (legacy support)
 */
data class LayerUiState(
    val id: String,
    val name: String,
    val serviceId: String = "unknown",
    val serviceName: String = "Unknown",
    val isVisible: Boolean,
    val isOnMap: Boolean = true,
    val geometryType: GeometryType? = null,
    val legendItems: List<LegendItem> = emptyList(),
    val isExpanded: Boolean = false
)
