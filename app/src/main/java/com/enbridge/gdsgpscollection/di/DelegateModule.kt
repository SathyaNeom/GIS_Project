package com.enbridge.gdsgpscollection.di

import com.enbridge.gdsgpscollection.ui.map.delegates.BasemapManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.BasemapManagerDelegateImpl
import com.enbridge.gdsgpscollection.ui.map.delegates.ExtentManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.ExtentManagerDelegateImpl
import com.enbridge.gdsgpscollection.ui.map.delegates.GeodatabaseManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.GeodatabaseManagerDelegateImpl
import com.enbridge.gdsgpscollection.ui.map.delegates.LayerManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.LayerManagerDelegateImpl
import com.enbridge.gdsgpscollection.ui.map.delegates.LocationManagerDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.LocationManagerDelegateImpl
import com.enbridge.gdsgpscollection.ui.map.delegates.NetworkConnectivityDelegate
import com.enbridge.gdsgpscollection.ui.map.delegates.NetworkConnectivityDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for providing delegate implementations.
 *
 * This module binds delegate interfaces to their concrete implementations, following the
 * Delegation Pattern for MainMapViewModel architecture. All delegates are scoped as singletons
 * within the SingletonComponent lifecycle.
 *
 * Delegates provided by this module:
 * - LayerManagerDelegate: Layer visibility and attribute-based filtering
 * - BasemapManagerDelegate: Basemap style and OSM layer management
 * - GeodatabaseManagerDelegate: Geodatabase lifecycle and persistence
 * - ExtentManagerDelegate: Viewpoint calculation for extent visualization
 * - NetworkConnectivityDelegate: Network state monitoring for offline awareness
 * - LocationManagerDelegate: Location display and position tracking
 *
 * @author Sathya Narayanan
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DelegateModule {

    @Binds
    @Singleton
    abstract fun bindLayerManagerDelegate(
        impl: LayerManagerDelegateImpl
    ): LayerManagerDelegate

    @Binds
    @Singleton
    abstract fun bindBasemapManagerDelegate(
        impl: BasemapManagerDelegateImpl
    ): BasemapManagerDelegate

    @Binds
    @Singleton
    abstract fun bindGeodatabaseManagerDelegate(
        impl: GeodatabaseManagerDelegateImpl
    ): GeodatabaseManagerDelegate

    @Binds
    @Singleton
    abstract fun bindExtentManagerDelegate(
        impl: ExtentManagerDelegateImpl
    ): ExtentManagerDelegate

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityDelegate(
        impl: NetworkConnectivityDelegateImpl
    ): NetworkConnectivityDelegate

    @Binds
    @Singleton
    abstract fun bindLocationManagerDelegate(
        impl: LocationManagerDelegateImpl
    ): LocationManagerDelegate

    companion object {
        /**
         * Provides an application-scoped CoroutineScope for delegates
         * that need to launch coroutines (like NetworkConnectivityDelegate).
         */
        @Provides
        @Singleton
        @DelegateCoroutineScope
        fun provideDelegateCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }
    }
}

/**
 * Qualifier annotation for delegate-specific CoroutineScope
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DelegateCoroutineScope
