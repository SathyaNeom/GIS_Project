package com.enbridge.gdsgpscollection.domain.usecase

import com.arcgismaps.geometry.Envelope
import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.entity.MultiServiceDownloadProgress
import com.enbridge.gdsgpscollection.domain.repository.ManageESRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for downloading geodatabases from all configured feature services.
 *
 * This use case orchestrates the parallel download of multiple geodatabases based on
 * the current environment configuration (Project or Wildfire). It provides a unified
 * interface for the presentation layer to initiate multi-service downloads without
 * needing to know about individual service configurations.
 *
 * Business Logic:
 * - Determines current environment (Project vs. Wildfire)
 * - Retrieves configured feature services for that environment
 * - Delegates to repository for parallel download execution
 * - Returns combined progress for all services
 *
 * Architecture Benefits:
 * - Encapsulates environment-specific logic
 * - Provides testable business logic layer
 * - Decouples ViewModel from configuration details
 * - Single Responsibility: Coordinates multi-service downloads
 *
 * @property repository Data layer for geodatabase operations
 * @property configuration Environment configuration provider
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * class ManageESViewModel(
 *     private val downloadAllServicesUseCase: DownloadAllServicesUseCase
 * ) : ViewModel() {
 *     fun onDownloadClicked(extent: Envelope) {
 *         viewModelScope.launch {
 *             downloadAllServicesUseCase(extent).collect { progress ->
 *                 _uiState.update { it.copy(downloadProgress = progress) }
 *             }
 *         }
 *     }
 * }
 * ```
 */
class DownloadAllServicesUseCase @Inject constructor(
    private val repository: ManageESRepository,
    private val configuration: FeatureServiceConfiguration
) {
    /**
     * Executes the multi-service download operation.
     *
     * This method retrieves the current environment configuration and initiates
     * downloads for all configured services within that environment. Progress
     * updates are emitted through a Flow for reactive UI updates.
     *
     * Environment Handling:
     * - **Project Environment:** Downloads Operations and Basemap services in parallel
     * - **Wildfire Environment:** Downloads single Wildfire service
     *
     * Progress Tracking:
     * - Individual progress per service
     * - Combined overall progress (0.0 - 1.0)
     * - Human-readable status messages
     * - Error reporting with fail-fast strategy
     *
     * @param extent Geographic extent to download data for (same for all services)
     * @return Flow emitting combined download progress for all services
     *
     * @throws Exception if download fails (propagated from repository layer)
     */
    suspend operator fun invoke(extent: Envelope): Flow<MultiServiceDownloadProgress> {
        val environment = configuration.getCurrentEnvironment()
        return repository.downloadMultipleServices(extent, environment.featureServices)
    }
}
