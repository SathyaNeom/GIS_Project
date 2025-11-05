package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.domain.entity.ESDataDistance
import com.enbridge.gpsdeviceproj.domain.entity.ESDataDownloadProgress
import com.enbridge.gpsdeviceproj.domain.repository.ManageESRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for downloading ES geodatabase data within a specified radius
 * Coordinates with location services and repository to fetch data
 */
class DownloadESDataUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(
        distance: ESDataDistance,
        latitude: Double,
        longitude: Double
    ): Flow<ESDataDownloadProgress> {
        return manageESRepository.downloadESData(distance, latitude, longitude)
    }
}
