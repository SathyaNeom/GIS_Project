package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for deleting saved job cards from local storage
 * Returns the number of job cards successfully deleted
 */
class DeleteJobCardsUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(): Result<Int> {
        return manageESRepository.deleteJobCards()
    }
}
