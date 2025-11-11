package com.enbridge.gdsgpscollection.domain.usecase

import com.enbridge.gdsgpscollection.domain.entity.GeodatabaseInfo
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for loading all existing geodatabases from local storage.
 *
 * This use case retrieves metadata for all geodatabases that have been previously
 * downloaded and are available in the app's internal storage. It's typically used
 * during app startup to restore the user's offline data and display layers from
 * multiple services.
 *
 * Loading Strategy:
 * - Scans internal storage for geodatabase files
 * - Matches files to configured services
 * - Validates file integrity (size, loadability)
 * - Extracts metadata (layer count, file size, last sync time)
 * - Associates with service configuration (displayOnMap flag)
 *
 * Use Cases:
 * - App startup: Restore previously downloaded data
 * - Map initialization: Load layers onto map
 * - Table of Contents: Display available layers
 * - Storage management: Show geodatabase info
 * - Data freshness checks: Compare last sync times
 *
 * Business Logic:
 * - Only loads valid, non-corrupted geodatabases
 * - Skips missing or empty files
 * - Continues loading even if one geodatabase fails
 * - Returns empty list if no geodatabases found (not an error)
 *
 * @property repository Data layer for geodatabase operations
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * class MainMapViewModel(
 *     private val loadAllGeodatabasesUseCase: LoadAllGeodatabasesUseCase
 * ) : ViewModel() {
 *     fun checkAndLoadExistingData() {
 *         viewModelScope.launch {
 *             loadAllGeodatabasesUseCase().onSuccess { geodatabases ->
 *                 if (geodatabases.isNotEmpty()) {
 *                     loadLayersOntoMap(geodatabases)
 *                 } else {
 *                     showFirstTimeGuidance()
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 */
class LoadAllGeodatabasesUseCase @Inject constructor(
    private val repository: ManageESRepository
) {
    /**
     * Executes the geodatabase loading operation.
     *
     * This method scans the app's internal storage for geodatabase files
     * matching the configured services and loads their metadata. It performs
     * integrity checks and validation to ensure only usable geodatabases
     * are returned.
     *
     * Loading Process (Per Service):
     * 1. Check if geodatabase file exists for service
     * 2. Validate file size (must be > 0)
     * 3. Load geodatabase instance
     * 4. Validate has feature tables (must have layers)
     * 5. Extract metadata (layer count, file size, timestamps)
     * 6. Associate with service configuration
     * 7. Add to result list
     *
     * Metadata Included:
     * - Service ID and name
     * - File name and size
     * - Loaded Geodatabase instance
     * - Last sync timestamp
     * - Layer count
     * - Display on map flag
     *
     * @return Result containing list of loaded geodatabase information
     *         - Empty list: No geodatabases found (not an error)
     *         - Non-empty list: Successfully loaded geodatabases
     *         - Failure: Critical error during loading process
     *
     * Note: Individual geodatabase failures don't cause overall failure.
     * The method continues loading other geodatabases and returns
     * successfully loaded ones.
     */
    suspend operator fun invoke(): Result<List<GeodatabaseInfo>> {
        return repository.loadExistingGeodatabases()
    }
}
