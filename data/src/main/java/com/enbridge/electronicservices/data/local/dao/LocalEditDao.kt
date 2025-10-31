package com.enbridge.electronicservices.data.local.dao

/**
 * @author Sathya Narayanan
 */
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.enbridge.electronicservices.data.local.entity.LocalEditEntity

@Dao
interface LocalEditDao {
    @Query("SELECT * FROM local_edits WHERE isSynced = 0")
    suspend fun getUnsyncedEdits(): List<LocalEditEntity>

    @Query("SELECT * FROM local_edits")
    suspend fun getAllEdits(): List<LocalEditEntity>

    @Insert
    suspend fun insertEdit(edit: LocalEditEntity): Long

    @Update
    suspend fun updateEdit(edit: LocalEditEntity)

    @Query("UPDATE local_edits SET isSynced = 1 WHERE id = :editId")
    suspend fun markAsSynced(editId: Long)

    @Query("DELETE FROM local_edits WHERE isSynced = 1")
    suspend fun deleteSyncedEdits()
}
