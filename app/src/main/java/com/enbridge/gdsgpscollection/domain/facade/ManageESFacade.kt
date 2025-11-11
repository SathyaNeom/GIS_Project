package com.enbridge.gdsgpscollection.domain.facade

import com.arcgismaps.data.Geodatabase
import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import kotlinx.coroutines.flow.Flow

/**
 * Facade for managing Electronic Services data operations.
 *
 * Provides a simplified interface for downloading, posting, and managing ES data,
 * grouping related use cases to reduce ViewModel dependencies and improve cohesion.
 *
 * Following the Facade pattern from SOLID principles to provide a unified interface
 * to a set of interfaces in the domain layer.
 *
 * @author Sathya Narayanan
 */
interface ManageESFacade {

    /**
     * Downloads Electronic Services data for the specified extent.
     *
     * @param extent Geographic extent to download data for
     * @return Flow of download progress updates
     */
    suspend fun downloadESData(extent: Envelope): Flow<ESDataDownloadProgress>

    /**
     * Posts (uploads) local Electronic Services data changes to the server.
     *
     * @return Result containing success status (true if successful)
     */
    suspend fun postESData(): Result<Boolean>

    /**
     * Retrieves all locally changed data that needs to be posted.
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
}
