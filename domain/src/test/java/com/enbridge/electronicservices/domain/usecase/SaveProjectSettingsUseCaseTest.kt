package com.enbridge.electronicservices.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.domain.entity.ProjectSettings
import com.enbridge.electronicservices.domain.repository.ProjectSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SaveProjectSettingsUseCase
 *
 * Tests the saving of project settings ensuring proper handling of:
 * - Successful save operations
 * - Failed save operations
 * - Validation of work order and settings
 * - Repository interaction
 */
class SaveProjectSettingsUseCaseTest {

    private lateinit var repository: ProjectSettingsRepository
    private lateinit var useCase: SaveProjectSettingsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveProjectSettingsUseCase(repository)
    }

    @Test
    fun `invoke should successfully save project settings`() = runTest {
        // Given
        val workOrderNumber = "WO12345"
        val settings = ProjectSettings(
            contractor = "ABC Construction",
            crewId = "CREW001",
            supervisor = "John Doe",
            fitterName = "Jane Smith",
            welderName = "Bob Wilson",
            workOrderTypes = listOf("Installation"),
            defaultDownloadDistance = 100
        )
        coEvery {
            repository.saveProjectSettings(workOrderNumber, settings)
        } returns Result.success(Unit)

        // When
        val result = useCase(workOrderNumber, settings)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveProjectSettings(workOrderNumber, settings)
        }
    }

    @Test
    fun `invoke should return failure when save operation fails`() = runTest {
        // Given
        val workOrderNumber = "WO99999"
        val settings = ProjectSettings(
            contractor = "Test",
            crewId = "TEST",
            supervisor = "Test",
            fitterName = "Test",
            welderName = "Test",
            workOrderTypes = emptyList(),
            defaultDownloadDistance = 100
        )
        val exception = Exception("Failed to save settings")
        coEvery {
            repository.saveProjectSettings(workOrderNumber, settings)
        } returns Result.failure(exception)

        // When
        val result = useCase(workOrderNumber, settings)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to save settings", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.saveProjectSettings(workOrderNumber, settings)
        }
    }

    @Test
    fun `invoke should handle database error`() = runTest {
        // Given
        val workOrderNumber = "WO88888"
        val settings = ProjectSettings(
            contractor = "Error Test",
            crewId = "ERROR",
            supervisor = "Error",
            fitterName = "Error",
            welderName = "Error",
            workOrderTypes = emptyList(),
            defaultDownloadDistance = 50
        )
        val exception = Exception("Database write error")
        coEvery {
            repository.saveProjectSettings(workOrderNumber, settings)
        } returns Result.failure(exception)

        // When
        val result = useCase(workOrderNumber, settings)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database write error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should save settings with multiple work order types`() = runTest {
        // Given
        val workOrderNumber = "WO11111"
        val settings = ProjectSettings(
            contractor = "XYZ Corp",
            crewId = "CREW002",
            supervisor = "Alice",
            fitterName = "Tom",
            welderName = "Sarah",
            workOrderTypes = listOf("6 Foot Pole", "8 Foot Pole", "Handheld"),
            defaultDownloadDistance = 200
        )
        coEvery {
            repository.saveProjectSettings(workOrderNumber, settings)
        } returns Result.success(Unit)

        // When
        val result = useCase(workOrderNumber, settings)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveProjectSettings(workOrderNumber, settings)
        }
    }

    @Test
    fun `invoke should handle empty work order number`() = runTest {
        // Given
        val workOrderNumber = ""
        val settings = ProjectSettings(
            contractor = "Test",
            crewId = "TEST",
            supervisor = "Test",
            fitterName = "Test",
            welderName = "Test",
            workOrderTypes = emptyList(),
            defaultDownloadDistance = 100
        )
        val exception = Exception("Work order number cannot be empty")
        coEvery {
            repository.saveProjectSettings(workOrderNumber, settings)
        } returns Result.failure(exception)

        // When
        val result = useCase(workOrderNumber, settings)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Work order number cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should propagate repository result correctly`() = runTest {
        // Given
        val workOrderNumber = "WO55555"
        val settings = ProjectSettings(
            contractor = "Final Test",
            crewId = "FINAL",
            supervisor = "Final Supervisor",
            fitterName = "Final Fitter",
            welderName = "Final Welder",
            workOrderTypes = listOf("Final Type"),
            defaultDownloadDistance = 150
        )
        coEvery {
            repository.saveProjectSettings(workOrderNumber, settings)
        } returns Result.success(Unit)

        // When
        val result = useCase(workOrderNumber, settings)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveProjectSettings(workOrderNumber, settings)
        }
    }
}
