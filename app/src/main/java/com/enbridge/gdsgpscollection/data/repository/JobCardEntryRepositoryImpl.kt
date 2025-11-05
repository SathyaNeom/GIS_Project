package com.enbridge.gdsgpscollection.data.repository

/**
 * @author Sathya Narayanan
 */
import com.enbridge.gdsgpscollection.domain.entity.JobCardEntry
import com.enbridge.gdsgpscollection.domain.repository.JobCardEntryRepository
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobCardEntryRepositoryImpl @Inject constructor() : JobCardEntryRepository {

    private val _entries = MutableStateFlow<Map<String, JobCardEntry>>(emptyMap())

    companion object {
        private const val TAG = "JobCardEntryRepo"
    }

    override suspend fun saveJobCardEntry(entry: JobCardEntry): Result<Unit> {
        Logger.d(TAG, "Saving job card entry - WO: ${entry.workOrder}")
        return try {
            val entryWithId = if (entry.id.isEmpty()) {
                val newId = UUID.randomUUID().toString()
                Logger.d(TAG, "Generating new ID for entry: $newId")
                entry.copy(id = newId)
            } else {
                Logger.d(TAG, "Updating existing entry with ID: ${entry.id}")
                entry
            }

            _entries.value = _entries.value + (entryWithId.id to entryWithId)
            Logger.i(
                TAG,
                "Job card entry saved successfully - ID: ${entryWithId.id}, WO: ${entry.workOrder}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to save job card entry - WO: ${entry.workOrder}", e)
            Result.failure(e)
        }
    }

    override suspend fun getJobCardEntry(id: String): Result<JobCardEntry> {
        Logger.d(TAG, "Retrieving job card entry with ID: $id")
        return try {
            val entry = _entries.value[id]
            if (entry != null) {
                Logger.i(TAG, "Job card entry found - ID: $id, WO: ${entry.workOrder}")
                Result.success(entry)
            } else {
                Logger.w(TAG, "Job card entry not found - ID: $id")
                Result.failure(Exception("Job Card Entry not found"))
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error retrieving job card entry - ID: $id", e)
            Result.failure(e)
        }
    }

    override fun getAllJobCardEntries(): Flow<List<JobCardEntry>> {
        val count = _entries.value.size
        Logger.d(TAG, "Retrieving all job card entries - Count: $count")
        return MutableStateFlow(_entries.value.values.toList()).asStateFlow()
    }

    override suspend fun deleteJobCardEntry(id: String): Result<Unit> {
        Logger.d(TAG, "Deleting job card entry with ID: $id")
        return try {
            val entry = _entries.value[id]
            _entries.value = _entries.value - id
            Logger.i(
                TAG,
                "Job card entry deleted - ID: $id${entry?.let { ", WO: ${it.workOrder}" } ?: ""}")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to delete job card entry - ID: $id", e)
            Result.failure(e)
        }
    }
}
