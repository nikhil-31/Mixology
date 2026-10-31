package com.capstone.nik.mixology.Network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Tells [NetworkMonitor] whether the server is answering, from every call that goes through. */
class ReachabilityInterceptor(
    private val networkMonitor: NetworkMonitor,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = try {
            chain.proceed(chain.request())
        } catch (error: IOException) {
            // A cancelled call says nothing about the server.
            if (!chain.call().isCanceled()) networkMonitor.markUnreachable()
            throw error
        }
        if (response.code == SERVICE_UNAVAILABLE) {
            networkMonitor.markUnreachable()
        } else {
            networkMonitor.markReachable()
        }
        return response
    }

    private companion object {
        const val SERVICE_UNAVAILABLE = 503
    }
}
