package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.ESDataDistance
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for retrieving the user's selected distance preference
 * Returns the saved distance or a default value
 */
class GetSelectedDistanceUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(): ESDataDistance {
        return manageESRepository.getSelectedDistance()
    }
}
