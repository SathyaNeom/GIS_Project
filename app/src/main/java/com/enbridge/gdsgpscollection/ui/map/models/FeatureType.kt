package com.enbridge.gdsgpscollection.ui.map.models

/**
 * @author Sathya Narayanan
 */
import androidx.compose.ui.graphics.Color

/**
 * Represents a feature type in the GPS Device Project system
 */
data class FeatureType(
    val id: String,
    val name: String,
    val legendColor: Color,
    val attributes: List<FeatureAttribute>
)

/**
 * Represents an attribute field for a feature
 */
data class FeatureAttribute(
    val id: String,
    val label: String,
    val type: AttributeType,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
    val hint: String = "",
    val value: String = ""
)

/**
 * Types of attributes supported
 */
enum class AttributeType {
    TEXT,
    TEXTMULTILINE,
    NUMBER,
    DROPDOWN,
    DATE,
    LOCATION
}
