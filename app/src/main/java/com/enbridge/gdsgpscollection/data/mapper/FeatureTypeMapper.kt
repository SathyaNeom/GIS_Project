package com.enbridge.gdsgpscollection.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.data.dto.FeatureAttributeDto
import com.enbridge.gdsgpscollection.data.dto.FeatureTypeDto
import com.enbridge.gdsgpscollection.domain.entity.AttributeType
import com.enbridge.gdsgpscollection.domain.entity.FeatureAttribute
import com.enbridge.gdsgpscollection.domain.entity.FeatureType
import com.enbridge.gdsgpscollection.domain.entity.GeometryType

fun FeatureTypeDto.toDomain(): FeatureType {
    return FeatureType(
        id = id,
        name = name,
        geometryType = geometryType.toGeometryType(),
        legendColor = legendColor,
        attributes = attributes.map { it.toDomain() }
    )
}

fun FeatureAttributeDto.toDomain(): FeatureAttribute {
    return FeatureAttribute(
        id = id,
        label = label,
        type = type.toAttributeType(),
        isRequired = isRequired,
        options = options,
        hint = hint,
        defaultValue = defaultValue
    )
}

private fun String.toGeometryType(): GeometryType {
    return when (this.uppercase()) {
        "POINT" -> GeometryType.POINT
        "POLYLINE", "LINE", "LINESTRING" -> GeometryType.POLYLINE
        "POLYGON" -> GeometryType.POLYGON
        else -> GeometryType.UNKNOWN
    }
}

private fun String.toAttributeType(): AttributeType {
    return when (this.uppercase()) {
        "TEXT" -> AttributeType.TEXT
        "TEXTMULTILINE" -> AttributeType.TEXTMULTILINE
        "NUMBER" -> AttributeType.NUMBER
        "DROPDOWN" -> AttributeType.DROPDOWN
        "DATE" -> AttributeType.DATE
        "LOCATION" -> AttributeType.LOCATION
        else -> AttributeType.TEXT
    }
}
