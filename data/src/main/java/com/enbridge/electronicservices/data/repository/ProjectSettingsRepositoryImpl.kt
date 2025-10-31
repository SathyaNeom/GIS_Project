package com.enbridge.electronicservices.data.repository

import android.util.Log
import com.enbridge.electronicservices.data.api.ElectronicServicesApi
import com.enbridge.electronicservices.data.mapper.ProjectSettingsMapper
import com.enbridge.electronicservices.data.mapper.WorkOrderMapper
import com.enbridge.electronicservices.domain.entity.ProjectSettings
import com.enbridge.electronicservices.domain.entity.WorkOrder
import com.enbridge.electronicservices.domain.repository.ProjectSettingsRepository
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
            Log.d(
                TAG,
                "Fetching work orders - poleType: $poleType, lat: $latitude, lon: $longitude, distance: $distance"
            )

            api.getWorkOrders(poleType, latitude, longitude, distance)
                .mapCatching { dtoList ->
                    val workOrders = workOrderMapper.mapToDomain(dtoList)
                    Log.d(TAG, "Successfully fetched ${workOrders.size} work orders")
                    workOrders
                }
                .onFailure { exception ->
                    Log.e(TAG, "Error fetching work orders", exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getWorkOrders", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectSettings(): Result<ProjectSettings> {
        return try {
            Log.d(TAG, "Fetching project settings")

            api.getProjectSettings()
                .mapCatching { dto ->
                    val settings = projectSettingsMapper.mapToDomain(dto)
                    Log.d(TAG, "Successfully fetched project settings: ${settings.crewId}")
                    settings
                }
                .onFailure { exception ->
                    Log.e(TAG, "Error fetching project settings", exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in getProjectSettings", e)
            Result.failure(e)
        }
    }

    override suspend fun saveProjectSettings(
        workOrderNumber: String,
        projectSettings: ProjectSettings
    ): Result<Unit> {
        return try {
            Log.d(
                TAG,
                "Saving project settings - WO: $workOrderNumber, Crew: ${projectSettings.crewId}"
            )

            val dto = projectSettingsMapper.mapToDto(projectSettings)
            api.saveProjectSettings(workOrderNumber, dto)
                .onSuccess {
                    Log.d(TAG, "Successfully saved project settings")
                }
                .onFailure { exception ->
                    Log.e(TAG, "Error saving project settings", exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in saveProjectSettings", e)
            Result.failure(e)
        }
    }
}
