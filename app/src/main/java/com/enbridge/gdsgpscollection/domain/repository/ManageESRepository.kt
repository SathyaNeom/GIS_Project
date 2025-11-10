package com.enbridge.gdsgpscollection.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.arcgismaps.geometry.Envelope
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Electronic Services data operations
 * Handles downloading, posting, and deleting ES data and job cards
 */
interface ManageESRepository {

    /**
     * Downloads ES geodatabase data within the specified extent
     * Returns a Flow to track download progress
     *
     * TODO: Integration Point - Use ArcGIS GeodatabaseSyncTask here
     * Example:
     * val geodatabaseSyncTask = GeodatabaseSyncTask(serviceUrl)
     * val params = GenerateGeodatabaseParameters().apply {
     *     extent = Envelope(centerPoint, distance)
     *     returnAttachments = false
     * }
     * geodatabaseSyncTask.generateGeodatabase(params, outputPath)
     *
     * @param extent The visible extent of the map to download data for
     * @return Flow of download progress updates
     */
    suspend fun downloadESData(
        extent: Envelope
    ): Flow<ESDataDownloadProgress>

    /**
     * Posts/uploads local ES data changes to the server
     *
     * TODO: Integration Point - Use ArcGIS GeodatabaseSyncTask.syncGeodatabase() here
     *
     * @return Result indicating success or failure
     */
    suspend fun postESData(): Result<Boolean>

    /**
     * Retrieves list of locally changed/edited features
     *
     * @return Result containing list of job cards with local changes
     */
    suspend fun getChangedData(): Result<List<JobCard>>

    /**
     * Deletes saved job cards from local storage
     *
     * @return Result indicating number of job cards deleted
     */
    suspend fun deleteJobCards(): Result<Int>

    /**
     * Gets the currently selected distance preference
     *
     * @return The saved distance preference or default value
     */
    suspend fun getSelectedDistance(): ESDataDistance

    /**
     * Saves the selected distance preference
     *
     * @param distance The distance to save
     */
    suspend fun saveSelectedDistance(distance: ESDataDistance)

    /**
     * Loads an existing geodatabase from local storage if available.
     *
     * This method checks if a previously downloaded geodatabase exists on disk,
     * validates its integrity, and loads it for use.
     *
     * @return Result containing:
     *         - Success(Geodatabase) if found and loaded successfully
     *         - Success(null) if no geodatabase file exists
     *         - Failure(Exception) if file exists but is corrupted or cannot be loaded
     */
    suspend fun loadExistingGeodatabase(): Result<com.arcgismaps.data.Geodatabase?>
}
