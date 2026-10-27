package com.capstone.nik.mixology

import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.repository.AuthResult
import com.capstone.nik.mixology.repository.ChatPage
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.repository.ChatTurn
import com.capstone.nik.mixology.repository.DrinkMatchPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeChatRepository : ChatRepository {
    var session: Session? = null
    var history: List<ChatTurn> = emptyList()
    var signInResult: AuthResult = AuthResult.Success(
        Session(
            idToken = "token",
            email = "ada@mixology.test",
            displayName = "Ada",
            expiresAtEpochMs = Long.MAX_VALUE,
        ),
    )
    var sendFlow: Flow<ChatStreamEvent> = flowOf()
    val sent = mutableListOf<String>()
    val signUps = mutableListOf<Triple<String, String, String>>()
    val googleTokens = mutableListOf<String>()
    val appleSignIns = mutableListOf<Pair<String, String?>>()
    var signedOut = false

    override fun currentSession(): Session? = session

    override suspend fun signIn(email: String, password: String): AuthResult {
        val result = signInResult
        if (result is AuthResult.Success) session = result.session
        return result
    }

    override suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        signUps += Triple(email, password, displayName)
        return signIn(email, password)
    }

    override suspend fun signInWithGoogle(firebaseIdToken: String): AuthResult {
        googleTokens += firebaseIdToken
        return signIn("", "")
    }

    override suspend fun signInWithApple(firebaseIdToken: String, displayName: String?): AuthResult {
        appleSignIns += firebaseIdToken to displayName
        return signIn("", "")
    }

    override fun signOut() {
        session = null
        signedOut = true
    }

    /** Messages per page; unlimited unless a test pages on purpose. */
    var pageSize = Int.MAX_VALUE
    val historyRequests = mutableListOf<Long?>()

    override suspend fun history(before: Long?): ChatPage {
        historyRequests += before
        val older = if (before == null) history else history.filter { it.serverId in 1 until before }
        val page = older.takeLast(pageSize)
        return ChatPage(page, hasMore = older.size > page.size)
    }

    /** Drinks [matches] serves for any query, [matchPageSize] at a time. */
    var matchDrinks: List<Drink> = emptyList()
    var matchPageSize = 20
    var matchError: Exception? = null
    /** The search [matches] reports, when it differs from the request. */
    var matchQuery: String? = null
    val matchRequests = mutableListOf<Triple<String, List<String>, Int>>()

    override suspend fun matches(query: String, exclude: List<String>, offset: Int): DrinkMatchPage {
        matchRequests += Triple(query, exclude, offset)
        matchError?.let { throw it }
        val rest = matchDrinks.filter { it.id !in exclude }.drop(offset)
        return DrinkMatchPage(
            query = matchQuery ?: query,
            drinks = rest.take(matchPageSize),
            hasMore = rest.size > matchPageSize,
        )
    }

    override fun send(message: String): Flow<ChatStreamEvent> {
        sent += message
        return sendFlow
    }
}
