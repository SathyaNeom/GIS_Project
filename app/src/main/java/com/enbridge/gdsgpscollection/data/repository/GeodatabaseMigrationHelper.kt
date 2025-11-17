package com.enbridge.gdsgpscollection.data.repository

import android.content.Context
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper for migrating geodatabases from older app versions.
 *
 * Handles:
 * - Detection of pre-existing geodatabases from older versions
 * - Metadata reconstruction for legacy files
 * - Graceful fallback when metadata is unavailable
 *
 * Migration Strategy:
 * - V1 (Legacy): Geodatabases without service metadata (Wildfire only)
 * - V2 (Current): Geodatabases with service-based metadata tracking
 *
 * For backward compatibility, legacy geodatabases are assumed to use
 * Wildfire environment configuration (displayOnMap = true).
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
@Singleton
class GeodatabaseMigrationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "GeodatabaseMigration"
        private const val MIGRATION_PREF_KEY = "geodatabase_migrated_v2"
        private const val PREFS_NAME = "migration"
    }

    /**
     * Checks if migration is needed and performs it if necessary.
     *
     * Migration Logic:
     * 1. Check if migration already completed (stored in SharedPreferences)
     * 2. Scan for pre-existing geodatabase files
     * 3. Log legacy files for tracking (no file modification needed)
     * 4. Mark migration as complete
     *
     * Note: Current implementation only logs legacy files since the new
     * layer metadata system handles them dynamically during loading.
     * Future versions may need to add actual file migration logic.
     *
     * @return true if migration was performed, false otherwise
     */
    fun checkAndMigrate(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val alreadyMigrated = prefs.getBoolean(MIGRATION_PREF_KEY, false)

        if (alreadyMigrated) {
            Logger.d(TAG, "Geodatabase migration already performed")
            return false
        }

        // Check for pre-existing geodatabase files
        val geodatabaseDir = context.filesDir
        val geodatabaseFiles = geodatabaseDir.listFiles { file ->
            file.extension == "geodatabase"
        } ?: emptyArray()

        if (geodatabaseFiles.isEmpty()) {
            Logger.d(TAG, "No pre-existing geodatabases found, skipping migration")
            prefs.edit().putBoolean(MIGRATION_PREF_KEY, true).apply()
            return false
        }

        Logger.i(
            TAG,
            "Found ${geodatabaseFiles.size} pre-existing geodatabase(s), performing migration"
        )

        // For legacy files, we assume they should be displayed on map (Wildfire default behavior)
        // The new metadata system will handle them dynamically during loading
        geodatabaseFiles.forEach { file ->
            Logger.d(TAG, "Detected legacy geodatabase: ${file.name} (${file.length() / 1024} KB)")
        }

        prefs.edit().putBoolean(MIGRATION_PREF_KEY, true).apply()
        Logger.i(TAG, "Migration completed successfully")

        return true
    }

    /**
     * Resets migration flag for testing or reprocessing.
     * Should only be called in debug/test scenarios.
     */
    fun resetMigrationFlag() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(MIGRATION_PREF_KEY, false).apply()
        Logger.w(TAG, "Migration flag reset (for testing only)")
    }
}
