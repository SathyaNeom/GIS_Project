package com.enbridge.gdsgpscollection.ui.map.delegates

import android.app.Application
import com.arcgismaps.data.Geodatabase
import com.arcgismaps.mapping.layers.FeatureLayer
import com.enbridge.gdsgpscollection.domain.usecase.LoadExistingGeodatabaseUseCase
import com.enbridge.gdsgpscollection.ui.map.GeodatabaseMetadata
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GeodatabaseManagerDelegate.
 *
 * Manages geodatabase lifecycle including loading, deleting, and health checks.
 *
 * @author Sathya Narayanan
 */
@Singleton
class GeodatabaseManagerDelegateImpl @Inject constructor(
    private val application: Application,
    private val loadExistingGeodatabaseUseCase: LoadExistingGeodatabaseUseCase
) : GeodatabaseManagerDelegate {

    private val _geodatabaseMetadata = MutableStateFlow<GeodatabaseMetadata?>(null)
    override val geodatabaseMetadata: StateFlow<GeodatabaseMetadata?> =
        _geodatabaseMetadata.asStateFlow()

    private val _showFirstTimeGuidance = MutableStateFlow(false)
    override val showFirstTimeGuidance: StateFlow<Boolean> =
        _showFirstTimeGuidance.asStateFlow()

    private val _geodatabaseLoadError = MutableStateFlow<String?>(null)
    override val geodatabaseLoadError: StateFlow<String?> =
        _geodatabaseLoadError.asStateFlow()

    private var _currentGeodatabase: Geodatabase? = null
    override val currentGeodatabase: Geodatabase?
        get() = _currentGeodatabase

    private val sharedPrefs by lazy {
        application.getSharedPreferences("map_prefs", android.content.Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "GeodatabaseManagerDelegate"
        private const val PREF_FIRST_LAUNCH = "is_first_launch"
        private const val PREF_GEODATABASE_TIMESTAMP = "geodatabase_timestamp"
    }

    /**
     * Path to the geodatabase file in internal storage
     */
    private val geodatabaseFilePath: String
        get() = File(application.filesDir, Constants.GEODATABASE_FILE_NAME).absolutePath

    override suspend fun checkAndLoadExisting(): Result<Geodatabase?> {
        return try {
            Logger.i(TAG, "Checking for existing geodatabase on startup")

            // Load existing geodatabase with health check
            val result = loadExistingGeodatabaseUseCase()

            result.onSuccess { geodatabase ->
                if (geodatabase != null) {
                    // Geodatabase found and loaded successfully
                    Logger.i(TAG, "Existing geodatabase found and validated")

                    // Store metadata
                    val fileSize = File(geodatabaseFilePath).length()
                    val timestamp = sharedPrefs.getLong(
                        PREF_GEODATABASE_TIMESTAMP,
                        System.currentTimeMillis()
                    )
                    val metadata = GeodatabaseMetadata(
                        lastDownloadTime = timestamp,
                        layerCount = geodatabase.featureTables.size,
                        fileSizeKB = fileSize / 1024
                    )

                    _geodatabaseMetadata.update { metadata }
                    _currentGeodatabase = geodatabase

                    Logger.d(TAG, "Geodatabase metadata: $metadata")
                } else {
                    // No geodatabase found - check if first-time user
                    Logger.i(TAG, "No geodatabase found on startup")

                    val isFirstLaunch = sharedPrefs.getBoolean(PREF_FIRST_LAUNCH, true)
                    if (isFirstLaunch) {
                        Logger.i(TAG, "First-time user detected, showing guidance")
                        _showFirstTimeGuidance.value = true
                        sharedPrefs.edit().putBoolean(PREF_FIRST_LAUNCH, false).apply()
                    }
                }
            }.onFailure { error ->
                // Geodatabase exists but is corrupted or invalid
                Logger.e(TAG, "Geodatabase health check failed", error)
                _geodatabaseLoadError.value = error.message ?: "Failed to load existing data"
            }

            result

        } catch (e: Exception) {
            Logger.e(TAG, "Unexpected error during geodatabase auto-load", e)
            _geodatabaseLoadError.value = "An unexpected error occurred"
            Result.failure(e)
        }
    }

    override suspend fun deleteGeodatabase(featureLayers: List<FeatureLayer>) {
        try {
            // Close current geodatabase if open
            _currentGeodatabase?.close()
            _currentGeodatabase = null

            // Delete file
            val geodatabaseFile = File(geodatabaseFilePath)
            if (geodatabaseFile.exists()) {
                val deleted = geodatabaseFile.delete()
                Logger.d(TAG, "Geodatabase file deleted: $deleted")
            }

            // Clear metadata
            _geodatabaseMetadata.value = null

            Logger.i(TAG, "Geodatabase deleted successfully")

        } catch (e: Exception) {
            Logger.e(TAG, "Error deleting geodatabase", e)
        }
    }

    override fun geodatabaseExists(): Boolean {
        return File(geodatabaseFilePath).exists()
    }

    override fun saveTimestamp() {
        val timestamp = System.currentTimeMillis()
        sharedPrefs.edit().putLong(PREF_GEODATABASE_TIMESTAMP, timestamp).apply()
        Logger.d(TAG, "Saved geodatabase timestamp: $timestamp")
    }

    override fun dismissFirstTimeGuidance() {
        _showFirstTimeGuidance.value = false
        Logger.d(TAG, "First-time guidance dismissed")
    }

    override fun dismissGeodatabaseLoadError() {
        _geodatabaseLoadError.value = null
        Logger.d(TAG, "Geodatabase load error dismissed")
    }

    override fun setCurrentGeodatabase(geodatabase: Geodatabase?) {
        _currentGeodatabase = geodatabase
        Logger.d(TAG, "Current geodatabase set: ${geodatabase?.path}")
    }

    override fun closeGeodatabase() {
        _currentGeodatabase?.close()
        _currentGeodatabase = null
        Logger.d(TAG, "Geodatabase connection closed")
    }
}
