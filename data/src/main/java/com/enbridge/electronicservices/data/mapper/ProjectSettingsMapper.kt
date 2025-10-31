package com.enbridge.electronicservices.data.mapper

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.data.dto.ProjectSettingsDto
import com.enbridge.electronicservices.domain.entity.ProjectSettings
import javax.inject.Inject

/**
 * Mapper class for converting between ProjectSettingsDto and ProjectSettings domain entity
 */
class ProjectSettingsMapper @Inject constructor() {

    /**
     * Convert ProjectSettingsDto to domain entity
     */
    fun mapToDomain(dto: ProjectSettingsDto): ProjectSettings {
        return ProjectSettings(
            contractor = dto.contractor,
            crewId = dto.crewId,
            supervisor = dto.supervisor,
            fitterName = dto.fitterName,
            welderName = dto.welderName,
            workOrderTypes = dto.workOrderTypes,
            defaultDownloadDistance = dto.defaultDownloadDistance
        )
    }

    /**
     * Convert domain entity to ProjectSettingsDto
     */
    fun mapToDto(entity: ProjectSettings): ProjectSettingsDto {
        return ProjectSettingsDto(
            contractor = entity.contractor,
            crewId = entity.crewId,
            supervisor = entity.supervisor,
            fitterName = entity.fitterName,
            welderName = entity.welderName,
            workOrderTypes = entity.workOrderTypes,
            defaultDownloadDistance = entity.defaultDownloadDistance
        )
    }
}

/**
 * Extension function for backward compatibility
 */
fun ProjectSettingsDto.toDomain(): ProjectSettings {
    return ProjectSettings(
        contractor = contractor,
        crewId = crewId,
        supervisor = supervisor,
        fitterName = fitterName,
        welderName = welderName,
        workOrderTypes = workOrderTypes,
        defaultDownloadDistance = defaultDownloadDistance
    )
}
