package com.enbridge.gdsgpscollection.data.api

/**
 * @author Sathya Narayanan
 */
import android.content.Context
import com.enbridge.gdsgpscollection.data.dto.FeatureTypeDto
import com.enbridge.gdsgpscollection.data.dto.JobCardDto
import com.enbridge.gdsgpscollection.data.dto.LoginRequestDto
import com.enbridge.gdsgpscollection.data.dto.LoginResponseDto
import com.enbridge.gdsgpscollection.data.dto.ProjectSettingsDto
import com.enbridge.gdsgpscollection.data.dto.WorkOrderDto
import com.enbridge.gdsgpscollection.util.Logger
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
        Logger.d(TAG, "Login request received for username: ${request.username}")
        return try {
            // Simulate network delay
            Logger.d(TAG, "Simulating network delay for login...")
            delay(1000)

            // For mock, accept any non-empty credentials
            if (request.username.isNotBlank() && request.password.isNotBlank()) {
                val jsonString = readAssetFile("login_success_response.json")
                val response = json.decodeFromString<LoginResponseDto>(jsonString)
                Logger.i(TAG, "Mock login successful - Username: ${request.username}")
                Result.success(response)
            } else {
                Logger.w(TAG, "Mock login failed - Invalid credentials")
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Mock login error", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectSettings(): Result<ProjectSettingsDto> {
        Logger.d(TAG, "Fetching mock project settings")
        return try {
            delay(500)
            val jsonString = readAssetFile("project_settings_response.json")
            val response = json.decodeFromString<ProjectSettingsDto>(jsonString)
            Logger.i(TAG, "Mock project settings retrieved - Crew: ${response.crewId}")
            Result.success(response)
        } catch (e: Exception) {
            Logger.e(TAG, "Error fetching mock project settings", e)
            Result.failure(e)
        }
    }

    override suspend fun getJobCards(
        distance: Int,
        workOrderType: String
    ): Result<List<JobCardDto>> {
        Logger.d(TAG, "Fetching mock job cards - Distance: $distance, Type: $workOrderType")
        return try {
            delay(800)
            val jsonString = readAssetFile("job_cards_response.json")
            val response = json.decodeFromString<List<JobCardDto>>(jsonString)
            Logger.i(TAG, "Mock job cards retrieved - Count: ${response.size}")
            Result.success(response)
        } catch (e: Exception) {
            Logger.e(TAG, "Error fetching mock job cards", e)
            Result.failure(e)
        }
    }

    override suspend fun getFeatureTypes(): Result<List<FeatureTypeDto>> {
        Logger.d(TAG, "Fetching mock feature types")
        return try {
            delay(500)
            val jsonString = readAssetFile("feature_types_response.json")
            val response = json.decodeFromString<List<FeatureTypeDto>>(jsonString)
            Logger.i(TAG, "Mock feature types retrieved - Count: ${response.size}")
            Result.success(response)
        } catch (e: Exception) {
            Logger.e(TAG, "Error fetching mock feature types", e)
            Result.failure(e)
        }
    }

    override suspend fun getWorkOrders(
        poleType: String,
        latitude: Double,
        longitude: Double,
        distance: Int
    ): Result<List<WorkOrderDto>> {
        Logger.d(
            TAG,
            "Fetching mock work orders - Pole: $poleType, Lat: $latitude, Lon: $longitude, Distance: $distance"
        )

        return try {
            // Simulate network delay
            delay(800)

            val jsonString = readAssetFile("work_orders_response.json")
            val workOrdersMap = json.decodeFromString<Map<String, List<WorkOrderDto>>>(jsonString)

            // Get work orders for the specified pole type
            val workOrders = workOrdersMap[poleType] ?: emptyList()
            Logger.d(TAG, "Found ${workOrders.size} work orders for pole type: $poleType")

            // Filter by distance if needed
            val filteredWorkOrders =
                workOrders.filter { it.distance?.let { d -> d <= distance } ?: true }

            Logger.i(TAG, "Returning ${filteredWorkOrders.size} filtered work orders for $poleType")
            Result.success(filteredWorkOrders)
        } catch (e: Exception) {
            Logger.e(TAG, "Error fetching mock work orders", e)
            Result.failure(e)
        }
    }

    override suspend fun saveProjectSettings(
        workOrderNumber: String,
        projectSettings: ProjectSettingsDto
    ): Result<Unit> {
        Logger.d(
            TAG,
            "Saving mock project settings - WO: $workOrderNumber, Crew: ${projectSettings.crewId}"
        )

        return try {
            // Simulate network delay
            delay(1000)

            // Mock successful save
            Logger.i(TAG, "Mock project settings saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Error saving mock project settings", e)
            Result.failure(e)
        }
    }

    private fun readAssetFile(fileName: String): String {
        Logger.d(TAG, "Reading asset file: $fileName")
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Logger.e(TAG, "Error reading asset file: $fileName", e)
            throw e
        }
    }
}
