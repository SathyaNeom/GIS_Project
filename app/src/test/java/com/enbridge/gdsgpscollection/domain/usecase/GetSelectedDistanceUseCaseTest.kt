package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GetSelectedDistanceUseCase
 */
class GetSelectedDistanceUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: GetSelectedDistanceUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSelectedDistanceUseCase(repository)
    }

    @Test
    fun `invoke should return default distance`() = runTest {
        // Given
        coEvery { repository.getSelectedDistance() } returns ESDataDistance.HUNDRED_METERS

        // When
        val result = useCase()

        // Then
        assertEquals(ESDataDistance.HUNDRED_METERS, result)

        coVerify(exactly = 1) {
            repository.getSelectedDistance()
        }
    }

    @Test
    fun `invoke should return saved distance`() = runTest {
        // Given
        coEvery { repository.getSelectedDistance() } returns ESDataDistance.FIVE_HUNDRED_METERS

        // When
        val result = useCase()

        // Then
        assertEquals(ESDataDistance.FIVE_HUNDRED_METERS, result)
        assertEquals(500, result.meters)

        coVerify(exactly = 1) {
            repository.getSelectedDistance()
        }
    }

    @Test
    fun `invoke should return correct distance for all enum values`() = runTest {
        // Test 50 meters
        coEvery { repository.getSelectedDistance() } returns ESDataDistance.FIFTY_METERS
        var result = useCase()
        assertEquals(50, result.meters)
        assertEquals("50 Meters", result.displayText)

        // Test 100 meters
        coEvery { repository.getSelectedDistance() } returns ESDataDistance.HUNDRED_METERS
        result = useCase()
        assertEquals(100, result.meters)
        assertEquals("100 Meters", result.displayText)

        // Test 500 meters
        coEvery { repository.getSelectedDistance() } returns ESDataDistance.FIVE_HUNDRED_METERS
        result = useCase()
        assertEquals(500, result.meters)
        assertEquals("500 Meters", result.displayText)
    }
}
