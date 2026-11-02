package com.example.feature.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.Closeable

enum class ConnectivityStatus {
    Online,
    Offline
}

interface ConnectivityMonitor {
    val status: StateFlow<ConnectivityStatus>
    val isOnline: StateFlow<Boolean>

    fun isOnlineNow(): Boolean = status.value == ConnectivityStatus.Online
}

/**
 * Process-scoped connectivity monitor. It reports validated internet access,
 * not merely the presence of a Wi-Fi or cellular transport.
 */
class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor, Closeable {
    private val connectivityManager =
        context.applicationContext.getSystemService(ConnectivityManager::class.java)

    private val _status = MutableStateFlow(currentStatus())
    override val status: StateFlow<ConnectivityStatus> = _status.asStateFlow()

    private val _isOnline = MutableStateFlow(_status.value == ConnectivityStatus.Online)
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refresh()

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            refresh()
        }

        override fun onLost(network: Network) = refresh()
    }

    init {
        runCatching {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                callback
            )
        }
    }

    override fun isOnlineNow(): Boolean = currentStatus() == ConnectivityStatus.Online

    private fun refresh() {
        val next = currentStatus()
        _status.value = next
        _isOnline.value = next == ConnectivityStatus.Online
    }

    private fun currentStatus(): ConnectivityStatus {
        val network = connectivityManager.activeNetwork ?: return ConnectivityStatus.Offline
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectivityStatus.Offline
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        return if (hasInternet && validated) ConnectivityStatus.Online else ConnectivityStatus.Offline
    }

    override fun close() {
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
