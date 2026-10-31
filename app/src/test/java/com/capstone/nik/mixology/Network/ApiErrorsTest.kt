package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.repository.RecipeHttpException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiErrorsTest {

    @Test
    fun ioFailures_areNoConnectionWhileOffline() {
        listOf(UnknownHostException(), ConnectException(), SocketTimeoutException()).forEach { error ->
            assertEquals(ConnectionError.NO_CONNECTION, ApiErrors.connectionError(error, online = false))
        }
    }

    @Test
    fun ioFailures_areServerUnreachableWhileOnline() {
        listOf(UnknownHostException(), ConnectException(), SocketTimeoutException()).forEach { error ->
            assertEquals(ConnectionError.SERVER_UNREACHABLE, ApiErrors.connectionError(error, online = true))
        }
    }

    @Test
    fun status503_isServerUnreachableInEveryWrapper() {
        listOf(
            FakeRecipeService.httpError(503),
            ChatHttpException(503, "Service Unavailable"),
            RecipeHttpException(503, "Service Unavailable"),
        ).forEach { error ->
            assertEquals(ConnectionError.SERVER_UNREACHABLE, ApiErrors.connectionError(error, online = true))
        }
    }

    @Test
    fun answersAndOtherFailures_areNotConnectionErrors() {
        listOf(
            FakeRecipeService.httpError(500),
            ChatHttpException(402, "Out of credits"),
            RecipeHttpException(502, "Bad gateway"),
            IllegalStateException("Malformed recipe"),
            CancellationException(),
        ).forEach { error ->
            assertNull(ApiErrors.connectionError(error, online = true))
        }
    }
}
