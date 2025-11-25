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
        private const val KEY_OSM_VISIBLE = "osm_basemap_visible"
        private const val KEY_OSM_GUIDANCE_SHOWN = "osm_guidance_shown"
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

    /**
     * Saves the OSM basemap visibility preference
     *
     * @param visible true to show basemap, false to hide
     */
    fun saveOsmVisibility(visible: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_OSM_VISIBLE, visible)
            .apply()
    }

    /**
     * Gets the OSM basemap visibility preference
     * Returns default value of false (hidden by default) if not set
     *
     * @return true if basemap should be visible, false otherwise
     */
    fun getOsmVisibility(): Boolean {
        return sharedPreferences.getBoolean(KEY_OSM_VISIBLE, false)
    }

    /**
     * Saves whether the OSM guidance message has been shown to the user
     *
     * @param shown true if guidance has been displayed
     */
    fun setOsmGuidanceShown(shown: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_OSM_GUIDANCE_SHOWN, shown)
            .apply()
    }

    /**
     * Checks if the OSM guidance message has been shown to the user
     *
     * @return true if guidance has been shown before, false if first time
     */
    fun hasShownOsmGuidance(): Boolean {
        return sharedPreferences.getBoolean(KEY_OSM_GUIDANCE_SHOWN, false)
    }
}
