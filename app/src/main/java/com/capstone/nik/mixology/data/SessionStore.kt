package com.capstone.nik.mixology.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class Session(
    val idToken: String,
    val email: String,
    val displayName: String,
    val expiresAtEpochMs: Long,
)

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun current(): Session? {
        val token = prefs.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() } ?: return null
        val expiresAt = prefs.getLong(KEY_EXPIRES, 0L)
        if (expiresAt <= System.currentTimeMillis()) {
            clear()
            return null
        }
        return Session(
            idToken = token,
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
            displayName = prefs.getString(KEY_NAME, "").orEmpty(),
            expiresAtEpochMs = expiresAt,
        )
    }

    fun save(
        idToken: String,
        expiresInSeconds: Long,
        email: String,
        displayName: String,
    ): Session {
        val lifetimeMs = expiresInSeconds.coerceAtLeast(1L) * 1000L
        val skewMs = if (lifetimeMs > 60_000L) 30_000L else 0L
        val session = Session(
            idToken = idToken,
            email = email,
            displayName = displayName,
            expiresAtEpochMs = System.currentTimeMillis() + lifetimeMs - skewMs,
        )
        prefs.edit()
            .putString(KEY_TOKEN, session.idToken)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_NAME, session.displayName)
            .putLong(KEY_EXPIRES, session.expiresAtEpochMs)
            .apply()
        return session
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS = "mixology_session"
        const val KEY_TOKEN = "id_token"
        const val KEY_EMAIL = "email"
        const val KEY_NAME = "display_name"
        const val KEY_EXPIRES = "expires_at"
    }
}
