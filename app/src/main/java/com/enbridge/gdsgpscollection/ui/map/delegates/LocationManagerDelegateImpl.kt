package com.enbridge.gdsgpscollection.ui.map.delegates

import android.app.Application
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.Polyline
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.location.LocationDataSource
import com.arcgismaps.location.LocationDisplayAutoPanMode
import com.arcgismaps.location.SimulatedLocationDataSource
import com.arcgismaps.location.SimulationParameters
import com.enbridge.gdsgpscollection.domain.config.LocationFeatureFlags
import com.enbridge.gdsgpscollection.util.Constants
import com.enbridge.gdsgpscollection.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LocationManagerDelegate that manages location display.
 *
 * ## Current Implementation
 * This implementation uses a SimulatedLocationDataSource to display a static
 * location at San Francisco coordinates (Constants.SanFrancisco) for testing
 * purposes without requiring device GPS or permissions.
 *
 * ## Future Bluetooth GPS Integration Guide
 *
 * ### Step 1: Create Bluetooth GPS Data Model
 * ```kotlin
 * data class BluetoothGpsData(
 *     val latitude: Double,      // Decimal degrees
 *     val longitude: Double,     // Decimal degrees
 *     val elevation: Double,     // Meters above sea level
 *     val horizontalAccuracy: Double,  // Accuracy in meters
 *     val verticalAccuracy: Double,    // Accuracy in meters
 *     val speed: Double,         // Meters per second
 *     val bearing: Double,       // Degrees from true north
 *     val timestamp: Instant     // GPS timestamp
 * )
 * ```
 *
 * ### Step 2: Create Bluetooth GPS Service/Repository
 * Create a service that connects to the Bluetooth GPS device and emits location data:
 * ```kotlin
 * interface BluetoothGpsRepository {
 *     val gpsDataFlow: Flow<BluetoothGpsData>
 *     val headingFlow: Flow<Double>
 *     val connectionState: StateFlow<BluetoothConnectionState>
 *     suspend fun connect(deviceAddress: String): Result<Unit>
 *     suspend fun disconnect()
 * }
 * ```
 *
 * ### Step 3: Implement Custom Location Provider
 * ```kotlin
 * class BluetoothGpsLocationProvider(
 *     private val bluetoothGpsRepository: BluetoothGpsRepository
 * ) : CustomLocationDataSource.LocationProvider {
 *
 *     override val locations: Flow<Location> = bluetoothGpsRepository.gpsDataFlow
 *         .map { gpsData ->
 *             Location.create(
 *                 position = Point(
 *                     gpsData.longitude,
 *                     gpsData.latitude,
 *                     gpsData.elevation,
 *                     SpatialReference.wgs84()
 *                 ),
 *                 horizontalAccuracy = gpsData.horizontalAccuracy,
 *                 verticalAccuracy = gpsData.verticalAccuracy,
 *                 speed = gpsData.speed,
 *                 course = gpsData.bearing,
 *                 lastKnown = false,
 *                 timestamp = gpsData.timestamp
 *             )
 *         }
 *         .catch { error ->
 *             Logger.e(TAG, "Error reading Bluetooth GPS data", error)
 *             // Handle error - could fallback to device GPS
 *         }
 *
 *     override val headings: Flow<Double> = bluetoothGpsRepository.headingFlow
 * }
 * ```
 *
 * ### Step 4: Update This Implementation
 * Replace the createLocationDataSource() method implementation:
 * ```kotlin
 * @Inject constructor(
 *     private val application: Application,
 *     private val bluetoothGpsRepository: BluetoothGpsRepository  // Add this dependency
 * ) : LocationManagerDelegate {
 *
 *     override fun createLocationDataSource(): LocationDataSource {
 *         val locationProvider = BluetoothGpsLocationProvider(bluetoothGpsRepository)
 *         return CustomLocationDataSource(locationProvider)
 *     }
 * }
 * ```
 *
 * ### Step 5: Handle Connection State
 * Monitor Bluetooth GPS connection and update UI:
 * ```kotlin
 * viewModelScope.launch {
 *     bluetoothGpsRepository.connectionState.collect { state ->
 *         when (state) {
 *             BluetoothConnectionState.Connected -> {
 *                 // Show connected indicator
 *                 // Display GPS accuracy percentage
 *             }
 *             BluetoothConnectionState.Disconnected -> {
 *                 // Show disconnected indicator
 *                 // Optionally fallback to device GPS
 *             }
 *             is BluetoothConnectionState.Error -> {
 *                 // Show error message
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ### Step 6: Update Coordinate Info Bar
 * The coordinate info bar in MainMapScreen should display Bluetooth GPS data:
 * - Horizontal Accuracy (from GPS)
 * - Vertical Accuracy (from GPS)
 * - Elevation (from GPS)
 * - Signal strength / Satellite count
 *
 * ### Step 7: Add Required Permissions (Already in AndroidManifest.xml)
 * - ACCESS_FINE_LOCATION
 * - ACCESS_COARSE_LOCATION
 * - BLUETOOTH_CONNECT (API 31+)
 * - BLUETOOTH_SCAN (API 31+)
 *
 * @param application Application context for location services
 * @param featureFlags Configuration determining simulated vs real location usage
 * @author Sathya Narayanan
 */
@Singleton
class LocationManagerDelegateImpl @Inject constructor(
    private val application: Application,
    private val featureFlags: LocationFeatureFlags
) : LocationManagerDelegate {

    companion object {
        private const val TAG = "LocationManagerDelegate"
    }

    private val _isLocationEnabled = MutableStateFlow(false)
    override val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    private val _currentAutoPanMode: MutableStateFlow<LocationDisplayAutoPanMode> =
        MutableStateFlow(LocationDisplayAutoPanMode.Off)
    override val currentAutoPanMode: StateFlow<LocationDisplayAutoPanMode> =
        _currentAutoPanMode.asStateFlow()

    private val _currentLocation = MutableStateFlow<Point?>(null)
    override val currentLocation: StateFlow<Point?> = _currentLocation.asStateFlow()

    /**
     * Derived StateFlow indicating location availability.
     * True when currentLocation is non-null, false otherwise.
     */
    override val isLocationAvailable: StateFlow<Boolean> =
        _currentLocation.map { it != null }
            .stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    /**
     * Creates a LocationDataSource based on feature flag configuration.
     *
     * ## Configuration Behavior:
     * - **useSimulatedLocation = true**: Returns SimulatedLocationDataSource (San Francisco)
     * - **useSimulatedLocation = false**: Returns null (uses toolkit's default SystemLocationDataSource)
     *
     * ## Development Mode:
     * Creates SimulatedLocationDataSource with static San Francisco coordinates for testing
     * without requiring GPS hardware or permissions.
     *
     * ## Production Mode:
     * Returns null to indicate that the default system location data source should be used.
     * The ArcGIS Maps SDK Toolkit automatically creates and manages a SystemLocationDataSource
     * that uses the device's actual GPS/location services.
     *
     * ## Future Bluetooth GPS Integration:
     * Replace the null return with CustomLocationDataSource using
     * BluetoothGpsLocationProvider as described in the class documentation.
     *
     * @return LocationDataSource for simulated location, or null for system default
     */
    override fun createLocationDataSource(): LocationDataSource? {
        return if (featureFlags.useSimulatedLocation) {
            Logger.d(TAG, "Creating simulated location data source (Development Mode)")
            createSimulatedLocationDataSource()
        } else {
            Logger.d(TAG, "Using default system location data source (Production Mode)")
            null // Let toolkit create default SystemLocationDataSource
        }
    }

    /**
     * Creates a SimulatedLocationDataSource for development/testing.
     *
     * Provides a stationary location at San Francisco coordinates without requiring
     * actual GPS hardware or location permissions.
     *
     * @return SimulatedLocationDataSource configured with San Francisco coordinates
     */
    private fun createSimulatedLocationDataSource(): LocationDataSource {
        // Create a static point at San Francisco coordinates (Web Mercator projection)
        val sanFranciscoPoint = Point(
            x = Constants.SanFrancisco.CENTER_X,
            y = Constants.SanFrancisco.CENTER_Y,
            spatialReference = SpatialReference.webMercator()
        )

        // SimulatedLocationDataSource requires a Polyline for the path
        // For a stationary location, create a polyline with two very close points
        val locationPolyline = PolylineBuilder(SpatialReference.webMercator()) {
            addPoint(sanFranciscoPoint)
            // Add a second point very close to the first to create a minimal path
            addPoint(
                Point(
                    x = Constants.SanFrancisco.CENTER_X + 0.1,
                    y = Constants.SanFrancisco.CENTER_Y + 0.1,
                    spatialReference = SpatialReference.webMercator()
                )
            )
        }.toGeometry()

        // Configure simulation parameters
        val simulationParameters = SimulationParameters(
            startTime = Instant.now(),
            velocity = 0.1,  // Very slow movement to keep location nearly stationary
            horizontalAccuracy = 5.0,  // Simulated accuracy: 5 meters
            verticalAccuracy = 5.0     // Simulated accuracy: 5 meters
        )

        return SimulatedLocationDataSource(locationPolyline, simulationParameters).also {
            Logger.i(TAG, "Simulated location data source created successfully")
        }
    }

    override fun enableLocation() {
        _isLocationEnabled.value = true
        Logger.d(TAG, "Location display enabled")
    }

    override fun disableLocation() {
        _isLocationEnabled.value = false
        Logger.d(TAG, "Location display disabled")
    }

    override fun setAutoPanMode(mode: LocationDisplayAutoPanMode) {
        _currentAutoPanMode.value = mode
        Logger.d(TAG, "Auto-pan mode set to: $mode")
    }

    override fun toggleLocationFollowing(): LocationDisplayAutoPanMode {
        val newMode: LocationDisplayAutoPanMode = when (_currentAutoPanMode.value) {
            is LocationDisplayAutoPanMode.Off,
            is LocationDisplayAutoPanMode.Recenter -> {
                // Enable location following with CompassNavigation mode
                // This is best for walking/field work scenarios
                Logger.i(TAG, "Enabling location following (CompassNavigation mode)")
                LocationDisplayAutoPanMode.CompassNavigation
            }

            is LocationDisplayAutoPanMode.Navigation,
            is LocationDisplayAutoPanMode.CompassNavigation -> {
                // Disable location following, return to Off mode
                Logger.i(TAG, "Disabling location following (Off mode)")
                LocationDisplayAutoPanMode.Off
            }
        }

        _currentAutoPanMode.value = newMode
        return newMode
    }

    override fun updateCurrentLocation(location: Point?) {
        _currentLocation.value = location
        if (location != null) {
            Logger.v(TAG, "Current location updated: (${location.x}, ${location.y})")
        } else {
            Logger.v(TAG, "Current location cleared (null)")
        }
    }
}
