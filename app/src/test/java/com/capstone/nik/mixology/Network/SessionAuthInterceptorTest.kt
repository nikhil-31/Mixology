package com.capstone.nik.mixology.Network

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.Network.remoteModel.AuthTokenResponse
import com.capstone.nik.mixology.Network.remoteModel.AuthUser
import com.capstone.nik.mixology.Network.remoteModel.LoginRequest
import com.capstone.nik.mixology.Network.remoteModel.ProviderLoginRequest
import com.capstone.nik.mixology.Network.remoteModel.RefreshRequest
import com.capstone.nik.mixology.Network.remoteModel.SignupRequest
import com.capstone.nik.mixology.data.SessionEnd
import com.capstone.nik.mixology.data.SessionStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SessionAuthInterceptorTest {

    /** Hands out "fresh" for "refresh-1", and counts how often it was asked. */
    private class FakeAuthService : AuthService {
        var refreshes = 0
        var failure: Exception? = null

        override suspend fun login(body: LoginRequest): AuthTokenResponse = error("unused")
        override suspend fun signup(body: SignupRequest): AuthTokenResponse = error("unused")
        override suspend fun google(body: ProviderLoginRequest): AuthTokenResponse = error("unused")
        override suspend fun apple(body: ProviderLoginRequest): AuthTokenResponse = error("unused")

        override suspend fun refresh(body: RefreshRequest): AuthTokenResponse {
            refreshes += 1
            failure?.let { throw it }
            check(body.refreshToken == "refresh-1") { "unknown refresh token ${body.refreshToken}" }
            return AuthTokenResponse(
                idToken = "fresh",
                refreshToken = "refresh-2",
                expiresIn = 3600,
                user = AuthUser(uid = "uid-1", email = "ada@example.com", access = "trial"),
            )
        }
    }

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val auth = FakeAuthService()
    private lateinit var sessions: SessionStore
    /** The bearer token of every request that reached the "server". */
    private val seen = mutableListOf<String?>()
    /** Tokens the "server" accepts; anything else gets 401. */
    private val accepted = mutableSetOf("fresh")

    @Before
    fun setUp() {
        context.getSharedPreferences("mixology_session", Context.MODE_PRIVATE).edit().clear().commit()
        sessions = SessionStore(context)
    }

    private fun client(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(SessionAuthInterceptor(sessions, TokenRefresher(auth, sessions)))
        .addInterceptor(
            Interceptor { chain ->
                val token = chain.request().header("Authorization")?.removePrefix("Bearer ")
                seen += token
                val code = if (token in accepted) 200 else 401
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

    private fun get(): Response = client().newCall(Request.Builder().url("http://mixology.test/me").build()).execute()

    @Test
    fun aTokenAboutToRunOut_isRenewedBeforeTheRequest() {
        sessions.save("stale", expiresInSeconds = 10, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")

        val response = get()

        assertEquals(200, response.code)
        assertEquals(listOf("fresh"), seen)
        assertEquals(1, auth.refreshes)
        assertEquals("fresh", sessions.current()?.idToken)
        assertEquals("refresh-2", sessions.current()?.refreshToken)
    }

    @Test
    fun aTokenTheServerRejects_isRenewedAndTheRequestSentAgain() {
        sessions.save("stale", expiresInSeconds = 3600, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")

        val response = get()

        assertEquals(200, response.code)
        assertEquals(listOf("stale", "fresh"), seen)
        assertEquals(1, auth.refreshes)
    }

    @Test
    fun aRefreshTheServerRefuses_endsTheSessionAsExpired() {
        sessions.save("stale", expiresInSeconds = 3600, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")
        auth.failure = HttpException(retrofit2.Response.error<Unit>(401, "{}".toResponseBody(null)))

        val response = get()

        assertEquals(401, response.code)
        assertEquals(listOf("stale"), seen)
        assertNull(sessions.current())
        assertEquals(SessionEnd.EXPIRED, sessions.lastEnd.value)
    }

    @Test
    fun aSessionWithNoRefreshToken_expiresWhenRejected() {
        sessions.save("stale", expiresInSeconds = 3600, "ada@example.com", "Ada", uid = "uid-1")

        val response = get()

        assertEquals(401, response.code)
        assertEquals(0, auth.refreshes)
        assertNull(sessions.current())
        assertEquals(SessionEnd.EXPIRED, sessions.lastEnd.value)
    }

    @Test
    fun aServerFaultDuringRefresh_leavesTheSessionForNextTime() {
        sessions.save("stale", expiresInSeconds = 3600, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")
        auth.failure = HttpException(retrofit2.Response.error<Unit>(503, "{}".toResponseBody(null)))

        val response = get()

        assertEquals(401, response.code)
        assertEquals("stale", sessions.current()?.idToken)
        assertNull(sessions.lastEnd.value)
    }

    @Test
    fun offlineDuringARenewal_failsTheRequestAsOffline_andKeepsTheSession() {
        sessions.save("stale", expiresInSeconds = 10, "ada@example.com", "Ada", uid = "uid-1", refreshToken = "refresh-1")
        auth.failure = IOException("no route to host")

        val thrown = runCatching { get() }.exceptionOrNull()

        assertEquals("no route to host", thrown?.message)
        assertEquals("stale", sessions.current()?.idToken)
    }

    @Test
    fun signedOut_requestsCarryNoToken() {
        val response = get()

        assertEquals(401, response.code)
        assertEquals(listOf<String?>(null), seen)
        assertEquals(0, auth.refreshes)
    }
}
