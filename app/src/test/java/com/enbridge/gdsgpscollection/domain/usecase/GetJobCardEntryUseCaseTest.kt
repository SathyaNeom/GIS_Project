package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */

import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry
import com.enbridge.gdsgpscollection.domain.repository.JobCardEntryRepository
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
 * Unit tests for GetJobCardEntryUseCase
 *
 * Tests the retrieval of job card entries ensuring proper handling of:
 * - Successful retrieval with valid ID
 * - Failed retrieval with invalid ID
 * - Database errors
 * - Empty entries
 */
class GetJobCardEntryUseCaseTest {

    private lateinit var repository: JobCardEntryRepository
    private lateinit var useCase: GetJobCardEntryUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetJobCardEntryUseCase(repository)
    }

    @Test
    fun `invoke should return job card entry when found`() = runTest {
        // Given
        val entryId = "JCE001"
        val expectedEntry = JobCardEntry(
            id = entryId,
            workOrder = "WO12345",
            address = "123 Main St",
            municipality = "Toronto",
            serviceType = "Gas"
        )
        coEvery { repository.getJobCardEntry(entryId) } returns Result.success(expectedEntry)

        // When
        val result = useCase(entryId)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(expectedEntry.id, result.getOrNull()?.id)
        assertEquals(expectedEntry.workOrder, result.getOrNull()?.workOrder)
        assertEquals(expectedEntry.address, result.getOrNull()?.address)

        coVerify(exactly = 1) {
            repository.getJobCardEntry(entryId)
        }
    }

    @Test
    fun `invoke should return failure when entry not found`() = runTest {
        // Given
        val entryId = "INVALID_ID"
        val exception = Exception("Job card entry not found")
        coEvery { repository.getJobCardEntry(entryId) } returns Result.failure(exception)

        // When
        val result = useCase(entryId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Job card entry not found", result.exceptionOrNull()?.message)

        coVerify(exactly = 1) {
            repository.getJobCardEntry(entryId)
        }
    }

    @Test
    fun `invoke should return failure when database error occurs`() = runTest {
        // Given
        val entryId = "JCE002"
        val exception = Exception("Database connection failed")
        coEvery { repository.getJobCardEntry(entryId) } returns Result.failure(exception)

        // When
        val result = useCase(entryId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should return entry with all fields populated`() = runTest {
        // Given
        val entryId = "JCE003"
        val fullEntry = JobCardEntry(
            id = entryId,
            workOrder = "WO99999",
            address = "456 Oak Ave",
            blockLot = "BL-123",
            municipality = "Mississauga",
            parentAssetId = "PA-456",
            serviceType = "Gas",
            connectionType = "Residential",
            meterNumber = "MTR-789",
            meterSize = "G4",
            testPressure = "700"
        )
        coEvery { repository.getJobCardEntry(entryId) } returns Result.success(fullEntry)

        // When
        val result = useCase(entryId)

        // Then
        assertTrue(result.isSuccess)
        val retrievedEntry = result.getOrNull()
        assertNotNull(retrievedEntry)
        assertEquals(fullEntry.workOrder, retrievedEntry?.workOrder)
        assertEquals(fullEntry.blockLot, retrievedEntry?.blockLot)
        assertEquals(fullEntry.meterNumber, retrievedEntry?.meterNumber)
        assertEquals(fullEntry.testPressure, retrievedEntry?.testPressure)
    }

    @Test
    fun `invoke should handle empty ID gracefully`() = runTest {
        // Given
        val entryId = ""
        val exception = Exception("Invalid entry ID")
        coEvery { repository.getJobCardEntry(entryId) } returns Result.failure(exception)

        // When
        val result = useCase(entryId)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid entry ID", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should return default entry values when created new`() = runTest {
        // Given
        val entryId = "NEW_ENTRY"
        val newEntry = JobCardEntry(id = entryId)
        coEvery { repository.getJobCardEntry(entryId) } returns Result.success(newEntry)

        // When
        val result = useCase(entryId)

        // Then
        assertTrue(result.isSuccess)
        val entry = result.getOrNull()
        assertNotNull(entry)
        assertEquals(entryId, entry?.id)
        assertEquals("", entry?.workOrder)
        assertEquals("", entry?.address)
        assertEquals("00000", entry?.meterOnIndex)
        assertEquals("5", entry?.meterNoDials)
    }
}
