package com.enbridge.gdsgpscollection.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder

/**
 * Repository interface for Project Settings operations
 */
interface ProjectSettingsRepository {
    /**
     * Get work orders based on pole type, location, and distance
     *
     * @param poleType The type of pole (6 Foot Pole, 8 Foot Pole, Handheld)
     * @param latitude Current latitude
     * @param longitude Current longitude
     * @param distance Search radius in meters
     * @return Result containing list of work orders or error
     */
    suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrder>>

    /**
     * Get current project settings
     *
     * @return Result containing project settings or error
     */
    suspend fun getProjectSettings(): Result<ProjectSettings>

    /**
     * Save project settings with selected work order
     *
     * @param workOrderNumber The selected work order number
     * @param projectSettings The project settings to save
     * @return Result indicating success or failure
     */
    suspend fun saveProjectSettings(
        workOrderNumber: String,
        projectSettings: ProjectSettings
    ): Result<Unit>
}
