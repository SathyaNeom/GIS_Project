package com.enbridge.gpsdeviceproj.data.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gpsdeviceproj.domain.entity.JobCardEntry
import com.enbridge.gpsdeviceproj.domain.repository.JobCardEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobCardEntryRepositoryImpl @Inject constructor() : JobCardEntryRepository {

    private val _entries = MutableStateFlow<Map<String, JobCardEntry>>(emptyMap())

    override suspend fun saveJobCardEntry(entry: JobCardEntry): Result<Unit> {
        return try {
            val entryWithId = if (entry.id.isEmpty()) {
                entry.copy(id = UUID.randomUUID().toString())
            } else {
                entry
            }

            _entries.value = _entries.value + (entryWithId.id to entryWithId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getJobCardEntry(id: String): Result<JobCardEntry> {
        return try {
            val entry = _entries.value[id]
            if (entry != null) {
                Result.success(entry)
            } else {
                Result.failure(Exception("Job Card Entry not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllJobCardEntries(): Flow<List<JobCardEntry>> {
        return MutableStateFlow(_entries.value.values.toList()).asStateFlow()
    }

    override suspend fun deleteJobCardEntry(id: String): Result<Unit> {
        return try {
            _entries.value = _entries.value - id
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
