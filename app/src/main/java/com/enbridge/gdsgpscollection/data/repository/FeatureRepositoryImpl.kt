package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.data.api.ElectronicServicesApi
import com.enbridge.gdsgpscollection.data.mapper.toDomain
import com.enbridge.gdsgpscollection.domain.entity.FeatureType
import com.enbridge.gdsgpscollection.domain.repository.FeatureRepository
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject

/**
 * Implementation of FeatureRepository that handles feature type operations.
 *
 * This repository provides access to the list of available feature types
 * that can be collected in the field.
 *
 * @property api The API service for making feature type requests
 */
class FeatureRepositoryImpl @Inject constructor(
    private val api: ElectronicServicesApi
) : FeatureRepository {

    companion object {
        private const val TAG = "FeatureRepository"
    }

    /**
     * Retrieves all available feature types from the API.
     *
     * @return Result containing list of feature types on success, or an error on failure
     */
    override suspend fun getFeatureTypes(): Result<List<FeatureType>> {
        Logger.d(TAG, "Fetching feature types from API")
        return try {
            val result = api.getFeatureTypes().map { dtos ->
                dtos.map { it.toDomain() }
            }

            result.onSuccess { featureTypes ->
                Logger.i(TAG, "Successfully fetched ${featureTypes.size} feature types")
            }.onFailure { error ->
                Logger.e(TAG, "Failed to fetch feature types", error)
            }

            result
        } catch (e: Exception) {
            Logger.e(TAG, "Exception while fetching feature types", e)
            Result.failure(e)
        }
    }
}
