package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gpsdeviceproj.domain.entity.JobCardEntry
import com.enbridge.gpsdeviceproj.domain.repository.JobCardEntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SaveJobCardEntryUseCase
 *
 * Tests the saving of job card entries ensuring proper handling of:
 * - Successful save operations
 * - Failed save operations
 * - Database errors
 * - Validation scenarios
 */
class SaveJobCardEntryUseCaseTest {

    private lateinit var repository: JobCardEntryRepository
    private lateinit var useCase: SaveJobCardEntryUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SaveJobCardEntryUseCase(repository)
    }

    @Test
    fun `invoke should successfully save job card entry`() = runTest {
        // Given
        val entry = JobCardEntry(
            id = "JCE001",
            workOrder = "WO12345",
            address = "123 Main St",
            municipality = "Toronto",
            serviceType = "Gas"
        )
        coEvery { repository.saveJobCardEntry(entry) } returns Result.success(Unit)

        // When
        val result = useCase(entry)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveJobCardEntry(entry)
        }
    }

    @Test
    fun `invoke should return failure when save operation fails`() = runTest {
        // Given
        val entry = JobCardEntry(id = "JCE002")
        val exception = Exception("Failed to save entry")
        coEvery { repository.saveJobCardEntry(entry) } returns Result.failure(exception)

        // When
        val result = useCase(entry)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to save entry", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.saveJobCardEntry(entry)
        }
    }

    @Test
    fun `invoke should handle database error`() = runTest {
        // Given
        val entry = JobCardEntry(id = "JCE003")
        val exception = Exception("Database write error")
        coEvery { repository.saveJobCardEntry(entry) } returns Result.failure(exception)

        // When
        val result = useCase(entry)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database write error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should save entry with complete data`() = runTest {
        // Given
        val completeEntry = JobCardEntry(
            id = "JCE004",
            workOrder = "WO99999",
            address = "456 Oak Ave",
            blockLot = "BL-123",
            municipality = "Mississauga",
            serviceType = "Gas",
            connectionType = "Residential",
            meterNumber = "MTR-789",
            meterSize = "G4",
            testPressure = "700",
            testDuration = "30",
            testUnit = "minutes"
        )
        coEvery { repository.saveJobCardEntry(completeEntry) } returns Result.success(Unit)

        // When
        val result = useCase(completeEntry)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveJobCardEntry(completeEntry)
        }
    }

    @Test
    fun `invoke should save entry with minimal data`() = runTest {
        // Given
        val minimalEntry = JobCardEntry(id = "JCE005")
        coEvery { repository.saveJobCardEntry(minimalEntry) } returns Result.success(Unit)

        // When
        val result = useCase(minimalEntry)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveJobCardEntry(minimalEntry)
        }
    }

    @Test
    fun `invoke should propagate repository errors correctly`() = runTest {
        // Given
        val entry = JobCardEntry(id = "JCE006")
        val exception = Exception("Constraint violation")
        coEvery { repository.saveJobCardEntry(entry) } returns Result.failure(exception)

        // When
        val result = useCase(entry)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Constraint violation", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should successfully update existing entry`() = runTest {
        // Given
        val updatedEntry = JobCardEntry(
            id = "JCE001",
            workOrder = "WO12345",
            address = "123 Main St - Updated",
            municipality = "Toronto"
        )
        coEvery { repository.saveJobCardEntry(updatedEntry) } returns Result.success(Unit)

        // When
        val result = useCase(updatedEntry)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) {
            repository.saveJobCardEntry(updatedEntry)
        }
    }
}
