package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.capstone.nik.mixology.Network.RemoteShoppingItem
import com.capstone.nik.mixology.Network.ShoppingService
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.ShoppingDao
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.google.gson.Gson
import com.google.gson.JsonParseException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the shopping list in step with the signed-in account, so Ada, the
 * website and other devices see the same list as this one.
 */
interface ShoppingSync {
    /** Sends the whole local list to the server shortly after a change; failures are retried by the next [sync]. */
    fun push()

    /** Sends any unsent edits, then pulls the account's list into the local database. */
    suspend fun sync()

    companion object {
        /** For tests and previews: the list stays on the device. */
        val None = object : ShoppingSync {
            override fun push() = Unit
            override suspend fun sync() = Unit
        }
    }
}

@Singleton
class NetworkShoppingSync internal constructor(
    private val service: ShoppingService,
    private val dao: ShoppingDao,
    private val sessionStore: SessionStore,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
    private val pushDelayMillis: Long = PUSH_DELAY_MS,
) : ShoppingSync {

    @Inject
    constructor(
        service: ShoppingService,
        dao: ShoppingDao,
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
    private val gson = Gson()

    init {
        scope.launch {
            // Signing in (or starting signed in) brings the account's list down.
            sessionStore.session.collect { session ->
                if (session != null) sync()
            }
        }
    }

    override fun push() {
        // Kept across restarts and sign-outs, so edits made offline still go up.
        markDirty(true)
        if (sessionStore.current() == null) return
        scope.launch {
            // Quick edits in a row go up as one PUT: the first flush sends them all.
            delay(pushDelayMillis)
            lock.withLock { runCatchingNetwork { flush() } }
        }
    }

    override suspend fun sync() {
        val session = sessionStore.current() ?: return
        lock.withLock {
            runCatchingNetwork { pull(session) }
        }
    }

    private suspend fun pull(session: Session) {
        flush()
        val response = service.items().orThrow()
        val remote = response.body().orEmpty()
        val account = account(session)
        val claimed = prefs.getString(KEY_ACCOUNT, null)
        if (claimed != null && claimed != account) {
            markDirty(false)
            prefs.edit().remove(KEY_BASE).apply()
        }
        val base = if (claimed == account) loadBase() else null
        val merged = applyLocally { local ->
            if (base != null) {
                // Edits a failed push left behind survive alongside the server's.
                mergeShoppingEdits(base.items, local, remote)
            } else {
                // The first account on this device keeps what was listed before
                // signing in; a different account replaces it.
                merge(local, remote, keepUnsynced = claimed == null || claimed == account)
            }
        }
        saveBase(remote, response.etag())
        prefs.edit().putString(KEY_ACCOUNT, account).apply()
        if (merged.any { it.remoteId == null }) markDirty(true)
        flush()
    }

    private suspend fun flush() {
        val session = sessionStore.current() ?: return
        // Until this account has pulled once its server list is unknown, and a
        // PUT would wipe it; the pull merges these edits instead.
        if (prefs.getString(KEY_ACCOUNT, null) != account(session)) return
        if (!prefs.getBoolean(KEY_DIRTY, false)) return
        markDirty(false)
        try {
            repeat(PUT_ATTEMPTS) {
                if (put()) return
            }
            // Still racing another device: the next push or sync tries again.
            markDirty(true)
        } catch (error: Exception) {
            markDirty(true)
            throw error
        }
    }

    /**
     * Sends the local list if the server still has the version it was based on.
     * False when another device got there first; its changes are then merged
     * in locally, ready for another try.
     */
    private suspend fun put(): Boolean {
        val base = loadBase()
        val sent = dao.getAll()
        val response = service.replace(base?.etag, sent.map { it.toRemote() })
        if (response.code() == HTTP_PRECONDITION_FAILED) {
            val latest = service.items().orThrow()
            val remote = latest.body().orEmpty()
            applyLocally { local -> mergeShoppingEdits(base?.items.orEmpty(), local, remote) }
            saveBase(remote, latest.etag())
            return false
        }
        val stored = response.orThrow().body().orEmpty()
        saveBase(stored, response.etag())
        val merged = merge(sent, stored, keepUnsynced = false)
        if (!dao.replaceAll(sent, merged)) {
            // Edited while the PUT was out: keep those edits (their own push is
            // queued) and only note the ids the server gave.
            merged.forEach { row ->
                val remoteId = row.remoteId
                if (row.id != 0L && remoteId != null) dao.setRemoteId(row.id, remoteId)
            }
        }
        return true
    }

    /** Rewrites the local list from [build], retrying if a local edit lands in between. */
    private suspend fun applyLocally(
        build: (List<ShoppingItemEntity>) -> List<ShoppingItemEntity>,
    ): List<ShoppingItemEntity> {
        var merged = emptyList<ShoppingItemEntity>()
        var attempts = 0
        while (attempts++ < APPLY_ATTEMPTS) {
            val local = dao.getAll()
            merged = build(local)
            if (dao.replaceAll(local, merged)) break
        }
        return merged
    }

    private fun loadBase(): ShoppingBase? {
        val json = prefs.getString(KEY_BASE, null) ?: return null
        return try {
            gson.fromJson(json, ShoppingBase::class.java)
        } catch (error: JsonParseException) {
            null
        }
    }

    private fun saveBase(items: List<RemoteShoppingItem>, etag: String?) {
        prefs.edit().putString(KEY_BASE, gson.toJson(ShoppingBase(etag, items))).apply()
    }

    private fun markDirty(dirty: Boolean) {
        prefs.edit().putBoolean(KEY_DIRTY, dirty).apply()
    }

    private fun account(session: Session) = session.uid.ifEmpty { session.email }

    private suspend fun runCatchingNetwork(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Offline, signed out or past the trial: keep the local list and try again later.
            Log.w(TAG, "Shopping list sync failed", error)
        }
    }

    private companion object {
        const val TAG = "ShoppingSync"
        const val PREFS = "shopping_sync"
        const val KEY_ACCOUNT = "account"
        const val KEY_DIRTY = "dirty"
        const val KEY_BASE = "base"
        const val PUSH_DELAY_MS = 500L
        const val APPLY_ATTEMPTS = 3
        const val PUT_ATTEMPTS = 3
        const val HTTP_PRECONDITION_FAILED = 412
    }
}

/** The server list this device last applied, and the version it had. */
private data class ShoppingBase(
    val etag: String? = null,
    val items: List<RemoteShoppingItem> = emptyList(),
)

private fun <T> Response<T>.orThrow(): Response<T> {
    if (!isSuccessful) throw HttpException(this)
    return this
}

private fun Response<*>.etag(): String? = headers()["ETag"]

private fun ShoppingItemEntity.toRemote() = RemoteShoppingItem(
    id = remoteId,
    name = name,
    checked = checked,
    drinkId = drinkId.ifEmpty { null },
)

/**
 * The server's list as local rows. A row matches on its server id, or on drink
 * and name if the server had not stored it yet, and keeps its local id so the
 * UI's keys stay put.
 */
private fun merge(
    local: List<ShoppingItemEntity>,
    remote: List<RemoteShoppingItem>,
    keepUnsynced: Boolean,
): List<ShoppingItemEntity> {
    val synced = local.filter { it.remoteId != null }.associateBy { it.remoteId }
    val unsynced = local.filter { it.remoteId == null }.toMutableList()
    val rows = remote.map { item ->
        val drinkId = item.drinkId.orEmpty()
        val key = item.name.lowercase()
        val match = item.id?.let { synced[it] }
            ?: unsynced.firstOrNull { it.drinkId == drinkId && it.name.lowercase() == key }
                ?.also { unsynced.remove(it) }
        ShoppingItemEntity(
            id = match?.id ?: 0,
            name = item.name,
            checked = item.checked,
            drinkId = drinkId,
            remoteId = item.id,
        )
    }
    return if (keepUnsynced) rows + unsynced else rows
}

/**
 * Three-way merge for when another device changed the list since [base], the
 * server list this device last applied. Deletions and additions on either side
 * survive; a checkbox this device flipped wins over the server's.
 */
internal fun mergeShoppingEdits(
    base: List<RemoteShoppingItem>,
    local: List<ShoppingItemEntity>,
    remote: List<RemoteShoppingItem>,
): List<ShoppingItemEntity> {
    val before = base.mapNotNull { item -> item.id?.let { it to item } }.toMap()
    val mine = local.filter { it.remoteId != null }.associateBy { it.remoteId }
    // Items missing from remote were deleted elsewhere, so only remote's are walked.
    val rows = remote.mapNotNull { item ->
        val id = item.id
        val row = id?.let { mine[it] }
        val was = id?.let { before[it] }
        when {
            row != null -> ShoppingItemEntity(
                id = row.id,
                name = item.name,
                checked = if (was != null && row.checked != was.checked) row.checked else item.checked,
                drinkId = item.drinkId.orEmpty(),
                remoteId = id,
            )
            // Known before but gone here: deleted on this device.
            was != null -> null
            // Added elsewhere.
            else -> ShoppingItemEntity(
                name = item.name,
                checked = item.checked,
                drinkId = item.drinkId.orEmpty(),
                remoteId = id,
            )
        }
    }
    val keys = rows.mapTo(mutableSetOf()) { it.drinkId to it.name.lowercase() }
    val added = local.filter { it.remoteId == null && keys.add(it.drinkId to it.name.lowercase()) }
    return rows + added
}
