package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gpsdeviceproj.domain.entity.FeatureType
import com.enbridge.gpsdeviceproj.domain.entity.GeometryType
import com.enbridge.gpsdeviceproj.domain.repository.FeatureRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetFeatureTypesUseCase
 *
 * Tests the retrieval of feature types ensuring proper handling of:
 * - Successful retrieval of feature types list
 * - Empty feature types list
 * - Repository errors
 * - Feature type properties
 */
class GetFeatureTypesUseCaseTest {

    private lateinit var repository: FeatureRepository
    private lateinit var useCase: GetFeatureTypesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetFeatureTypesUseCase(repository)
    }

    @Test
    fun `invoke should return list of feature types when available`() = runTest {
        // Given
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Gas Pipe",
                geometryType = GeometryType.POLYLINE,
                legendColor = "#E57373",
                attributes = emptyList()
            ),
            FeatureType(
                id = "FT002",
                name = "Service Point",
                geometryType = GeometryType.POINT,
                legendColor = "#64B5F6",
                attributes = emptyList()
            )
        )
        coEvery { repository.getFeatureTypes() } returns Result.success(featureTypes)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Gas Pipe", result.getOrNull()?.get(0)?.name)
        assertEquals("Service Point", result.getOrNull()?.get(1)?.name)

        coVerify(exactly = 1) {
            repository.getFeatureTypes()
        }
    }

    @Test
    fun `invoke should return empty list when no feature types available`() = runTest {
        // Given
        val emptyList = emptyList<FeatureType>()
        coEvery { repository.getFeatureTypes() } returns Result.success(emptyList)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertTrue(result.getOrNull()?.isEmpty() == true)

        coVerify(exactly = 1) {
            repository.getFeatureTypes()
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val exception = Exception("Failed to fetch feature types")
        coEvery { repository.getFeatureTypes() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to fetch feature types", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.getFeatureTypes()
        }
    }

    @Test
    fun `invoke should handle network error`() = runTest {
        // Given
        val exception = Exception("Network connection failed")
        coEvery { repository.getFeatureTypes() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should return feature types with different geometry types`() = runTest {
        // Given
        val featureTypes = listOf(
            FeatureType(
                id = "FT001",
                name = "Point Feature",
                geometryType = GeometryType.POINT,
                legendColor = "#FFD54F",
                attributes = emptyList()
            ),
            FeatureType(
                id = "FT002",
                name = "Line Feature",
                geometryType = GeometryType.POLYLINE,
                legendColor = "#81C784",
                attributes = emptyList()
            ),
            FeatureType(
                id = "FT003",
                name = "Polygon Feature",
                geometryType = GeometryType.POLYGON,
                legendColor = "#9575CD",
                attributes = emptyList()
            )
        )
        coEvery { repository.getFeatureTypes() } returns Result.success(featureTypes)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        val types = result.getOrNull()
        assertNotNull(types)
        assertEquals(3, types?.size)
        assertEquals(GeometryType.POINT, types?.get(0)?.geometryType)
        assertEquals(GeometryType.POLYLINE, types?.get(1)?.geometryType)
        assertEquals(GeometryType.POLYGON, types?.get(2)?.geometryType)
    }

    @Test
    fun `invoke should propagate repository result correctly`() = runTest {
        // Given
        val singleFeatureType = listOf(
            FeatureType(
                id = "FT999",
                name = "Test Feature",
                geometryType = GeometryType.POINT,
                legendColor = "#FF8A65",
                attributes = emptyList()
            )
        )
        coEvery { repository.getFeatureTypes() } returns Result.success(singleFeatureType)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(singleFeatureType, result.getOrNull())

        coVerify(exactly = 1) {
            repository.getFeatureTypes()
        }
    }
}
