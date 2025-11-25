package com.enbridge.gdsgpscollection.domain.usecase

import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import com.enbridge.gdsgpscollection.util.Logger
import javax.inject.Inject

/**
 * Use case for checking if geodatabase has unsaved changes before destructive actions.
 *
 * This prevents data loss by warning users before:
 * - Downloading new data (would overwrite existing geodatabase)
 * - Clearing geodatabase (would delete unsaved edits)
 *
 * Follows Single Responsibility Principle: only checks for changes, doesn't perform sync.
 *
 * ## Architecture Context:
 * - **Layer**: Domain Layer (business logic)
 * - **Dependencies**: ManageESRepository interface (data abstraction)
 * - **Used By**: ManageESFacade, MainMapViewModel, ManageESViewModel
 *
 * ## Use Cases:
 * 1. **Before Download**: Warn user if existing data has unsaved changes
 * 2. **Before Clear**: Require explicit confirmation if changes exist
 * 3. **Data Loss Prevention**: Part of comprehensive data protection strategy
 *
 * @property repository Repository for geodatabase operations
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
class CheckUnsyncedChangesUseCase @Inject constructor(
    private val repository: ManageESRepository
) {
    /**
     * Checks if any geodatabase has local edits that need syncing.
     *
     * This method delegates to the repository layer which handles environment-specific
     * logic (Wildfire: single geodatabase, Project: multiple geodatabases).
     *
     * ## Implementation Flow:
     * 1. Repository loads all configured geodatabases
     * 2. Checks each geodatabase using `Geodatabase.hasLocalEdits()`
     * 3. Returns true if ANY geodatabase has unsaved changes
     * 4. Returns false only if ALL geodatabases are clean
     *
     * ## Error Handling:
     * - Geodatabase not found: Treated as no changes (safe default)
     * - Load failure: Logged and skipped (safe default)
     * - Unexpected error: Returns Result.failure with details
     *
     * @return Result<Boolean> - true if unsaved changes exist, false otherwise
     *
     * Example:
     * ```kotlin
     * val result = checkUnsyncedChangesUseCase()
     * result.onSuccess { hasChanges ->
     *     if (hasChanges) {
     *         // Show warning dialog
     *     } else {
     *         // Safe to proceed
     *     }
     * }
     * ```
     */
    suspend operator fun invoke(): Result<Boolean> {
        Logger.d(TAG, "Executing CheckUnsyncedChangesUseCase")
        return repository.hasUnsyncedChanges()
    }

    companion object {
        private const val TAG = "CheckUnsyncedChangesUseCase"
    }
}
