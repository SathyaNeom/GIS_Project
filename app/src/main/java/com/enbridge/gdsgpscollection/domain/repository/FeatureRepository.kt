package com.enbridge.gdsgpscollection.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.FeatureType

/**
 * Repository interface for managing feature types.
 *
 * This repository provides access to available feature types
 * that can be collected in the GPS Device Project application.
 */
interface FeatureRepository {
    /**
     * Retrieves all available feature types.
     *
     * @return Result containing list of feature types on success, or an error on failure
     */
    suspend fun getFeatureTypes(): Result<List<FeatureType>>
}
