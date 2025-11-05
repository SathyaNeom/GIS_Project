package com.enbridge.gdsgpscollection.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry
import com.enbridge.gdsgpscollection.domain.repository.JobCardEntryRepository
import javax.inject.Inject

class SaveJobCardEntryUseCase @Inject constructor(
    private val repository: JobCardEntryRepository
) {
    suspend operator fun invoke(entry: JobCardEntry): Result<Unit> {
        return repository.saveJobCardEntry(entry)
    }
}
