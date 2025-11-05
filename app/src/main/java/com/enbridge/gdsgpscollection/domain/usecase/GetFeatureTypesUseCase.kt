package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.FeatureType
import com.enbridge.gdsgpscollection.domain.repository.FeatureRepository
import javax.inject.Inject

class GetFeatureTypesUseCase @Inject constructor(
    private val featureRepository: FeatureRepository
) {
    suspend operator fun invoke(): Result<List<FeatureType>> {
        return featureRepository.getFeatureTypes()
    }
}
