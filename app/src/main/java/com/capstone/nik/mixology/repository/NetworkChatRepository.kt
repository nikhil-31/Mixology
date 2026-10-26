package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.ApiErrors
import com.capstone.nik.mixology.Network.AuthService
import com.capstone.nik.mixology.Network.ChatHttpException
import com.capstone.nik.mixology.Network.ChatService
import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.Network.ChatStreamer
import com.capstone.nik.mixology.Network.remoteModel.AuthTokenResponse
import com.capstone.nik.mixology.Network.remoteModel.ProviderLoginRequest
import com.capstone.nik.mixology.Network.remoteModel.LoginRequest
import com.capstone.nik.mixology.Network.remoteModel.SignupRequest
import com.capstone.nik.mixology.data.GoogleAuth
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkChatRepository @Inject constructor(
    private val authService: AuthService,
    private val chatService: ChatService,
    private val streamer: ChatStreamer,
    private val sessionStore: SessionStore,
    private val googleAuth: GoogleAuth,
) : ChatRepository {

    override fun currentSession(): Session? = sessionStore.current()

    override suspend fun signIn(email: String, password: String): AuthResult {
        return authenticate(email.trim(), null) {
            authService.login(LoginRequest(email.trim(), password))
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        val name = displayName.trim().takeIf { it.isNotEmpty() }
        return authenticate(email.trim(), name) {
            authService.signup(SignupRequest(email.trim(), password, name))
        }
    }

    override suspend fun signInWithGoogle(firebaseIdToken: String): AuthResult {
        return authenticate("", null) {
            authService.google(ProviderLoginRequest(firebaseIdToken))
        }
    }

    override suspend fun signInWithApple(firebaseIdToken: String, displayName: String?): AuthResult {
        return authenticate("", displayName) {
            authService.apple(ProviderLoginRequest(firebaseIdToken, displayName))
        }
    }

    override fun signOut() {
        sessionStore.clear()
        googleAuth.signOut()
    }

    override suspend fun history(before: Long?): ChatPage {
        return try {
            val page = chatService.history(limit = CHAT_PAGE_SIZE, before = before)
            ChatPage(
                turns = page.messages.map { message ->
                    ChatTurn(
                        role = message.role,
                        content = message.content,
                        drinks = message.drinks.orEmpty().mapNotNull { it.toDrink() },
                        serverId = message.id,
                    )
                },
                hasMore = page.hasMore,
            )
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            throw chatFailure(error.code(), error.response()?.errorBody()?.string(), error.message())
        }
    }

    override fun send(message: String): Flow<ChatStreamEvent> {
        if (sessionStore.current() == null) {
            return flow {
                throw ChatHttpException(401, "Not authenticated")
            }
        }
        return streamer.stream(message).catch { error ->
            if (error is CancellationException) throw error
            if (error is ChatHttpException && error.code == 401) {
                sessionStore.clear()
            }
            throw error
        }
    }

    private suspend fun authenticate(
        email: String,
        displayName: String?,
        call: suspend () -> AuthTokenResponse,
    ): AuthResult {
        return try {
            val response = call()
            if (response.idToken.isBlank()) {
                AuthResult.Failure("Not authenticated")
            } else {
                val session = sessionStore.save(
                    idToken = response.idToken,
                    expiresInSeconds = response.expiresIn,
                    email = response.user.email ?: email,
                    displayName = response.user.displayName ?: displayName.orEmpty(),
                )
                AuthResult.Success(session)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: HttpException) {
            AuthResult.Failure(
                ApiErrors.detail(error.response()?.errorBody()?.string(), error.message()),
            )
        } catch (error: Exception) {
            AuthResult.Failure(error.message.orEmpty())
        }
    }

    private fun chatFailure(code: Int, body: String?, fallback: String): ChatHttpException {
        if (code == 401) sessionStore.clear()
        return ChatHttpException(code, ApiErrors.detail(body, fallback))
    }
}

/** Messages per chat page; enough to fill a screen with some to scroll. */
private const val CHAT_PAGE_SIZE = 20
