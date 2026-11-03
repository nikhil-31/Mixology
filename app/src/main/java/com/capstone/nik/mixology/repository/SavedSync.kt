package com.capstone.nik.mixology.repository

import android.util.Log
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.NoteBody
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
 * The account's saved drinks and drink notes. The server owns both; this keeps
 * them in memory, and Room only caches the drinks' details.
 */
interface SavedSync {
    /** Saved drink ids: the server's list with this device's unsent changes applied. */
    val savedIds: StateFlow<Set<String>>

    /** Saves or unsaves one drink; the change shows at once and is undone if the server refuses it. */
    fun push(id: String, saved: Boolean)

    /** Drink notes by drink id: the server's notes with this device's unsent edits applied. Blank notes are absent. */
    val notes: StateFlow<Map<String, String>>

    /**
     * Sets one drink's note; a blank note deletes it. The change shows at once.
     * If the server can't be reached the text is kept, and the next [sync] sends it again.
     */
    fun pushNotes(id: String, notes: String)

    /** Replaces [savedIds] and [notes] with the account's state from the server. */
    suspend fun sync()

    companion object {
        /**
         * For tests and previews: no server. Each access returns a new
         * instance whose saves and notes live only in its own memory, so a
         * repository built with it still sees its own changes.
         */
        val None: SavedSync get() = InMemorySavedSync()
    }
}

private class InMemorySavedSync : SavedSync {
    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val savedIds: StateFlow<Set<String>> = ids.asStateFlow()

    private val notesById = MutableStateFlow<Map<String, String>>(emptyMap())
    override val notes: StateFlow<Map<String, String>> = notesById.asStateFlow()

    override fun push(id: String, saved: Boolean) {
        ids.value = if (saved) ids.value + id else ids.value - id
    }

    override fun pushNotes(id: String, notes: String) {
        notesById.value = notesById.value.withNote(id, notes)
    }

    override suspend fun sync() = Unit
}

/** [notes] for [id], or no entry when it's blank. */
internal fun Map<String, String>.withNote(id: String, notes: String): Map<String, String> =
    if (notes.isBlank()) this - id else this + (id to notes)

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

    /** Saves and unsaves the server has not acknowledged yet, by drink id. Also guards [ids], [pendingNotes] and [notesById]. */
    private val pending = mutableMapOf<String, Boolean>()
    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val savedIds: StateFlow<Set<String>> = ids.asStateFlow()

    /** Note edits the server has not acknowledged yet, by drink id; "" deletes. */
    private val pendingNotes = mutableMapOf<String, String>()
    private val notesById = MutableStateFlow<Map<String, String>>(emptyMap())
    override val notes: StateFlow<Map<String, String>> = notesById.asStateFlow()

    init {
        scope.launch {
            // Saves used to live in Room; they are dropped, never uploaded.
            runCatchingLogged("Clearing local saved flags failed") { dao.clearSavedFlags() }
            // So did notes; they are dropped the same way.
            runCatchingLogged("Clearing local notes failed") { dao.clearNotes() }
            // Signing in (or starting signed in) brings the account's list down;
            // signing out forgets it. An expired session keeps unsent changes
            // for when the same account signs back in.
            sessionStore.session.collect { session ->
                if (session != null) {
                    sync()
                } else if (sessionStore.signedOut()) {
                    synchronized(pending) {
                        pending.clear()
                        ids.value = emptySet()
                        pendingNotes.clear()
                        notesById.value = emptyMap()
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

    // Unlike saves, the server takes notes on the user's own recipes too.
    override fun pushNotes(id: String, notes: String) {
        val text = if (notes.isBlank()) "" else notes
        val signedIn = sessionStore.current() != null
        synchronized(pending) {
            notesById.value = notesById.value.withNote(id, text)
            if (signedIn) pendingNotes[id] = text
        }
        if (!signedIn) return
        scope.launch {
            lock.withLock {
                // Already sent by a pull, or replaced by a newer edit that sends itself.
                if (synchronized(pending) { pendingNotes[id] } != text) return@withLock
                // No rollback on failure: the text stays on screen and pending,
                // and the next pull sends it again.
                runCatchingLogged("Saving notes for $id failed") { sendNotes(id, text) }
            }
        }
    }

    override suspend fun sync() {
        if (sessionStore.current() == null) return
        lock.withLock {
            runCatchingLogged("Saved drinks and notes sync failed") { pull() }
        }
    }

    private suspend fun pull() {
        synchronized(pending) { pending.toMap() }.forEach { (id, saved) -> send(id, saved) }
        synchronized(pending) { pendingNotes.toMap() }.forEach { (id, text) -> sendNotes(id, text) }
        val dto = service.sync()
        val remote = dto.saved.toSet()
        val remoteNotes = dto.notes.filterValues { it.isNotBlank() }
        val merged = synchronized(pending) {
            var merged = remote
            pending.forEach { (id, saved) -> merged = merged.with(id, saved) }
            ids.value = merged
            var mergedNotes = remoteNotes
            pendingNotes.forEach { (id, text) -> mergedNotes = mergedNotes.withNote(id, text) }
            notesById.value = mergedNotes
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

    private suspend fun sendNotes(id: String, text: String) {
        service.setNotes(id, NoteBody(text))
        synchronized(pending) {
            if (pendingNotes[id] == text) pendingNotes.remove(id)
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
