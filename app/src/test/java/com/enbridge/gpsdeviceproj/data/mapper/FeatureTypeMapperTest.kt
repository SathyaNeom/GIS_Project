package com.enbridge.gpsdeviceproj.data.mapper

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gpsdeviceproj.data.dto.FeatureAttributeDto
import com.enbridge.gpsdeviceproj.data.dto.FeatureTypeDto
import com.enbridge.gpsdeviceproj.domain.entity.AttributeType
import com.enbridge.gpsdeviceproj.domain.entity.GeometryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FeatureTypeMapper
 *
 * Tests DTO to domain entity mapping for FeatureType ensuring:
 * - Correct field mapping
 * - Proper geometry type conversion
 * - Attribute type conversion
 * - Case-insensitive type mapping
 */
class FeatureTypeMapperTest {

    @Test
    fun `toDomain should correctly map FeatureTypeDto to FeatureType`() {
        // Given
        val dto = FeatureTypeDto(
            id = "FT001",
            name = "Gas Pipe",
            geometryType = "POLYLINE",
            legendColor = "#E57373",
            attributes = emptyList()
        )

        // When
        val featureType = dto.toDomain()

        // Then
        assertEquals("FT001", featureType.id)
        assertEquals("Gas Pipe", featureType.name)
        assertEquals(GeometryType.POLYLINE, featureType.geometryType)
        assertEquals("#E57373", featureType.legendColor)
        assertTrue(featureType.attributes.isEmpty())
    }

    @Test
    fun `toDomain should map POINT geometry type correctly`() {
        // Given
        val dto = FeatureTypeDto(
            id = "FT001",
            name = "Service Point",
            geometryType = "POINT",
            legendColor = "#000000",
            attributes = emptyList()
        )

        // When
        val featureType = dto.toDomain()

        // Then
        assertEquals(GeometryType.POINT, featureType.geometryType)
    }

    @Test
    fun `toDomain should map POLYLINE and LINE geometry types correctly`() {
        // Given
        val dtoPolyline = FeatureTypeDto(
            id = "FT001",
            name = "Pipe",
            geometryType = "POLYLINE",
            legendColor = "#000000",
            attributes = emptyList()
        )
        val dtoLine = FeatureTypeDto(
            id = "FT002",
            name = "Line",
            geometryType = "LINE",
            legendColor = "#000000",
            attributes = emptyList()
        )

        // When
        val featureTypePolyline = dtoPolyline.toDomain()
        val featureTypeLine = dtoLine.toDomain()

        // Then
        assertEquals(GeometryType.POLYLINE, featureTypePolyline.geometryType)
        assertEquals(GeometryType.POLYLINE, featureTypeLine.geometryType)
    }

    @Test
    fun `toDomain should map POLYGON geometry type correctly`() {
        // Given
        val dto = FeatureTypeDto(
            id = "FT001",
            name = "Area",
            geometryType = "POLYGON",
            legendColor = "#000000",
            attributes = emptyList()
        )

        // When
        val featureType = dto.toDomain()

        // Then
        assertEquals(GeometryType.POLYGON, featureType.geometryType)
    }

    @Test
    fun `toDomain should map unknown geometry type to UNKNOWN`() {
        // Given
        val dto = FeatureTypeDto(
            id = "FT001",
            name = "Unknown",
            geometryType = "UNKNOWN_TYPE",
            legendColor = "#000000",
            attributes = emptyList()
        )

        // When
        val featureType = dto.toDomain()

        // Then
        assertEquals(GeometryType.UNKNOWN, featureType.geometryType)
    }

    @Test
    fun `toDomain should handle case-insensitive geometry types`() {
        // Given
        val dtoLowercase = FeatureTypeDto(
            id = "FT001",
            name = "Test",
            geometryType = "point",
            legendColor = "#000000",
            attributes = emptyList()
        )
        val dtoMixedCase = FeatureTypeDto(
            id = "FT002",
            name = "Test",
            geometryType = "PoLyGoN",
            legendColor = "#000000",
            attributes = emptyList()
        )

        // When
        val featureTypeLower = dtoLowercase.toDomain()
        val featureTypeMixed = dtoMixedCase.toDomain()

        // Then
        assertEquals(GeometryType.POINT, featureTypeLower.geometryType)
        assertEquals(GeometryType.POLYGON, featureTypeMixed.geometryType)
    }

    @Test
    fun `toDomain should correctly map FeatureAttributeDto to FeatureAttribute`() {
        // Given
        val attrDto = FeatureAttributeDto(
            id = "ATTR001",
            label = "Name",
            type = "TEXT",
            isRequired = true,
            options = listOf("Option1", "Option2"),
            hint = "Enter name",
            defaultValue = "Default"
        )

        // When
        val attribute = attrDto.toDomain()

        // Then
        assertEquals("ATTR001", attribute.id)
        assertEquals("Name", attribute.label)
        assertEquals(AttributeType.TEXT, attribute.type)
        assertTrue(attribute.isRequired)
        assertEquals(2, attribute.options.size)
        assertEquals("Enter name", attribute.hint)
        assertEquals("Default", attribute.defaultValue)
    }

    @Test
    fun `toDomain should map all AttributeTypes correctly`() {
        // Given
        val textAttr =
            FeatureAttributeDto(id = "1", label = "Text", type = "TEXT", isRequired = false)
        val multiTextAttr = FeatureAttributeDto(
            id = "2",
            label = "Multi",
            type = "TEXTMULTILINE",
            isRequired = false
        )
        val numberAttr =
            FeatureAttributeDto(id = "3", label = "Number", type = "NUMBER", isRequired = false)
        val dropdownAttr =
            FeatureAttributeDto(id = "4", label = "Dropdown", type = "DROPDOWN", isRequired = false)
        val dateAttr =
            FeatureAttributeDto(id = "5", label = "Date", type = "DATE", isRequired = false)
        val locationAttr =
            FeatureAttributeDto(id = "6", label = "Location", type = "LOCATION", isRequired = false)

        // When
        val text = textAttr.toDomain()
        val multiText = multiTextAttr.toDomain()
        val number = numberAttr.toDomain()
        val dropdown = dropdownAttr.toDomain()
        val date = dateAttr.toDomain()
        val location = locationAttr.toDomain()

        // Then
        assertEquals(AttributeType.TEXT, text.type)
        assertEquals(AttributeType.TEXTMULTILINE, multiText.type)
        assertEquals(AttributeType.NUMBER, number.type)
        assertEquals(AttributeType.DROPDOWN, dropdown.type)
        assertEquals(AttributeType.DATE, date.type)
        assertEquals(AttributeType.LOCATION, location.type)
    }

    @Test
    fun `toDomain should default unknown attribute types to TEXT`() {
        // Given
        val attrDto = FeatureAttributeDto(
            id = "ATTR001",
            label = "Unknown",
            type = "UNKNOWN_TYPE",
            isRequired = false
        )

        // When
        val attribute = attrDto.toDomain()

        // Then
        assertEquals(AttributeType.TEXT, attribute.type)
    }

    @Test
    fun `toDomain should map FeatureType with multiple attributes`() {
        // Given
        val attributes = listOf(
            FeatureAttributeDto(
                id = "ATTR001",
                label = "Name",
                type = "TEXT",
                isRequired = true,
                options = emptyList(),
                hint = "Enter name",
                defaultValue = ""
            ),
            FeatureAttributeDto(
                id = "ATTR002",
                label = "Status",
                type = "DROPDOWN",
                isRequired = false,
                options = listOf("Active", "Inactive"),
                hint = "Select status",
                defaultValue = "Active"
            ),
            FeatureAttributeDto(
                id = "ATTR003",
                label = "Count",
                type = "NUMBER",
                isRequired = true,
                options = emptyList(),
                hint = "Enter count",
                defaultValue = "0"
            )
        )
        val dto = FeatureTypeDto(
            id = "FT001",
            name = "Complex Feature",
            geometryType = "POINT",
            legendColor = "#E57373",
            attributes = attributes
        )

        // When
        val featureType = dto.toDomain()

        // Then
        assertEquals(3, featureType.attributes.size)
        assertEquals("Name", featureType.attributes[0].label)
        assertEquals(AttributeType.TEXT, featureType.attributes[0].type)
        assertTrue(featureType.attributes[0].isRequired)

        assertEquals("Status", featureType.attributes[1].label)
        assertEquals(AttributeType.DROPDOWN, featureType.attributes[1].type)
        assertFalse(featureType.attributes[1].isRequired)
        assertEquals(2, featureType.attributes[1].options.size)

        assertEquals("Count", featureType.attributes[2].label)
        assertEquals(AttributeType.NUMBER, featureType.attributes[2].type)
        assertTrue(featureType.attributes[2].isRequired)
    }

    @Test
    fun `toDomain should handle case-insensitive attribute types`() {
        // Given
        val attrLower =
            FeatureAttributeDto(id = "1", label = "Text", type = "text", isRequired = false)
        val attrMixed =
            FeatureAttributeDto(id = "2", label = "Number", type = "NuMbEr", isRequired = false)

        // When
        val attributeLower = attrLower.toDomain()
        val attributeMixed = attrMixed.toDomain()

        // Then
        assertEquals(AttributeType.TEXT, attributeLower.type)
        assertEquals(AttributeType.NUMBER, attributeMixed.type)
    }

    @Test
    fun `toDomain should preserve attribute default values and hints`() {
        // Given
        val attrDto = FeatureAttributeDto(
            id = "ATTR001",
            label = "Field",
            type = "TEXT",
            isRequired = false,
            options = emptyList(),
            hint = "This is a helpful hint",
            defaultValue = "Initial Value"
        )

        // When
        val attribute = attrDto.toDomain()

        // Then
        assertEquals("This is a helpful hint", attribute.hint)
        assertEquals("Initial Value", attribute.defaultValue)
    }

    @Test
    fun `toDomain should handle empty attribute lists`() {
        // Given
        val dto = FeatureTypeDto(
            id = "FT001",
            name = "Simple Feature",
            geometryType = "POINT",
            legendColor = "#000000",
            attributes = emptyList()
        )

        // When
        val featureType = dto.toDomain()

        // Then
        assertTrue(featureType.attributes.isEmpty())
    }
}
