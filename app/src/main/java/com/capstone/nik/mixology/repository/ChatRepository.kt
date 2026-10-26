package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.Session
import kotlinx.coroutines.flow.Flow

data class ChatTurn(
    val role: String,
    val content: String,
    val drinks: List<Drink> = emptyList(),
)

sealed interface AuthResult {
    data class Success(val session: Session) : AuthResult
    data class Failure(val detail: String) : AuthResult
}

interface ChatRepository {
    fun currentSession(): Session?

    suspend fun signIn(email: String, password: String): AuthResult

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult

    fun signOut()

    suspend fun history(): List<ChatTurn>

    fun send(message: String): Flow<ChatStreamEvent>
}
