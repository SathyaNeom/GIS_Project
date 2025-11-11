package com.enbridge.gdsgpscollection.domain.usecase

import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import javax.inject.Inject

/**
 * Use case for synchronizing all geodatabases with their respective feature services.
 *
 * This use case coordinates the bidirectional synchronization of local geodatabase
 * changes with remote feature services. It handles both uploading local edits
 * (adds, updates, deletes) and downloading server-side changes for all configured
 * services in the current environment.
 *
 * Synchronization Strategy:
 * - Sequential execution to prevent conflicts
 * - Independent results per service
 * - Continues syncing even if one service fails
 * - Returns comprehensive status map
 *
 * Use Cases:
 * - "Post Data" button in ManageES screen
 * - Background sync operations
 * - Conflict resolution workflows
 * - Data consistency checks
 *
 * Business Rules:
 * - Only syncs services that have local geodatabases
 * - Requires network connectivity
 * - Uses bidirectional sync (upload + download)
 * - Maintains data integrity with rollback support
 *
 * @property repository Data layer for geodatabase operations
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * class ManageESViewModel(
 *     private val syncAllGeodatabasesUseCase: SyncAllGeodatabasesUseCase
 * ) : ViewModel() {
 *     fun onPostDataClicked() {
 *         viewModelScope.launch {
 *             syncAllGeodatabasesUseCase().onSuccess { results ->
 *                 val successCount = results.values.count { it }
 *                 showMessage("Synced $successCount/${results.size} services")
 *             }
 *         }
 *     }
 * }
 * ```
 */
class SyncAllGeodatabasesUseCase @Inject constructor(
    private val repository: ManageESRepository
) {
    /**
     * Executes synchronization for all geodatabases.
     *
     * This method sequentially syncs each geodatabase with its corresponding
     * feature service. Sequential execution prevents conflicts and ensures
     * data consistency across services.
     *
     * Sync Process (Per Service):
     * 1. Load local geodatabase
     * 2. Create sync parameters (bidirectional)
     * 3. Upload local changes to server
     * 4. Download server changes to local
     * 5. Update timestamps
     * 6. Close geodatabase
     *
     * Error Handling:
     * - Individual service failures don't stop other services
     * - Each service has independent success/failure status
     * - Detailed error logging per service
     *
     * @return Result containing map of service ID to sync success status
     *         - Key: Service ID (e.g., "operations", "basemap", "wildfire")
     *         - Value: true if sync succeeded, false if failed
     *         Example: {"operations": true, "basemap": false}
     *
     * Result Interpretation:
     * - Success with all true: All services synced successfully
     * - Success with mixed values: Partial sync (some services failed)
     * - Failure: Critical error prevented sync operation
     */
    suspend operator fun invoke(): Result<Map<String, Boolean>> {
        return repository.syncAllGeodatabases()
    }
}
