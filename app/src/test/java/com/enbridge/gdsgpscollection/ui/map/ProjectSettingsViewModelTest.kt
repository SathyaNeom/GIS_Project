package com.enbridge.gdsgpscollection.ui.map

import app.cash.turbine.test
import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import com.enbridge.gdsgpscollection.domain.facade.ProjectSettingsFacade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProjectSettingsViewModel
 * Tests all business logic and state management with new facade architecture
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProjectSettingsViewModelTest {

    private lateinit var projectSettingsFacade: ProjectSettingsFacade
    private lateinit var viewModel: ProjectSettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testProjectSettings = ProjectSettings(
        contractor = "NPL Canada Ltd.",
        crewId = "CREW-05",
        supervisor = "Bhavani S.",
        fitterName = "John Doe",
        welderName = "Jane Smith",
        workOrderTypes = listOf("6 Foot Pole", "8 Foot Pole", "Handheld"),
        defaultDownloadDistance = 500
    )

    private val testWorkOrders = listOf(
        WorkOrder(
            id = "wo_001",
            workOrderNumber = "22910778",
            address = "33 BRIAN DR NORTH YORK",
            poleType = "8 Foot Pole",
            distance = 150,
            displayText = "||33 BRIAN DR NORTH YORK||22910778"
        ),
        WorkOrder(
            id = "wo_002",
            workOrderNumber = "22910764",
            address = "37 BRIAN DR NORTH YORK",
            poleType = "8 Foot Pole",
            distance = 200,
            displayText = "||37 BRIAN DR NORTH YORK||22910764"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mock
        projectSettingsFacade = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProjectSettingsViewModel {
        return ProjectSettingsViewModel(
            projectSettingsFacade = projectSettingsFacade
        )
    }

    @Test
    fun `initial state should have default values`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("8 Foot Pole", state.selectedPoleType)
        assertTrue(state.workOrders.isEmpty())
        assertNull(state.selectedWorkOrder)
        assertEquals("", state.searchQuery)
        assertFalse(state.isLoadingWorkOrders)
    }

    @Test
    fun `loadProjectSettings should update state on success`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(testProjectSettings, state.projectSettings)
        assertFalse(state.isLoadingProjectSettings)
        assertNull(state.projectSettingsError)
    }

    @Test
    fun `loadProjectSettings should update state on failure`() = runTest {
        // Given
        val errorMessage = "Failed to load settings"
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.failure(
            Exception(
                errorMessage
            )
        )

        // When
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.projectSettings)
        assertFalse(state.isLoadingProjectSettings)
        assertEquals(errorMessage, state.projectSettingsError)
    }

    @Test
    fun `getWorkOrders should fetch work orders for selected pole type`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.getWorkOrders(
                poleType = "8 Foot Pole",
                latitude = 43.6532,
                longitude = -79.3832,
                distance = 500
            )
        } returns Result.success(testWorkOrders)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.getWorkOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(testWorkOrders, state.workOrders)
        assertEquals(testWorkOrders, state.filteredWorkOrders)
        assertFalse(state.isLoadingWorkOrders)
        assertNull(state.workOrdersError)

        coVerify {
            projectSettingsFacade.getWorkOrders(
                poleType = "8 Foot Pole",
                latitude = 43.6532,
                longitude = -79.3832,
                distance = 500
            )
        }
    }

    @Test
    fun `getWorkOrders should handle error`() = runTest {
        // Given
        val errorMessage = "Failed to fetch work orders"
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.getWorkOrders(
                poleType = "8 Foot Pole",
                latitude = 43.6532,
                longitude = -79.3832,
                distance = 500
            )
        } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.getWorkOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.workOrders.isEmpty())
        assertFalse(state.isLoadingWorkOrders)
        assertEquals(errorMessage, state.workOrdersError)
    }

    @Test
    fun `selectPoleType should update selected pole type`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectPoleType("6 Foot Pole")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("6 Foot Pole", state.selectedPoleType)
    }

    @Test
    fun `selectWorkOrder should update selected work order`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectWorkOrder(testWorkOrders[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(testWorkOrders[0], state.selectedWorkOrder)
    }

    @Test
    fun `updateSearchQuery should filter work orders by work order number`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.getWorkOrders(
                poleType = "8 Foot Pole",
                latitude = 43.6532,
                longitude = -79.3832,
                distance = 500
            )
        } returns Result.success(testWorkOrders)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getWorkOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updateSearchQuery("22910778")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("22910778", state.searchQuery)
        assertEquals(1, state.filteredWorkOrders.size)
        assertEquals("22910778", state.filteredWorkOrders[0].workOrderNumber)
    }

    @Test
    fun `clearSearch should reset filtered work orders`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.getWorkOrders(
                poleType = "8 Foot Pole",
                latitude = 43.6532,
                longitude = -79.3832,
                distance = 500
            )
        } returns Result.success(testWorkOrders)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getWorkOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateSearchQuery("22910778")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(testWorkOrders.size, state.filteredWorkOrders.size)
    }

    @Test
    fun `updateProjectSettings should update project settings in state`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedSettings = testProjectSettings.copy(contractor = "Updated Contractor")

        // When
        viewModel.updateProjectSettings(updatedSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Updated Contractor", state.projectSettings?.contractor)
    }

    @Test
    fun `saveProjectSettings should save successfully`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.saveProjectSettings(
                workOrderNumber = testWorkOrders[0].workOrderNumber,
                settings = testProjectSettings
            )
        } returns Result.success(Unit)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectWorkOrder(testWorkOrders[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.saveProjectSettings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.saveSuccess)
        assertFalse(state.isSaving)
        assertNull(state.saveError)

        coVerify {
            projectSettingsFacade.saveProjectSettings(
                workOrderNumber = testWorkOrders[0].workOrderNumber,
                settings = testProjectSettings
            )
        }
    }

    @Test
    fun `saveProjectSettings should fail when no work order is selected`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.saveProjectSettings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.saveSuccess)
        assertFalse(state.isSaving)
        assertEquals("Please select a work order", state.saveError)
    }

    @Test
    fun `saveProjectSettings should handle save error`() = runTest {
        // Given
        val errorMessage = "Failed to save"
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.saveProjectSettings(
                workOrderNumber = testWorkOrders[0].workOrderNumber,
                settings = testProjectSettings
            )
        } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectWorkOrder(testWorkOrders[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.saveProjectSettings()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.saveSuccess)
        assertFalse(state.isSaving)
        assertEquals(errorMessage, state.saveError)
    }

    @Test
    fun `clearErrors should clear all error states`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.failure(Exception("Error"))
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearErrors()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.workOrdersError)
        assertNull(state.projectSettingsError)
        assertNull(state.saveError)
    }

    @Test
    fun `resetState should reset to initial state`() = runTest {
        // Given
        coEvery { projectSettingsFacade.getProjectSettings() } returns Result.success(
            testProjectSettings
        )
        coEvery {
            projectSettingsFacade.getWorkOrders(
                poleType = "8 Foot Pole",
                latitude = 43.6532,
                longitude = -79.3832,
                distance = 500
            )
        } returns Result.success(testWorkOrders)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getWorkOrders()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectWorkOrder(testWorkOrders[0])
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.resetState()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.workOrders.isEmpty())
        assertNull(state.selectedWorkOrder)
        assertEquals("", state.searchQuery)
        assertEquals("8 Foot Pole", state.selectedPoleType)
        // Project settings should be retained
        assertEquals(testProjectSettings, state.projectSettings)
    }
}
