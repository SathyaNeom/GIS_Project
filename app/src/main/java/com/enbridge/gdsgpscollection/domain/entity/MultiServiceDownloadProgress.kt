package com.enbridge.gdsgpscollection.domain.entity

/**
 * Progress tracking for simultaneous downloads from multiple feature services.
 *
 * This data class aggregates progress information from multiple concurrent
 * geodatabase downloads, providing both individual service progress and
 * combined overall progress. It enables unified progress display in the UI
 * while maintaining visibility into each service's download state.
 *
 * Architecture Pattern: Aggregate Pattern
 * Consolidates multiple progress objects into a single coherent view,
 * simplifying state management in ViewModels and UI components.
 *
 * @property serviceProgresses Map of service ID to individual progress state.
 *                             Key: Service ID (e.g., "operations", "basemap")
 *                             Value: ESDataDownloadProgress for that specific service
 * @property overallProgress Combined progress value across all services (0.0 - 1.0).
 *                           Calculated as the average of all individual service progresses.
 * @property overallMessage Human-readable message describing overall download state.
 *                          Example: "Downloading 1 of 2 services…"
 * @property isComplete Flag indicating whether all services have completed downloading.
 *                      true when all services reach 100% progress without errors.
 * @property error Error message if any service fails during download.
 *                 When set, download is considered failed for all services.
 *                 Error-first: First error encountered stops entire operation.
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Progress Calculation Strategy:
 * - **Overall Progress**: Mean average of all service progresses
 * - **Completion**: All services must complete successfully
 * - **Error Handling**: Fail-fast - any service error fails entire operation
 *
 * Example usage:
 * ```kotlin
 * val progress = MultiServiceDownloadProgress(
 *     serviceProgresses = mapOf(
 *         "operations" to ESDataDownloadProgress(0.8f, "Downloading operations…", false),
 *         "basemap" to ESDataDownloadProgress(0.6f, "Downloading basemap…", false)
 *     ),
 *     overallProgress = 0.7f,
 *     overallMessage = "Downloading 1 of 2 services…",
 *     isComplete = false,
 *     error = null
 * )
 * ```
 */
data class MultiServiceDownloadProgress(
    val serviceProgresses: Map<String, ESDataDownloadProgress>,
    val overallProgress: Float,
    val overallMessage: String,
    val isComplete: Boolean,
    val error: String? = null
) {
    /**
     * Returns the number of services that have completed successfully.
     */
    val completedCount: Int
        get() = serviceProgresses.values.count { it.isComplete && it.error == null }

    /**
     * Returns the total number of services being downloaded.
     */
    val totalCount: Int
        get() = serviceProgresses.size

    /**
     * Returns true if any service has encountered an error.
     */
    val hasError: Boolean
        get() = error != null || serviceProgresses.values.any { it.error != null }

    /**
     * Returns a percentage string (e.g., "70%") for UI display.
     */
    val percentageString: String
        get() = "${(overallProgress * 100).toInt()}%"
}
