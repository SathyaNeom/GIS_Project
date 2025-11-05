package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for DownloadESDataUseCase
 */
class DownloadESDataUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: DownloadESDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DownloadESDataUseCase(repository)
    }

    @Test
    fun `invoke should call repository downloadESData`() = runTest {
        // Given
        val distance = ESDataDistance.HUNDRED_METERS
        val latitude = 43.6532
        val longitude = -79.3832
        val progressFlow = flowOf(
            ESDataDownloadProgress(0.0f, "Starting", false),
            ESDataDownloadProgress(0.5f, "Downloading", false),
            ESDataDownloadProgress(1.0f, "Complete", true)
        )

        coEvery {
            repository.downloadESData(distance, latitude, longitude)
        } returns progressFlow

        // When
        val result = useCase(distance, latitude, longitude).toList()

        // Then
        assertEquals(3, result.size)
        assertEquals(0.0f, result[0].progress)
        assertEquals(0.5f, result[1].progress)
        assertEquals(1.0f, result[2].progress)
        assertEquals(true, result[2].isComplete)

        coVerify(exactly = 1) {
            repository.downloadESData(distance, latitude, longitude)
        }
    }

    @Test
    fun `invoke should work with different distance values`() = runTest {
        // Given
        val distance = ESDataDistance.FIVE_HUNDRED_METERS
        val latitude = 0.0
        val longitude = 0.0
        val progressFlow = flowOf(
            ESDataDownloadProgress(1.0f, "Done", true)
        )

        coEvery {
            repository.downloadESData(distance, latitude, longitude)
        } returns progressFlow

        // When
        val result = useCase(distance, latitude, longitude).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals("Done", result[0].message)

        coVerify(exactly = 1) {
            repository.downloadESData(distance, latitude, longitude)
        }
    }
}
