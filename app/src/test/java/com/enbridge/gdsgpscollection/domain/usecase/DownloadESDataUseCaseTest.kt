package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.SpatialReference
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for DownloadESDataUseCase
 *
 * Tests the download use case with the new Envelope-based API.
 *
 * Note: Tests using ArcGIS geometry classes (Envelope, SpatialReference) are ignored
 * because they require native ArcGIS libraries that cannot be loaded in JVM unit tests.
 * These tests should be run as instrumented tests (androidTest) on a device/emulator.
 */
class DownloadESDataUseCaseTest {

    private lateinit var repository: ManageESRepository
    private lateinit var useCase: DownloadESDataUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DownloadESDataUseCase(repository)
    }

    @Ignore("Requires ArcGIS native libraries - run as instrumented test instead")
    @Test
    fun `invoke should call repository downloadESData with envelope`() = runTest {
        // Given
        val extent = Envelope(
            xMin = -79.3832 - 0.01,
            yMin = 43.6532 - 0.01,
            xMax = -79.3832 + 0.01,
            yMax = 43.6532 + 0.01,
            spatialReference = SpatialReference.wgs84()
        )

        val progressFlow = flowOf(
            ESDataDownloadProgress(0.0f, "Starting", false),
            ESDataDownloadProgress(0.5f, "Downloading", false),
            ESDataDownloadProgress(1.0f, "Complete", true)
        )

        coEvery {
            repository.downloadESData(any())
        } returns progressFlow

        // When
        val result = useCase(extent).toList()

        // Then
        assertEquals(3, result.size)
        assertEquals(0.0f, result[0].progress)
        assertEquals(0.5f, result[1].progress)
        assertEquals(1.0f, result[2].progress)
        assertEquals(true, result[2].isComplete)

        coVerify(exactly = 1) {
            repository.downloadESData(any())
        }
    }

    @Ignore("Requires ArcGIS native libraries - run as instrumented test instead")
    @Test
    fun `invoke should work with different extent sizes`() = runTest {
        // Given
        val smallExtent = Envelope(
            xMin = -79.39,
            yMin = 43.64,
            xMax = -79.38,
            yMax = 43.65,
            spatialReference = SpatialReference.wgs84()
        )

        val progressFlow = flowOf(
            ESDataDownloadProgress(1.0f, "Done", true)
        )

        coEvery {
            repository.downloadESData(any())
        } returns progressFlow

        // When
        val result = useCase(smallExtent).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals("Done", result[0].message)

        coVerify(exactly = 1) {
            repository.downloadESData(any())
        }
    }

    @Ignore("Requires ArcGIS native libraries - run as instrumented test instead")
    @Test
    fun `invoke should propagate progress updates in sequence`() = runTest {
        // Given
        val extent = Envelope(
            xMin = -80.0,
            yMin = 43.0,
            xMax = -79.0,
            yMax = 44.0,
            spatialReference = SpatialReference.wgs84()
        )

        val progressFlow = flowOf(
            ESDataDownloadProgress(0.0f, "Initializing", false),
            ESDataDownloadProgress(0.25f, "Downloading layer 1", false),
            ESDataDownloadProgress(0.5f, "Downloading layer 2", false),
            ESDataDownloadProgress(0.75f, "Downloading layer 3", false),
            ESDataDownloadProgress(1.0f, "Complete", true)
        )

        coEvery {
            repository.downloadESData(any())
        } returns progressFlow

        // When
        val result = useCase(extent).toList()

        // Then
        assertEquals(5, result.size)
        assertEquals("Initializing", result[0].message)
        assertEquals("Downloading layer 1", result[1].message)
        assertEquals("Downloading layer 2", result[2].message)
        assertEquals("Downloading layer 3", result[3].message)
        assertEquals("Complete", result[4].message)
        assertTrue(result[4].isComplete)
    }
}
