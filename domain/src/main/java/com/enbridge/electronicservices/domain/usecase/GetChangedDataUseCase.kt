package com.enbridge.electronicservices.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.domain.entity.JobCard
import com.enbridge.electronicservices.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for retrieving locally changed/edited ES features
 * Returns list of job cards that have been modified but not yet synchronized
 */
class GetChangedDataUseCase @Inject constructor(
    private val manageESRepository: ManageESRepository
) {
    suspend operator fun invoke(): Result<List<JobCard>> {
        return manageESRepository.getChangedData()
    }
}
