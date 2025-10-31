package com.enbridge.electronicservices.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.domain.entity.ESDataDistance
import com.enbridge.electronicservices.domain.entity.ESDataDownloadProgress
import com.enbridge.electronicservices.domain.entity.JobCard
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing Electronic Services data operations
 * Handles downloading, posting, and deleting ES data and job cards
 */
interface ManageESRepository {

    /**
     * Downloads ES geodatabase data within the specified distance from user's location
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
     * @param distance The radius around user's location to download data
     * @param latitude User's current latitude
     * @param longitude User's current longitude
     * @return Flow of download progress updates
     */
    suspend fun downloadESData(
        distance: ESDataDistance,
        latitude: Double,
        longitude: Double
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
}
