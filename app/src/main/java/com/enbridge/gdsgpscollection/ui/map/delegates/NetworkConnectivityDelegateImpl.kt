package com.enbridge.gdsgpscollection.ui.map.delegates

import com.enbridge.gdsgpscollection.di.DelegateCoroutineScope
import com.enbridge.gdsgpscollection.util.Logger
import com.enbridge.gdsgpscollection.util.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of NetworkConnectivityDelegate.
 *
 * Monitors network connectivity and exposes offline state to the UI.
 *
 * @author Sathya Narayanan
 */
@Singleton
class NetworkConnectivityDelegateImpl @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    @DelegateCoroutineScope private val coroutineScope: CoroutineScope
) : NetworkConnectivityDelegate {

    private val _isOffline = MutableStateFlow(false)
    override val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private var observeJob: Job? = null

    companion object {
        private const val TAG = "NetworkConnectivityDelegate"
    }

    override fun startObserving() {
        observeJob?.cancel()
        observeJob = coroutineScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _isOffline.value = !isConnected

                // Log connectivity state changes
                if (isConnected) {
                    Logger.i(TAG, "Network connection restored")
                } else {
                    Logger.w(TAG, "Network connection lost - Operating in offline mode")
                }
            }
        }
        Logger.d(TAG, "Started observing network connectivity")
    }

    override fun stopObserving() {
        observeJob?.cancel()
        observeJob = null
        Logger.d(TAG, "Stopped observing network connectivity")
    }
}
