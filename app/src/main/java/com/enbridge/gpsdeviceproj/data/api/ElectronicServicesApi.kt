package com.enbridge.gpsdeviceproj.data.api

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.data.dto.FeatureTypeDto
import com.enbridge.gpsdeviceproj.data.dto.JobCardDto
import com.enbridge.gpsdeviceproj.data.dto.LoginRequestDto
import com.enbridge.gpsdeviceproj.data.dto.LoginResponseDto
import com.enbridge.gpsdeviceproj.data.dto.ProjectSettingsDto
import com.enbridge.gpsdeviceproj.data.dto.WorkOrderDto

interface ElectronicServicesApi {
    suspend fun login(request: LoginRequestDto): Result<LoginResponseDto>
    suspend fun getProjectSettings(): Result<ProjectSettingsDto>
    suspend fun getJobCards(distance: Int, workOrderType: String): Result<List<JobCardDto>>
    suspend fun getFeatureTypes(): Result<List<FeatureTypeDto>>
    suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrderDto>>

    suspend fun saveProjectSettings(
        workOrderNumber: String,
        projectSettings: ProjectSettingsDto
    ): Result<Unit>
    // suspend fun postEdits(...): Result<Boolean> // For future implementation
}
