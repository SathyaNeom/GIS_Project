package com.enbridge.gdsgpscollection.domain.config

/**
 * Sealed class representing the application's deployment environment configuration.
 *
 * This sealed class provides a type-safe way to manage different deployment environments,
 * each with its own set of feature services. The application behavior adapts based on
 * the active environment, allowing seamless switching between project-specific services
 * and fallback (Wildfire) services.
 *
 * Architecture Pattern: Strategy Pattern
 * Each environment encapsulates a different strategy for data source configuration,
 * allowing the application to adapt its behavior without conditional logic scattered
 * throughout the codebase.
 *
 * @property featureServices List of feature service configurations for this environment.
 *
 * @author Sathya Narayanan
 * @since 1.0.0
 *
 * Supported Environments:
 * - **Project**: Production environment with Operations and Basemap services
 * - **Wildfire**: Fallback environment using Esri's public Wildfire sample service
 */
sealed class AppEnvironment {
    /**
     * List of feature services configured for this environment.
     * Each environment must provide at least one feature service.
     */
    abstract val featureServices: List<FeatureServiceConfig>

    /**
     * Project environment configuration.
     *
     * This environment is used when working within the organization's network
     * and provides access to two separate services:
     * 1. Operations Service: Contains operational data (shown in Table of Contents only)
     * 2. Basemap Service: Contains basemap layers (rendered on map)
     *
     * Use Case:
     * - Working on-site with access to internal servers
     * - Production deployment within organization network
     * - Development/testing with project-specific data
     *
     * @property featureServices List containing Operations and Basemap service configurations
     */
    data class Project(
        override val featureServices: List<FeatureServiceConfig>
    ) : AppEnvironment() {
        init {
            require(featureServices.isNotEmpty()) {
                "Project environment must have at least one feature service configured"
            }
        }
    }

    /**
     * Wildfire environment configuration.
     *
     * This environment serves as the fallback when project services are unavailable.
     * It uses Esri's publicly accessible Wildfire sync sample service, which allows
     * development and testing without VPN or internal network access.
     *
     * Use Case:
     * - Working remotely without VPN access
     * - Development/testing outside organization network
     * - Demonstration or training scenarios
     * - Fallback when project servers are unreachable
     *
     * @property featureServices List containing the single Wildfire service configuration
     */
    data class Wildfire(
        override val featureServices: List<FeatureServiceConfig>
    ) : AppEnvironment() {
        init {
            require(featureServices.size == 1) {
                "Wildfire environment should have exactly one feature service"
            }
        }
    }
}
