package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Signs requests with the session's bearer token. A token about to run out is
 * renewed first; one the server rejects is renewed and the request sent again.
 */
class SessionAuthInterceptor(
    private val sessionStore: SessionStore,
    private val refresher: TokenRefresher,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val initial = sessionStore.current()
        val session = if (initial != null && initial.needsRefresh()) {
            runBlocking { refresher.refresh(initial.idToken) } ?: sessionStore.current()
        } else {
            initial
        }
        val response = chain.proceed(chain.request().withBearer(session))
        if (response.code != UNAUTHORIZED || session == null) return response
        val renewed = runBlocking { refresher.refresh(session.idToken) } ?: return response
        if (renewed.idToken == session.idToken) return response
        response.close()
        return chain.proceed(chain.request().withBearer(renewed))
    }

    private fun Request.withBearer(session: Session?): Request {
        val token = session?.idToken?.takeIf { it.isNotBlank() } ?: return this
        return newBuilder().header("Authorization", "Bearer $token").build()
    }

    private companion object {
        const val UNAUTHORIZED = 401
    }
}
