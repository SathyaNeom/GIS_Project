package com.enbridge.electronicservices.data.repository

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.domain.entity.JobCardEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for JobCardEntryRepositoryImpl
 *
 * Tests job card entry repository operations ensuring proper handling of:
 * - Save operations with ID generation
 * - Retrieval operations
 * - Update operations
 * - Delete operations
 * - Flow-based listing
 */
class JobCardEntryRepositoryImplTest {

    private lateinit var repository: JobCardEntryRepositoryImpl

    @Before
    fun setup() {
        repository = JobCardEntryRepositoryImpl()
    }

    @Test
    fun `saveJobCardEntry should generate ID for new entry`() = runTest {
        // Given
        val entry = JobCardEntry(
            id = "",
            workOrder = "WO12345",
            address = "123 Main St"
        )

        // When
        val result = repository.saveJobCardEntry(entry)

        // Then
        assertTrue(result.isSuccess)

        // Verify entry was saved with generated ID
        val savedEntries = repository.getAllJobCardEntries().first()
        assertEquals(1, savedEntries.size)
        assertNotEquals("", savedEntries[0].id)
    }

    @Test
    fun `saveJobCardEntry should preserve existing ID`() = runTest {
        // Given
        val entry = JobCardEntry(
            id = "EXISTING_ID",
            workOrder = "WO12345",
            address = "123 Main St"
        )

        // When
        val result = repository.saveJobCardEntry(entry)

        // Then
        assertTrue(result.isSuccess)

        val savedEntries = repository.getAllJobCardEntries().first()
        assertEquals(1, savedEntries.size)
        assertEquals("EXISTING_ID", savedEntries[0].id)
    }

    @Test
    fun `saveJobCardEntry should save multiple entries`() = runTest {
        // Given
        val entry1 = JobCardEntry(id = "ID1", workOrder = "WO001", address = "Address 1")
        val entry2 = JobCardEntry(id = "ID2", workOrder = "WO002", address = "Address 2")
        val entry3 = JobCardEntry(id = "ID3", workOrder = "WO003", address = "Address 3")

        // When
        repository.saveJobCardEntry(entry1)
        repository.saveJobCardEntry(entry2)
        repository.saveJobCardEntry(entry3)

        // Then
        val savedEntries = repository.getAllJobCardEntries().first()
        assertEquals(3, savedEntries.size)
    }

    @Test
    fun `saveJobCardEntry should update existing entry`() = runTest {
        // Given
        val entry = JobCardEntry(id = "ID1", workOrder = "WO001", address = "Original Address")
        repository.saveJobCardEntry(entry)

        val updatedEntry = entry.copy(address = "Updated Address")

        // When
        val result = repository.saveJobCardEntry(updatedEntry)

        // Then
        assertTrue(result.isSuccess)

        val savedEntries = repository.getAllJobCardEntries().first()
        assertEquals(1, savedEntries.size)
        assertEquals("Updated Address", savedEntries[0].address)
    }

    @Test
    fun `getJobCardEntry should return entry when it exists`() = runTest {
        // Given
        val entry = JobCardEntry(
            id = "TEST_ID",
            workOrder = "WO12345",
            address = "123 Main St",
            municipality = "Toronto"
        )
        repository.saveJobCardEntry(entry)

        // When
        val result = repository.getJobCardEntry("TEST_ID")

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("TEST_ID", result.getOrNull()?.id)
        assertEquals("WO12345", result.getOrNull()?.workOrder)
        assertEquals("123 Main St", result.getOrNull()?.address)
    }

    @Test
    fun `getJobCardEntry should return failure when entry not found`() = runTest {
        // When
        val result = repository.getJobCardEntry("NON_EXISTENT_ID")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Job Card Entry not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getJobCardEntry should handle multiple entries correctly`() = runTest {
        // Given
        val entry1 = JobCardEntry(id = "ID1", workOrder = "WO001")
        val entry2 = JobCardEntry(id = "ID2", workOrder = "WO002")
        val entry3 = JobCardEntry(id = "ID3", workOrder = "WO003")

        repository.saveJobCardEntry(entry1)
        repository.saveJobCardEntry(entry2)
        repository.saveJobCardEntry(entry3)

        // When
        val result = repository.getJobCardEntry("ID2")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("ID2", result.getOrNull()?.id)
        assertEquals("WO002", result.getOrNull()?.workOrder)
    }

    @Test
    fun `getAllJobCardEntries should return empty list initially`() = runTest {
        // When
        val entries = repository.getAllJobCardEntries().first()

        // Then
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `getAllJobCardEntries should return all saved entries`() = runTest {
        // Given
        val entry1 = JobCardEntry(id = "ID1", workOrder = "WO001")
        val entry2 = JobCardEntry(id = "ID2", workOrder = "WO002")

        repository.saveJobCardEntry(entry1)
        repository.saveJobCardEntry(entry2)

        // When
        val entries = repository.getAllJobCardEntries().first()

        // Then
        assertEquals(2, entries.size)
        assertTrue(entries.any { it.id == "ID1" })
        assertTrue(entries.any { it.id == "ID2" })
    }

    @Test
    fun `deleteJobCardEntry should remove entry successfully`() = runTest {
        // Given
        val entry = JobCardEntry(id = "DELETE_ID", workOrder = "WO999")
        repository.saveJobCardEntry(entry)

        // When
        val result = repository.deleteJobCardEntry("DELETE_ID")

        // Then
        assertTrue(result.isSuccess)

        val entries = repository.getAllJobCardEntries().first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `deleteJobCardEntry should not affect other entries`() = runTest {
        // Given
        val entry1 = JobCardEntry(id = "ID1", workOrder = "WO001")
        val entry2 = JobCardEntry(id = "ID2", workOrder = "WO002")
        val entry3 = JobCardEntry(id = "ID3", workOrder = "WO003")

        repository.saveJobCardEntry(entry1)
        repository.saveJobCardEntry(entry2)
        repository.saveJobCardEntry(entry3)

        // When
        repository.deleteJobCardEntry("ID2")

        // Then
        val entries = repository.getAllJobCardEntries().first()
        assertEquals(2, entries.size)
        assertTrue(entries.any { it.id == "ID1" })
        assertFalse(entries.any { it.id == "ID2" })
        assertTrue(entries.any { it.id == "ID3" })
    }

    @Test
    fun `deleteJobCardEntry should handle non-existent ID gracefully`() = runTest {
        // When
        val result = repository.deleteJobCardEntry("NON_EXISTENT")

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `saveJobCardEntry should preserve all entry fields`() = runTest {
        // Given
        val fullEntry = JobCardEntry(
            id = "FULL_ID",
            workOrder = "WO12345",
            address = "123 Main St",
            blockLot = "BL-123",
            municipality = "Toronto",
            serviceType = "Gas",
            meterNumber = "MTR-789",
            meterSize = "G4",
            testPressure = "700"
        )

        // When
        repository.saveJobCardEntry(fullEntry)

        // Then
        val result = repository.getJobCardEntry("FULL_ID")
        assertTrue(result.isSuccess)

        val retrieved = result.getOrNull()
        assertEquals(fullEntry.workOrder, retrieved?.workOrder)
        assertEquals(fullEntry.address, retrieved?.address)
        assertEquals(fullEntry.blockLot, retrieved?.blockLot)
        assertEquals(fullEntry.municipality, retrieved?.municipality)
        assertEquals(fullEntry.serviceType, retrieved?.serviceType)
        assertEquals(fullEntry.meterNumber, retrieved?.meterNumber)
        assertEquals(fullEntry.meterSize, retrieved?.meterSize)
        assertEquals(fullEntry.testPressure, retrieved?.testPressure)
    }
}
