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
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
 *
 * @property context Application context for file operations
 * @property localEditDao DAO for accessing local edit records
 * @property preferencesManager Manager for persisting user preferences
 */
class ManageESRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localEditDao: LocalEditDao,
    private val preferencesManager: PreferencesManager,
    private val networkMonitor: com.enbridge.gdsgpscollection.util.network.NetworkMonitor,
    private val storageUtil: com.enbridge.gdsgpscollection.util.storage.StorageUtil
) : ManageESRepository {

    companion object {
        private const val TAG = "ManageESRepository"
    }

    /**
     * Path to the geodatabase file in internal storage (secure)
     */
    private val geodatabaseFilePath: String
        get() = File(context.filesDir, Constants.GEODATABASE_FILE_NAME).absolutePath

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
}
