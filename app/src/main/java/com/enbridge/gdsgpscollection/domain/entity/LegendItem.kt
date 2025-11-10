package com.enbridge.gdsgpscollection.domain.entity

/**
 * @author Sathya Narayanan
 */

/**
 * Domain entity representing a single legend item within a layer.
 *
 * A legend item corresponds to a symbol from the layer's renderer (SimpleRenderer,
 * UniqueValueRenderer, or ClassBreaksRenderer). It contains the visual representation
 * (symbol image file path) and descriptive information about what the symbol represents.
 *
 * @property label Human-readable label describing what this symbol represents
 *                 (e.g., "Main Line", "High Priority", "Active")
 * @property symbolImagePath File path to the rendered symbol image in cache directory
 *                           (e.g., "/cache/legend_symbols/layer_id_0.png")
 * @property value Optional attribute value or range this symbol represents
 *                 (e.g., "MAIN" for unique value renderer, "0-100" for class breaks)
 */
data class LegendItem(
    val label: String,
    val symbolImagePath: String,
    val value: String? = null
)
