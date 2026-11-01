package com.capstone.nik.mixology.repository

import android.util.Log
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.SavedService
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.toEntity
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The account's saved drinks. The server owns the list; this keeps it in
 * memory, and Room only caches the drinks' details.
 */
interface SavedSync {
    /** Saved drink ids: the server's list with this device's unsent changes applied. */
    val savedIds: StateFlow<Set<String>>

    /** Saves or unsaves one drink; the change shows at once and is undone if the server refuses it. */
    fun push(id: String, saved: Boolean)

    /** Replaces [savedIds] with the account's list from the server. */
    suspend fun sync()

    companion object {
        /**
         * For tests and previews: no server. Each access returns a new
         * instance whose saves live only in its own in-memory set, so a
         * repository built with it still sees its own saves and unsaves.
         */
        val None: SavedSync get() = InMemorySavedSync()
    }
}

private class InMemorySavedSync : SavedSync {
    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val savedIds: StateFlow<Set<String>> = ids.asStateFlow()

    override fun push(id: String, saved: Boolean) {
        ids.value = if (saved) ids.value + id else ids.value - id
    }

    override suspend fun sync() = Unit
}

@Singleton
class NetworkSavedSync internal constructor(
    private val service: SavedService,
    private val cocktails: CocktailService,
    private val dao: DrinkDao,
    private val sessionStore: SessionStore,
    private val scope: CoroutineScope,
) : SavedSync {

    @Inject
    constructor(
        service: SavedService,
        cocktails: CocktailService,
        dao: DrinkDao,
        sessionStore: SessionStore,
    ) : this(
        service,
        cocktails,
        dao,
        sessionStore,
        CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    private val lock = Mutex()

    /** Saves and unsaves the server has not acknowledged yet, by drink id. Also guards [ids]. */
    private val pending = mutableMapOf<String, Boolean>()
    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val savedIds: StateFlow<Set<String>> = ids.asStateFlow()

    init {
        scope.launch {
            // Saves used to live in Room; they are dropped, never uploaded.
            runCatchingLogged("Clearing local saved flags failed") { dao.clearSavedFlags() }
            // Signing in (or starting signed in) brings the account's list down;
            // signing out forgets it.
            sessionStore.session.collect { session ->
                if (session != null) {
                    sync()
                } else {
                    synchronized(pending) {
                        pending.clear()
                        ids.value = emptySet()
                    }
                }
            }
        }
    }

    override fun push(id: String, saved: Boolean) {
        if (id.startsWith(Drink.USER_RECIPE_PREFIX)) return
        val signedIn = sessionStore.current() != null
        synchronized(pending) {
            ids.value = ids.value.with(id, saved)
            if (signedIn) pending[id] = saved
        }
        if (!signedIn) return
        scope.launch {
            lock.withLock {
                // Already sent by a pull, or replaced by a newer change that sends itself.
                if (synchronized(pending) { pending[id] } != saved) return@withLock
                try {
                    send(id, saved)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Log.w(TAG, "Saving $id failed", error)
                    synchronized(pending) {
                        if (pending[id] == saved) {
                            pending.remove(id)
                            ids.value = ids.value.with(id, !saved)
                        }
                    }
                }
            }
        }
    }

    override suspend fun sync() {
        if (sessionStore.current() == null) return
        lock.withLock {
            runCatchingLogged("Saved drinks sync failed") { pull() }
        }
    }

    private suspend fun pull() {
        synchronized(pending) { pending.toMap() }.forEach { (id, saved) -> send(id, saved) }
        val remote = service.sync().saved.toSet()
        val merged = synchronized(pending) {
            var merged = remote
            pending.forEach { (id, saved) -> merged = merged.with(id, saved) }
            ids.value = merged
            merged
        }
        for (id in merged) {
            if (id.startsWith(Drink.USER_RECIPE_PREFIX) || dao.getById(id) != null) continue
            runCatchingLogged("Fetching saved drink $id failed") {
                val drink = cocktails.getDrinkById(id).drinks.orEmpty().firstOrNull()?.toDrink()
                if (drink != null) dao.saveDrink(drink.toEntity())
            }
        }
    }

    private suspend fun send(id: String, saved: Boolean) {
        if (saved) service.save(id) else service.unsave(id)
        synchronized(pending) {
            if (pending[id] == saved) pending.remove(id)
        }
    }

    private suspend fun runCatchingLogged(message: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Offline, signed out or past the trial: keep what we have and try again later.
            Log.w(TAG, message, error)
        }
    }

    private fun Set<String>.with(id: String, saved: Boolean) = if (saved) this + id else this - id

    private companion object {
        const val TAG = "SavedSync"
    }
}
