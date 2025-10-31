package com.enbridge.electronicservices.data.local

/**
 * @author Sathya Narayanan
 */
import androidx.room.Database
import androidx.room.RoomDatabase
import com.enbridge.electronicservices.data.local.dao.LocalEditDao
import com.enbridge.electronicservices.data.local.entity.LocalEditEntity

@Database(
    entities = [LocalEditEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localEditDao(): LocalEditDao
}
