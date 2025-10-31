package com.enbridge.electronicservices.domain.entity

/**
 * @author Sathya Narayanan
 */
data class FeatureType(
    val id: String,
    val name: String,
    val geometryType: GeometryType,
    val legendColor: String, // Hex color string like "#E57373"
    val attributes: List<FeatureAttribute>
)

data class FeatureAttribute(
    val id: String,
    val label: String,
    val type: AttributeType,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
    val hint: String = "",
    val defaultValue: String = ""
)

enum class AttributeType {
    TEXT,
    TEXTMULTILINE,
    NUMBER,
    DROPDOWN,
    DATE,
    LOCATION
}
