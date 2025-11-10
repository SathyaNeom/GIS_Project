package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.entity.ESDataDownloadProgress
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for downloading ES geodatabase data for a specified extent
 * Coordinates with the repository to fetch data
 */
class DownloadESDataUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(
        extent: Envelope
    ): Flow<ESDataDownloadProgress> {
        return manageESRepository.downloadESData(extent)
    }
}
