package com.enbridge.gpsdeviceproj.data.api

/**
 * @author Sathya Narayanan
 */
import android.content.Context
import android.util.Log
import com.enbridge.gpsdeviceproj.data.dto.FeatureTypeDto
import com.enbridge.gpsdeviceproj.data.dto.JobCardDto
import com.enbridge.gpsdeviceproj.data.dto.LoginRequestDto
import com.enbridge.gpsdeviceproj.data.dto.LoginResponseDto
import com.enbridge.gpsdeviceproj.data.dto.ProjectSettingsDto
import com.enbridge.gpsdeviceproj.data.dto.WorkOrderDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MockElectronicServicesApi @Inject constructor(
    @ApplicationContext private val context: Context
) : ElectronicServicesApi {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val TAG = "MockElectronicServicesApi"
    }

    override suspend fun login(request: LoginRequestDto): Result<LoginResponseDto> {
        return try {
            // Simulate network delay
            delay(1000)

            // For mock, accept any non-empty credentials
            if (request.username.isNotBlank() && request.password.isNotBlank()) {
                val jsonString = readAssetFile("login_success_response.json")
                val response = json.decodeFromString<LoginResponseDto>(jsonString)
                Result.success(response)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProjectSettings(): Result<ProjectSettingsDto> {
        return try {
            delay(500)
            val jsonString = readAssetFile("project_settings_response.json")
            val response = json.decodeFromString<ProjectSettingsDto>(jsonString)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getJobCards(
        distance: Int,
        workOrderType: String
    ): Result<List<JobCardDto>> {
        return try {
            delay(800)
            val jsonString = readAssetFile("job_cards_response.json")
            val response = json.decodeFromString<List<JobCardDto>>(jsonString)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFeatureTypes(): Result<List<FeatureTypeDto>> {
        return try {
            delay(500)
            val jsonString = readAssetFile("feature_types_response.json")
            val response = json.decodeFromString<List<FeatureTypeDto>>(jsonString)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrderDto>> {
        return try {
            Log.d(
                TAG,
                "getWorkOrders - poleType: $poleType, lat: $latitude, lon: $longitude, distance: $distance"
            )

            // Simulate network delay
            delay(800)

            val jsonString = readAssetFile("work_orders_response.json")
            val workOrdersMap = json.decodeFromString<Map<String, List<WorkOrderDto>>>(jsonString)

            // Get work orders for the specified pole type
            val workOrders = workOrdersMap[poleType] ?: emptyList()

            // Filter by distance if needed
            val filteredWorkOrders =
                workOrders.filter { it.distance?.let { d -> d <= distance } ?: true }

            Log.d(TAG, "Returning ${filteredWorkOrders.size} work orders for $poleType")
            Result.success(filteredWorkOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching work orders", e)
            Result.failure(e)
        }
    }

    override suspend fun saveProjectSettings(
        workOrderNumber: String,
        projectSettings: ProjectSettingsDto
    ): Result<Unit> {
        return try {
            Log.d(TAG, "saveProjectSettings - WO: $workOrderNumber, Settings: $projectSettings")

            // Simulate network delay
            delay(1000)

            // Mock successful save
            Log.d(TAG, "Project settings saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving project settings", e)
            Result.failure(e)
        }
    }

    private fun readAssetFile(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
