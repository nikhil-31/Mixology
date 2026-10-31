package com.capstone.nik.mixology.Network

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
import java.io.IOException

class ReachabilityInterceptorTest {

    private val monitor = NetworkMonitor.forTests(online = true)

    /** Runs one call whose "network" is [server], so nothing leaves the test. */
    private fun call(server: Interceptor) {
        OkHttpClient.Builder()
            .addInterceptor(ReachabilityInterceptor(monitor))
            .addInterceptor(server)
            .build()
            .newCall(Request.Builder().url("http://mixology.test/").build())
            .execute()
            .close()
    }

    private fun respond(code: Int) = Interceptor { chain ->
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
            .body("".toResponseBody(null))
            .build()
    }

    @Test
    fun ioFailure_marksUnreachable_andRethrows() {
        assertThrows(IOException::class.java) { call { throw IOException("refused") } }
        assertFalse(monitor.serverReachable.value)
    }

    @Test
    fun status503_marksUnreachable() {
        call(respond(503))
        assertFalse(monitor.serverReachable.value)
    }

    @Test
    fun anyOtherAnswer_marksReachableAgain() {
        monitor.markUnreachable()
        call(respond(500))
        assertTrue(monitor.serverReachable.value)
    }
}
