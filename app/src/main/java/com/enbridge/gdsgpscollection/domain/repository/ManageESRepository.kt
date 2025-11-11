package com.enbridge.gdsgpscollection.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfig
import com.arcgismaps.geometry.Envelope
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Electronic Services data operations.
 *
 * Handles downloading, posting, and deleting ES data and job cards.
 * Supports both single-service (legacy/Wildfire) and multi-service (Project) environments.
 *
 * Architecture Evolution:
 * - Phase 1: Single geodatabase from one feature service (Wildfire)
 * - Phase 2: Multiple geodatabases from different services (Operations + Basemap)
 *
 * Backward Compatibility:
 * All existing single-service methods remain unchanged to support Wildfire environment.
 * New multi-service methods added with clear naming convention.
 *
 * @since 1.0.0
 * @author Sathya Narayanan
 */
interface ManageESRepository {

    // ========== SINGLE-SERVICE METHODS (Legacy/Wildfire Support) ==========

    /**
     * Downloads ES geodatabase data within the specified extent.
     *
     * Legacy method for single-service downloads (Wildfire environment).
     * Returns a Flow to track download progress.
     *
     * TODO: This method will be deprecated once all environments use multi-service approach.
     * Current usage: Wildfire environment only.
     *
     * @param extent The visible extent of the map to download data for
     * @return Flow of download progress updates
     */
    suspend fun downloadESData(
        extent: Envelope
    ): Flow<ESDataDownloadProgress>

    /**
     * Posts/uploads local ES data changes to the server.
     *
     * Legacy method for single-service sync (Wildfire environment).
     *
     * TODO: This method will be deprecated once all environments use multi-service approach.
     * Current usage: Wildfire environment only.
     *
     * @return Result indicating success or failure
     */
    suspend fun postESData(): Result<Boolean>

    /**
     * Loads an existing geodatabase from local storage if available.
     *
     * Legacy method for single geodatabase (Wildfire environment).
     *
     * TODO: This method will be deprecated once all environments use loadExistingGeodatabases().
     * Current usage: Wildfire environment only.
     *
     * @return Result containing:
     *         - Success(Geodatabase) if found and loaded successfully
     *         - Success(null) if no geodatabase file exists
     *         - Failure(Exception) if file exists but is corrupted or cannot be loaded
     */
    suspend fun loadExistingGeodatabase(): Result<com.arcgismaps.data.Geodatabase?>

    // ========== MULTI-SERVICE METHODS (Project Environment Support) ==========

    /**
     * Downloads geodatabase from a specific feature service.
     *
     * This method downloads data from a single configured service, allowing for
     * selective downloads or sequential processing. Use [downloadMultipleServices]
     * for parallel downloads from multiple services.
     *
     * @param extent The geographic extent to download data for
     * @param featureServiceConfig Configuration for the target feature service
     * @return Flow emitting download progress updates for this service
     *
     * Example:
     * ```kotlin
     * val operationsConfig = FeatureServiceConfig(...)
     * downloadServiceData(extent, operationsConfig).collect { progress ->
     *     println("Operations download: ${progress.progress * 100}%")
     * }
     * ```
     */
    suspend fun downloadServiceData(
        extent: Envelope,
        featureServiceConfig: FeatureServiceConfig
    ): Flow<ESDataDownloadProgress>

    /**
     * Downloads geodatabases from multiple feature services in parallel.
     *
     * This method coordinates simultaneous downloads from multiple services,
     * providing combined progress tracking. Each service downloads to a separate
     * geodatabase file named after the service ID.
     *
     * Download Strategy:
     * - Parallel execution using coroutines for maximum efficiency
     * - Individual progress tracking per service
     * - Combined overall progress calculation
     * - Fail-fast error handling (any service failure aborts all)
     *
     * File Naming:
     * - Service ID is used as filename (e.g., "operations.geodatabase", "basemap.geodatabase")
     * - Files stored in app's internal storage (secure)
     *
     * Error Handling:
     * - If ANY service fails, the entire operation is considered failed
     * - Partial downloads are cleaned up
     * - Detailed error information provided per service
     *
     * @param extent The geographic extent to download data for (same for all services)
     * @param featureServices List of feature service configurations to download from
     * @return Flow emitting combined progress updates for all services
     *
     * Example:
     * ```kotlin
     * val services = listOf(operationsConfig, basemapConfig)
     * downloadMultipleServices(extent, services).collect { progress ->
     *     println("Overall: ${progress.overallProgress * 100}%")
     *     println("Completed: ${progress.completedCount}/${progress.totalCount}")
     * }
     * ```
     */
    suspend fun downloadMultipleServices(
        extent: Envelope,
        featureServices: List<FeatureServiceConfig>
    ): Flow<MultiServiceDownloadProgress>

    /**
     * Synchronizes a specific geodatabase with its feature service.
     *
     * Uploads local changes (adds, updates, deletes) to the server and downloads
     * any server-side changes. Supports bidirectional sync.
     *
     * @param serviceId Identifier of the service to sync (e.g., "operations", "basemap")
     * @return Result indicating success or failure with error details
     *
     * Example:
     * ```kotlin
     * syncServiceGeodatabase("operations").onSuccess {
     *     println("Operations data synchronized")
     * }.onFailure { error ->
     *     println("Sync failed: ${error.message}")
     * }
     * ```
     */
    suspend fun syncServiceGeodatabase(serviceId: String): Result<Boolean>

    /**
     * Synchronizes all geodatabases with their respective feature services.
     *
     * Sequentially syncs each geodatabase, ensuring data consistency across
     * all services. Returns a map indicating success/failure per service.
     *
     * Sync Strategy:
     * - Sequential execution to avoid conflicts
     * - Independent results per service
     * - Continues syncing even if one service fails
     *
     * @return Result containing a map of service ID to sync success status
     *         Example: {"operations": true, "basemap": false}
     *
     * Example:
     * ```kotlin
     * syncAllGeodatabases().onSuccess { results ->
     *     results.forEach { (serviceId, success) ->
     *         println("$serviceId: ${if (success) "✓" else "✗"}")
     *     }
     * }
     * ```
     */
    suspend fun syncAllGeodatabases(): Result<Map<String, Boolean>>

    /**
     * Loads all existing geodatabases from local storage.
     *
     * Scans internal storage for geodatabase files matching configured services,
     * loads each one, and returns metadata including layer counts and file sizes.
     *
     * Loading Strategy:
     * - Validates file existence and integrity
     * - Loads geodatabase instances
     * - Extracts metadata (layer count, file size, last sync time)
     * - Associates with source service configuration
     *
     * @return Result containing list of loaded geodatabase information
     *         Empty list if no geodatabases found
     *         Failure if any geodatabase is corrupted
     *
     * Example:
     * ```kotlin
     * loadExistingGeodatabases().onSuccess { geodatabases ->
     *     geodatabases.forEach { info ->
     *         println("${info.serviceName}: ${info.layerCount} layers")
     *     }
     * }
     * ```
     */
    suspend fun loadExistingGeodatabases(): Result<List<GeodatabaseInfo>>

    // ========== COMMON METHODS (Used by Both Single and Multi-Service) ==========

    /**
     * Retrieves list of locally changed/edited features.
     *
     * Queries all geodatabases for features with local edits (adds, updates, deletes).
     * Works with both single and multiple geodatabases.
     *
     * @return Result containing list of job cards with local changes
     */
    suspend fun getChangedData(): Result<List<JobCard>>

    /**
     * Deletes saved job cards from local storage.
     *
     * @return Result indicating number of job cards deleted
     */
    suspend fun deleteJobCards(): Result<Int>

    /**
     * Gets the currently selected distance preference.
     *
     * @return The saved distance preference or default value
     */
    suspend fun getSelectedDistance(): ESDataDistance

    /**
     * Saves the selected distance preference.
     *
     * @param distance The distance to save
     */
    suspend fun saveSelectedDistance(distance: ESDataDistance)
}
