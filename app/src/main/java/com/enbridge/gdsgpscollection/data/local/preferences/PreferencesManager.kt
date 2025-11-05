package com.enbridge.gdsgpscollection.data.local.preferences

/**
 * @author Sathya Narayanan
 */
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling app preferences using SharedPreferences
 * Provides type-safe access to stored preferences
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "electronic_services_prefs"
        private const val KEY_ES_DATA_DISTANCE = "es_data_distance"
    }

    /**
     * Saves the selected ES data distance in meters
     */
    fun saveESDataDistance(distanceInMeters: Int) {
        sharedPreferences.edit()
            .putInt(KEY_ES_DATA_DISTANCE, distanceInMeters)
            .apply()
    }

    /**
     * Gets the selected ES data distance in meters
     * Returns default value of 100 meters if not set
     */
    fun getESDataDistance(): Int {
        return sharedPreferences.getInt(KEY_ES_DATA_DISTANCE, 100)
    }
}
