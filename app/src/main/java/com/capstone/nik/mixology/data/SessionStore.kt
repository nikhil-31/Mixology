package com.capstone.nik.mixology.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class Session(
    val idToken: String,
    val email: String,
    val displayName: String,
    val expiresAtEpochMs: Long,
    /** Empty for sessions saved before the app stored it. */
    val uid: String = "",
    /** Last known `trial`, `subscribed` or `free`; refreshed from `/billing/status`. */
    val access: String = "",
    /** Trades for a new [idToken] at `POST /auth/refresh`; empty for sessions saved before the app kept it. */
    val refreshToken: String = "",
) {
    /** The token has run out, or is about to, and should be renewed before use. */
    fun needsRefresh(now: Long = System.currentTimeMillis()): Boolean =
        expiresAtEpochMs - now < REFRESH_AHEAD_MS

    private companion object {
        const val REFRESH_AHEAD_MS = 60_000L
    }
}

/** Why [SessionStore.session] is null. */
enum class SessionEnd {
    /** The user signed out (or deleted the account): forget the account's data on this device. */
    SIGNED_OUT,
    /** The token ran out or the server rejected it: the same account will sign in again, so keep its data. */
    EXPIRED,
}

@Singleton
class SessionStore internal constructor(
    context: Context,
    private val cipher: TokenCipher,
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context, KeystoreTokenCipher())

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Stored value to token, so reading the session doesn't go to the keystore on every request. */
    private val opened = ConcurrentHashMap<String, String>()

    private val _lastEnd = MutableStateFlow<SessionEnd?>(null)

    /** How the last session ended, or null when none has ended since the app started. */
    val lastEnd: StateFlow<SessionEnd?> = _lastEnd.asStateFlow()

    private val _session = MutableStateFlow(read())

    /** The signed-in session, or null once it is cleared or found expired. */
    val session: StateFlow<Session?> = _session.asStateFlow()

    fun current(): Session? {
        val session = read()
        if (session == null && _session.value != null) {
            _session.value = null
        }
        return session
    }

    private fun read(): Session? {
        val storedToken = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() } ?: return null
        val storedRefresh = prefs.getString(KEY_REFRESH, "").orEmpty()
        val token = open(storedToken)
        val refreshToken = open(storedRefresh)
        if (token == null || refreshToken == null) {
            // The key that sealed the tokens is gone, so they are too: sign in again.
            prefs.edit().clear().apply()
            _lastEnd.value = SessionEnd.EXPIRED
            return null
        }
        sealIfPlain(storedToken, storedRefresh)
        val expiresAt = prefs.getLong(KEY_EXPIRES, 0L)
        // A run-out token with nothing to renew it is gone; with a refresh token it is
        // still a session, and the next request renews it first.
        if (expiresAt <= System.currentTimeMillis() && refreshToken.isBlank()) {
            prefs.edit().clear().apply()
            _lastEnd.value = SessionEnd.EXPIRED
            return null
        }
        return Session(
            idToken = token,
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            displayName = prefs.getString(KEY_NAME, "").orEmpty(),
            expiresAtEpochMs = expiresAt,
            uid = prefs.getString(KEY_UID, "").orEmpty(),
            access = prefs.getString(KEY_ACCESS, "").orEmpty(),
            refreshToken = refreshToken,
        )
    }

    fun save(
        idToken: String,
        expiresInSeconds: Long,
        email: String,
        displayName: String,
        uid: String = "",
        access: String = "",
        refreshToken: String = "",
    ): Session {
        val lifetimeMs = expiresInSeconds.coerceAtLeast(1L) * 1000L
        val skewMs = if (lifetimeMs > 60_000L) 30_000L else 0L
        val session = Session(
            idToken = idToken,
            email = email,
            displayName = displayName,
            expiresAtEpochMs = System.currentTimeMillis() + lifetimeMs - skewMs,
            uid = uid,
            access = access,
            refreshToken = refreshToken,
        )
        prefs.edit()
            .putString(KEY_TOKEN, seal(session.idToken))
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_NAME, session.displayName)
            .putLong(KEY_EXPIRES, session.expiresAtEpochMs)
            .putString(KEY_UID, session.uid)
            .putString(KEY_ACCESS, session.access)
            .putString(KEY_REFRESH, seal(session.refreshToken))
            .apply()
        _lastEnd.value = null
        _session.value = session
        return session
    }

    /** Remembers the latest access level so the next launch opens to the right screen. */
    fun updateAccess(access: String) {
        val session = _session.value ?: return
        if (session.access == access) return
        prefs.edit().putString(KEY_ACCESS, access).apply()
        _session.value = session.copy(access = access)
    }

    /** The user signed out. */
    fun clear() = end(SessionEnd.SIGNED_OUT)

    /** The server no longer accepts the token; the user has to sign in again. */
    fun expire() = end(SessionEnd.EXPIRED)

    /** [token] as it is stored: sealed, or as it is when the keystore can't be used. */
    private fun seal(token: String): String {
        if (token.isEmpty()) return token
        val sealed = cipher.seal(token) ?: return token
        return (SEALED + sealed).also { opened[it] = token }
    }

    /** The token behind [stored], which is sealed or was saved before tokens were; null when it can't be opened. */
    private fun open(stored: String): String? {
        if (!stored.startsWith(SEALED)) return stored
        opened[stored]?.let { return it }
        return cipher.open(stored.removePrefix(SEALED))?.also { opened[stored] = it }
    }

    /** Seals tokens an older version of the app left readable in the prefs file. */
    private fun sealIfPlain(storedToken: String, storedRefresh: String) {
        if (storedToken.startsWith(SEALED) && (storedRefresh.isEmpty() || storedRefresh.startsWith(SEALED))) return
        val token = seal(open(storedToken) ?: return)
        val refresh = seal(open(storedRefresh) ?: return)
        if (token == storedToken && refresh == storedRefresh) return
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_REFRESH, refresh).apply()
    }

    private fun end(reason: SessionEnd) {
        opened.clear()
        prefs.edit().clear().apply()
        _lastEnd.value = reason
        _session.value = null
    }

    /** Whether [session] went null because the user signed out, rather than the token expiring. */
    fun signedOut(): Boolean = _lastEnd.value == SessionEnd.SIGNED_OUT

    private companion object {
        const val PREFS = "mixology_session"
        const val KEY_TOKEN = "id_token"
        const val KEY_EMAIL = "email"
        const val KEY_NAME = "display_name"
        const val KEY_EXPIRES = "expires_at"
        const val KEY_UID = "uid"
        const val KEY_ACCESS = "access"
        const val KEY_REFRESH = "refresh_token"

        /** Marks a stored token as sealed by [TokenCipher]; one without it is from before tokens were sealed. */
        const val SEALED = "sealed1:"
    }
}
