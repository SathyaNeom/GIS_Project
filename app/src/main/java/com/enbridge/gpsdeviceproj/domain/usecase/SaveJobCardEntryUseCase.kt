package com.enbridge.gpsdeviceproj.domain.usecase

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.domain.entity.JobCardEntry
import com.enbridge.gpsdeviceproj.domain.repository.JobCardEntryRepository
import javax.inject.Inject

class SaveJobCardEntryUseCase @Inject constructor(
    private val repository: JobCardEntryRepository
) {
    suspend operator fun invoke(entry: JobCardEntry): Result<Unit> {
        return repository.saveJobCardEntry(entry)
    }
}
