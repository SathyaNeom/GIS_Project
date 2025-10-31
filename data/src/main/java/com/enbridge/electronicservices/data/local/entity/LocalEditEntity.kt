package com.enbridge.electronicservices.data.local.entity

/**
 * @author Sathya Narayanan
 */
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_edits")
data class LocalEditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityId: String,
    val entityType: String,
    val isSynced: Boolean = false
)
