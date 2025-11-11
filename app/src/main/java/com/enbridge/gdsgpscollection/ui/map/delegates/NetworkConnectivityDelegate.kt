package com.enbridge.gdsgpscollection.ui.map.delegates

import kotlinx.coroutines.flow.StateFlow

/**
 * Delegate interface for monitoring network connectivity.
 *
 * Responsibilities:
 * - Observe network connectivity changes
 * - Expose offline state to UI
 * - Log connectivity state changes
 *
 * @author Sathya Narayanan
 */
interface NetworkConnectivityDelegate {

    /**
     * StateFlow indicating whether the device is offline (no network connection)
     */
    val isOffline: StateFlow<Boolean>

    /**
     * Start observing network connectivity changes.
     * Should be called during initialization.
     */
    fun startObserving()

    /**
     * Stop observing network connectivity changes.
     * Should be called during cleanup.
     */
    fun stopObserving()
}
