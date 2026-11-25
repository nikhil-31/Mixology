package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.repository.RecipeHttpException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ApiErrorsConnectionTest {

    private fun http(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    @Test
    fun ioException_offline_isNoConnection() {
        assertEquals(ConnectionError.NO_CONNECTION, ApiErrors.connectionError(IOException(), online = false))
    }

    @Test
    fun ioException_online_isServerUnreachable() {
        assertEquals(
            ConnectionError.SERVER_UNREACHABLE,
            ApiErrors.connectionError(SocketTimeoutException(), online = true),
        )
    }

    @Test
    fun status503_fromEveryHttpErrorType_isServerUnreachable() {
        listOf(http(503), ChatHttpException(503, "down"), RecipeHttpException(503, "down")).forEach {
            assertEquals(ConnectionError.SERVER_UNREACHABLE, ApiErrors.connectionError(it, online = true))
        }
    }

    @Test
    fun otherStatuses_areTheServersOwnError() {
        listOf(http(500), http(404), ChatHttpException(402, "no credits"), RecipeHttpException(422, "bad")).forEach {
            assertNull(ApiErrors.connectionError(it, online = true))
        }
    }

    @Test
    fun unrelatedException_isNotAConnectionError() {
        assertNull(ApiErrors.connectionError(IllegalStateException(), online = false))
    }

    @Test
    fun networkMonitorHelpers_useCurrentOnlineState() {
        val offline = NetworkMonitor.forTests(online = false)
        assertEquals(ConnectionError.NO_CONNECTION, offline.connectionError(IOException()))
        assertEquals(ConnectionError.NO_CONNECTION.messageRes, offline.errorMessageRes(IOException(), fallback = 0))
        assertEquals(42, offline.errorMessageRes(http(500), fallback = 42))
    }
}
