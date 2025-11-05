package com.enbridge.gpsdeviceproj.domain.usecase

import com.enbridge.gpsdeviceproj.domain.entity.ProjectSettings
import com.enbridge.gpsdeviceproj.domain.repository.ProjectSettingsRepository
import javax.inject.Inject

/**
 * Use case for saving project settings with work order
 */
class SaveProjectSettingsUseCase @Inject constructor(
    private val repository: ProjectSettingsRepository
) {
    /**
     * Execute the use case to save project settings
     *
     * @param workOrderNumber The selected work order number
     * @param projectSettings The project settings to save
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        workOrderNumber: String,
        projectSettings: ProjectSettings
    ): Result<Unit> {
        return repository.saveProjectSettings(workOrderNumber, projectSettings)
    }
}
