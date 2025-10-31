package com.enbridge.electronicservices.domain.usecase

import com.enbridge.electronicservices.domain.entity.ProjectSettings
import com.enbridge.electronicservices.domain.repository.ProjectSettingsRepository
import javax.inject.Inject

/**
 * Use case for retrieving project settings
 */
class GetProjectSettingsUseCase @Inject constructor(
    private val repository: ProjectSettingsRepository
) {
    /**
     * Execute the use case to get project settings
     *
     * @return Result containing project settings or error
     */
    suspend operator fun invoke(): Result<ProjectSettings> {
        return repository.getProjectSettings()
    }
}
