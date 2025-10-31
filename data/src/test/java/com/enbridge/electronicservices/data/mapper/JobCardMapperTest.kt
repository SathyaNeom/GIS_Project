package com.enbridge.electronicservices.data.mapper

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.data.dto.JobCardDto
import com.enbridge.electronicservices.domain.entity.JobStatus
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for JobCardMapper
 *
 * Tests DTO to domain entity mapping for JobCard ensuring:
 * - Correct field mapping
 * - Proper status enum conversion
 * - Case-insensitive status mapping
 * - Handling of unknown statuses
 */
class JobCardMapperTest {

    @Test
    fun `toDomain should correctly map all fields from JobCardDto to JobCard`() {
        // Given
        val dto = JobCardDto(
            id = "JC001",
            address = "123 Main St",
            municipality = "Toronto",
            status = "ASSIGNED",
            serviceType = "Gas",
            connectionType = "Residential"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals("JC001", jobCard.id)
        assertEquals("123 Main St", jobCard.address)
        assertEquals("Toronto", jobCard.municipality)
        assertEquals(JobStatus.ASSIGNED, jobCard.status)
        assertEquals("Gas", jobCard.serviceType)
        assertEquals("Residential", jobCard.connectionType)
    }

    @Test
    fun `toDomain should map ASSIGNED status correctly`() {
        // Given
        val dto = JobCardDto(
            id = "JC001",
            address = "Address",
            municipality = "City",
            status = "ASSIGNED",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.ASSIGNED, jobCard.status)
    }

    @Test
    fun `toDomain should map IN_PROGRESS status correctly`() {
        // Given
        val dto = JobCardDto(
            id = "JC002",
            address = "Address",
            municipality = "City",
            status = "IN_PROGRESS",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.IN_PROGRESS, jobCard.status)
    }

    @Test
    fun `toDomain should map INPROGRESS status to IN_PROGRESS`() {
        // Given
        val dto = JobCardDto(
            id = "JC003",
            address = "Address",
            municipality = "City",
            status = "INPROGRESS",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.IN_PROGRESS, jobCard.status)
    }

    @Test
    fun `toDomain should map COMPLETED status correctly`() {
        // Given
        val dto = JobCardDto(
            id = "JC004",
            address = "Address",
            municipality = "City",
            status = "COMPLETED",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.COMPLETED, jobCard.status)
    }

    @Test
    fun `toDomain should map unknown status to UNKNOWN`() {
        // Given
        val dto = JobCardDto(
            id = "JC005",
            address = "Address",
            municipality = "City",
            status = "UNKNOWN_STATUS",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.UNKNOWN, jobCard.status)
    }

    @Test
    fun `toDomain should handle lowercase status strings`() {
        // Given
        val dto = JobCardDto(
            id = "JC006",
            address = "Address",
            municipality = "City",
            status = "assigned",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.ASSIGNED, jobCard.status)
    }

    @Test
    fun `toDomain should handle mixed case status strings`() {
        // Given
        val dto = JobCardDto(
            id = "JC007",
            address = "Address",
            municipality = "City",
            status = "In_Progress",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.IN_PROGRESS, jobCard.status)
    }

    @Test
    fun `toDomain should handle status with spaces`() {
        // Given
        val dto = JobCardDto(
            id = "JC008",
            address = "Address",
            municipality = "City",
            status = "IN PROGRESS",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.IN_PROGRESS, jobCard.status)
    }

    @Test
    fun `toDomain should handle empty string status as UNKNOWN`() {
        // Given
        val dto = JobCardDto(
            id = "JC009",
            address = "Address",
            municipality = "City",
            status = "",
            serviceType = "Gas",
            connectionType = "Type"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals(JobStatus.UNKNOWN, jobCard.status)
    }

    @Test
    fun `toDomain should preserve all string fields exactly`() {
        // Given
        val dto = JobCardDto(
            id = "JC_SPECIAL_ID_123",
            address = "456 Oak Ave, Unit #5",
            municipality = "Mississauga, ON",
            status = "ASSIGNED",
            serviceType = "Natural Gas - Residential",
            connectionType = "New Connection - Underground"
        )

        // When
        val jobCard = dto.toDomain()

        // Then
        assertEquals("JC_SPECIAL_ID_123", jobCard.id)
        assertEquals("456 Oak Ave, Unit #5", jobCard.address)
        assertEquals("Mississauga, ON", jobCard.municipality)
        assertEquals("Natural Gas - Residential", jobCard.serviceType)
        assertEquals("New Connection - Underground", jobCard.connectionType)
    }
}
