package com.enbridge.electronicservices.feature.jobs

/**
 * @author Sathya Narayanan
 */

import app.cash.turbine.test
import com.enbridge.electronicservices.domain.entity.JobCardEntry
import com.enbridge.electronicservices.domain.usecase.GetJobCardEntryUseCase
import com.enbridge.electronicservices.domain.usecase.SaveJobCardEntryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for JobCardEntryViewModel
 *
 * Tests job card entry view model operations ensuring proper handling of:
 * - Field updates
 * - Tab navigation
 * - Save operations
 * - Load operations
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JobCardEntryViewModelTest {

    private lateinit var saveJobCardEntryUseCase: SaveJobCardEntryUseCase
    private lateinit var getJobCardEntryUseCase: GetJobCardEntryUseCase
    private lateinit var viewModel: JobCardEntryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        saveJobCardEntryUseCase = mockk()
        getJobCardEntryUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): JobCardEntryViewModel {
        return JobCardEntryViewModel(saveJobCardEntryUseCase, getJobCardEntryUseCase)
    }

    @Test
    fun `initial state should have default values`() = runTest {
        // When
        viewModel = createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSaving)
        assertEquals(JobCardEntry(), state.entry)
        assertEquals(0, state.selectedTab)
        assertNull(state.error)
        assertFalse(state.saveSuccess)
    }

    @Test
    fun `updateField should update entry correctly`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.updateField { it.copy(workOrder = "WO12345") }

        // Then
        assertEquals("WO12345", viewModel.uiState.value.entry.workOrder)
    }

    @Test
    fun `updateField should update multiple fields independently`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.updateField { it.copy(workOrder = "WO12345") }
        viewModel.updateField { it.copy(address = "123 Main St") }
        viewModel.updateField { it.copy(municipality = "Toronto") }

        // Then
        val entry = viewModel.uiState.value.entry
        assertEquals("WO12345", entry.workOrder)
        assertEquals("123 Main St", entry.address)
        assertEquals("Toronto", entry.municipality)
    }

    @Test
    fun `selectTab should update selected tab index`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.selectTab(1)

        // Then
        assertEquals(1, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `selectTab should handle all tab indices`() = runTest {
        // Given
        viewModel = createViewModel()

        // When & Then
        viewModel.selectTab(0)
        assertEquals(0, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(1)
        assertEquals(1, viewModel.uiState.value.selectedTab)

        viewModel.selectTab(2)
        assertEquals(2, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `saveJobCardEntry should save successfully and update state`() = runTest {
        // Given
        val entry = JobCardEntry(
            id = "JCE001",
            workOrder = "WO12345",
            address = "123 Main St"
        )
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.success(Unit)
        viewModel = createViewModel()

        viewModel.updateField { entry }

        // When
        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.saveSuccess)
        assertNull(state.error)

        coVerify(exactly = 1) {
            saveJobCardEntryUseCase(entry)
        }
    }

    @Test
    fun `saveJobCardEntry should handle failure and show error`() = runTest {
        // Given
        val exception = Exception("Failed to save")
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.saveSuccess)
        assertEquals("Failed to save", state.error)

        coVerify(exactly = 1) {
            saveJobCardEntryUseCase(any())
        }
    }

    @Test
    fun `saveJobCardEntry should set saving state during operation`() = runTest {
        // Given
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.success(Unit)
        viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            // Initial state
            assertFalse(awaitItem().isSaving)

            viewModel.saveJobCardEntry()
            testDispatcher.scheduler.advanceUntilIdle()

            // Saving state
            assertTrue(awaitItem().isSaving)

            // Success state
            val finalState = awaitItem()
            assertFalse(finalState.isSaving)
            assertTrue(finalState.saveSuccess)
        }
    }

    @Test
    fun `loadJobCardEntry should load entry successfully`() = runTest {
        // Given
        val entryId = "JCE001"
        val entry = JobCardEntry(
            id = entryId,
            workOrder = "WO12345",
            address = "123 Main St",
            municipality = "Toronto"
        )
        coEvery { getJobCardEntryUseCase(entryId) } returns Result.success(entry)
        viewModel = createViewModel()

        // When
        viewModel.loadJobCardEntry(entryId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(entry, state.entry)
        assertNull(state.error)

        coVerify(exactly = 1) {
            getJobCardEntryUseCase(entryId)
        }
    }

    @Test
    fun `loadJobCardEntry should handle failure`() = runTest {
        // Given
        val entryId = "INVALID_ID"
        val exception = Exception("Entry not found")
        coEvery { getJobCardEntryUseCase(entryId) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.loadJobCardEntry(entryId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Entry not found", state.error)

        coVerify(exactly = 1) {
            getJobCardEntryUseCase(entryId)
        }
    }

    @Test
    fun `loadJobCardEntry should set loading state during operation`() = runTest {
        // Given
        val entryId = "JCE001"
        val entry = JobCardEntry(id = entryId)
        coEvery { getJobCardEntryUseCase(entryId) } returns Result.success(entry)
        viewModel = createViewModel()

        // When & Then
        viewModel.uiState.test {
            // Initial state
            assertFalse(awaitItem().isLoading)

            viewModel.loadJobCardEntry(entryId)
            testDispatcher.scheduler.advanceUntilIdle()

            // Loading state
            assertTrue(awaitItem().isLoading)

            // Success state
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(entry, finalState.entry)
        }
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        // Given
        val exception = Exception("Test error")
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.failure(exception)
        viewModel = createViewModel()

        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error is present
        assertEquals("Test error", viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `clearSaveSuccess should reset save success flag`() = runTest {
        // Given
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.success(Unit)
        viewModel = createViewModel()

        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify saveSuccess is true
        assertTrue(viewModel.uiState.value.saveSuccess)

        // When
        viewModel.clearSaveSuccess()

        // Then
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `saveJobCardEntry should clear previous error before saving`() = runTest {
        // Given
        val firstException = Exception("First error")
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.failure(firstException)
        viewModel = createViewModel()

        // First failed save
        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("First error", viewModel.uiState.value.error)

        // Setup for successful save
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.success(Unit)

        // When - Second save attempt
        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Error should be cleared
        assertNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `updateField should preserve all JobCard tab fields`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.updateField {
            it.copy(
                workOrder = "WO12345",
                address = "123 Main St",
                blockLot = "BL-123",
                municipality = "Toronto",
                serviceType = "Gas",
                connectionType = "Residential"
            )
        }

        // Then
        val entry = viewModel.uiState.value.entry
        assertEquals("WO12345", entry.workOrder)
        assertEquals("123 Main St", entry.address)
        assertEquals("BL-123", entry.blockLot)
        assertEquals("Toronto", entry.municipality)
        assertEquals("Gas", entry.serviceType)
        assertEquals("Residential", entry.connectionType)
    }

    @Test
    fun `updateField should preserve all Measurements tab fields`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.updateField {
            it.copy(
                streetWidth = "10",
                tapSize = "2",
                riserOnWall = "Yes",
                riserDistance = "5",
                mainToBuildingLine = "15"
            )
        }

        // Then
        val entry = viewModel.uiState.value.entry
        assertEquals("10", entry.streetWidth)
        assertEquals("2", entry.tapSize)
        assertEquals("Yes", entry.riserOnWall)
        assertEquals("5", entry.riserDistance)
        assertEquals("15", entry.mainToBuildingLine)
    }

    @Test
    fun `updateField should preserve all MeterInfo tab fields`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.updateField {
            it.copy(
                meterOnIndex = "12345",
                meterSize = "G4",
                meterNumber = "MTR-789",
                meterLocation = "Basement",
                regulatorLocation = "Exterior Wall"
            )
        }

        // Then
        val entry = viewModel.uiState.value.entry
        assertEquals("12345", entry.meterOnIndex)
        assertEquals("G4", entry.meterSize)
        assertEquals("MTR-789", entry.meterNumber)
        assertEquals("Basement", entry.meterLocation)
        assertEquals("Exterior Wall", entry.regulatorLocation)
    }

    @Test
    fun `saveJobCardEntry should handle exception without message`() = runTest {
        // Given
        val exception = Exception()
        coEvery { saveJobCardEntryUseCase(any()) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.saveJobCardEntry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Failed to save job card entry", viewModel.uiState.value.error)
    }

    @Test
    fun `loadJobCardEntry should handle exception without message`() = runTest {
        // Given
        val entryId = "JCE001"
        val exception = Exception()
        coEvery { getJobCardEntryUseCase(entryId) } returns Result.failure(exception)
        viewModel = createViewModel()

        // When
        viewModel.loadJobCardEntry(entryId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Failed to load job card entry", viewModel.uiState.value.error)
    }
}
