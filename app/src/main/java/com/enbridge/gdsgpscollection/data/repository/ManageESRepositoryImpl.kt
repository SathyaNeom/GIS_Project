package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */

import android.content.Context
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.Envelope
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
import com.enbridge.gdsgpscollection.util.error.ErrorMapper
import com.enbridge.gdsgpscollection.util.error.GeodatabaseError.*
import com.enbridge.gdsgpscollection.util.network.RetryStrategy
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
     * Features whose geometry intersects the extent are downloaded with their complete
     * (non-clipped) geometry, ensuring users see full features even if portions extend
     * beyond the selected distance boundary.
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
            val retryStrategy = RetryStrategy.DEFAULT

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
                val errorType = ErrorMapper.mapError(error)
                val errorMessage = when (errorType) {
                    NO_INTERNET ->
                        context.getString(R.string.error_no_internet_message)

                    SERVER_UNAVAILABLE ->
                        context.getString(R.string.error_server_unavailable_message)

                    SERVER_TIMEOUT ->
                        context.getString(R.string.error_server_timeout_message)

                    AUTHENTICATION_FAILED ->
                        context.getString(R.string.error_authentication_failed_message)

                    AUTHORIZATION_FAILED ->
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
     * **Full Feature Download Strategy:**
     * Any feature whose geometry intersects the extent boundary (even if just touching)
     * will be downloaded with its COMPLETE geometry (not clipped to extent). This ensures
     * users see full features without visual artifacts.
     *
     * **Performance Consideration:**
     * Large features (e.g., 10km pipelines) intersecting a small extent (e.g., 500m) will
     * download their entire geometry, resulting in:
     * - Larger file sizes than extent-based area calculation
     * - Longer download times for features extending far beyond extent
     * - Increased storage usage
     *
     * This trade-off prioritizes data completeness and user experience over file optimization.
     *
     * **Progress Calculation Strategy:**
     * This method implements a smooth, monotonically increasing progress display from 0% to 100%
     * by mapping different download phases to specific progress ranges:
     *
     * - **0-5%**: Pre-flight connectivity check
     * - **5-10%**: Storage validation
     * - **10-15%**: Task initialization and parameter creation
     * - **15-95%**: Actual geodatabase generation (mapped from ArcGIS job's 0-100% progress)
     * - **95-100%**: Post-processing and finalization
     *
     * **Race Condition Prevention:**
     * The ArcGIS `GenerateGeodatabaseJob.progress` Flow emits values asynchronously in a background
     * coroutine. Without proper cancellation, these emissions can continue AFTER we've moved to the
     * post-processing phase, causing progress to jump backwards (e.g., 95% → 32% → 33%).
     *
     * To prevent this, we:
     * 1. Store the progress monitoring Job reference
     * 2. Cancel it explicitly before sending the 95% "processing" message
     * 3. Ensure all subsequent progress updates are deterministic
     *
     * **Granular Progress Messages:**
     * During the main download phase (15-95%), we provide contextual feedback by mapping
     * job progress ranges to specific operations:
     * - 0-30%: Downloading features
     * - 30-60%: Downloading attributes
     * - 60-90%: Downloading geometry
     * - 90-100%: Finalizing data structures
     *
     * This enhances UX by showing users what's happening at each stage, rather than a
     * generic "Downloading data…" message throughout the entire operation.
     *
     * @param extent The geographic extent to download data for
     * @param attempt Current retry attempt number (0-indexed)
     * @param channel ProducerScope for emitting progress updates to the Flow
     * @return Successfully downloaded Geodatabase instance
     * @throws Exception if download fails (network errors, server errors, etc.)
     */
    private suspend fun executeDownload(
        extent: Envelope,
        attempt: Int,
        channel: kotlinx.coroutines.channels.ProducerScope<ESDataDownloadProgress>
    ): com.arcgismaps.data.Geodatabase {
        Logger.d(TAG, "Executing download (attempt ${attempt + 1})")

        // ========== PHASE 1: Initialize GeodatabaseSyncTask (10-15%) ==========
        channel.send(
            ESDataDownloadProgress(
                0.1f,
                context.getString(R.string.progress_initializing),
                false
            )
        )
        Logger.d(TAG, "Progress: 10% - Initializing GeodatabaseSyncTask")

        val geodatabaseSyncTask = GeodatabaseSyncTask(Constants.FEATURE_SERVICE_URL)

        geodatabaseSyncTask.load().onFailure { error ->
            Logger.e(TAG, "Failed to load GeodatabaseSyncTask", error)
            throw error
        }

        Logger.d(TAG, "GeodatabaseSyncTask loaded successfully")

        // ========== PHASE 2: Create Generate Parameters (15%) ==========
        channel.send(
            ESDataDownloadProgress(
                0.15f,
                context.getString(R.string.progress_connecting),
                false
            )
        )
        Logger.d(TAG, "Progress: 15% - Creating geodatabase parameters")

        val generateParams = geodatabaseSyncTask
            .createDefaultGenerateGeodatabaseParameters(extent)
            .getOrElse { error ->
                Logger.e(TAG, "Failed to create parameters", error)
                throw error
            }

        // IMPORTANT: Full Feature Geometry Download Configuration
        //
        // The extent parameter passed to createDefaultGenerateGeodatabaseParameters(extent)
        // instructs the ArcGIS Server to:
        // 1. Identify features whose geometry intersects the extent (including features that only touch the boundary)
        // 2. Download COMPLETE (non-clipped) geometries for all intersecting features
        //
        // Feature Selection Behavior:
        // - Points: Downloaded if point is within extent
        // - Lines: Downloaded if ANY segment intersects extent → FULL line geometry returned (not clipped)
        //   Example: 5km pipeline with 1km intersecting extent → entire 5km geometry downloaded
        // - Polygons: Downloaded if ANY part intersects or touches extent → FULL polygon geometry returned (not clipped)
        //
        // Performance Trade-off:
        // This approach prioritizes feature completeness over file size optimization.
        // Users see complete features even if large portions extend beyond the selected distance.
        //
        // CAUTION: Large features intersecting small extents will significantly increase:
        // - Download time (entire feature geometry transferred)
        // - File size (full geometries stored)
        // - Storage requirements (may exceed extent-based estimates)
        //
        // Server Requirements:
        // - The server must have "Extract" capability enabled
        // - The server determines which features intersect based on extent
        // - returnGeometry parameter should be true (default) to include full geometries
        //
        // Reference: https://github.com/Esri/arcgis-maps-sdk-kotlin-samples

        generateParams.apply {
            returnAttachments = false
            outSpatialReference = SpatialReference.webMercator()
            // returnGeometry defaults to true, ensuring full geometries are downloaded
        }

        // Delete existing geodatabase if present
        val geodatabaseFile = File(geodatabaseFilePath)
        if (geodatabaseFile.exists()) {
            Logger.d(TAG, "Deleting existing geodatabase before download")
            geodatabaseFile.delete()
        }

        // ========== PHASE 3: Start Generate Job and Monitor Progress (15-95%) ==========
        Logger.d(TAG, "Progress: Starting geodatabase generation job")

        val generateJob =
            geodatabaseSyncTask.createGenerateGeodatabaseJob(generateParams, geodatabaseFilePath)
        generateJob.start()

        // Track the last emitted progress to ensure monotonic increase
        var lastEmittedProgress = 0.15f

        /**
         * Monitor progress in a separate coroutine.
         *
         * CRITICAL: We store the Job reference to cancel it before moving to post-processing.
         * Without cancellation, this coroutine continues emitting stale progress values that
         * can overwrite the 95% "processing" message, causing the UI to jump backwards.
         *
         * Progress Mapping Strategy:
         * - ArcGIS job progress: 0-100 (from GenerateGeodatabaseJob)
         * - Mapped to UI progress: 15-95% (80% of total progress range)
         * - Formula: adjustedProgress = 0.15 + (jobProgress / 100.0 * 0.80)
         *
         * This ensures:
         * 1. Progress starts at 15% (after pre-flight checks)
         * 2. Progress ends at 95% (before post-processing)
         * 3. Smooth linear progression throughout the download
         */
        val progressMonitoringJob = channel.launch {
            try {
                generateJob.progress.collect { jobProgress ->
                    // Map job progress (0-100) to UI progress range (15-95%)
                    val adjustedProgress = 0.15f + (jobProgress / 100f * 0.80f)

                    // Ensure monotonic increase (prevent backwards jumps due to async updates)
                    if (adjustedProgress > lastEmittedProgress) {
                        lastEmittedProgress = adjustedProgress

                        // Provide granular contextual messages based on download phase
                        val progressMessage = when {
                            jobProgress < 30 -> context.getString(R.string.progress_downloading_features)
                            jobProgress < 60 -> context.getString(R.string.progress_downloading_attributes)
                            jobProgress < 90 -> context.getString(R.string.progress_downloading_geometry)
                            else -> context.getString(R.string.progress_finalizing)
                        }

                        channel.send(
                            ESDataDownloadProgress(
                                adjustedProgress,
                                progressMessage,
                                false
                            )
                        )

                        Logger.v(
                            TAG,
                            "Progress: ${(adjustedProgress * 100).toInt()}% (Job: $jobProgress%) - $progressMessage"
                        )
                    }
                }
            } catch (e: Exception) {
                // Expected when job completes or is cancelled
                Logger.d(TAG, "Progress monitoring ended: ${e.message}")
            }
        }

        // Wait for the generate job to complete
        val result = generateJob.result()

        // ========== CRITICAL: Cancel Progress Monitoring Before Post-Processing ==========
        /**
         * Cancel the progress monitoring coroutine BEFORE sending the "processing" message.
         *
         * Rationale:
         * The `generateJob.progress` Flow may continue emitting values even after `result()`
         * returns, due to internal buffering and async execution. If we don't cancel the
         * monitoring job, late-arriving progress updates (e.g., 32%, 33%) can overwrite
         * the 95% "processing" message we're about to send, causing confusing UI jumps.
         *
         * By cancelling here, we guarantee:
         * 1. No more progress updates from the monitoring coroutine
         * 2. Clean transition to post-processing phase
         * 3. Deterministic progress sequence: 15% → ... → 94% → 95% → 100%
         */
        progressMonitoringJob.cancel()
        Logger.d(
            TAG,
            "Progress monitoring cancelled - preventing race condition with post-processing phase"
        )

        // ========== PHASE 4: Post-Processing (95-100%) ==========
        channel.send(
            ESDataDownloadProgress(
                0.95f,
                context.getString(R.string.progress_processing),
                false
            )
        )
        Logger.d(TAG, "Progress: 95% - Processing geodatabase")

        // Get the geodatabase or throw if generation failed
        val geodatabase = result.getOrElse { error ->
            Logger.e(TAG, "Geodatabase generation failed", error)
            throw error
        }

        // Load geodatabase to ensure it's ready for use
        geodatabase.load().onFailure { error ->
            Logger.e(TAG, "Failed to load geodatabase", error)
            throw error
        }
        Logger.i(
            TAG,
            "Geodatabase loaded with ${geodatabase.featureTables.size} feature tables - ready for use")

        return geodatabase
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
     * **Full Feature Download Strategy:**
     * Any feature whose geometry intersects the extent boundary (even if just touching)
     * will be downloaded with its COMPLETE geometry (not clipped to extent). This ensures
     * users see full features without visual artifacts.
     *
     * **Environment-Specific Behavior:**
     * In Project environment, only the Basemap service applies full-feature download logic
     * (as Basemap layers are displayed on map). Operations service follows default server
     * behavior (TOC-only display, may use server-side optimization).
     *
     * **Progress Calculation Strategy:**
     * Implements the same smooth progress tracking as [executeDownload]:
     * - **0-10%**: Pre-flight checks (implicit, handled by caller)
     * - **10-15%**: Task initialization
     * - **15-95%**: Actual download with granular progress messages
     * - **95-100%**: Post-processing
     *
     * **Multi-Service Context:**
     * When used in parallel downloads (Project environment), this method's progress
     * is aggregated with other service downloads to show combined overall progress.
     * Each service progresses independently from 0-100%, and the
     * [downloadMultipleServices] method calculates the average for display.
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
            // ========== PHASE 1: Initialize (0-15%) ==========
            send(
                ESDataDownloadProgress(
                    0.0f,
                    context.getString(R.string.multi_service_starting, featureServiceConfig.name),
                    false
                )
            )
            Logger.d(TAG, "Progress: 0% - Starting download for ${featureServiceConfig.name}")

            // Initialize GeodatabaseSyncTask with service URL
            send(
                ESDataDownloadProgress(
                    0.10f,
                    context.getString(R.string.progress_initializing),
                    false
                )
            )
            Logger.d(TAG, "Progress: 10% - Initializing task for ${featureServiceConfig.name}")

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
                    0.15f,
                    context.getString(R.string.progress_connecting),
                    false
                )
            )
            Logger.d(TAG, "Progress: 15% - Creating parameters for ${featureServiceConfig.name}")

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

            // Configure parameters for full-feature download (same as single-service download)
            // Note: In Project environment, this applies primarily to Basemap service (displayed on map)
            // Operations service (TOC-only) may follow different server-side behavior
            generateParams.apply {
                returnAttachments = false
                outSpatialReference = SpatialReference.webMercator()
                // returnGeometry defaults to true, ensuring full geometries are downloaded
            }

            // Get geodatabase path for this service
            val servicePath = getGeodatabasePath(featureServiceConfig.id)

            // Delete existing geodatabase if present
            val geodatabaseFile = File(servicePath)
            if (geodatabaseFile.exists()) {
                Logger.d(TAG, "Deleting existing geodatabase for ${featureServiceConfig.name}")
                geodatabaseFile.delete()
            }

            // ========== PHASE 2: Generate Job and Monitor Progress (15-95%) ==========
            Logger.d(
                TAG,
                "Progress: Starting geodatabase generation for ${featureServiceConfig.name}"
            )

            val generateJob =
                geodatabaseSyncTask.createGenerateGeodatabaseJob(generateParams, servicePath)
            generateJob.start()

            // Track last emitted progress for monotonic increase
            var lastEmittedProgress = 0.15f

            /**
             * Monitor progress with race condition prevention.
             * Same strategy as [executeDownload]: store Job reference for explicit cancellation.
             */
            val progressMonitoringJob = launch {
                try {
                    generateJob.progress.collect { jobProgress ->
                        // Map job progress (0-100) to UI progress range (15-95%)
                        val adjustedProgress = 0.15f + (jobProgress / 100f * 0.80f)

                        // Ensure monotonic increase
                        if (adjustedProgress > lastEmittedProgress) {
                            lastEmittedProgress = adjustedProgress

                            // Provide granular contextual messages
                            val progressMessage = when {
                                jobProgress < 30 -> context.getString(R.string.progress_downloading_features)
                                jobProgress < 60 -> context.getString(R.string.progress_downloading_attributes)
                                jobProgress < 90 -> context.getString(R.string.progress_downloading_geometry)
                                else -> context.getString(R.string.progress_finalizing)
                            }

                            send(
                                ESDataDownloadProgress(
                                    adjustedProgress,
                                    progressMessage,
                                    false
                                )
                            )

                            Logger.v(
                                TAG,
                                "${featureServiceConfig.name} Progress: ${(adjustedProgress * 100).toInt()}% (Job: $jobProgress%)"
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Expected when job completes or is cancelled
                    Logger.d(
                        TAG,
                        "Progress monitoring ended for ${featureServiceConfig.name}: ${e.message}"
                    )
                }
            }

            // Wait for the generate job to complete
            val result = generateJob.result()

            // ========== CRITICAL: Cancel Progress Monitoring Before Post-Processing ==========
            /**
             * Prevent race condition by canceling progress monitoring before post-processing.
             * This ensures no late-arriving progress updates can overwrite the 95% message.
             */
            progressMonitoringJob.cancel()
            Logger.d(
                TAG,
                "Progress monitoring cancelled for ${featureServiceConfig.name} - preventing race condition"
            )

            // ========== PHASE 3: Post-Processing (95-100%) ==========
            send(
                ESDataDownloadProgress(
                    0.95f,
                    context.getString(R.string.progress_processing),
                    false
                )
            )
            Logger.d(TAG, "Progress: 95% - Processing ${featureServiceConfig.name}")

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

    /**
     * Checks if any geodatabase has local edits that need to be synced.
     *
     * This method prevents data loss by detecting unsaved changes before destructive operations.
     * It iterates through all configured geodatabases (Wildfire: 1, Project: 2+) and checks
     * if any have local edits using the ArcGIS SDK's `Geodatabase.hasLocalEdits()` method.
     *
     * Implementation Strategy:
     * 1. Get current environment configuration (Wildfire or Project)
     * 2. For each configured service, check if geodatabase file exists
     * 3. Load the geodatabase and call `hasLocalEdits()`
     * 4. Return true immediately if ANY geodatabase has edits (fail-fast)
     * 5. Return false only if ALL geodatabases are clean or don't exist
     *
     * Error Handling:
     * - Geodatabase not found: Skip and continue (no changes to sync)
     * - Geodatabase load failure: Log error and continue (safe default behavior)
     * - Unexpected exception: Return failure with error details
     *
     * @return Result<Boolean> - true if unsaved changes exist, false otherwise
     */
    override suspend fun hasUnsyncedChanges(): Result<Boolean> {
        return try {
            Logger.i(TAG, "Checking for unsaved changes in geodatabase(s)")

            val environment = configuration.getCurrentEnvironment()
            var hasChanges = false

            // Iterate through all configured services
            for (service in environment.featureServices) {
                val servicePath = getGeodatabasePath(service.id)
                val geodatabaseFile = File(servicePath)

                // Skip if geodatabase doesn't exist (nothing to sync)
                if (!geodatabaseFile.exists()) {
                    Logger.d(TAG, "Geodatabase for ${service.name} does not exist, skipping")
                    continue
                }

                Logger.d(TAG, "Checking ${service.name} geodatabase for local edits")

                // Load geodatabase to check for edits
                val geodatabase = Geodatabase(servicePath)
                val loadResult = geodatabase.load()

                if (loadResult.isFailure) {
                    Logger.e(
                        TAG,
                        "Failed to load geodatabase for ${service.name}, skipping",
                        loadResult.exceptionOrNull()
                    )
                    geodatabase.close()
                    continue
                }

                // CRITICAL: Check for local edits using ArcGIS SDK method
                if (geodatabase.hasLocalEdits()) {
                    Logger.w(TAG, "Geodatabase ${service.name} has unsaved changes")
                    hasChanges = true
                    geodatabase.close()
                    break // Fail-fast: found changes, no need to check others
                } else {
                    Logger.d(TAG, "Geodatabase ${service.name} has no local edits")
                }

                geodatabase.close()
            }

            Logger.i(TAG, "Unsaved changes check complete: hasChanges=$hasChanges")
            Result.success(hasChanges)

        } catch (e: Exception) {
            Logger.e(TAG, "Error checking for unsaved changes", e)
            Result.failure(e)
        }
    }

    /**
     * Returns the count of geodatabase files matching configured services.
     *
     * Only counts files that match the current environment's service IDs to avoid
     * counting orphaned files from previous configurations or temporary files.
     *
     * Implementation:
     * 1. Get current environment configuration
     * 2. For each configured service, check if file exists
     * 3. Count existing files
     * 4. Return total count
     *
     * @return Count of geodatabase files (0 or positive integer)
     */
    override suspend fun getGeodatabaseFileCount(): Int {
        return try {
            Logger.d(TAG, "Counting geodatabase files")

            val environment = configuration.getCurrentEnvironment()
            var fileCount = 0

            for (service in environment.featureServices) {
                val servicePath = getGeodatabasePath(service.id)
                val geodatabaseFile = File(servicePath)

                if (geodatabaseFile.exists()) {
                    fileCount++
                    Logger.d(
                        TAG,
                        "Found geodatabase file: ${service.id}.geodatabase (${geodatabaseFile.length() / 1024}KB)"
                    )
                }
            }

            Logger.i(TAG, "Geodatabase file count: $fileCount")
            fileCount

        } catch (e: Exception) {
            Logger.e(TAG, "Error counting geodatabase files", e)
            0 // Return 0 on error (safe default)
        }
    }

    /**
     * Checks if existing geodatabases contain actual feature data.
     *
     * Validates that at least one feature table in at least one geodatabase
     * contains features. Empty geodatabases indicate failed or incomplete downloads.
     *
     * Implementation Strategy:
     * 1. Load all existing geodatabases
     * 2. For each geodatabase, iterate through feature tables
     * 3. Use queryFeatureCount() for O(1) metadata check (fast)
     * 4. Return true immediately if any table has features (fail-fast)
     * 5. Return false if all tables in all geodatabases are empty
     *
     * Performance:
     * Uses queryFeatureCount() instead of loading features, querying geodatabase
     * metadata rather than scanning actual data. This provides instant results
     * even for large geodatabases.
     *
     * @return Result<Boolean> - true if data exists, false if all empty
     */
    override suspend fun hasDataToLoad(): Result<Boolean> {
        return try {
            Logger.i(TAG, "Checking if geodatabases contain feature data")

            val environment = configuration.getCurrentEnvironment()
            var hasData = false

            for (service in environment.featureServices) {
                val servicePath = getGeodatabasePath(service.id)
                val geodatabaseFile = File(servicePath)

                // Skip non-existent files
                if (!geodatabaseFile.exists()) {
                    Logger.d(TAG, "Geodatabase ${service.id} does not exist, skipping")
                    continue
                }

                Logger.d(TAG, "Loading geodatabase ${service.id} to check data")

                val geodatabase = Geodatabase(servicePath)
                val loadResult = geodatabase.load()

                if (loadResult.isFailure) {
                    Logger.e(
                        TAG,
                        "Failed to load geodatabase ${service.id}",
                        loadResult.exceptionOrNull()
                    )
                    geodatabase.close()
                    continue
                }

                // Check each feature table for data
                for (featureTable in geodatabase.featureTables) {
                    try {
                        // Load table to access metadata
                        featureTable.load().getOrThrow()

                        // Query feature count using empty QueryParameters (gets all features count)
                        // This is an O(1) metadata operation that doesn't load actual features
                        val queryParameters = com.arcgismaps.data.QueryParameters().apply {
                            whereClause = "1=1" // Return all features
                        }
                        val featureCountResult = featureTable.queryFeatureCount(queryParameters)
                        val featureCount = featureCountResult.getOrElse { 0L }

                        if (featureCount > 0) {
                            Logger.i(
                                TAG,
                                "Found data: ${featureTable.tableName} has $featureCount features"
                            )
                            hasData = true
                            geodatabase.close()
                            // Fail-fast: found data, return immediately
                            return Result.success(true)
                        } else {
                            Logger.d(
                                TAG,
                                "Table ${featureTable.tableName} is empty (0 features)"
                            )
                        }
                        } catch (e: Exception) {
                        Logger.e(TAG, "Error checking table ${featureTable.tableName}", e)
                        // Continue checking other tables
                    }
                }

                geodatabase.close()
            }

            Logger.i(TAG, "Data check complete: hasData=$hasData")
            Result.success(hasData)

        } catch (e: Exception) {
            Logger.e(TAG, "Error checking for data in geodatabases", e)
            Result.failure(e)
        }
    }

    /**
     * Clears all geodatabase files silently (no UI notifications).
     *
     * Used for automatic cleanup operations:
     * - After "No Data" error (remove empty files)
     * - Recovery from corrupted state
     *
     * Implementation:
     * 1. Get current environment configuration
     * 2. For each service, delete geodatabase file if exists
     * 3. Count deleted files
     * 4. Log operations but don't trigger UI notifications
     *
     * @return Result<Int> - Count of deleted files
     */
    override suspend fun clearGeodatabases(): Result<Int> {
        return try {
            Logger.i(TAG, "Starting silent geodatabase clear operation")

            val environment = configuration.getCurrentEnvironment()
            var deletedCount = 0

            for (service in environment.featureServices) {
                val servicePath = getGeodatabasePath(service.id)
                val geodatabaseFile = File(servicePath)

                if (geodatabaseFile.exists()) {
                    val fileSize = geodatabaseFile.length() / 1024 // KB
                    val deleted = geodatabaseFile.delete()

                    if (deleted) {
                        deletedCount++
                        Logger.d(
                            TAG,
                            "Deleted geodatabase: ${service.id}.geodatabase (${fileSize}KB)"
                        )
                    } else {
                        Logger.w(TAG, "Failed to delete geodatabase: ${service.id}.geodatabase")
                    }
                } else {
                    Logger.d(TAG, "Geodatabase ${service.id} does not exist, skipping")
                }
            }

            Logger.i(TAG, "Silent clear complete: deleted $deletedCount file(s)")
            Result.success(deletedCount)

        } catch (e: Exception) {
            Logger.e(TAG, "Error during silent geodatabase clear", e)
            Result.failure(e)
        }
    }
}
