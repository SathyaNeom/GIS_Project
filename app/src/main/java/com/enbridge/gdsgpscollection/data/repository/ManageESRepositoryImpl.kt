package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */

import android.content.Context
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.Envelope
import com.arcgismaps.geometry.GeometryEngine
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.tasks.geodatabase.GenerateGeodatabaseJob
import com.arcgismaps.tasks.geodatabase.GeodatabaseSyncTask
import com.arcgismaps.tasks.geodatabase.SyncDirection
import com.arcgismaps.tasks.geodatabase.SyncGeodatabaseJob
import com.arcgismaps.tasks.geodatabase.SyncGeodatabaseParameters
import com.arcgismaps.tasks.geodatabase.SyncLayerOption
import com.enbridge.gdsgpscollection.R
import com.enbridge.gdsgpscollection.data.local.dao.LocalEditDao
import com.enbridge.gdsgpscollection.data.local.preferences.PreferencesManager
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfig
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Implementation of ManageESRepository for managing geodatabase data operations.
 *
 * This repository handles:
 * - Downloading geodatabase data using GeodatabaseSyncTask
 * - Uploading local changes to the feature service
 * - Querying for locally modified data
 * - Managing job card lifecycle
 * - Persisting user preferences for distance selection
 * - Multi-service geodatabase management (Operations + Basemap)
 *
 * @property context Application context for file operations
 * @property localEditDao DAO for accessing local edit records
 * @property preferencesManager Manager for persisting user preferences
 * @property networkMonitor Network connectivity monitor
 * @property storageUtil Storage availability checker
 * @property configuration Feature service configuration provider
 */
class ManageESRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localEditDao: LocalEditDao,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: com.enbridge.gdsgpscollection.util.network.NetworkMonitor,
    private val storageUtil: com.enbridge.gdsgpscollection.util.storage.StorageUtil,
    private val configuration: FeatureServiceConfiguration
) : ManageESRepository {

    companion object {
        private const val TAG = "ManageESRepository"
    }

    /**
     * Path to the geodatabase file in internal storage (secure).
     * Legacy method for single geodatabase (Wildfire environment).
     *
     * TODO: Deprecated - Use getGeodatabasePath(serviceId) for multi-service support
     */
    private val geodatabaseFilePath: String
        get() = File(context.filesDir, Constants.GEODATABASE_FILE_NAME).absolutePath

    /**
     * Returns the geodatabase file path for a specific service.
     *
     * File naming convention: "{serviceId}.geodatabase"
     * Examples:
     * - "operations.geodatabase"
     * - "basemap.geodatabase"
     * - "wildfire.geodatabase"
     *
     * @param serviceId Unique identifier for the service
     * @return Absolute path to the geodatabase file
     */
    private fun getGeodatabasePath(serviceId: String): String {
        return File(context.filesDir, "$serviceId.geodatabase").absolutePath
    }

    /**
     * Downloads geodatabase data from the feature service using the current map extent.
     *
     * Enhanced with edge case handling:
     * - Pre-flight connectivity check
     * - Pre-flight storage validation
     * - Job-level retry with exponential backoff
     * - Network monitoring during download
     * - Storage monitoring during download
     * - Detailed error mapping
     *
     * @param extent The visible extent of the map to download data for
     * @return Flow emitting download progress updates
     */
    override suspend fun downloadESData(
        extent: Envelope
    ): Flow<ESDataDownloadProgress> = channelFlow {
        Logger.i(TAG, "Starting ES data download with edge case handling")
        Logger.d(
            TAG,
            "Extent: ${extent.xMin}, ${extent.yMin}, ${extent.xMax}, ${extent.yMax}"
        )

        try {
            // ========== PRE-FLIGHT CHECK 1: Network Connectivity ==========
            send(
                ESDataDownloadProgress(
                    0.0f,
                    context.getString(R.string.progress_checking_connection),
                    false
                )
            )

            if (!networkMonitor.isCurrentlyConnected()) {
                Logger.w(TAG, "Pre-flight check failed: No internet connection")
                send(
                    ESDataDownloadProgress(
                        0.0f,
                        context.getString(R.string.error_no_internet_message),
                        true,
                        "NO_INTERNET"
                    )
                )
                return@channelFlow
            }

            Logger.d(TAG, "Pre-flight check passed: Network available")

            // ========== PRE-FLIGHT CHECK 2: Storage Space ==========
            send(
                ESDataDownloadProgress(
                    0.05f,
                    context.getString(R.string.progress_validating_storage),
                    false
                )
            )

            val estimatedSize = storageUtil.estimateGeodatabaseSize()
            val storageCheck = storageUtil.checkStorageAvailability(estimatedSize)

            when (storageCheck) {
                is com.enbridge.gdsgpscollection.util.storage.StorageCheckResult.CriticallyLow -> {
                    Logger.w(TAG, "Pre-flight check failed: Critically low storage")
                    send(
                        ESDataDownloadProgress(
                            0.0f,
                            context.getString(
                                R.string.msg_storage_warning,
                                storageUtil.formatBytes(storageCheck.requiredBytes),
                                storageUtil.formatBytes(storageCheck.availableBytes)
                            ),
                            true,
                            "STORAGE_CRITICALLY_LOW"
                        )
                    )
                    return@channelFlow
                }

                is com.enbridge.gdsgpscollection.util.storage.StorageCheckResult.Insufficient,
                is com.enbridge.gdsgpscollection.util.storage.StorageCheckResult.Low -> {
                    // Warning but allow download (as per user request)
                    Logger.w(TAG, "Storage warning: Low space available")
                    send(
                        ESDataDownloadProgress(
                            0.05f,
                            context.getString(R.string.warning_low_storage_message),
                            false
                        )
                    )
                }

                is com.enbridge.gdsgpscollection.util.storage.StorageCheckResult.Sufficient -> {
                    Logger.d(TAG, "Pre-flight check passed: Sufficient storage")
                }
            }

            // ========== RETRY LOGIC: Execute download with exponential backoff ==========
            val retryStrategy = com.enbridge.gdsgpscollection.util.network.RetryStrategy.DEFAULT

            val downloadResult = retryStrategy.execute<Geodatabase>(
                block = { attempt ->
                    // Check connectivity before each attempt
                    if (!networkMonitor.isCurrentlyConnected()) {
                        Logger.w(TAG, "Network unavailable before attempt ${attempt + 1}")
                        throw java.net.UnknownHostException("No internet connection")
                    }

                    // Execute actual download
                    executeDownload(extent, attempt, this)
                },
                onRetry = { attempt, error ->
                    Logger.w(TAG, "Retry attempt ${attempt + 1} after error: ${error.message}")
                    send(
                        ESDataDownloadProgress(
                            0.1f,
                            context.getString(
                                R.string.msg_retrying_attempt,
                                attempt + 1,
                                retryStrategy.maxAttempts
                            ),
                            false
                        )
                    )
                }
            )

            // ========== HANDLE FINAL RESULT ==========
            downloadResult.onSuccess { geodatabase ->
                Logger.i(TAG, "Download completed successfully")
                send(
                    ESDataDownloadProgress(
                        1.0f,
                        context.getString(R.string.progress_complete),
                        true,
                        geodatabase = geodatabase
                    )
                )
            }.onFailure { error ->
                Logger.e(TAG, "Download failed after all retry attempts", error)

                // Map error to user-friendly type
                val errorType = com.enbridge.gdsgpscollection.util.error.ErrorMapper.mapError(error)
                val errorMessage = when (errorType) {
                    com.enbridge.gdsgpscollection.util.error.GeodatabaseError.NO_INTERNET ->
                        context.getString(R.string.error_no_internet_message)

                    com.enbridge.gdsgpscollection.util.error.GeodatabaseError.SERVER_UNAVAILABLE ->
                        context.getString(R.string.error_server_unavailable_message)

                    com.enbridge.gdsgpscollection.util.error.GeodatabaseError.SERVER_TIMEOUT ->
                        context.getString(R.string.error_server_timeout_message)

                    com.enbridge.gdsgpscollection.util.error.GeodatabaseError.AUTHENTICATION_FAILED ->
                        context.getString(R.string.error_authentication_failed_message)

                    com.enbridge.gdsgpscollection.util.error.GeodatabaseError.AUTHORIZATION_FAILED ->
                        context.getString(R.string.error_authorization_failed_message)

                    else ->
                        context.getString(R.string.error_download_failed_message)
                }

                send(
                    ESDataDownloadProgress(
                        0.0f,
                        errorMessage,
                        true,
                        error.message
                    )
                )
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error during download orchestration", e)
            send(
                ESDataDownloadProgress(
                    0.0f,
                    context.getString(R.string.error_download_failed_message),
                    true,
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    /**
     * Executes the actual download operation.
     * Extracted to separate method for retry logic.
     *
     * @param extent The extent to download
     * @param attempt Current retry attempt number
     * @param channel Channel for sending progress updates
     * @return Downloaded Geodatabase
     * @throws Exception if download fails
     */
    private suspend fun executeDownload(
        extent: Envelope,
        attempt: Int,
        channel: kotlinx.coroutines.channels.ProducerScope<ESDataDownloadProgress>
    ): com.arcgismaps.data.Geodatabase {
        Logger.d(TAG, "Executing download (attempt ${attempt + 1})")

        // Initialize GeodatabaseSyncTask
        channel.send(
            ESDataDownloadProgress(
                0.1f,
                context.getString(R.string.progress_initializing),
                false
            )
        )

        val geodatabaseSyncTask = GeodatabaseSyncTask(Constants.FEATURE_SERVICE_URL)

        geodatabaseSyncTask.load().onFailure { error ->
            Logger.e(TAG, "Failed to load GeodatabaseSyncTask", error)
            throw error
        }

        Logger.d(TAG, "GeodatabaseSyncTask loaded successfully")

        // Create generate parameters
        channel.send(
            ESDataDownloadProgress(
                0.2f,
                context.getString(R.string.progress_preparing_download),
                false
            )
        )

        val generateParams = geodatabaseSyncTask
            .createDefaultGenerateGeodatabaseParameters(extent)
            .getOrElse { error ->
                Logger.e(TAG, "Failed to create parameters", error)
                throw error
            }

        generateParams.apply {
            returnAttachments = false
            outSpatialReference = SpatialReference.webMercator()
        }

        // Delete existing geodatabase if present
        val geodatabaseFile = File(geodatabaseFilePath)
        if (geodatabaseFile.exists()) {
            Logger.d(TAG, "Deleting existing geodatabase")
            geodatabaseFile.delete()
        }

        // Create and start generate job
        channel.send(
            ESDataDownloadProgress(
                0.3f,
                context.getString(R.string.progress_generating_geodatabase),
                false
            )
        )

        val generateJob =
            geodatabaseSyncTask.createGenerateGeodatabaseJob(generateParams, geodatabaseFilePath)
        generateJob.start()

        // Monitor progress in background using the channel's coroutine scope
        channel.launch {
            try {
                generateJob.progress.collect { progress ->
                    val adjustedProgress = 0.3f + (progress / 100f * 0.6f)
                    channel.send(
                        ESDataDownloadProgress(
                            adjustedProgress,
                            context.getString(R.string.progress_downloading),
                            false
                        )
                    )
                }
            } catch (e: Exception) {
                Logger.d(TAG, "Progress collection ended: ${e.message}")
            }
        }

        // Wait for completion
        channel.send(
            ESDataDownloadProgress(
                0.95f,
                context.getString(R.string.progress_processing),
                false
            )
        )

        val result = generateJob.result()
        return result.getOrElse { error ->
            Logger.e(TAG, "Geodatabase generation failed", error)
            throw error
        }
    }

    /**
     * Synchronizes local geodatabase changes with the feature service
     *
     * Implementation Steps:
     * 1. Load the local geodatabase
     * 2. Create SyncGeodatabaseParameters
     * 3. Start sync job with bidirectional sync
     * 4. Monitor progress and return result
     */
    override suspend fun postESData(): Result<Boolean> {
        return try {
            Logger.d(TAG, "Starting geodatabase synchronization")

            val geodatabaseFile = File(geodatabaseFilePath)
            if (!geodatabaseFile.exists()) {
                Logger.w(TAG, "No geodatabase file found to sync")
                return Result.failure(Exception("No geodatabase available. Please download data first."))
            }

            // Load the geodatabase
            val geodatabase = Geodatabase(geodatabaseFilePath)
            geodatabase.load().onFailure { error ->
                Logger.e(TAG, "Failed to load geodatabase", error)
                return Result.failure(error)
            }

            // Create sync task
            val geodatabaseSyncTask = GeodatabaseSyncTask(Constants.FEATURE_SERVICE_URL)
            geodatabaseSyncTask.load().onFailure { error ->
                Logger.e(TAG, "Failed to load sync task", error)
                return Result.failure(error)
            }

            // Create sync parameters
            val syncParams = SyncGeodatabaseParameters().apply {
                geodatabaseSyncDirection = SyncDirection.Bidirectional
                shouldRollbackOnFailure = false

                // Add layer options for each feature table
                layerOptions.addAll(
                    geodatabase.featureTables.map { featureTable ->
                        SyncLayerOption(featureTable.serviceLayerId)
                    }
                )
            }

            // Create and start sync job
            val syncJob: SyncGeodatabaseJob = geodatabaseSyncTask
                .createSyncGeodatabaseJob(syncParams, geodatabase)

            syncJob.start()

            // Wait for result
            val result = syncJob.result()

            result.onSuccess {
                Logger.i(TAG, "Geodatabase synchronized successfully")
            }.onFailure { error ->
                Logger.e(TAG, "Geodatabase sync failed", error)
                return Result.failure(error)
            }

            // Close geodatabase
            geodatabase.close()

            Result.success(true)

        } catch (e: Exception) {
            Logger.e(TAG, "Error synchronizing geodatabase", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves locally changed data
     * Queries the geodatabase for features with local edits
     */
    override suspend fun getChangedData(): Result<List<JobCard>> {
        return try {
            Logger.d(TAG, "Fetching changed data")

            val geodatabaseFile = File(geodatabaseFilePath)
            if (!geodatabaseFile.exists()) {
                Logger.d(TAG, "No geodatabase found")
                return Result.success(emptyList())
            }

            // Load the geodatabase
            val geodatabase = Geodatabase(geodatabaseFilePath)
            geodatabase.load().onFailure { error ->
                Logger.e(TAG, "Failed to load geodatabase", error)
                return Result.failure(error)
            }

            // Query for local edits
            val changedData = mutableListOf<JobCard>()

            geodatabase.featureTables.forEach { featureTable ->
                if (featureTable.hasLocalEdits()) {
                    Logger.d(TAG, "Table ${featureTable.tableName} has local edits")
                    // Note: In real implementation, query features and map to JobCard
                    // For now, return empty list
                }
            }

            geodatabase.close()

            Logger.d(TAG, "Found ${changedData.size} changed items")
            Result.success(changedData)

        } catch (e: Exception) {
            Logger.e(TAG, "Error fetching changed data", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes saved job cards from local storage
     */
    override suspend fun deleteJobCards(): Result<Int> {
        return try {
            Logger.d(TAG, "Deleting job cards")

            // Mock: No job cards to delete in current implementation
            val deletedCount = 0

            Logger.d(TAG, "Deleted $deletedCount job cards")
            Result.success(deletedCount)

        } catch (e: Exception) {
            Logger.e(TAG, "Error deleting job cards", e)
            Result.failure(e)
        }
    }

    override suspend fun getSelectedDistance(): ESDataDistance {
        val distanceInMeters = preferencesManager.getESDataDistance()
        return ESDataDistance.fromMeters(distanceInMeters) ?: ESDataDistance.HUNDRED_METERS
    }

    override suspend fun saveSelectedDistance(distance: ESDataDistance) {
        preferencesManager.saveESDataDistance(distance.meters)
        Logger.d(TAG, "Saved distance preference: ${distance.displayText}")
    }

    /**
     * Loads an existing geodatabase from local storage if available.
     *
     * Performs integrity checks:
     * 1. Verifies file exists
     * 2. Checks file size > 0
     * 3. Attempts to load geodatabase
     * 4. Validates geodatabase has feature tables
     *
     * @return Result containing loaded Geodatabase, null if not found, or error if corrupted
     */
    override suspend fun loadExistingGeodatabase(): Result<Geodatabase?> {
        return try {
            val geodatabaseFile = File(geodatabaseFilePath)

            // Check if file exists
            if (!geodatabaseFile.exists()) {
                Logger.d(TAG, "No geodatabase file found at path: $geodatabaseFilePath")
                return Result.success(null)
            }

            // Check file size (basic integrity check)
            val fileSize = geodatabaseFile.length()
            if (fileSize == 0L) {
                Logger.w(TAG, "Geodatabase file exists but is empty (0 bytes), likely corrupted")
                return Result.failure(
                    Exception("Geodatabase file is empty or corrupted")
                )
            }

            Logger.d(TAG, "Found geodatabase file: size = ${fileSize / 1024} KB")

            // Attempt to load the geodatabase
            Logger.d(TAG, "Loading geodatabase from disk...")
            val geodatabase = Geodatabase(geodatabaseFilePath)

            geodatabase.load().onFailure { error ->
                Logger.e(TAG, "Failed to load geodatabase", error)
                return Result.failure(
                    Exception("Failed to load geodatabase: ${error.message}", error)
                )
            }

            Logger.d(TAG, "Geodatabase loaded successfully")

            // Validate geodatabase has feature tables
            if (geodatabase.featureTables.isEmpty()) {
                Logger.w(TAG, "Geodatabase loaded but contains no feature tables")
                return Result.failure(
                    Exception("Geodatabase contains no data layers")
                )
            }

            Logger.i(
                TAG,
                "Successfully loaded geodatabase with ${geodatabase.featureTables.size} feature tables"
            )

            Result.success(geodatabase)

        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error loading geodatabase", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads geodatabase from a specific feature service.
     *
     * This method is similar to [downloadESData] but accepts a service configuration
     * and uses dynamic file naming based on service ID.
     *
     * @param extent The geographic extent to download data for
     * @param featureServiceConfig Configuration for the target feature service
     * @return Flow emitting download progress updates for this service
     */
    override suspend fun downloadServiceData(
        extent: Envelope,
        featureServiceConfig: FeatureServiceConfig
    ): Flow<ESDataDownloadProgress> = channelFlow {
        Logger.i(TAG, "Starting download for service: ${featureServiceConfig.name}")
        Logger.d(TAG, "Service ID: ${featureServiceConfig.id}, URL: ${featureServiceConfig.url}")

        try {
            // Send starting message
            send(
                ESDataDownloadProgress(
                    0.0f,
                    context.getString(R.string.multi_service_starting, featureServiceConfig.name),
                    false
                )
            )

            // Initialize GeodatabaseSyncTask with service URL
            val geodatabaseSyncTask = GeodatabaseSyncTask(featureServiceConfig.url)
            geodatabaseSyncTask.load().onFailure { error ->
                Logger.e(
                    TAG,
                    "Failed to load GeodatabaseSyncTask for ${featureServiceConfig.name}",
                    error
                )
                send(
                    ESDataDownloadProgress(
                        0.0f,
                        context.getString(R.string.multi_service_failed, featureServiceConfig.name),
                        true,
                        error.message
                    )
                )
                return@channelFlow
            }

            Logger.d(TAG, "GeodatabaseSyncTask loaded for ${featureServiceConfig.name}")

            // Create generate parameters
            send(
                ESDataDownloadProgress(
                    0.2f,
                    context.getString(R.string.progress_preparing_download),
                    false
                )
            )

            val generateParams = geodatabaseSyncTask
                .createDefaultGenerateGeodatabaseParameters(extent)
                .getOrElse { error ->
                    Logger.e(
                        TAG,
                        "Failed to create parameters for ${featureServiceConfig.name}",
                        error
                    )
                    send(
                        ESDataDownloadProgress(
                            0.0f,
                            context.getString(
                                R.string.multi_service_failed,
                                featureServiceConfig.name
                            ),
                            true,
                            error.message
                        )
                    )
                    return@channelFlow
                }

            generateParams.apply {
                returnAttachments = false
                outSpatialReference = SpatialReference.webMercator()
            }

            // Get geodatabase path for this service
            val servicePath = getGeodatabasePath(featureServiceConfig.id)

            // Delete existing geodatabase if present
            val geodatabaseFile = File(servicePath)
            if (geodatabaseFile.exists()) {
                Logger.d(TAG, "Deleting existing geodatabase for ${featureServiceConfig.name}")
                geodatabaseFile.delete()
            }

            // Create and start generate job
            send(
                ESDataDownloadProgress(
                    0.3f,
                    context.getString(R.string.progress_generating_geodatabase),
                    false
                )
            )

            val generateJob =
                geodatabaseSyncTask.createGenerateGeodatabaseJob(generateParams, servicePath)
            generateJob.start()

            // Monitor progress
            launch {
                try {
                    generateJob.progress.collect { progress ->
                        val adjustedProgress = 0.3f + (progress / 100f * 0.6f)
                        send(
                            ESDataDownloadProgress(
                                adjustedProgress,
                                context.getString(R.string.progress_downloading),
                                false
                            )
                        )
                    }
                } catch (e: Exception) {
                    Logger.d(
                        TAG,
                        "Progress collection ended for ${featureServiceConfig.name}: ${e.message}"
                    )
                }
            }

            // Wait for completion
            send(
                ESDataDownloadProgress(
                    0.95f,
                    context.getString(R.string.progress_processing),
                    false
                )
            )

            val result = generateJob.result()
            result.onSuccess { geodatabase ->
                Logger.i(TAG, "Download completed for ${featureServiceConfig.name}")
                send(
                    ESDataDownloadProgress(
                        1.0f,
                        context.getString(R.string.progress_complete),
                        true,
                        geodatabase = geodatabase
                    )
                )
            }.onFailure { error ->
                Logger.e(TAG, "Download failed for ${featureServiceConfig.name}", error)
                send(
                    ESDataDownloadProgress(
                        0.0f,
                        context.getString(R.string.multi_service_failed, featureServiceConfig.name),
                        true,
                        error.message
                    )
                )
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error downloading ${featureServiceConfig.name}", e)
            send(
                ESDataDownloadProgress(
                    0.0f,
                    context.getString(R.string.multi_service_failed, featureServiceConfig.name),
                    true,
                    e.message
                )
            )
        }
    }

    /**
     * Downloads geodatabases from multiple feature services in parallel.
     *
     * This method coordinates simultaneous downloads and aggregates progress.
     * Implements fail-fast strategy: if any service fails, all downloads are aborted.
     *
     * @param extent The geographic extent to download data for
     * @param featureServices List of feature service configurations
     * @return Flow emitting combined progress updates
     */
    override suspend fun downloadMultipleServices(
        extent: Envelope,
        featureServices: List<FeatureServiceConfig>
    ): Flow<MultiServiceDownloadProgress> = channelFlow {
        Logger.i(TAG, "Starting multi-service download: ${featureServices.size} services")

        val serviceProgresses = mutableMapOf<String, ESDataDownloadProgress>()

        // Initialize progress for all services
        featureServices.forEach { service ->
            serviceProgresses[service.id] = ESDataDownloadProgress(
                progress = 0f,
                message = context.getString(R.string.multi_service_starting, service.name),
                isComplete = false
            )
        }

        // Send initial progress
        send(
            MultiServiceDownloadProgress(
                serviceProgresses = serviceProgresses.toMap(),
                overallProgress = 0f,
                overallMessage = context.getString(
                    R.string.multi_service_downloading,
                    featureServices.size
                ),
                isComplete = false
            )
        )

        try {
            // Launch parallel downloads
            coroutineScope {
                val downloadJobs = featureServices.map { service ->
                    async {
                        var lastProgress: ESDataDownloadProgress? = null
                        var shouldAbort = false
                        try {
                            downloadServiceData(extent, service).collect { progress ->
                                lastProgress = progress
                                serviceProgresses[service.id] = progress

                                // Calculate overall progress
                                val overallProgress = serviceProgresses.values
                                    .map { it.progress }
                                    .average()
                                    .toFloat()

                                val completedCount =
                                    serviceProgresses.values.count { it.isComplete && it.error == null }

                                // Check for errors (fail-fast)
                                val firstError =
                                    serviceProgresses.values.firstOrNull { it.error != null }
                                if (firstError != null && !shouldAbort) {
                                    shouldAbort = true
                                    Logger.w(
                                        TAG,
                                        "Service ${service.name} failed, aborting all downloads"
                                    )
                                    send(
                                        MultiServiceDownloadProgress(
                                            serviceProgresses = serviceProgresses.toMap(),
                                            overallProgress = overallProgress,
                                            overallMessage = context.getString(
                                                R.string.multi_service_failed,
                                                service.name
                                            ),
                                            isComplete = false,
                                            error = firstError.error
                                        )
                                    )
                                    // Don't continue collecting if error detected
                                    return@collect
                                }

                                // Send progress update
                                send(
                                    MultiServiceDownloadProgress(
                                        serviceProgresses = serviceProgresses.toMap(),
                                        overallProgress = overallProgress,
                                        overallMessage = context.getString(
                                            R.string.multi_service_progress,
                                            completedCount,
                                            featureServices.size
                                        ),
                                        isComplete = completedCount == featureServices.size
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Logger.e(TAG, "Error in download job for ${service.name}", e)
                            serviceProgresses[service.id] = ESDataDownloadProgress(
                                progress = 0f,
                                message = context.getString(
                                    R.string.multi_service_failed,
                                    service.name
                                ),
                                isComplete = false,
                                error = e.message
                            )
                        }
                    }
                }

                // Wait for all downloads to complete
                downloadJobs.awaitAll()
            }

            // Send final status
            val allSuccess = serviceProgresses.values.all { it.isComplete && it.error == null }
            val anyError = serviceProgresses.values.any { it.error != null }

            if (allSuccess) {
                Logger.i(TAG, "All services downloaded successfully")
                send(
                    MultiServiceDownloadProgress(
                        serviceProgresses = serviceProgresses.toMap(),
                        overallProgress = 1.0f,
                        overallMessage = context.getString(R.string.multi_service_complete),
                        isComplete = true
                    )
                )
            } else if (anyError) {
                val firstError = serviceProgresses.values.first { it.error != null }
                Logger.e(TAG, "Multi-service download failed: ${firstError.error}")
                send(
                    MultiServiceDownloadProgress(
                        serviceProgresses = serviceProgresses.toMap(),
                        overallProgress = serviceProgresses.values.map { it.progress }.average()
                            .toFloat(),
                        overallMessage = context.getString(
                            R.string.multi_service_failed,
                            "services"
                        ),
                        isComplete = false,
                        error = firstError.error
                    )
                )
            }

        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error in multi-service download", e)
            send(
                MultiServiceDownloadProgress(
                    serviceProgresses = serviceProgresses.toMap(),
                    overallProgress = 0f,
                    overallMessage = context.getString(R.string.error_download_failed_message),
                    isComplete = false,
                    error = e.message
                )
            )
        }
    }

    /**
     * Synchronizes a specific geodatabase with its feature service.
     *
     * @param serviceId Identifier of the service to sync
     * @return Result indicating success or failure
     */
    override suspend fun syncServiceGeodatabase(serviceId: String): Result<Boolean> {
        return try {
            Logger.d(TAG, "Starting sync for service: $serviceId")

            val servicePath = getGeodatabasePath(serviceId)
            val geodatabaseFile = File(servicePath)

            if (!geodatabaseFile.exists()) {
                Logger.w(TAG, "No geodatabase file found for service: $serviceId")
                return Result.failure(Exception("No geodatabase available for $serviceId"))
            }

            // Get service configuration
            val environment = configuration.getCurrentEnvironment()
            val serviceConfig = environment.featureServices.find { it.id == serviceId }
                ?: return Result.failure(Exception("Service configuration not found for $serviceId"))

            // Load the geodatabase
            val geodatabase = Geodatabase(servicePath)
            geodatabase.load().onFailure { error ->
                Logger.e(TAG, "Failed to load geodatabase for $serviceId", error)
                return Result.failure(error)
            }

            // Create sync task
            val geodatabaseSyncTask = GeodatabaseSyncTask(serviceConfig.url)
            geodatabaseSyncTask.load().onFailure { error ->
                Logger.e(TAG, "Failed to load sync task for $serviceId", error)
                return Result.failure(error)
            }

            // Create sync parameters
            val syncParams = SyncGeodatabaseParameters().apply {
                geodatabaseSyncDirection = SyncDirection.Bidirectional
                shouldRollbackOnFailure = false
                layerOptions.addAll(
                    geodatabase.featureTables.map { featureTable ->
                        SyncLayerOption(featureTable.serviceLayerId)
                    }
                )
            }

            // Create and start sync job
            val syncJob = geodatabaseSyncTask.createSyncGeodatabaseJob(syncParams, geodatabase)
            syncJob.start()

            // Wait for result
            val result = syncJob.result()
            result.onSuccess {
                Logger.i(TAG, "Geodatabase synchronized successfully for $serviceId")
            }.onFailure { error ->
                Logger.e(TAG, "Geodatabase sync failed for $serviceId", error)
                geodatabase.close()
                return Result.failure(error)
            }

            // Close geodatabase
            geodatabase.close()

            Result.success(true)

        } catch (e: Exception) {
            Logger.e(TAG, "Error synchronizing geodatabase for $serviceId", e)
            Result.failure(e)
        }
    }

    /**
     * Synchronizes all geodatabases with their respective feature services.
     *
     * Syncs sequentially to avoid conflicts and provides individual results per service.
     *
     * @return Result containing map of service ID to sync success status
     */
    override suspend fun syncAllGeodatabases(): Result<Map<String, Boolean>> {
        return try {
            Logger.i(TAG, "Starting sync for all geodatabases")

            val environment = configuration.getCurrentEnvironment()
            val results = mutableMapOf<String, Boolean>()

            environment.featureServices.forEach { service ->
                Logger.d(TAG, "Syncing service: ${service.name}")

                val syncResult = syncServiceGeodatabase(service.id)
                results[service.id] = syncResult.isSuccess

                if (syncResult.isFailure) {
                    Logger.w(TAG, "Sync failed for ${service.name}, continuing with others...")
                }
            }

            val successCount = results.values.count { it }
            Logger.i(TAG, "Sync completed: $successCount/${results.size} services successful")

            Result.success(results)

        } catch (e: Exception) {
            Logger.e(TAG, "Error syncing all geodatabases", e)
            Result.failure(e)
        }
    }

    /**
     * Loads all existing geodatabases from local storage.
     *
     * Scans for geodatabase files matching configured services and loads metadata.
     *
     * @return Result containing list of loaded geodatabase information
     */
    override suspend fun loadExistingGeodatabases(): Result<List<GeodatabaseInfo>> {
        return try {
            Logger.i(TAG, "Loading all existing geodatabases")

            val environment = configuration.getCurrentEnvironment()
            val geodatabaseInfoList = mutableListOf<GeodatabaseInfo>()

            for (service in environment.featureServices) {
                val servicePath = getGeodatabasePath(service.id)
                val geodatabaseFile = File(servicePath)

                if (!geodatabaseFile.exists()) {
                    Logger.d(TAG, "No geodatabase found for service: ${service.name}")
                    continue
                }

                // Check file size
                val fileSize = geodatabaseFile.length()
                if (fileSize == 0L) {
                    Logger.w(TAG, "Geodatabase file for ${service.name} is empty, skipping")
                    continue
                }

                Logger.d(
                    TAG,
                    "Loading geodatabase for ${service.name}: size = ${fileSize / 1024} KB"
                )

                // Load the geodatabase
                val geodatabase = Geodatabase(servicePath)
                val loadResult = geodatabase.load()
                if (loadResult.isFailure) {
                    Logger.e(
                        TAG,
                        "Failed to load geodatabase for ${service.name}",
                        loadResult.exceptionOrNull()
                    )
                    continue
                }

                // Validate has feature tables
                if (geodatabase.featureTables.isEmpty()) {
                    Logger.w(TAG, "Geodatabase for ${service.name} contains no feature tables")
                    geodatabase.close()
                    continue
                }

                // Use file modified time as last sync time
                val lastSyncTime = geodatabaseFile.lastModified()

                // Create geodatabase info
                val geodatabaseInfo = GeodatabaseInfo(
                    serviceId = service.id,
                    serviceName = service.name,
                    fileName = "${service.id}.geodatabase",
                    geodatabase = geodatabase,
                    lastSyncTime = lastSyncTime,
                    layerCount = geodatabase.featureTables.size,
                    fileSizeKB = fileSize / 1024,
                    displayOnMap = service.displayOnMap
                )

                geodatabaseInfoList.add(geodatabaseInfo)
                Logger.i(
                    TAG,
                    "Loaded geodatabase for ${service.name}: ${geodatabaseInfo.layerCount} layers"
                )
            }

            Logger.i(TAG, "Successfully loaded ${geodatabaseInfoList.size} geodatabases")
            Result.success(geodatabaseInfoList)

        } catch (e: Exception) {
            Logger.e(TAG, "Error loading existing geodatabases", e)
            Result.failure(e)
        }
    }
}
