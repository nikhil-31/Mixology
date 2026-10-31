package com.capstone.nik.mixology.repository

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeGoogleAuth
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.Network.AuthService
import com.capstone.nik.mixology.Network.ChatService
import com.capstone.nik.mixology.Network.ChatStreamer
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.remoteModel.AuthTokenResponse
import com.capstone.nik.mixology.Network.remoteModel.ChatHistoryResponse
import com.capstone.nik.mixology.Network.remoteModel.ChatMatchesResponse
import com.capstone.nik.mixology.Network.remoteModel.LoginRequest
import com.capstone.nik.mixology.Network.remoteModel.ProviderLoginRequest
import com.capstone.nik.mixology.Network.remoteModel.SignupRequest
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.ConnectException

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class NetworkChatRepositoryAuthTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val auth = FailingAuthService()

    @Test
    fun signIn_offline_isTheLocalizedOfflineMessage() = runTest {
        auth.failure = ConnectException("Failed to connect to /10.0.2.2:8000")

        val result = repository(online = false).signIn("ada@example.com", "secret")

        assertEquals(AuthResult.Failure("You’re offline. Check your connection and try again."), result)
    }

    @Test
    fun signIn_serverDown_isTheLocalizedUnreachableMessage() = runTest {
        auth.failure = ConnectException("Failed to connect to /10.0.2.2:8000")

        val result = repository(online = true).signIn("ada@example.com", "secret")

        assertEquals(AuthResult.Failure("Can’t reach Mixology right now. Try again in a moment."), result)
    }

    @Test
    fun signIn_serverRefusal_keepsTheServerDetail() = runTest {
        auth.failure = FakeRecipeService.httpError(401, "Wrong email or password")

        val result = repository(online = true).signIn("ada@example.com", "secret")

        assertEquals(AuthResult.Failure("Wrong email or password"), result)
    }

    private fun repository(online: Boolean): NetworkChatRepository {
        val sessions = SessionStore(context).also { it.clear() }
        return NetworkChatRepository(
            authService = auth,
            chatService = UnusedChatService,
            streamer = ChatStreamer(OkHttpClient()),
            sessionStore = sessions,
            googleAuth = FakeGoogleAuth(),
            subscriptions = SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions),
            networkMonitor = NetworkMonitor.forTests(online = online),
            context = context,
        )
    }
}

private class FailingAuthService : AuthService {
    var failure: Exception = IllegalStateException("unset")

    override suspend fun login(body: LoginRequest): AuthTokenResponse = throw failure
    override suspend fun signup(body: SignupRequest): AuthTokenResponse = throw failure
    override suspend fun google(body: ProviderLoginRequest): AuthTokenResponse = throw failure
    override suspend fun apple(body: ProviderLoginRequest): AuthTokenResponse = throw failure
}

private object UnusedChatService : ChatService {
    override suspend fun history(limit: Int?, before: Long?): ChatHistoryResponse = error("unused")
    override suspend fun matches(q: String, exclude: String?, offset: Int, limit: Int?): ChatMatchesResponse =
        error("unused")
}
