package com.enbridge.electronicservices.data.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.data.api.ElectronicServicesApi
import com.enbridge.electronicservices.data.mapper.toDomain
import com.enbridge.electronicservices.domain.entity.FeatureType
import com.enbridge.electronicservices.domain.repository.FeatureRepository
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

    /**
     * Retrieves all available feature types from the API.
     *
     * @return Result containing list of feature types on success, or an error on failure
     */
    override suspend fun getFeatureTypes(): Result<List<FeatureType>> {
        return try {
            api.getFeatureTypes().map { dtos ->
                dtos.map { it.toDomain() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
