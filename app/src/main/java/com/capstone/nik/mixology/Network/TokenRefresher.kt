package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.RefreshRequest
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trades the session's refresh token for a new id token. One refresh runs at
 * a time; callers that arrive while one is in flight get its result.
 */
@Singleton
class TokenRefresher @Inject constructor(
    private val authService: AuthService,
    private val sessionStore: SessionStore,
) {
    private val lock = Mutex()

    /**
     * The current session with a fresh token, or null when there is no session
     * left: nothing to refresh with, or the server refused, in which case the
     * session is marked expired. [staleToken] is the token the caller just
     * used; when another caller has already replaced it, that result is returned.
     * Throws [java.io.IOException] when the server could not be reached.
     */
    suspend fun refresh(staleToken: String?): Session? = lock.withLock {
        val current = sessionStore.current() ?: return null
        if (staleToken != null && current.idToken != staleToken && !current.needsRefresh()) return current
        if (current.refreshToken.isBlank()) {
            sessionStore.expire()
            return null
        }
        val response = try {
            authService.refresh(RefreshRequest(current.refreshToken))
        } catch (error: HttpException) {
            // The server knows this token and won't renew it: the user has to sign in again.
            // A server fault leaves the session alone, so the next call can try again.
            if (error.code() in 400..499) sessionStore.expire()
            return null
        }
        if (response.idToken.isBlank()) {
            sessionStore.expire()
            return null
        }
        sessionStore.save(
            idToken = response.idToken,
            expiresInSeconds = response.expiresIn,
            email = response.user.email ?: current.email,
            displayName = response.user.displayName ?: current.displayName,
            uid = response.user.uid.ifEmpty { current.uid },
            access = response.user.access.ifEmpty { current.access },
            refreshToken = response.refreshToken.ifEmpty { current.refreshToken },
        )
    }
}
