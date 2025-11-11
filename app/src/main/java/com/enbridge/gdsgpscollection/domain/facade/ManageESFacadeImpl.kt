package com.enbridge.gdsgpscollection.domain.facade

import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.entity.JobCard
import com.enbridge.gdsgpscollection.domain.usecase.DeleteJobCardsUseCase
import com.enbridge.gdsgpscollection.domain.usecase.DownloadESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetChangedDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.GetSelectedDistanceUseCase
import com.enbridge.gdsgpscollection.domain.usecase.PostESDataUseCase
import com.enbridge.gdsgpscollection.domain.usecase.SaveSelectedDistanceUseCase
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
 * @author Sathya Narayanan
 */
@Singleton
class ManageESFacadeImpl @Inject constructor(
    private val downloadESDataUseCase: DownloadESDataUseCase,
    private val postESDataUseCase: PostESDataUseCase,
    private val getChangedDataUseCase: GetChangedDataUseCase,
    private val deleteJobCardsUseCase: DeleteJobCardsUseCase,
    private val getSelectedDistanceUseCase: GetSelectedDistanceUseCase,
    private val saveSelectedDistanceUseCase: SaveSelectedDistanceUseCase
) : ManageESFacade {

    companion object {
        private const val TAG = "ManageESFacadeImpl"
    }

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
}
