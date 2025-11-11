package com.enbridge.gdsgpscollection.domain.facade

import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import com.enbridge.gdsgpscollection.domain.usecase.GetProjectSettingsUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetWorkOrdersUseCase
import com.enbridge.gdsgpscollection.domain.usecase.SaveProjectSettingsUseCase
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ProjectSettingsFacade] that delegates to individual use cases.
 *
 * This facade simplifies the ViewModel layer by providing a single dependency
 * for all project settings related operations.
 *
 * @author Sathya Narayanan
 */
@Singleton
class ProjectSettingsFacadeImpl @Inject constructor(
    private val getWorkOrdersUseCase: GetWorkOrdersUseCase,
    private val getProjectSettingsUseCase: GetProjectSettingsUseCase,
    private val saveProjectSettingsUseCase: SaveProjectSettingsUseCase
) : ProjectSettingsFacade {

    companion object {
        private const val TAG = "ProjectSettingsFacadeImpl"
    }

    override suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrder>> {
        Logger.d(
            TAG,
            "getWorkOrders called - Pole Type: $poleType, Location: ($latitude, $longitude), Distance: $distance"
        )
        return getWorkOrdersUseCase(poleType, latitude, longitude, distance)
    }

    override suspend fun getProjectSettings(): Result<ProjectSettings> {
        Logger.d(TAG, "getProjectSettings called")
        return getProjectSettingsUseCase()
    }

    override suspend fun saveProjectSettings(
        workOrderNumber: String,
        settings: ProjectSettings
    ): Result<Unit> {
        Logger.d(
            TAG,
            "saveProjectSettings called - Work Order: $workOrderNumber, Crew: ${settings.crewId}"
        )
        return saveProjectSettingsUseCase(workOrderNumber, settings)
    }
}
