package com.enbridge.gpsdeviceproj.data.dto

/**
 * @author Sathya Narayanan
 */
import kotlinx.serialization.Serializable

@Serializable
data class FeatureTypeDto(
    val id: String,
    val name: String,
    val geometryType: String,
    val legendColor: String,
    val attributes: List<FeatureAttributeDto>
)

@Serializable
data class FeatureAttributeDto(
    val id: String,
    val label: String,
    val type: String,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
    val hint: String = "",
    val defaultValue: String = ""
)
