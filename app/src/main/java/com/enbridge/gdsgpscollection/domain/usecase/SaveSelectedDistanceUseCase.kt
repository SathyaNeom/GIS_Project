package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for saving the user's selected distance preference
 * Persists the selection for future sessions
 */
class SaveSelectedDistanceUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(distance: ESDataDistance) {
        manageESRepository.saveSelectedDistance(distance)
    }
}
