package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.NoteBody
import com.capstone.nik.mixology.Network.SavedService
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkNoteDao
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.toEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
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
 * the device's copy in Room so they show offline, and sends changes made here.
 */
interface SavedSync {
    /** Saved drink ids: the server's list with this device's unsent changes applied. */
    val savedIds: StateFlow<Set<String>>

    /**
     * Saves or unsaves one drink. The change shows at once; if the server can't be
     * reached it is kept, and the next [sync] sends it again.
     */
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
    private val noteDao: DrinkNoteDao,
    private val sessionStore: SessionStore,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
) : SavedSync {

    @Inject
    constructor(
        service: SavedService,
        cocktails: CocktailService,
        dao: DrinkDao,
        noteDao: DrinkNoteDao,
        sessionStore: SessionStore,
        @ApplicationContext context: Context,
    ) : this(
        service,
        cocktails,
        dao,
        noteDao,
        sessionStore,
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE),
        CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    private val lock = Mutex()
    private val gson = Gson()

    /**
     * Saves and unsaves the server has not acknowledged yet, by drink id; mirrored
     * in [prefs] so they survive a restart. Also guards [ids], [pendingNotes] and [notesById].
     */
    private val pending = mutableMapOf<String, Boolean>().apply { putAll(loadPendingSaves()) }
    private val ids = MutableStateFlow<Set<String>>(emptySet())
    override val savedIds: StateFlow<Set<String>> = ids.asStateFlow()

    /** Note edits the server has not acknowledged yet, by drink id; "" deletes. Mirrored in [prefs]. */
    private val pendingNotes = mutableMapOf<String, String>().apply { putAll(loadPendingNotes()) }
    private val notesById = MutableStateFlow<Map<String, String>>(emptyMap())
    override val notes: StateFlow<Map<String, String>> = notesById.asStateFlow()

    /** Done once Room's copy has been read into [ids] and [notesById]. */
    private val loaded = CompletableDeferred<Unit>()

    init {
        scope.launch {
            // What this device last knew, with its unsent changes on top, shows
            // straight away; the pull then brings the account's current state.
            var onDevice = emptySet<String>()
            var notesOnDevice = emptyMap<String, String>()
            runCatchingLogged("Reading saved drinks from the device failed") {
                onDevice = dao.getSavedIds().toSet()
                notesOnDevice = noteDao.getAll().associate { it.drinkId to it.notes }
            }
            synchronized(pending) {
                ids.value = pending.entries.fold(onDevice) { set, (id, saved) -> set.with(id, saved) }
                notesById.value = pendingNotes.entries.fold(notesOnDevice) { map, (id, text) -> map.withNote(id, text) }
            }
            loaded.complete(Unit)
            // Signing in (or starting signed in) brings the account's list down;
            // signing out forgets it. An expired session keeps everything for
            // when the same account signs back in.
            sessionStore.session.collect { session ->
                if (session != null) {
                    sync()
                } else if (sessionStore.signedOut()) {
                    forgetAccount()
                }
            }
        }
    }

    override fun push(id: String, saved: Boolean) {
        if (id.startsWith(Drink.USER_RECIPE_PREFIX)) return
        val signedIn = sessionStore.current() != null
        synchronized(pending) {
            ids.value = ids.value.with(id, saved)
            if (signedIn) {
                pending[id] = saved
                persistPending()
            }
        }
        scope.launch {
            runCatchingLogged("Storing the saved flag for $id failed") { dao.setSaved(id, saved) }
        }
        if (!signedIn) return
        scope.launch {
            lock.withLock {
                // Already sent by a pull, or replaced by a newer change that sends itself.
                if (synchronized(pending) { pending[id] } != saved) return@withLock
                // No rollback on failure: the change stays pending and the next pull sends it.
                runCatchingLogged("Saving $id failed") { send(id, saved) }
            }
        }
    }

    // Unlike saves, the server takes notes on the user's own recipes too.
    override fun pushNotes(id: String, notes: String) {
        val text = if (notes.isBlank()) "" else notes
        val signedIn = sessionStore.current() != null
        synchronized(pending) {
            notesById.value = notesById.value.withNote(id, text)
            if (signedIn) {
                pendingNotes[id] = text
                persistPending()
            }
        }
        scope.launch {
            runCatchingLogged("Storing the note for $id failed") { noteDao.set(id, text) }
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
        loaded.await()
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
        val (merged, mergedNotes) = synchronized(pending) {
            val merged = pending.entries.fold(remote) { set, (id, saved) -> set.with(id, saved) }
            val mergedNotes = pendingNotes.entries.fold(remoteNotes) { map, (id, text) -> map.withNote(id, text) }
            ids.value = merged
            notesById.value = mergedNotes
            merged to mergedNotes
        }
        for (id in merged) {
            if (id.startsWith(Drink.USER_RECIPE_PREFIX) || dao.getById(id) != null) continue
            runCatchingLogged("Fetching saved drink $id failed") {
                val drink = cocktails.getDrinkById(id).drinks.orEmpty().firstOrNull()?.toDrink()
                if (drink != null) dao.saveDrink(drink.toEntity())
            }
        }
        runCatchingLogged("Storing the account's saved drinks on the device failed") {
            dao.replaceSaved(merged.toList())
            noteDao.replaceAll(mergedNotes)
        }
    }

    private suspend fun send(id: String, saved: Boolean) {
        if (saved) service.save(id) else service.unsave(id)
        synchronized(pending) {
            if (pending[id] == saved) {
                pending.remove(id)
                persistPending()
            }
        }
    }

    private suspend fun sendNotes(id: String, text: String) {
        service.setNotes(id, NoteBody(text))
        synchronized(pending) {
            if (pendingNotes[id] == text) {
                pendingNotes.remove(id)
                persistPending()
            }
        }
    }

    /** The user signed out: the account's list, notes and unsent changes leave the device. */
    private suspend fun forgetAccount() {
        synchronized(pending) {
            pending.clear()
            pendingNotes.clear()
            persistPending()
            ids.value = emptySet()
            notesById.value = emptyMap()
        }
        runCatchingLogged("Clearing the account's saved drinks from the device failed") {
            dao.clearSavedFlags()
            noteDao.clear()
        }
    }

    /** Pending saves as [persistPending] writes them: "+id" to save, "-id" to unsave. */
    private fun loadPendingSaves(): Map<String, Boolean> =
        prefs.getStringSet(KEY_PENDING_SAVES, emptySet()).orEmpty().mapNotNull { encoded ->
            val saved = when (encoded.firstOrNull()) {
                '+' -> true
                '-' -> false
                else -> return@mapNotNull null
            }
            encoded.drop(1).takeIf { it.isNotBlank() }?.let { it to saved }
        }.toMap()

    private fun loadPendingNotes(): Map<String, String> {
        val json = prefs.getString(KEY_PENDING_NOTES, null) ?: return emptyMap()
        return runCatching { gson.fromJson<Map<String, String>>(json, NOTES_TYPE) }.getOrNull().orEmpty()
    }

    /** Writes [pending] and [pendingNotes] to [prefs]; call while holding the [pending] monitor. */
    private fun persistPending() {
        prefs.edit()
            .putStringSet(KEY_PENDING_SAVES, pending.map { (id, saved) -> (if (saved) "+" else "-") + id }.toSet())
            .putString(KEY_PENDING_NOTES, gson.toJson(pendingNotes))
            .apply()
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
        const val PREFS = "saved_sync"
        const val KEY_PENDING_SAVES = "pending_saves"
        const val KEY_PENDING_NOTES = "pending_notes"
        val NOTES_TYPE = object : TypeToken<Map<String, String>>() {}.type
    }
}
