package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.Session
import kotlinx.coroutines.flow.Flow

data class ChatTurn(
    val role: String,
    val content: String,
    val drinks: List<Drink> = emptyList(),
    /** Server message id, used to page backwards; 0 when unknown. */
    val serverId: Long = 0,
)

/** One page of chat, oldest first. [hasMore] means older messages remain. */
data class ChatPage(
    val turns: List<ChatTurn>,
    val hasMore: Boolean,
)

sealed interface AuthResult {
    data class Success(val session: Session) : AuthResult
    data class Failure(val detail: String) : AuthResult
}

interface ChatRepository {
    fun currentSession(): Session?

    suspend fun signIn(email: String, password: String): AuthResult

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult

    /** Trades a Firebase ID token from Google sign-in for a backend session. */
    suspend fun signInWithGoogle(firebaseIdToken: String): AuthResult

    /** Trades a Firebase ID token from Sign in with Apple for a backend session. */
    suspend fun signInWithApple(firebaseIdToken: String, displayName: String?): AuthResult

    fun signOut()

    /** The newest page of chat, or the page just before message id [before]. */
    suspend fun history(before: Long? = null): ChatPage

    fun send(message: String): Flow<ChatStreamEvent>
}
