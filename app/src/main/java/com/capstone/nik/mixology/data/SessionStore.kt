package com.capstone.nik.mixology.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
)

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

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
        val token = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() } ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES, 0L)
        if (expiresAt <= System.currentTimeMillis()) {
            prefs.edit().clear().apply()
            return null
        }
        return Session(
            idToken = token,
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            displayName = prefs.getString(KEY_NAME, "").orEmpty(),
            expiresAtEpochMs = expiresAt,
            uid = prefs.getString(KEY_UID, "").orEmpty(),
            access = prefs.getString(KEY_ACCESS, "").orEmpty(),
        )
    }

    fun save(
        idToken: String,
        expiresInSeconds: Long,
        email: String,
        displayName: String,
        uid: String = "",
        access: String = "",
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
        )
        prefs.edit()
            .putString(KEY_TOKEN, session.idToken)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_NAME, session.displayName)
            .putLong(KEY_EXPIRES, session.expiresAtEpochMs)
            .putString(KEY_UID, session.uid)
            .putString(KEY_ACCESS, session.access)
            .apply()
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

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private companion object {
        const val PREFS = "mixology_session"
        const val KEY_TOKEN = "id_token"
        const val KEY_EMAIL = "email"
        const val KEY_NAME = "display_name"
        const val KEY_EXPIRES = "expires_at"
        const val KEY_UID = "uid"
        const val KEY_ACCESS = "access"
    }
}
