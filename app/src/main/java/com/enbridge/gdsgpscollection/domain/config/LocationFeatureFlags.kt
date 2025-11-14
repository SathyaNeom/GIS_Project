package com.enbridge.gdsgpscollection.domain.config

import com.enbridge.gdsgpscollection.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration interface for location-related feature flags.
 *
 * Provides clear separation between development and production location behavior,
 * enabling simulated locations for testing while enforcing real GPS in production.
 *
 * ## Development Mode (BuildConfig.DEBUG = true)
 * - Uses simulated location at San Francisco for testing
 * - No GPS hardware or permissions required
 * - Immediate location availability
 *
 * ## Production Mode (BuildConfig.DEBUG = false)
 * - Uses real device GPS (or future Bluetooth GPS)
 * - Requires location permissions
 * - Location acquisition may take 5-30 seconds
 *
 * @author Sathya Narayanan
 */
interface LocationFeatureFlags {
    /**
     * Determines whether to use simulated location data.
     *
     * - `true`: Use SimulatedLocationDataSource with San Francisco coordinates
     * - `false`: Use AndroidLocationDataSource (real GPS)
     *
     * Default: `BuildConfig.DEBUG` (simulated in debug builds, real GPS in release)
     */
    val useSimulatedLocation: Boolean

    /**
     * Determines whether location is required for ManageES "Get Data" feature.
     *
     * - `true`: "Get Data" button disabled until location available
     * - `false`: "Get Data" button enabled regardless of location (uses fallback)
     *
     * Default: `true` (location always required)
     */
    val requireLocationForManageES: Boolean

    /**
     * Determines whether to show the debug settings menu.
     *
     * - `true`: Debug icon visible in toolbar for accessing debug settings
     * - `false`: Debug icon hidden
     *
     * Default: `BuildConfig.DEBUG` (visible in debug builds only)
     */
    val showDebugMenu: Boolean
}

/**
 * Default implementation of LocationFeatureFlags.
 *
 * Uses BuildConfig.DEBUG to automatically configure behavior:
 * - Debug builds: Simulated location, debug menu visible
 * - Release builds: Real GPS, debug menu hidden
 *
 * Can be overridden for testing by providing a custom implementation via Hilt.
 */
@Singleton
class LocationFeatureFlagsImpl @Inject constructor() : LocationFeatureFlags {
    override val useSimulatedLocation: Boolean = BuildConfig.DEBUG
    override val requireLocationForManageES: Boolean = true
    override val showDebugMenu: Boolean = BuildConfig.DEBUG
}
