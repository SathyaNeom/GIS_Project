package com.enbridge.gdsgpscollection.ui.map

/**
 * @author Sathya Narayanan
 */

import app.cash.turbine.test
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.JobStatus
import com.enbridge.gdsgpscollection.domain.usecase.DeleteJobCardsUseCase
import com.enbridge.gdsgpscollection.domain.usecase.DownloadESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetChangedDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetSelectedDistanceUseCase
import com.enbridge.gdsgpscollection.domain.usecase.PostESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.SaveSelectedDistanceUseCase
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Test

/**
 * Unit tests for ManageESViewModel
 * Tests all user interactions and state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ManageESViewModelTest {

    private lateinit var downloadESDataUseCase: DownloadESDataUseCase
    private lateinit var postESDataUseCase: PostESDataUseCase
    private lateinit var getChangedDataUseCase: GetChangedDataUseCase
    private lateinit var deleteJobCardsUseCase: DeleteJobCardsUseCase
    private lateinit var getSelectedDistanceUseCase: GetSelectedDistanceUseCase
    private lateinit var saveSelectedDistanceUseCase: SaveSelectedDistanceUseCase
    private lateinit var viewModel: ManageESViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        downloadESDataUseCase = mockk()
        postESDataUseCase = mockk()
        getChangedDataUseCase = mockk()
        deleteJobCardsUseCase = mockk()
        getSelectedDistanceUseCase = mockk()
        saveSelectedDistanceUseCase = mockk()

        // Default mocks for initialization
        coEvery { getSelectedDistanceUseCase() } returns ESDataDistance.HUNDRED_METERS
        coEvery { getChangedDataUseCase() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ManageESViewModel {
        return ManageESViewModel(
            downloadESDataUseCase,
            postESDataUseCase,
            getChangedDataUseCase,
            deleteJobCardsUseCase,
            getSelectedDistanceUseCase,
            saveSelectedDistanceUseCase
        )
    }

    @Test
    fun `initial state should have default values`() = runTest {
        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(ESDataDistance.HUNDRED_METERS, state.selectedDistance)
        assertFalse(state.isDownloading)
        assertFalse(state.isPosting)
        assertFalse(state.showDeleteDialog)
        assertTrue(state.changedData.isEmpty())
    }

    @Test
    fun `onDistanceSelected should update distance and save preference`() = runTest {
        // Given
        coEvery { saveSelectedDistanceUseCase(any()) } just runs
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onDistanceSelected(ESDataDistance.FIVE_HUNDRED_METERS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(ESDataDistance.FIVE_HUNDRED_METERS, state.selectedDistance)

        coVerify(exactly = 1) {
            saveSelectedDistanceUseCase(ESDataDistance.FIVE_HUNDRED_METERS)
        }
    }

    @Test
    fun `onGetDataClicked should download data and update progress`() = runTest {
        // Given
        val progressFlow = flowOf(
            ESDataDownloadProgress(0.0f, "Starting", false),
            ESDataDownloadProgress(0.5f, "Downloading", false),
            ESDataDownloadProgress(1.0f, "Complete", true)
        )
        coEvery {
            downloadESDataUseCase(any(), any(), any())
        } returns progressFlow
        coEvery { getChangedDataUseCase() } returns Result.success(emptyList())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            // Initial state
            assertEquals(false, awaitItem().isDownloading)

            // When
            viewModel.onGetDataClicked(43.6532, -79.3832)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then - should see download states
            val states = mutableListOf<ManageESUiState>()
            while (states.size < 4) {
                states.add(awaitItem())
            }

            // Verify progress updates
            assertTrue(states.any { it.isDownloading })
            assertTrue(states.any { it.downloadProgress == 1.0f })
            assertTrue(states.last().isDownloading == false)
        }
    }

    @Test
    fun `onPostDataClicked should post data successfully`() = runTest {
        // Given
        coEvery { postESDataUseCase() } returns Result.success(true)
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onPostDataClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isPosting)
        assertTrue(state.postSuccess)

        coVerify(exactly = 1) {
            postESDataUseCase()
        }
    }

    @Test
    fun `onPostDataClicked should handle error`() = runTest {
        // Given
        coEvery { postESDataUseCase() } returns Result.failure(Exception("Network error"))
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onPostDataClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isPosting)
        assertEquals("Network error", state.postError)
    }

    @Test
    fun `onDeleteJobCardsClicked should show dialog with count`() = runTest {
        // Given
        coEvery { deleteJobCardsUseCase() } returns Result.success(0)
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
            deleteJobCardsUseCase()
        }
    }

    @Test
    fun `onDeleteJobCardsClicked with multiple cards should show correct count`() = runTest {
        // Given
        coEvery { deleteJobCardsUseCase() } returns Result.success(5)
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
        coEvery { deleteJobCardsUseCase() } returns Result.success(0)
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
        coEvery { getChangedDataUseCase() } returns Result.success(jobCards)

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.changedData.size)
        assertEquals("123 Main St", state.changedData[0].address)
    }
}
