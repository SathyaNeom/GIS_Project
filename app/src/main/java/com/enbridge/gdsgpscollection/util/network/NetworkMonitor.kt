package com.enbridge.gdsgpscollection.util.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.enbridge.gdsgpscollection.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors network connectivity state changes.
 *
 * Provides real-time updates on network availability using ConnectivityManager.NetworkCallback.
 * This class is injected as a singleton via Hilt and can be used across the application
 * to track connectivity status.
 *
 * @property context Application context for accessing system services
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager: ConnectivityManager? = context.getSystemService()

    companion object {
        private const val TAG = "NetworkMonitor"
    }

    /**
     * Flow that emits network connectivity state changes.
     *
     * Emits `true` when network is available, `false` when unavailable.
     * The flow is hot and will continue emitting until collected.
     *
     * @return Flow<Boolean> representing network availability
     */
    val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks.add(network)
                Logger.d(TAG, "Network available: $network")
                trySend(true)
            }

            override fun onLost(network: Network) {
                networks.remove(network)
                Logger.d(TAG, "Network lost: $network")
                trySend(networks.isNotEmpty())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                Logger.d(TAG, "Network capabilities changed: $network")
                // Network still available
                trySend(networks.isNotEmpty())
            }
        }

        // Initial state check
        val currentState = checkCurrentConnectivity()
        Logger.i(TAG, "Initial connectivity state: $currentState")
        trySend(currentState)

        // Register callback
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, callback)
            ?: run {
                Logger.e(TAG, "ConnectivityManager not available")
                trySend(false)
            }

        awaitClose {
            Logger.d(TAG, "Unregistering network callback")
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Checks current network connectivity synchronously.
     *
     * @return true if connected, false otherwise
     */
    fun isCurrentlyConnected(): Boolean {
        return checkCurrentConnectivity()
    }

    /**
     * Internal helper to check current connectivity state.
     */
    private fun checkCurrentConnectivity(): Boolean {
        val connectivityManager = connectivityManager ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Gets the current network type (WiFi, Cellular, etc.)
     *
     * @return NetworkType enum value
     */
    fun getCurrentNetworkType(): NetworkType {
        val connectivityManager = connectivityManager ?: return NetworkType.NONE

        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return NetworkType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }
    }

    /**
     * Checks if the connection is metered (mobile data, limited WiFi).
     *
     * @return true if metered, false otherwise
     */
    fun isConnectionMetered(): Boolean {
        val connectivityManager = connectivityManager ?: return false
        return connectivityManager.isActiveNetworkMetered
    }
}

/**
 * Enum representing network connection types.
 */
enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER
}
