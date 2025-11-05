package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.domain.entity.FeatureType
import com.enbridge.gpsdeviceproj.domain.repository.FeatureRepository
import javax.inject.Inject

class GetFeatureTypesUseCase @Inject constructor(
    private val featureRepository: FeatureRepository
) {
    suspend operator fun invoke(): Result<List<FeatureType>> {
        return featureRepository.getFeatureTypes()
    }
}
