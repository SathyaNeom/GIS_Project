package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import app.cash.turbine.test
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.SpatialReference
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.JobStatus
import com.enbridge.gdsgpscollection.domain.facade.ManageESFacade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for ManageESViewModel
 * Tests all user interactions and state management with new multi-service architecture
 *
 * Note: Tests using ArcGIS geometry classes require native libraries and are marked @Ignore.
 * These should be run as instrumented tests on a device/emulator.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ManageESViewModelTest {

    private lateinit var manageESFacade: ManageESFacade
    private lateinit var configuration: FeatureServiceConfiguration
    private lateinit var viewModel: ManageESViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        manageESFacade = mockk()
        configuration = mockk()

        // Default mocks for initialization
        coEvery { manageESFacade.getSelectedDistance() } returns ESDataDistance.TWO_KILOMETERS
        coEvery { manageESFacade.getChangedData() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ManageESViewModel {
        return ManageESViewModel(
            manageESFacade = manageESFacade,
            configuration = configuration
        )
    }

    @Test
    fun `initial state should have default values`() = runTest {
        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(ESDataDistance.TWO_KILOMETERS, state.selectedDistance)
        assertFalse(state.isDownloading)
        assertFalse(state.isUploading)
        assertFalse(state.showDeleteDialog)
        assertTrue(state.changedData.isEmpty())
    }

    @Test
    fun `onDistanceSelected should update distance and save preference`() = runTest {
        // Given
        coEvery { manageESFacade.saveSelectedDistance(any()) } just runs
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onDistanceSelected(ESDataDistance.FIVE_HUNDRED_METERS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(ESDataDistance.FIVE_HUNDRED_METERS, state.selectedDistance)

        coVerify(exactly = 1) {
            manageESFacade.saveSelectedDistance(ESDataDistance.FIVE_HUNDRED_METERS)
        }
    }

    @Ignore("Requires ArcGIS native libraries (Envelope, SpatialReference) - run as instrumented test instead")
    @Test
    fun `onGetDataClicked should download data and update progress for single service`() = runTest {
        // Given
        val progressFlow = flowOf(
            ESDataDownloadProgress(0.0f, "Starting", false),
            ESDataDownloadProgress(0.5f, "Downloading", false),
            ESDataDownloadProgress(1.0f, "Complete", true, geodatabase = mockk())
        )

        // Mock single service environment
        every { configuration.getCurrentEnvironment() } returns mockk {
            every { featureServices } returns listOf(mockk())
        }

        coEvery {
            manageESFacade.downloadESData(any())
        } returns progressFlow

        coEvery { manageESFacade.getChangedData() } returns Result.success(emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Create a test extent
        val extent = Envelope(
            xMin = -79.4, yMin = 43.6,
            xMax = -79.3, yMax = 43.7,
            spatialReference = SpatialReference.wgs84()
        )

        // When
        viewModel.onGetDataClicked(extent, onGeodatabasesDownloaded = {}, onSaveTimestamp = {})
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isDownloadInProgress)

        coVerify(exactly = 1) {
            manageESFacade.downloadESData(any())
        }
    }

    @Test
    fun `onPostDataClicked should post data successfully`() = runTest {
        // Given
        coEvery { manageESFacade.postESData() } returns Result.success(true)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onPostDataClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isUploading)
        assertTrue(state.postSuccess)

        coVerify(exactly = 1) {
            manageESFacade.postESData()
        }
    }

    @Test
    fun `onPostDataClicked should handle error`() = runTest {
        // Given
        coEvery { manageESFacade.postESData() } returns Result.failure(Exception("Network error"))
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onPostDataClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isUploading)
        assertEquals("Network error", state.postError)
    }

    @Test
    fun `onDeleteJobCardsClicked should show dialog with count`() = runTest {
        // Given
        coEvery { manageESFacade.deleteJobCards() } returns Result.success(0)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onDeleteJobCardsClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.showDeleteDialog)
        assertEquals(0, state.deletedJobCardsCount)

        coVerify(exactly = 1) {
            manageESFacade.deleteJobCards()
        }
    }

    @Test
    fun `onDeleteJobCardsClicked with multiple cards should show correct count`() = runTest {
        // Given
        coEvery { manageESFacade.deleteJobCards() } returns Result.success(5)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onDeleteJobCardsClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.showDeleteDialog)
        assertEquals(5, state.deletedJobCardsCount)
    }

    @Test
    fun `onDismissDeleteDialog should hide dialog`() = runTest {
        // Given
        coEvery { manageESFacade.deleteJobCards() } returns Result.success(0)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDeleteJobCardsClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onDismissDeleteDialog()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showDeleteDialog)
    }

    @Test
    fun `loadChangedData should populate changed data list`() = runTest {
        // Given
        val jobCards = listOf(
            JobCard(
                id = "JC001",
                address = "123 Main St",
                municipality = "Toronto",
                status = JobStatus.IN_PROGRESS,
                serviceType = "Gas",
                connectionType = "Residential"
            )
        )
        coEvery { manageESFacade.getChangedData() } returns Result.success(jobCards)

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.changedData.size)
        assertEquals("123 Main St", state.changedData[0].address)
    }
}
