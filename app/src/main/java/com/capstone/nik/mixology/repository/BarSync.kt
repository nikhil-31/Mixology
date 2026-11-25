package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.capstone.nik.mixology.Network.BarIngredientBody
import com.capstone.nik.mixology.Network.BarIngredientsBody
import com.capstone.nik.mixology.Network.BarService
import com.capstone.nik.mixology.Network.BarStateDto
import com.capstone.nik.mixology.data.BarDao
import com.capstone.nik.mixology.data.BarIngredientEntity
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps My Bar in step with the signed-in account, so recommendations, Ada and
 * other devices see the same ingredients as this one.
 */
interface BarSync {
    /**
     * What the account's bar can make, from the server's last /bar response: the user's own
     * recipes first, then catalog drinks ranked by taste. Saved flags are not applied.
     * Null when signed out or before the first response.
     */
    val recommendations: StateFlow<BarRecommendations?>
        get() = NO_RECOMMENDATIONS

    /** Sends one add or remove to the server; failures are retried by the next [pull]. */
    fun push(name: String, owned: Boolean)

    /** Replaces the local bar with the account's, keeping changes the server has not seen yet. */
    suspend fun pull()

    companion object {
        private val NO_RECOMMENDATIONS: StateFlow<BarRecommendations?> = MutableStateFlow(null)

        /** For tests and previews: the bar stays on the device. */
        val None = object : BarSync {
            override fun push(name: String, owned: Boolean) = Unit
            override suspend fun pull() = Unit
        }
    }
}

@Singleton
class NetworkBarSync internal constructor(
    private val service: BarService,
    private val dao: BarDao,
    private val sessionStore: SessionStore,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
) : BarSync {

    @Inject
    constructor(
        service: BarService,
        dao: BarDao,
        sessionStore: SessionStore,
        @ApplicationContext context: Context,
    ) : this(
        service,
        dao,
        sessionStore,
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE),
        CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    private val lock = Mutex()
    /**
     * Adds and removes the server has not acknowledged yet, keyed by lowercase name.
     * Mirrored in [prefs], so an edit made offline survives the process dying.
     */
    private val pending = mutableMapOf<String, Change>().apply { putAll(loadPending()) }

    private data class Change(val name: String, val owned: Boolean)

    private val latest = MutableStateFlow<BarRecommendations?>(null)
    override val recommendations: StateFlow<BarRecommendations?> = latest.asStateFlow()

    init {
        scope.launch {
            // Signing in (or starting signed in) brings the account's bar down;
            // signing out forgets the account's recommendations.
            sessionStore.session.collect { session ->
                if (session != null) {
                    pull()
                } else {
                    latest.value = null
                    // Signed out: the account's unsent edits go with it. An expired
                    // session keeps them for when the same account signs back in.
                    if (sessionStore.signedOut()) synchronized(pending) { pending.clear(); persistPending() }
                }
            }
        }
    }

    override fun push(name: String, owned: Boolean) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || sessionStore.current() == null) return
        val change = Change(trimmed, owned)
        synchronized(pending) {
            pending[trimmed.lowercase()] = change
            persistPending()
        }
        scope.launch {
            lock.withLock { runCatchingNetwork { send(change) } }
        }
    }

    override suspend fun pull() {
        val session = sessionStore.current() ?: return
        lock.withLock {
            runCatchingNetwork { pull(session) }
        }
    }

    private suspend fun pull(session: Session) {
        pendingChanges().forEach { send(it) }
        var state = service.bar()
        var remote = state.ingredients
        val local = dao.getAll().map { it.name }
        val account = session.uid.ifEmpty { session.email }
        val claimed = prefs.getString(KEY_ACCOUNT, null)
        // The first account on this device keeps the bar stocked before signing
        // in; after that, and for any other account, the server's bar wins.
        if (claimed == null) {
            val remoteKeys = remote.map { it.lowercase() }.toSet()
            val deviceOnly = local.distinctBy { it.lowercase() }.filter { it.lowercase() !in remoteKeys }
            if (deviceOnly.isNotEmpty()) {
                state = service.replace(BarIngredientsBody(remote + deviceOnly))
                remote = state.ingredients
            }
        }
        publish(state)
        dao.replaceAll(merge(local, remote, pendingChanges()).map { BarIngredientEntity(it) })
        prefs.edit().putString(KEY_ACCOUNT, account).apply()
    }

    /**
     * The server's names, with changes made since the request applied on top.
     * Keeps this device's spelling for names both sides have.
     */
    private fun merge(local: List<String>, remote: List<String>, changes: List<Change>): List<String> {
        val localByKey = local.associateBy { it.lowercase() }
        val result = LinkedHashMap<String, String>()
        remote.forEach { name ->
            val key = name.lowercase()
            if (key !in result) result[key] = localByKey[key] ?: name
        }
        changes.forEach { change ->
            val key = change.name.lowercase()
            if (change.owned) result.getOrPut(key) { localByKey[key] ?: change.name } else result.remove(key)
        }
        return result.values.toList()
    }

    private fun pendingChanges(): List<Change> = synchronized(pending) { pending.values.toList() }

    /** Pending changes as saved by [persistPending]: "+Name" to add, "-Name" to remove. */
    private fun loadPending(): Map<String, Change> =
        prefs.getStringSet(KEY_PENDING, emptySet()).orEmpty().mapNotNull { encoded ->
            val owned = when (encoded.firstOrNull()) {
                '+' -> true
                '-' -> false
                else -> return@mapNotNull null
            }
            val name = encoded.drop(1)
            if (name.isBlank()) null else name.lowercase() to Change(name, owned)
        }.toMap()

    /** Writes [pending] to [prefs]; call while holding the [pending] monitor. */
    private fun persistPending() {
        val encoded = pending.values.map { (if (it.owned) "+" else "-") + it.name }.toSet()
        prefs.edit().putStringSet(KEY_PENDING, encoded).apply()
    }

    private suspend fun send(change: Change) {
        val state = if (change.owned) {
            service.add(BarIngredientBody(change.name))
        } else {
            try {
                service.remove(change.name)
            } catch (error: HttpException) {
                // Already gone from the server, which is what we wanted.
                if (error.code() != 404) throw error
                null
            }
        }
        synchronized(pending) {
            val key = change.name.lowercase()
            if (pending[key] == change) {
                pending.remove(key)
                persistPending()
            }
        }
        // A 404 carries no bar, so ask for it to keep recommendations current.
        publish(state ?: if (pendingChanges().isEmpty()) service.bar() else return)
    }

    /**
     * Takes the server's recommendations, unless more edits are still on their way
     * (their responses will be newer) or the account signed out meanwhile.
     */
    private fun publish(state: BarStateDto) {
        if (pendingChanges().isNotEmpty() || sessionStore.current() == null) return
        latest.value = barRecommendationsOf(
            state.makeable,
            state.almost,
            items = state.items,
            buyNext = state.buyNext,
        )
    }

    private suspend fun runCatchingNetwork(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Offline, signed out or past the trial: keep the local bar and try again later.
            Log.w(TAG, "Bar sync failed", error)
        }
    }

    private companion object {
        const val TAG = "BarSync"
        const val PREFS = "bar_sync"
        const val KEY_ACCOUNT = "account"
        const val KEY_PENDING = "pending"
    }
}
