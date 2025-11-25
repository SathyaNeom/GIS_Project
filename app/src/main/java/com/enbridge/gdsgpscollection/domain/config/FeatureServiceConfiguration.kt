package com.enbridge.gdsgpscollection.domain.config

import com.enbridge.gdsgpscollection.BuildConfig
import com.enbridge.gdsgpscollection.util.Logger

/**
 * Singleton configuration manager for feature services based on deployment environment.
 *
 * This object serves as the central source of truth for determining which feature services
 * the application should connect to based on the current environment (Project vs. Wildfire).
 * It provides environment-aware configuration that adapts the application's data sources
 * without requiring code changes.
 *
 * Architecture Pattern: Strategy + Singleton
 * - Strategy: Different configurations for different environments
 * - Singleton: Single source of truth for service configuration
 *
 * Environment Detection Priority:
 * 1. BuildConfig.ENVIRONMENT - Compile-time configuration
 * 2. SharedPreferences (future enhancement) - Runtime toggle
 * 3. Network availability check (future enhancement) - Automatic detection
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 */
object FeatureServiceConfiguration {

    private const val TAG = "FeatureServiceConfiguration"

    // TODO: Replace with actual project server URLs when deploying to production
    private const val PROJECT_OPERATIONS_URL =
        "https://your-project-server.com/arcgis/rest/services/Operations/FeatureServer"
    private const val PROJECT_BASEMAP_URL =
        "https://your-project-server.com/arcgis/rest/services/Basemap/FeatureServer"

    // Esri public sample service for development/testing
    private const val WILDFIRE_URL =
        "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer"

    /**
     * Determines and returns the current application environment configuration.
     *
     * This method evaluates the build configuration and returns the appropriate
     * environment setup with its associated feature services. The environment
     * determines which data sources the application will use for geodatabase
     * downloads and synchronization.
     *
     * Environment Selection Logic:
     * - Reads BuildConfig.ENVIRONMENT value set during compilation
     * - "project": Returns Project environment with Operations and Basemap services
     * - "wildfire": Returns Wildfire environment with single sample service
     * - Default: Falls back to Wildfire for safety
     *
     * @return AppEnvironment configuration (Project or Wildfire)
     *
     * Example:
     * ```kotlin
     * val environment = FeatureServiceConfiguration.getCurrentEnvironment()
     * when (environment) {
     *     is AppEnvironment.Project -> // Multiple services
     *     is AppEnvironment.Wildfire -> // Single service
     * }
     * ```
     */
    fun getCurrentEnvironment(): AppEnvironment {
        val environmentType = BuildConfig.ENVIRONMENT
        Logger.i(TAG, "Determining environment configuration: $environmentType")

        return when (environmentType.lowercase()) {
            "project" -> createProjectEnvironment()
            "wildfire" -> createWildfireEnvironment()
            else -> {
                Logger.w(TAG, "Unknown environment '$environmentType', defaulting to wildfire")
                createWildfireEnvironment()
            }
        }
    }

    /**
     * Creates the Project environment configuration.
     *
     * This environment is designed for use within the organization's network
     * and provides access to two separate feature services:
     *
     * 1. **Operations Service**
     *    - Contains operational/business data
     *    - Prefix: "OP_"
     *    - Display: Table of Contents only (not rendered on map)
     *    - Purpose: Reference data for field operations
     *
     * 2. **Basemap Service**
     *    - Contains geographic basemap layers
     *    - Prefix: "BM_"
     *    - Display: Rendered on map + Table of Contents
     *    - Purpose: Visual context and navigation
     *
     * Both services support offline synchronization for field work scenarios.
     *
     * @return AppEnvironment.Project with configured services
     */
    private fun createProjectEnvironment(): AppEnvironment.Project {
        Logger.d(TAG, "Creating Project environment configuration")

        val services = listOf(
            FeatureServiceConfig(
                id = "operations",
                name = "Operations",
                url = PROJECT_OPERATIONS_URL,
                prefix = "OP_",
                displayOnMap = false,  // Only in Table of Contents
                syncEnabled = true
            ),
            FeatureServiceConfig(
                id = "basemap",
                name = "Basemap",
                url = PROJECT_BASEMAP_URL,
                prefix = "BM_",
                displayOnMap = true,   // Rendered on map
                syncEnabled = true
            )
        )

        Logger.i(TAG, "Project environment: ${services.size} services configured")
        services.forEach { service ->
            Logger.d(
                TAG,
                "  - ${service.name} (${service.id}): displayOnMap=${service.displayOnMap}"
            )
        }

        return AppEnvironment.Project(services)
    }

    /**
     * Creates the Wildfire environment configuration.
     *
     * This environment uses Esri's publicly accessible Wildfire sample service,
     * which is ideal for:
     * - Development and testing without VPN or CA Certificate
     * - Remote work scenarios
     * - Demonstrations and training
     * - Fallback when project servers are unavailable
     *
     * The Wildfire service provides sample data that mimics real-world GIS
     * scenarios and supports the same offline/sync capabilities as project services.
     *
     * Service Configuration:
     * - Single service with multiple layers
     * - Prefix: "GDB_" (maintains backward compatibility)
     * - Display: Rendered on map + Table of Contents
     * - Public access: No authentication required
     *
     * @return AppEnvironment.Wildfire with configured service
     */
    private fun createWildfireEnvironment(): AppEnvironment.Wildfire {
        Logger.d(TAG, "Creating Wildfire environment configuration")

        val service = FeatureServiceConfig(
            id = "wildfire",
            name = "Wildfire",
            url = WILDFIRE_URL,
            prefix = "GDB_",
            displayOnMap = true,
            syncEnabled = true
        )

        Logger.i(TAG, "Wildfire environment: Single service configured")
        Logger.d(TAG, "  - ${service.name} (${service.id}): ${service.url}")

        return AppEnvironment.Wildfire(listOf(service))
    }

    /**
     * Extracts the service name from a Feature Server URL.
     *
     * This utility method parses ArcGIS REST service URLs to extract the
     * human-readable service name from the URL structure. This is useful
     * for dynamic service discovery or configuration validation.
     *
     * URL Structure:
     * `https://server.com/arcgis/rest/services/[ServiceName]/FeatureServer`
     *
     * @param url Full Feature Server URL
     * @return Service name extracted from URL, or "Unknown" if parsing fails
     *
     * Examples:
     * ```kotlin
     * extractServiceName("https://server.com/arcgis/.../Operations/FeatureServer")
     * // Returns: "Operations"
     *
     * extractServiceName("https://server.com/arcgis/.../Basemap/FeatureServer")
     * // Returns: "Basemap"
     *
     * extractServiceName("invalid-url")
     * // Returns: "Unknown"
     * ```
     */
    fun extractServiceName(url: String): String {
        return try {
            url.substringBeforeLast("/FeatureServer")
                .substringAfterLast("/")
                .takeIf { it.isNotBlank() }
                ?: "Unknown"
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to extract service name from URL: $url", e)
            "Unknown"
        }
    }

    /**
     * Returns whether the current environment is Project mode.
     *
     * Utility method for quick environment checks without pattern matching.
     *
     * @return true if current environment is Project, false otherwise
     */
    fun isProjectEnvironment(): Boolean {
        return getCurrentEnvironment() is AppEnvironment.Project
    }

    /**
     * Returns whether the current environment is Wildfire mode.
     *
     * Utility method for quick environment checks without pattern matching.
     *
     * @return true if current environment is Wildfire, false otherwise
     */
    fun isWildfireEnvironment(): Boolean {
        return getCurrentEnvironment() is AppEnvironment.Wildfire
    }
}
