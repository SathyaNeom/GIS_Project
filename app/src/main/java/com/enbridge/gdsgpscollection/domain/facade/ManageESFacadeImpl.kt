package com.enbridge.gdsgpscollection.domain.facade

import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import com.enbridge.gdsgpscollection.domain.usecase.CheckUnsyncedChangesUseCase
import com.enbridge.gdsgpscollection.domain.usecase.DeleteJobCardsUseCase
import com.enbridge.gdsgpscollection.domain.usecase.DownloadAllServicesUseCase
import com.enbridge.gdsgpscollection.domain.usecase.DownloadESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetChangedDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetSelectedDistanceUseCase
import com.enbridge.gdsgpscollection.domain.usecase.LoadAllGeodatabasesUseCase
import com.enbridge.gdsgpscollection.domain.usecase.PostESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.SaveSelectedDistanceUseCase
import com.enbridge.gdsgpscollection.domain.usecase.SyncAllGeodatabasesUseCase
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ManageESFacade] that delegates to individual use cases.
 *
 * This facade simplifies the ViewModel layer by providing a single dependency
 * instead of multiple use case dependencies, while maintaining clear separation
 * of concerns and testability.
 *
 * Architecture Benefits:
 * - Reduces ViewModel dependencies from 9 to 1
 * - Groups related operations logically
 * - Maintains single responsibility per use case
 * - Simplifies testing (mock 1 facade vs. 9 use cases)
 *
 * @property downloadESDataUseCase Legacy single-service download
 * @property postESDataUseCase Legacy single-service sync
 * @property downloadAllServicesUseCase Multi-service download
 * @property syncAllGeodatabasesUseCase Multi-service sync
 * @property loadAllGeodatabasesUseCase Load existing geodatabases
 * @property getChangedDataUseCase Query local changes
 * @property deleteJobCardsUseCase Delete job cards
 * @property getSelectedDistanceUseCase Get distance preference
 * @property saveSelectedDistanceUseCase Save distance preference
 * @property checkUnsyncedChangesUseCase Check for unsaved changes before destructive operations
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
@Singleton
class ManageESFacadeImpl @Inject constructor(
    private val downloadESDataUseCase: DownloadESDataUseCase,
    private val postESDataUseCase: PostESDataUseCase,
    private val downloadAllServicesUseCase: DownloadAllServicesUseCase,
    private val syncAllGeodatabasesUseCase: SyncAllGeodatabasesUseCase,
    private val loadAllGeodatabasesUseCase: LoadAllGeodatabasesUseCase,
    private val getChangedDataUseCase: GetChangedDataUseCase,
    private val deleteJobCardsUseCase: DeleteJobCardsUseCase,
    private val getSelectedDistanceUseCase: GetSelectedDistanceUseCase,
    private val saveSelectedDistanceUseCase: SaveSelectedDistanceUseCase,
    private val checkUnsyncedChangesUseCase: CheckUnsyncedChangesUseCase
) : ManageESFacade {

    companion object {
        private const val TAG = "ManageESFacadeImpl"
    }

    // ========== SINGLE-SERVICE METHOD IMPLEMENTATIONS ==========

    override suspend fun downloadESData(extent: Envelope): Flow<ESDataDownloadProgress> {
        Logger.d(
            TAG,
            "downloadESData called - Extent: ${extent.xMin}, ${extent.yMin}, ${extent.xMax}, ${extent.yMax}"
        )
        return downloadESDataUseCase(extent)
    }

    override suspend fun postESData(): Result<Boolean> {
        Logger.d(TAG, "postESData called")
        return postESDataUseCase()
    }

    // ========== MULTI-SERVICE METHOD IMPLEMENTATIONS ==========

    override suspend fun downloadAllServices(extent: Envelope): Flow<MultiServiceDownloadProgress> {
        Logger.i(
            TAG,
            "downloadAllServices called - Extent: ${extent.xMin}, ${extent.yMin}, ${extent.xMax}, ${extent.yMax}"
        )
        return downloadAllServicesUseCase(extent)
    }

    override suspend fun syncAllServices(): Result<Map<String, Boolean>> {
        Logger.i(TAG, "syncAllServices called")
        return syncAllGeodatabasesUseCase()
    }

    override suspend fun loadAllGeodatabases(): Result<List<GeodatabaseInfo>> {
        Logger.d(TAG, "loadAllGeodatabases called")
        return loadAllGeodatabasesUseCase()
    }

    // ========== COMMON METHOD IMPLEMENTATIONS ==========

    override suspend fun getChangedData(): Result<List<JobCard>> {
        Logger.d(TAG, "getChangedData called")
        return getChangedDataUseCase()
    }

    override suspend fun deleteJobCards(): Result<Int> {
        Logger.d(TAG, "deleteJobCards called")
        return deleteJobCardsUseCase()
    }

    override suspend fun getSelectedDistance(): ESDataDistance {
        Logger.d(TAG, "getSelectedDistance called")
        return getSelectedDistanceUseCase()
    }

    override suspend fun saveSelectedDistance(distance: ESDataDistance) {
        Logger.d(TAG, "saveSelectedDistance called - Distance: ${distance.displayText}")
        saveSelectedDistanceUseCase(distance)
    }

    // ========== SYNC CHECK METHOD IMPLEMENTATION ==========

    override suspend fun hasUnsyncedChanges(): Result<Boolean> {
        Logger.d(TAG, "hasUnsyncedChanges called - checking for local edits")
        return checkUnsyncedChangesUseCase()
    }
}
