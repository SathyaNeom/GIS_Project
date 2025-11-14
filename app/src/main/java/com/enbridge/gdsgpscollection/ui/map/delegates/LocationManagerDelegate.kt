package com.enbridge.gdsgpscollection.ui.map.delegates

import com.arcgismaps.geometry.Point
import com.arcgismaps.location.LocationDataSource
import com.arcgismaps.location.LocationDisplayAutoPanMode
import kotlinx.coroutines.flow.StateFlow

/**
 * Delegate interface for managing location display functionality.
 *
 * This delegate is responsible for creating and managing the location data source
 * that provides the user's current position on the map.
 *
 * ## Current Implementation
 * - Uses simulated location data (San Francisco coordinates) for testing
 * - Provides a mock location without requiring device GPS
 *
 * ## Future Enhancement: Bluetooth GPS Integration
 * When integrating a Bluetooth GPS device with higher accuracy:
 *
 * 1. **Create a Custom Location Provider**:
 *    - Implement `CustomLocationDataSource.LocationProvider` interface
 *    - Override `locations: Flow<Location>` to emit GPS data
 *    - Override `headings: Flow<Double>` to emit device heading
 *
 * 2. **GPS Data to Collect from Bluetooth Device**:
 *    - Latitude and Longitude (decimal degrees)
 *    - Elevation/Altitude (meters)
 *    - Horizontal Accuracy (meters)
 *    - Vertical Accuracy (meters)
 *    - Speed (meters per second)
 *    - Course/Bearing (degrees)
 *    - Timestamp (Instant)
 *
 * 3. **Implementation Steps**:
 *    ```kotlin
 *    class BluetoothGpsLocationProvider(
 *        private val bluetoothGpsService: BluetoothGpsService
 *    ) : CustomLocationDataSource.LocationProvider {
 *
 *        override val locations: Flow<Location> = bluetoothGpsService.gpsDataFlow.map { gpsData ->
 *            Location.create(
 *                position = Point(gpsData.longitude, gpsData.latitude, SpatialReference.wgs84()),
 *                horizontalAccuracy = gpsData.horizontalAccuracy,
 *                verticalAccuracy = gpsData.verticalAccuracy,
 *                speed = gpsData.speed,
 *                course = gpsData.bearing,
 *                lastKnown = false,
 *                timestamp = gpsData.timestamp
 *            )
 *        }
 *
 *        override val headings: Flow<Double> = bluetoothGpsService.headingFlow
 *    }
 *    ```
 *
 * 4. **Update Implementation**:
 *    - Modify `LocationManagerDelegateImpl.createLocationDataSource()`
 *    - Replace `SimulatedLocationDataSource` with `CustomLocationDataSource`
 *    - Pass the Bluetooth GPS provider to the CustomLocationDataSource
 *
 * 5. **Error Handling**:
 *    - Handle Bluetooth connection failures
 *    - Fallback to device GPS if Bluetooth GPS disconnects
 *    - Display connection status to user
 *
 * 6. **Permissions Required**:
 *    - BLUETOOTH_CONNECT (API 31+)
 *    - BLUETOOTH_SCAN (API 31+)
 *    - BLUETOOTH and BLUETOOTH_ADMIN (API < 31)
 *
 * @author Sathya Narayanan
 */
interface LocationManagerDelegate {

    /**
     * StateFlow indicating whether location display is currently enabled.
     *
     * This can be used to show/hide location-related UI elements or
     * display connection status for external GPS devices.
     */
    val isLocationEnabled: StateFlow<Boolean>

    /**
     * StateFlow indicating the current auto-pan mode for location display.
     *
     * This tracks whether the map is actively following the user's location
     * and what type of following behavior is active:
     * - Off: User can freely pan/zoom, no auto-centering
     * - Recenter: Re-centers when location moves outside wander extent
     * - Navigation: Optimized for in-vehicle navigation
     * - CompassNavigation: Optimized for walking with compass-based rotation
     */
    val currentAutoPanMode: StateFlow<LocationDisplayAutoPanMode>

    /**
     * StateFlow containing the user's current location point.
     * Emits null if location is unavailable (no permission, no GPS fix, etc.).
     *
     * This is used for operations that need to center on user's location,
     * such as extent calculations in ManageES feature.
     */
    val currentLocation: StateFlow<Point?>

    /**
     * StateFlow indicating whether location is currently available for use.
     *
     * Returns true when a valid location point has been acquired (currentLocation != null).
     * Returns false when location is unavailable due to:
     * - No location permission granted
     * - GPS still acquiring first fix
     * - Location services disabled on device
     * - Bluetooth GPS disconnected (future)
     *
     * This is used by ManageES feature to enable/disable the "Get Data" button
     * based on location availability.
     */
    val isLocationAvailable: StateFlow<Boolean>

    /**
     * Creates and configures a LocationDataSource for displaying user location.
     *
     * ## Current Behavior:
     * - Development mode: Returns SimulatedLocationDataSource with San Francisco coordinates
     * - Production mode: Returns null (toolkit uses default SystemLocationDataSource)
     *
     * ## Future Bluetooth GPS Integration:
     * Replace the null return value with a CustomLocationDataSource that uses the
     * Bluetooth GPS provider implementation.
     *
     * @return LocationDataSource configured for the current mode, or null to use system default
     */
    fun createLocationDataSource(): LocationDataSource?

    /**
     * Enables location tracking and display.
     *
     * Updates the [isLocationEnabled] state to true.
     */
    fun enableLocation()

    /**
     * Disables location tracking and display.
     *
     * Updates the [isLocationEnabled] state to false.
     */
    fun disableLocation()

    /**
     * Sets the auto-pan mode for location display.
     *
     * This controls how the map responds to location updates:
     * - Off: No auto-panning, user has full control
     * - Recenter: Re-centers when user moves outside wander extent
     * - Navigation: Best for vehicle navigation (heading up)
     * - CompassNavigation: Best for walking (compass-based)
     *
     * @param mode The desired auto-pan mode
     */
    fun setAutoPanMode(mode: LocationDisplayAutoPanMode)

    /**
     * Toggles location following on/off.
     *
     * This provides a convenient way to switch between following the user's
     * location and allowing free map navigation.
     *
     * Behavior:
     * - If currently Off or Recenter: Enables CompassNavigation (following)
     * - If currently following (Navigation or CompassNavigation): Disables to Off
     *
     * @return The new auto-pan mode after toggling
     */
    fun toggleLocationFollowing(): LocationDisplayAutoPanMode

    /**
     * Updates the current location point.
     * This should be called by the UI layer when location updates are received.
     *
     * @param location The new location point, or null if unavailable
     */
    fun updateCurrentLocation(location: Point?)
}
