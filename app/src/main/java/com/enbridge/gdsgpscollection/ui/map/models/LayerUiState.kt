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
 * @property id Unique identifier for the layer (typically the layer name with prefix)
 * @property name Display name of the layer
 * @property isVisible Current visibility state of the layer on the map
 * @property geometryType The geometry type of features in this layer (for categorization)
 * @property legendItems List of legend items representing the symbols in this layer's renderer
 * @property isExpanded UI state indicating whether the legend section is expanded
 */
data class LayerUiState(
    val id: String,
    val name: String,
    val isVisible: Boolean,
    val geometryType: GeometryType? = null,
    val legendItems: List<LegendItem> = emptyList(),
    val isExpanded: Boolean = false
)
