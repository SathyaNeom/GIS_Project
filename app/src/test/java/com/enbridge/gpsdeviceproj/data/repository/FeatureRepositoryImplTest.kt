package com.enbridge.gpsdeviceproj.data.repository

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gpsdeviceproj.data.api.ElectronicServicesApi
import com.enbridge.gpsdeviceproj.data.dto.FeatureAttributeDto
import com.enbridge.gpsdeviceproj.data.dto.FeatureTypeDto
import com.enbridge.gpsdeviceproj.domain.entity.AttributeType
import com.enbridge.gpsdeviceproj.domain.entity.GeometryType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FeatureRepositoryImpl
 *
 * Tests feature repository operations ensuring proper handling of:
 * - Successful retrieval of feature types
 * - Empty feature types list
 * - Network errors
 * - DTO to domain entity mapping with geometry types
 */
class FeatureRepositoryImplTest {

    private lateinit var api: ElectronicServicesApi
    private lateinit var repository: FeatureRepositoryImpl

    @Before
    fun setup() {
        api = mockk()
        repository = FeatureRepositoryImpl(api)
    }

    @Test
    fun `getFeatureTypes should return success with feature types when API call succeeds`() =
        runTest {
            // Given
            val featureTypeDtos = listOf(
                FeatureTypeDto(
                    id = "FT001",
                    name = "Gas Pipe",
                    geometryType = "POLYLINE",
                    legendColor = "#E57373",
                    attributes = emptyList()
                ),
                FeatureTypeDto(
                    id = "FT002",
                    name = "Service Point",
                    geometryType = "POINT",
                    legendColor = "#64B5F6",
                    attributes = emptyList()
                )
            )
            coEvery { api.getFeatureTypes() } returns Result.success(featureTypeDtos)

            // When
            val result = repository.getFeatureTypes()

            // Then
            assertTrue(result.isSuccess)
            assertNotNull(result.getOrNull())
            val featureTypes = result.getOrNull()
            assertEquals(2, featureTypes?.size)
            assertEquals("Gas Pipe", featureTypes?.get(0)?.name)
            assertEquals(GeometryType.POLYLINE, featureTypes?.get(0)?.geometryType)
            assertEquals("Service Point", featureTypes?.get(1)?.name)
            assertEquals(GeometryType.POINT, featureTypes?.get(1)?.geometryType)

            coVerify(exactly = 1) {
                api.getFeatureTypes()
            }
        }

    @Test
    fun `getFeatureTypes should return empty list when no feature types available`() = runTest {
        // Given
        coEvery { api.getFeatureTypes() } returns Result.success(emptyList())

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertTrue(result.getOrNull()?.isEmpty() == true)

        coVerify(exactly = 1) {
            api.getFeatureTypes()
        }
    }

    @Test
    fun `getFeatureTypes should return failure when API call fails`() = runTest {
        // Given
        val exception = Exception("Failed to fetch feature types")
        coEvery { api.getFeatureTypes() } returns Result.failure(exception)

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to fetch feature types", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            api.getFeatureTypes()
        }
    }

    @Test
    fun `getFeatureTypes should return failure when network error occurs`() = runTest {
        // Given
        val exception = Exception("Network connection failed")
        coEvery { api.getFeatureTypes() } throws exception

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network connection failed", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            api.getFeatureTypes()
        }
    }

    @Test
    fun `getFeatureTypes should correctly map geometry types`() = runTest {
        // Given
        val featureTypeDtos = listOf(
            FeatureTypeDto(
                id = "FT001",
                name = "Point Feature",
                geometryType = "POINT",
                legendColor = "#FFD54F",
                attributes = emptyList()
            ),
            FeatureTypeDto(
                id = "FT002",
                name = "Line Feature",
                geometryType = "LINE",
                legendColor = "#81C784",
                attributes = emptyList()
            ),
            FeatureTypeDto(
                id = "FT003",
                name = "Polygon Feature",
                geometryType = "POLYGON",
                legendColor = "#9575CD",
                attributes = emptyList()
            ),
            FeatureTypeDto(
                id = "FT004",
                name = "Unknown Feature",
                geometryType = "UNKNOWN_TYPE",
                legendColor = "#FF8A65",
                attributes = emptyList()
            )
        )
        coEvery { api.getFeatureTypes() } returns Result.success(featureTypeDtos)

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isSuccess)
        val featureTypes = result.getOrNull()
        assertNotNull(featureTypes)
        assertEquals(4, featureTypes?.size)
        assertEquals(GeometryType.POINT, featureTypes?.get(0)?.geometryType)
        assertEquals(GeometryType.POLYLINE, featureTypes?.get(1)?.geometryType)
        assertEquals(GeometryType.POLYGON, featureTypes?.get(2)?.geometryType)
        assertEquals(GeometryType.UNKNOWN, featureTypes?.get(3)?.geometryType)
    }

    @Test
    fun `getFeatureTypes should correctly map attributes`() = runTest {
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
            )
        )
        val featureTypeDtos = listOf(
            FeatureTypeDto(
                id = "FT001",
                name = "Feature with Attributes",
                geometryType = "POINT",
                legendColor = "#E57373",
                attributes = attributes
            )
        )
        coEvery { api.getFeatureTypes() } returns Result.success(featureTypeDtos)

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isSuccess)
        val featureTypes = result.getOrNull()
        assertNotNull(featureTypes)
        assertEquals(1, featureTypes?.size)

        val featureType = featureTypes?.get(0)
        assertEquals(2, featureType?.attributes?.size)

        val firstAttr = featureType?.attributes?.get(0)
        assertEquals("Name", firstAttr?.label)
        assertEquals(AttributeType.TEXT, firstAttr?.type)
        assertTrue(firstAttr?.isRequired == true)

        val secondAttr = featureType?.attributes?.get(1)
        assertEquals("Status", secondAttr?.label)
        assertEquals(AttributeType.DROPDOWN, secondAttr?.type)
        assertEquals(2, secondAttr?.options?.size)
        assertEquals("Active", secondAttr?.defaultValue)
    }

    @Test
    fun `getFeatureTypes should handle all attribute types`() = runTest {
        // Given
        val attributes = listOf(
            FeatureAttributeDto(id = "1", label = "Text", type = "TEXT", isRequired = false),
            FeatureAttributeDto(
                id = "2",
                label = "MultiText",
                type = "TEXTMULTILINE",
                isRequired = false
            ),
            FeatureAttributeDto(id = "3", label = "Number", type = "NUMBER", isRequired = false),
            FeatureAttributeDto(
                id = "4",
                label = "Dropdown",
                type = "DROPDOWN",
                isRequired = false
            ),
            FeatureAttributeDto(id = "5", label = "Date", type = "DATE", isRequired = false),
            FeatureAttributeDto(id = "6", label = "Location", type = "LOCATION", isRequired = false)
        )
        val featureTypeDtos = listOf(
            FeatureTypeDto(
                id = "FT001",
                name = "All Types",
                geometryType = "POINT",
                legendColor = "#000000",
                attributes = attributes
            )
        )
        coEvery { api.getFeatureTypes() } returns Result.success(featureTypeDtos)

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isSuccess)
        val featureTypes = result.getOrNull()
        val attrs = featureTypes?.get(0)?.attributes
        assertEquals(6, attrs?.size)
        assertEquals(AttributeType.TEXT, attrs?.get(0)?.type)
        assertEquals(AttributeType.TEXTMULTILINE, attrs?.get(1)?.type)
        assertEquals(AttributeType.NUMBER, attrs?.get(2)?.type)
        assertEquals(AttributeType.DROPDOWN, attrs?.get(3)?.type)
        assertEquals(AttributeType.DATE, attrs?.get(4)?.type)
        assertEquals(AttributeType.LOCATION, attrs?.get(5)?.type)
    }

    @Test
    fun `getFeatureTypes should handle case-insensitive geometry types`() = runTest {
        // Given
        val featureTypeDtos = listOf(
            FeatureTypeDto(
                id = "FT001",
                name = "Lowercase",
                geometryType = "point",
                legendColor = "#E57373",
                attributes = emptyList()
            ),
            FeatureTypeDto(
                id = "FT002",
                name = "Mixed Case",
                geometryType = "PoLyLiNe",
                legendColor = "#64B5F6",
                attributes = emptyList()
            )
        )
        coEvery { api.getFeatureTypes() } returns Result.success(featureTypeDtos)

        // When
        val result = repository.getFeatureTypes()

        // Then
        assertTrue(result.isSuccess)
        val featureTypes = result.getOrNull()
        assertEquals(GeometryType.POINT, featureTypes?.get(0)?.geometryType)
        assertEquals(GeometryType.POLYLINE, featureTypes?.get(1)?.geometryType)
    }
}
