package com.capstone.nik.mixology.repository

import android.util.Log
import com.capstone.nik.mixology.Network.AccountService
import com.capstone.nik.mixology.data.GoogleAuth
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/** Ending or deleting the signed-in account. Every sign-out in the app goes through here. */
interface AuthRepository {
    /**
     * Signs out everywhere: the server session, Firebase, the device's saved
     * Google credential and this app. Never throws; offline, the server
     * session is left to expire on its own.
     */
    suspend fun signOut()

    /** Deletes the account and all its data on the server, then signs out here. Throws when the server didn't. */
    suspend fun deleteAccount()

    /** The account's data as JSON text. Throws when it can't be fetched. */
    suspend fun exportData(): String
}

@Singleton
class NetworkAuthRepository @Inject constructor(
    private val accountService: AccountService,
    private val sessionStore: SessionStore,
    private val googleAuth: GoogleAuth,
) : AuthRepository {

    override suspend fun signOut() {
        if (sessionStore.current() != null) {
            try {
                accountService.logout()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(TAG, "Server logout failed; signing out locally anyway", error)
            }
        }
        forgetLocally()
    }

    override suspend fun deleteAccount() {
        val response = accountService.deleteAccount()
        if (!response.isSuccessful) throw HttpException(response)
        forgetLocally()
    }

    override suspend fun exportData(): String = withContext(Dispatchers.IO) {
        accountService.export().use { it.string() }
    }

    private suspend fun forgetLocally() {
        sessionStore.clear()
        googleAuth.signOut()
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
