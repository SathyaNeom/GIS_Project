package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gpsdeviceproj.domain.entity.ProjectSettings
import com.enbridge.gpsdeviceproj.domain.repository.ProjectSettingsRepository
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
 * Unit tests for GetProjectSettingsUseCase
 *
 * Tests the retrieval of project settings ensuring proper handling of:
 * - Successful retrieval of project settings
 * - Default project settings
 * - Repository errors
 * - Settings validation
 */
class GetProjectSettingsUseCaseTest {

    private lateinit var repository: ProjectSettingsRepository
    private lateinit var useCase: GetProjectSettingsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetProjectSettingsUseCase(repository)
    }

    @Test
    fun `invoke should return project settings when available`() = runTest {
        // Given
        val settings = ProjectSettings(
            contractor = "ABC Construction",
            crewId = "CREW001",
            supervisor = "John Doe",
            fitterName = "Jane Smith",
            welderName = "Bob Wilson",
            workOrderTypes = listOf("Installation", "Maintenance"),
            defaultDownloadDistance = 100
        )
        coEvery { repository.getProjectSettings() } returns Result.success(settings)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("ABC Construction", result.getOrNull()?.contractor)
        assertEquals("CREW001", result.getOrNull()?.crewId)
        assertEquals(100, result.getOrNull()?.defaultDownloadDistance)

        coVerify(exactly = 1) {
            repository.getProjectSettings()
        }
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val exception = Exception("Failed to load settings")
        coEvery { repository.getProjectSettings() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to load settings", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.getProjectSettings()
        }
    }

    @Test
    fun `invoke should handle database error`() = runTest {
        // Given
        val exception = Exception("Database read error")
        coEvery { repository.getProjectSettings() } returns Result.failure(exception)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database read error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should return settings with multiple work order types`() = runTest {
        // Given
        val settings = ProjectSettings(
            contractor = "XYZ Corp",
            crewId = "CREW002",
            supervisor = "Alice Johnson",
            fitterName = "Tom Brown",
            welderName = "Sarah Davis",
            workOrderTypes = listOf("6 Foot Pole", "8 Foot Pole", "Handheld"),
            defaultDownloadDistance = 500
        )
        coEvery { repository.getProjectSettings() } returns Result.success(settings)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.workOrderTypes?.size)
        assertTrue(result.getOrNull()?.workOrderTypes?.contains("6 Foot Pole") == true)
    }

    @Test
    fun `invoke should propagate repository result correctly`() = runTest {
        // Given
        val settings = ProjectSettings(
            contractor = "Test Contractor",
            crewId = "TEST001",
            supervisor = "Test Supervisor",
            fitterName = "Test Fitter",
            welderName = "Test Welder",
            workOrderTypes = listOf("Test Type"),
            defaultDownloadDistance = 200
        )
        coEvery { repository.getProjectSettings() } returns Result.success(settings)

        // When
        val result = useCase()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(settings, result.getOrNull())

        coVerify(exactly = 1) {
            repository.getProjectSettings()
        }
    }
}
