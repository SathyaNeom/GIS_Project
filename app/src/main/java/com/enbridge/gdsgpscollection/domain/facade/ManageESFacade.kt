package com.enbridge.gdsgpscollection.domain.facade

import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import kotlinx.coroutines.flow.Flow

/**
 * Facade for managing Electronic Services data operations.
 *
 * Provides a simplified interface for downloading, posting, and managing ES data,
 * grouping related use cases to reduce ViewModel dependencies and improve cohesion.
 * Supports both single-service (legacy/Wildfire) and multi-service (Project) operations.
 *
 * Following the Facade pattern from SOLID principles to provide a unified interface
 * to a set of interfaces in the domain layer.
 *
 * Architecture Evolution:
 * - Original: 6 individual use case dependencies in ViewModel
 * - Refactored: 1 facade dependency (83% reduction)
 * - Enhanced: Multi-service support for Project environment
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
interface ManageESFacade {

    // ========== SINGLE-SERVICE METHODS (Legacy/Wildfire Support) ==========

    /**
     * Downloads Electronic Services data for the specified extent.
     *
     * Legacy method for single-service downloads (Wildfire environment).
     *
     * TODO: Will be deprecated in favor of downloadAllServices() once
     *       all environments use multi-service approach.
     *
     * @param extent Geographic extent to download data for
     * @return Flow of download progress updates
     */
    suspend fun downloadESData(extent: Envelope): Flow<ESDataDownloadProgress>

    /**
     * Posts (uploads) local Electronic Services data changes to the server.
     *
     * Legacy method for single-service sync (Wildfire environment).
     *
     * TODO: Will be deprecated in favor of syncAllServices() once
     *       all environments use multi-service approach.
     *
     * @return Result containing success status (true if successful)
     */
    suspend fun postESData(): Result<Boolean>

    // ========== MULTI-SERVICE METHODS (Project Environment Support) ==========

    /**
     * Downloads geodatabases from all configured feature services.
     *
     * This method adapts to the current environment:
     * - Project Environment: Downloads Operations and Basemap in parallel
     * - Wildfire Environment: Downloads single Wildfire service
     *
     * Provides unified progress tracking across all services with fail-fast
     * error handling strategy.
     *
     * @param extent Geographic extent to download data for (same for all services)
     * @return Flow emitting combined progress for all services
     *
     * Example:
     * ```kotlin
     * downloadAllServices(extent).collect { progress ->
     *     updateUI("Overall: ${progress.overallProgress * 100}%")
     *     updateUI("Services: ${progress.completedCount}/${progress.totalCount}")
     * }
     * ```
     */
    suspend fun downloadAllServices(extent: Envelope): Flow<MultiServiceDownloadProgress>

    /**
     * Synchronizes all geodatabases with their respective feature services.
     *
     * Uploads local changes and downloads server changes for all configured
     * services. Used by the "Post Data" button in ManageES screen.
     *
     * Sync Strategy:
     * - Sequential execution to prevent conflicts
     * - Independent results per service
     * - Continues syncing even if one service fails
     *
     * @return Result containing map of service ID to sync success status
     *         Example: {"operations": true, "basemap": false}
     *
     * Example:
     * ```kotlin
     * syncAllServices().onSuccess { results ->
     *     val successCount = results.values.count { it }
     *     showMessage("Synced $successCount/${results.size} services")
     * }
     * ```
     */
    suspend fun syncAllServices(): Result<Map<String, Boolean>>

    /**
     * Loads all existing geodatabases from local storage.
     *
     * Retrieves metadata for all previously downloaded geodatabases,
     * including service information, layer counts, and file sizes.
     * Used during app startup to restore offline data.
     *
     * @return Result containing list of loaded geodatabase information
     *         Empty list if no geodatabases found (not an error)
     *
     * Example:
     * ```kotlin
     * loadAllGeodatabases().onSuccess { geodatabases ->
     *     geodatabases.forEach { info ->
     *         println("${info.serviceName}: ${info.layerCount} layers")
     *         if (info.displayOnMap) {
     *             addLayersToMap(info.geodatabase)
     *         }
     *     }
     * }
     * ```
     */
    suspend fun loadAllGeodatabases(): Result<List<GeodatabaseInfo>>

    // ========== COMMON METHODS (Used by Both Single and Multi-Service) ==========

    /**
     * Retrieves all locally changed data that needs to be posted.
     *
     * Queries all geodatabases for features with local edits.
     * Works with both single and multiple geodatabases.
     *
     * @return Result containing list of changed job cards
     */
    suspend fun getChangedData(): Result<List<JobCard>>

    /**
     * Deletes all job cards from the local database.
     *
     * @return Result containing the number of deleted job cards
     */
    suspend fun deleteJobCards(): Result<Int>

    /**
     * Retrieves the user's selected download distance preference.
     *
     * @return The selected distance setting
     */
    suspend fun getSelectedDistance(): ESDataDistance

    /**
     * Saves the user's selected download distance preference.
     *
     * @param distance The distance setting to save
     */
    suspend fun saveSelectedDistance(distance: ESDataDistance)

    // ========== SYNC CHECK METHODS (Data Loss Prevention) ==========

    /**
     * Checks if any geodatabase has unsaved changes that need syncing.
     *
     * This method is critical for preventing data loss before destructive operations:
     * - Downloading new data (overwrites existing geodatabase)
     * - Clearing geodatabase (deletes all local data)
     *
     * Works seamlessly across both environments:
     * - Wildfire: Checks single geodatabase
     * - Project: Checks all geodatabases (Operations + Basemap)
     *
     * Returns true if ANY geodatabase has unsaved changes, ensuring users are
     * warned before potentially losing their work.
     *
     * @return Result<Boolean> - true if unsaved changes exist, false otherwise
     *
     * Example:
     * ```kotlin
     * hasUnsyncedChanges().onSuccess { hasChanges ->
     *     if (hasChanges) {
     *         showWarningDialog("You have unsaved changes. Sync before continuing?")
     *     } else {
     *         proceedWithOperation()
     *     }
     * }
     * ```
     */
    suspend fun hasUnsyncedChanges(): Result<Boolean>
}
