package com.enbridge.electronicservices.data.mapper

/**
 * @author Sathya Narayanan
 */

import com.enbridge.electronicservices.data.dto.ProjectSettingsDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProjectSettingsMapper
 *
 * Tests DTO to domain entity mapping and vice versa for ProjectSettings ensuring:
 * - Correct bidirectional mapping
 * - Preservation of all fields
 * - Handling of complex data types (lists)
 */
class ProjectSettingsMapperTest {

    private lateinit var mapper: ProjectSettingsMapper

    @Before
    fun setup() {
        mapper = ProjectSettingsMapper()
    }

    @Test
    fun `mapToDomain should correctly map all fields from DTO to domain entity`() {
        // Given
        val dto = ProjectSettingsDto(
            contractor = "ABC Construction",
            crewId = "CREW001",
            supervisor = "John Doe",
            fitterName = "Jane Smith",
            welderName = "Bob Wilson",
            workOrderTypes = listOf("6 Foot Pole", "8 Foot Pole"),
            defaultDownloadDistance = 100
        )

        // When
        val entity = mapper.mapToDomain(dto)

        // Then
        assertEquals("ABC Construction", entity.contractor)
        assertEquals("CREW001", entity.crewId)
        assertEquals("John Doe", entity.supervisor)
        assertEquals("Jane Smith", entity.fitterName)
        assertEquals("Bob Wilson", entity.welderName)
        assertEquals(2, entity.workOrderTypes.size)
        assertEquals("6 Foot Pole", entity.workOrderTypes[0])
        assertEquals("8 Foot Pole", entity.workOrderTypes[1])
        assertEquals(100, entity.defaultDownloadDistance)
    }

    @Test
    fun `mapToDto should correctly map all fields from domain entity to DTO`() {
        // Given
        val entity = com.enbridge.electronicservices.domain.entity.ProjectSettings(
            contractor = "XYZ Corp",
            crewId = "CREW002",
            supervisor = "Alice Johnson",
            fitterName = "Tom Brown",
            welderName = "Sarah Davis",
            workOrderTypes = listOf("Handheld", "6 Foot Pole"),
            defaultDownloadDistance = 200
        )

        // When
        val dto = mapper.mapToDto(entity)

        // Then
        assertEquals("XYZ Corp", dto.contractor)
        assertEquals("CREW002", dto.crewId)
        assertEquals("Alice Johnson", dto.supervisor)
        assertEquals("Tom Brown", dto.fitterName)
        assertEquals("Sarah Davis", dto.welderName)
        assertEquals(2, dto.workOrderTypes.size)
        assertEquals("Handheld", dto.workOrderTypes[0])
        assertEquals("6 Foot Pole", dto.workOrderTypes[1])
        assertEquals(200, dto.defaultDownloadDistance)
    }

    @Test
    fun `mapToDomain and mapToDto should be inverse operations`() {
        // Given
        val originalDto = ProjectSettingsDto(
            contractor = "Test Contractor",
            crewId = "TEST001",
            supervisor = "Test Supervisor",
            fitterName = "Test Fitter",
            welderName = "Test Welder",
            workOrderTypes = listOf("Type1", "Type2", "Type3"),
            defaultDownloadDistance = 150
        )

        // When
        val entity = mapper.mapToDomain(originalDto)
        val mappedBackDto = mapper.mapToDto(entity)

        // Then
        assertEquals(originalDto.contractor, mappedBackDto.contractor)
        assertEquals(originalDto.crewId, mappedBackDto.crewId)
        assertEquals(originalDto.supervisor, mappedBackDto.supervisor)
        assertEquals(originalDto.fitterName, mappedBackDto.fitterName)
        assertEquals(originalDto.welderName, mappedBackDto.welderName)
        assertEquals(originalDto.workOrderTypes, mappedBackDto.workOrderTypes)
        assertEquals(originalDto.defaultDownloadDistance, mappedBackDto.defaultDownloadDistance)
    }

    @Test
    fun `mapToDomain should handle empty strings`() {
        // Given
        val dto = ProjectSettingsDto(
            contractor = "",
            crewId = "",
            supervisor = "",
            fitterName = "",
            welderName = "",
            workOrderTypes = emptyList(),
            defaultDownloadDistance = 0
        )

        // When
        val entity = mapper.mapToDomain(dto)

        // Then
        assertEquals("", entity.contractor)
        assertEquals("", entity.crewId)
        assertEquals("", entity.supervisor)
        assertEquals("", entity.fitterName)
        assertEquals("", entity.welderName)
        assertTrue(entity.workOrderTypes.isEmpty())
        assertEquals(0, entity.defaultDownloadDistance)
    }

    @Test
    fun `mapToDomain should handle single work order type`() {
        // Given
        val dto = ProjectSettingsDto(
            contractor = "Contractor",
            crewId = "CREW",
            supervisor = "Supervisor",
            fitterName = "Fitter",
            welderName = "Welder",
            workOrderTypes = listOf("Single Type"),
            defaultDownloadDistance = 50
        )

        // When
        val entity = mapper.mapToDomain(dto)

        // Then
        assertEquals(1, entity.workOrderTypes.size)
        assertEquals("Single Type", entity.workOrderTypes[0])
    }

    @Test
    fun `mapToDomain should handle multiple work order types`() {
        // Given
        val dto = ProjectSettingsDto(
            contractor = "Contractor",
            crewId = "CREW",
            supervisor = "Supervisor",
            fitterName = "Fitter",
            welderName = "Welder",
            workOrderTypes = listOf("6 Foot Pole", "8 Foot Pole", "Handheld", "Other"),
            defaultDownloadDistance = 100
        )

        // When
        val entity = mapper.mapToDomain(dto)

        // Then
        assertEquals(4, entity.workOrderTypes.size)
        assertEquals("6 Foot Pole", entity.workOrderTypes[0])
        assertEquals("8 Foot Pole", entity.workOrderTypes[1])
        assertEquals("Handheld", entity.workOrderTypes[2])
        assertEquals("Other", entity.workOrderTypes[3])
    }

    @Test
    fun `mapToDomain should preserve special characters in strings`() {
        // Given
        val dto = ProjectSettingsDto(
            contractor = "ABC & XYZ Co., Ltd.",
            crewId = "CREW-001-A",
            supervisor = "O'Brien, John",
            fitterName = "Smith-Jones, Jane",
            welderName = "José García",
            workOrderTypes = listOf("Type (A)", "Type [B]"),
            defaultDownloadDistance = 250
        )

        // When
        val entity = mapper.mapToDomain(dto)

        // Then
        assertEquals("ABC & XYZ Co., Ltd.", entity.contractor)
        assertEquals("CREW-001-A", entity.crewId)
        assertEquals("O'Brien, John", entity.supervisor)
        assertEquals("Smith-Jones, Jane", entity.fitterName)
        assertEquals("José García", entity.welderName)
    }

    @Test
    fun `toDomain extension function should work correctly`() {
        // Given
        val dto = ProjectSettingsDto(
            contractor = "Extension Test",
            crewId = "EXT001",
            supervisor = "Test Supervisor",
            fitterName = "Test Fitter",
            welderName = "Test Welder",
            workOrderTypes = listOf("Test Type"),
            defaultDownloadDistance = 300
        )

        // When
        val entity = dto.toDomain()

        // Then
        assertEquals("Extension Test", entity.contractor)
        assertEquals("EXT001", entity.crewId)
        assertEquals("Test Supervisor", entity.supervisor)
        assertEquals("Test Fitter", entity.fitterName)
        assertEquals("Test Welder", entity.welderName)
        assertEquals(1, entity.workOrderTypes.size)
        assertEquals(300, entity.defaultDownloadDistance)
    }

    @Test
    fun `mapToDomain should handle various distance values`() {
        // Given
        val dto1 = ProjectSettingsDto(
            contractor = "C",
            crewId = "C",
            supervisor = "S",
            fitterName = "F",
            welderName = "W",
            workOrderTypes = emptyList(),
            defaultDownloadDistance = 50
        )
        val dto2 = dto1.copy(defaultDownloadDistance = 500)
        val dto3 = dto1.copy(defaultDownloadDistance = 1000)

        // When
        val entity1 = mapper.mapToDomain(dto1)
        val entity2 = mapper.mapToDomain(dto2)
        val entity3 = mapper.mapToDomain(dto3)

        // Then
        assertEquals(50, entity1.defaultDownloadDistance)
        assertEquals(500, entity2.defaultDownloadDistance)
        assertEquals(1000, entity3.defaultDownloadDistance)
    }
}
