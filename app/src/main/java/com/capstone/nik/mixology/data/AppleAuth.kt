package com.capstone.nik.mixology.data

import android.app.Activity
import android.content.Context
import com.capstone.nik.mixology.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.OAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AppleAuthResult {
    /**
     * Firebase ID token for `POST /auth/apple`. Apple shares the name only on
     * the first sign-in, so [displayName] is usually null afterwards.
     */
    data class Success(val firebaseIdToken: String, val displayName: String?) : AppleAuthResult
    data object Cancelled : AppleAuthResult
    data class Failure(val detail: String) : AppleAuthResult
}

interface AppleAuth {
    /** Opens Apple's sign-in page (Android has no native Apple sheet) and signs in to Firebase. */
    suspend fun signIn(activity: Activity): AppleAuthResult
}

@Singleton
class FirebaseAppleAuth @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppleAuth {

    override suspend fun signIn(activity: Activity): AppleAuthResult {
        val auth = FirebaseAuth.getInstance()
        val provider = OAuthProvider.newBuilder("apple.com")
            .setScopes(listOf("email", "name"))
            .build()
        return try {
            // A sign-in started before the activity was recreated finishes here.
            val result = auth.pendingAuthResult?.await()
                ?: auth.startActivityForSignInWithProvider(activity, provider).await()
            val user = result.user ?: return failure()
            val token = user.getIdToken(true).await().token ?: return failure()
            AppleAuthResult.Success(token, user.displayName?.takeIf { it.isNotBlank() })
        } catch (error: CancellationException) {
            throw error
        } catch (error: FirebaseAuthException) {
            if (error.errorCode == WEB_CONTEXT_CANCELED) AppleAuthResult.Cancelled else failure()
        } catch (error: Exception) {
            failure()
        }
    }

    private fun failure() = AppleAuthResult.Failure(context.getString(R.string.auth_apple_failed))

    private companion object {
        /** The user closed Apple's page without signing in. */
        const val WEB_CONTEXT_CANCELED = "ERROR_WEB_CONTEXT_CANCELED"
    }
}
