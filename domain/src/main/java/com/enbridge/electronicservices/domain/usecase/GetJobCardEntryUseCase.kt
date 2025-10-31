package com.enbridge.electronicservices.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.electronicservices.domain.entity.JobCardEntry
import com.enbridge.electronicservices.domain.repository.JobCardEntryRepository
import javax.inject.Inject

class GetJobCardEntryUseCase @Inject constructor(
    private val repository: JobCardEntryRepository
) {
    suspend operator fun invoke(id: String): Result<JobCardEntry> {
        return repository.getJobCardEntry(id)
    }
}
