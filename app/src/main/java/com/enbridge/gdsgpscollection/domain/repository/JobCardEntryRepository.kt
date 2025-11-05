package com.enbridge.gdsgpscollection.domain.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry
import kotlinx.coroutines.flow.Flow

interface JobCardEntryRepository {
    suspend fun saveJobCardEntry(entry: JobCardEntry): Result<Unit>
    suspend fun getJobCardEntry(id: String): Result<JobCardEntry>
    fun getAllJobCardEntries(): Flow<List<JobCardEntry>>
    suspend fun deleteJobCardEntry(id: String): Result<Unit>
}
