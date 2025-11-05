package com.enbridge.gdsgpscollection.data.repository

import com.enbridge.gdsgpscollection.data.api.ElectronicServicesApi
import com.enbridge.gdsgpscollection.data.mapper.ProjectSettingsMapper
import com.enbridge.gdsgpscollection.data.mapper.WorkOrderMapper
import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder
import com.enbridge.gdsgpscollection.domain.repository.ProjectSettingsRepository
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject

/**
 * Implementation of ProjectSettingsRepository
 * Handles data operations for project settings and work orders
 */
class ProjectSettingsRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi,
    private val workOrderMapper: WorkOrderMapper,
    private val projectSettingsMapper: ProjectSettingsMapper
) : ProjectSettingsRepository {

    companion object {
        private const val TAG = "ProjectSettingsRepo"
    }

    override suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrder>> {
        return try {
            Logger.d(
                TAG,
                "Fetching work orders - poleType: $poleType, lat: $latitude, lon: $longitude, distance: $distance"
            )

            api.getWorkOrders(poleType, latitude, longitude, distance)
                .mapCatching { dtoList ->
                    val workOrders = workOrderMapper.mapToDomain(dtoList)
                    Logger.i(TAG, "Successfully fetched ${workOrders.size} work orders")
                    workOrders
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Error fetching work orders", exception)
                }
        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error in getWorkOrders", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectSettings(): Result<ProjectSettings> {
        return try {
            Logger.d(TAG, "Fetching project settings")

            api.getProjectSettings()
                .mapCatching { dto ->
                    val settings = projectSettingsMapper.mapToDomain(dto)
                    Logger.i(
                        TAG,
                        "Successfully fetched project settings - Crew: ${settings.crewId}"
                    )
                    settings
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Error fetching project settings", exception)
                }
        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error in getProjectSettings", e)
            Result.failure(e)
        }
    }

    override suspend fun saveProjectSettings(
        workOrderNumber: String,
        projectSettings: ProjectSettings
    ): Result<Unit> {
        return try {
            Logger.i(
                TAG,
                "Saving project settings - WO: $workOrderNumber, Crew: ${projectSettings.crewId}"
            )

            val dto = projectSettingsMapper.mapToDto(projectSettings)
            api.saveProjectSettings(workOrderNumber, dto)
                .onSuccess {
                    Logger.i(TAG, "Successfully saved project settings")
                }
                .onFailure { exception ->
                    Logger.e(TAG, "Error saving project settings", exception)
                }
        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error in saveProjectSettings", e)
            Result.failure(e)
        }
    }
}
