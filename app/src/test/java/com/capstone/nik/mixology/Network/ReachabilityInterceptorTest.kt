package com.capstone.nik.mixology.Network

import android.app.Application
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ReachabilityInterceptorTest {

    private val monitor = NetworkMonitor.forTests(online = true)

    @Test
    fun refusedConnection_marksUnreachable_thenAnAnswerClearsIt() {
        val refused = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .addInterceptor(ReachabilityInterceptor(monitor))
            .build()
        assertThrows(IOException::class.java) {
            refused.newCall(Request.Builder().url("http://127.0.0.1:${closedPort()}/").build()).execute()
        }
        assertFalse(monitor.serverReachable.value)

        answering(200).newCall(request()).execute().close()
        assertTrue(monitor.serverReachable.value)
    }

    @Test
    fun status503_marksUnreachable() {
        answering(503).newCall(request()).execute().close()
        assertFalse(monitor.serverReachable.value)
    }

    @Test
    fun cancelledCall_leavesReachabilityAlone() {
        val client = OkHttpClient.Builder()
            .addInterceptor(ReachabilityInterceptor(monitor))
            .addInterceptor { chain ->
                chain.call().cancel()
                throw IOException("Canceled")
            }
            .build()
        assertThrows(IOException::class.java) { client.newCall(request()).execute() }
        assertTrue(monitor.serverReachable.value)
    }

    /** A client whose calls never leave the device and get [code] back. */
    private fun answering(code: Int): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ReachabilityInterceptor(monitor))
        .addInterceptor(
            Interceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("Status $code")
                    .body("{}".toResponseBody(null))
                    .build()
            },
        )
        .build()

    private fun request() = Request.Builder().url("http://mixology.test/").build()

    /** A local port with nothing listening on it. */
    private fun closedPort(): Int = ServerSocket(0).use { it.localPort }
}
