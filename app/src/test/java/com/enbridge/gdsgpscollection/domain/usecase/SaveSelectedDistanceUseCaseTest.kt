package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SaveSelectedDistanceUseCase
 */
class SaveSelectedDistanceUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: SaveSelectedDistanceUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveSelectedDistanceUseCase(repository)
    }

    @Test
    fun `invoke should save distance to repository`() = runTest {
        // Given
        val distance = ESDataDistance.HUNDRED_METERS
        coEvery { repository.saveSelectedDistance(any()) } just runs

        // When
        useCase(distance)

        // Then
        coVerify(exactly = 1) {
            repository.saveSelectedDistance(distance)
        }
    }

    @Test
    fun `invoke should save all distance values correctly`() = runTest {
        // Given
        coEvery { repository.saveSelectedDistance(any()) } just runs

        // When & Then - Test 50 meters
        useCase(ESDataDistance.FIFTY_METERS)
        coVerify { repository.saveSelectedDistance(ESDataDistance.FIFTY_METERS) }

        // Test 100 meters
        useCase(ESDataDistance.HUNDRED_METERS)
        coVerify { repository.saveSelectedDistance(ESDataDistance.HUNDRED_METERS) }

        // Test 500 meters
        useCase(ESDataDistance.FIVE_HUNDRED_METERS)
        coVerify { repository.saveSelectedDistance(ESDataDistance.FIVE_HUNDRED_METERS) }
    }
}
