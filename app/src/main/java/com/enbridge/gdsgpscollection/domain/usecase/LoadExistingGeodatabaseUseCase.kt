package com.enbridge.gdsgpscollection.domain.usecase

import com.arcgismaps.data.Geodatabase
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for loading an existing geodatabase from local storage.
 *
 * This use case is invoked at app startup to check if a previously downloaded
 * geodatabase exists and loads it for immediate map display.
 *
 * @property repository Repository handling geodatabase operations
 */
class LoadExistingGeodatabaseUseCase @Inject constructor(
    private val repository: ManageESRepository
) {
    /**
     * Loads the existing geodatabase if available.
     *
     * @return Result containing the Geodatabase if found and valid, null if not found,
     *         or an error if the file exists but is corrupted/invalid
     */
    suspend operator fun invoke(): Result<Geodatabase?> {
        return repository.loadExistingGeodatabase()
    }
}
