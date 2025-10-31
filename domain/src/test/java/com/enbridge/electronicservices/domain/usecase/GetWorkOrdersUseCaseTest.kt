package com.enbridge.electronicservices.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.domain.entity.WorkOrder
import com.enbridge.electronicservices.domain.repository.ProjectSettingsRepository
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
 * Unit tests for GetWorkOrdersUseCase
 *
 * Tests the retrieval of work orders ensuring proper handling of:
 * - Successful retrieval with various parameters
 * - Empty work orders list
 * - Repository errors
 * - Location-based filtering
 */
class GetWorkOrdersUseCaseTest {

    private lateinit var repository: ProjectSettingsRepository
    private lateinit var useCase: GetWorkOrdersUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetWorkOrdersUseCase(repository)
    }

    @Test
    fun `invoke should return work orders for given pole type and location`() = runTest {
        // Given
        val poleType = "6 Foot Pole"
        val latitude = 43.6532
        val longitude = -79.3832
        val distance = 100
        val workOrders = listOf(
            WorkOrder(
                id = "WO001",
                workOrderNumber = "22910778",
                address = "123 Main St",
                poleType = poleType,
                distance = 50,
                displayText = "||123 Main St||22910778"
            ),
            WorkOrder(
                id = "WO002",
                workOrderNumber = "22910779",
                address = "456 Oak Ave",
                poleType = poleType,
                distance = 75,
                displayText = "||456 Oak Ave||22910779"
            )
        )
        coEvery {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        } returns Result.success(workOrders)

        // When
        val result = useCase(poleType, latitude, longitude, distance)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("22910778", result.getOrNull()?.get(0)?.workOrderNumber)
        assertEquals("22910779", result.getOrNull()?.get(1)?.workOrderNumber)

        coVerify(exactly = 1) {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        }
    }

    @Test
    fun `invoke should return empty list when no work orders match criteria`() = runTest {
        // Given
        val poleType = "8 Foot Pole"
        val latitude = 43.0
        val longitude = -80.0
        val distance = 50
        coEvery {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        } returns Result.success(emptyList())

        // When
        val result = useCase(poleType, latitude, longitude, distance)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertTrue(result.getOrNull()?.isEmpty() == true)

        coVerify(exactly = 1) {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val poleType = "6 Foot Pole"
        val latitude = 43.6532
        val longitude = -79.3832
        val distance = 100
        val exception = Exception("Failed to fetch work orders")
        coEvery {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        } returns Result.failure(exception)

        // When
        val result = useCase(poleType, latitude, longitude, distance)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to fetch work orders", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        }
    }

    @Test
    fun `invoke should handle network error`() = runTest {
        // Given
        val poleType = "Handheld"
        val latitude = 43.5
        val longitude = -79.5
        val distance = 200
        val exception = Exception("Network connection failed")
        coEvery {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        } returns Result.failure(exception)

        // When
        val result = useCase(poleType, latitude, longitude, distance)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Network connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should retrieve work orders for different pole types`() = runTest {
        // Given
        val poleType = "8 Foot Pole"
        val workOrders = listOf(
            WorkOrder(
                id = "WO003",
                workOrderNumber = "22910780",
                address = "200 Test Ave",
                poleType = poleType,
                distance = 100,
                displayText = "||200 Test Ave||22910780"
            )
        )
        coEvery {
            repository.getWorkOrders(poleType, 43.5, -79.5, 200)
        } returns Result.success(workOrders)

        // When
        val result = useCase(poleType, 43.5, -79.5, 200)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("22910780", result.getOrNull()?.get(0)?.workOrderNumber)
        assertEquals(poleType, result.getOrNull()?.get(0)?.poleType)
    }

    @Test
    fun `invoke should handle different distance parameters`() = runTest {
        // Given
        val poleType = "6 Foot Pole"
        val workOrders = listOf(
            WorkOrder(
                id = "WO004",
                workOrderNumber = "22910781",
                address = "400 Distance Blvd",
                poleType = poleType,
                distance = 50,
                displayText = "||400 Distance Blvd||22910781"
            )
        )
        coEvery {
            repository.getWorkOrders(poleType, 43.6, -79.4, 50)
        } returns Result.success(workOrders)

        // When
        val result = useCase(poleType, 43.6, -79.4, 50)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)

        coVerify(exactly = 1) {
            repository.getWorkOrders(poleType, 43.6, -79.4, 50)
        }
    }

    @Test
    fun `invoke should propagate repository result correctly`() = runTest {
        // Given
        val poleType = "Handheld"
        val latitude = 43.6532
        val longitude = -79.3832
        val distance = 100
        val workOrders = listOf(
            WorkOrder(
                id = "WO005",
                workOrderNumber = "22910782",
                address = "500 Final St",
                poleType = poleType,
                distance = 80,
                displayText = "||500 Final St||22910782"
            )
        )
        coEvery {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        } returns Result.success(workOrders)

        // When
        val result = useCase(poleType, latitude, longitude, distance)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(workOrders, result.getOrNull())

        coVerify(exactly = 1) {
            repository.getWorkOrders(poleType, latitude, longitude, distance)
        }
    }
}
