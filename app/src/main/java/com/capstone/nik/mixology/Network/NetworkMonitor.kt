package com.capstone.nik.mixology.Network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor internal constructor(
    private val connectivityManager: ConnectivityManager?,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager,
    )

    private val _online = MutableStateFlow(currentlyOnline())
    val online: StateFlow<Boolean> = _online.asStateFlow()

    private val _retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val retries: SharedFlow<Unit> = _retries.asSharedFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _online.value = currentlyOnline()
        }

        override fun onLost(network: Network) {
            _online.value = currentlyOnline()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _online.value = currentlyOnline()
        }
    }

    fun start() {
        val manager = connectivityManager ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching { manager.registerNetworkCallback(request, callback) }
        _online.value = currentlyOnline()
    }

    fun retry() {
        _online.value = currentlyOnline()
        _retries.tryEmit(Unit)
    }

    private fun currentlyOnline(): Boolean {
        val manager = connectivityManager ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        fun forTests(online: Boolean = true): NetworkMonitor {
            return NetworkMonitor(null).also { it._online.value = online }
        }
    }
}
