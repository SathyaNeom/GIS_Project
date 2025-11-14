package com.enbridge.gdsgpscollection.di

import com.enbridge.gdsgpscollection.domain.config.FeatureServiceConfiguration
import com.enbridge.gdsgpscollection.domain.config.LocationFeatureFlags
import com.enbridge.gdsgpscollection.domain.config.LocationFeatureFlagsImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing configuration dependencies.
 *
 * Provides access to the Feature Service Configuration singleton for dependency injection
 * across the application.
 *
 * @author AI Assistant
 * @since 1.1.0
 */
@Module
@InstallIn(SingletonComponent::class)
object ConfigurationModule {

    /**
     * Provides the Feature Service Configuration singleton instance.
     *
     * The configuration object handles environment detection and provides access to
     * feature service URLs based on the current environment (Project or Wildfire).
     *
     * @return The singleton instance of FeatureServiceConfiguration
     */
    @Provides
    @Singleton
    fun provideFeatureServiceConfiguration(): FeatureServiceConfiguration {
        return FeatureServiceConfiguration
    }
}

/**
 * Hilt module for binding configuration interfaces to implementations.
 *
 * Uses @Binds for efficient interface binding without unnecessary object instantiation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ConfigurationBindModule {

    /**
     * Binds LocationFeatureFlags interface to its default implementation.
     *
     * Enables dependency injection of location feature flags throughout the application.
     *
     * @param impl The default implementation
     * @return The LocationFeatureFlags interface
     */
    @Binds
    @Singleton
    abstract fun bindLocationFeatureFlags(impl: LocationFeatureFlagsImpl): LocationFeatureFlags
}