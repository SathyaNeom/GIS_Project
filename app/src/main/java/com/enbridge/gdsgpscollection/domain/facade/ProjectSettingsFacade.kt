package com.enbridge.gdsgpscollection.domain.facade

import com.enbridge.gdsgpscollection.domain.entity.ProjectSettings
import com.enbridge.gdsgpscollection.domain.entity.WorkOrder

/**
 * Facade for managing project settings and work order operations.
 *
 * Provides a simplified interface for project settings management,
 * grouping related use cases to reduce ViewModel dependencies.
 *
 * Following the Facade pattern from SOLID principles.
 *
 * @author Sathya Narayanan
 */
interface ProjectSettingsFacade {

    /**
     * Retrieves work orders based on pole type and location.
     *
     * @param poleType Type of pole to filter by
     * @param latitude GPS latitude coordinate
     * @param longitude GPS longitude coordinate
     * @param distance Search radius in meters
     * @return Result containing list of work orders
     */
    suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrder>>

    /**
     * Retrieves the current project settings.
     *
     * @return Result containing project settings
     */
    suspend fun getProjectSettings(): Result<ProjectSettings>

    /**
     * Saves project settings with the selected work order.
     *
     * @param workOrderNumber Selected work order number
     * @param settings Project settings to save
     * @return Result indicating success or failure
     */
    suspend fun saveProjectSettings(
        workOrderNumber: String,
        settings: ProjectSettings
    ): Result<Unit>
}
