package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.SavedService
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps saved drinks in step with the signed-in account, so Ada (which reads
 * the server's list) and other devices agree with this one.
 */
interface SavedSync {
    /** Sends one save or unsave to the server; failures are retried by the next [sync]. */
    fun push(id: String, saved: Boolean)

    /** Pulls the account's saved ids into the local database. */
    suspend fun sync()

    companion object {
        /** For tests and previews: saves stay on the device. */
        val None = object : SavedSync {
            override fun push(id: String, saved: Boolean) = Unit
            override suspend fun sync() = Unit
        }
    }
}

@Singleton
class NetworkSavedSync internal constructor(
    private val service: SavedService,
    private val cocktails: CocktailService,
    private val dao: DrinkDao,
    private val sessionStore: SessionStore,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
) : SavedSync {

    @Inject
    constructor(
        service: SavedService,
        cocktails: CocktailService,
        dao: DrinkDao,
        sessionStore: SessionStore,
        @ApplicationContext context: Context,
    ) : this(
        service,
        cocktails,
        dao,
        sessionStore,
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE),
        CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    private val lock = Mutex()
    /** Saves and unsaves the server has not acknowledged yet, by drink id. */
    private val pending = mutableMapOf<String, Boolean>()

    init {
        scope.launch {
            // Signing in (or starting signed in) brings the account's list down.
            sessionStore.session.collect { session ->
                if (session != null) sync()
            }
        }
    }

    override fun push(id: String, saved: Boolean) {
        if (id.startsWith(Drink.USER_RECIPE_PREFIX) || sessionStore.current() == null) return
        synchronized(pending) { pending[id] = saved }
        scope.launch {
            lock.withLock { runCatchingNetwork { send(id, saved) } }
        }
    }

    override suspend fun sync() {
        val session = sessionStore.current() ?: return
        lock.withLock {
            runCatchingNetwork { pull(session) }
        }
    }

    private suspend fun pull(session: Session) {
        synchronized(pending) { pending.toMap() }.forEach { (id, saved) -> send(id, saved) }
        val remote = service.sync().saved.toMutableSet()
        val local = dao.observeSavedIds().first().toSet()
        val account = session.uid.ifEmpty { session.email }
        val claimed = prefs.getString(KEY_ACCOUNT, null)
        // The first account on this device keeps what was saved before signing
        // in; a different account replaces it.
        if (claimed == null || claimed == account) {
            for (id in local - remote) {
                if (id.startsWith(Drink.USER_RECIPE_PREFIX)) continue
                service.save(id)
                remote += id
            }
        }
        for (id in remote - local) {
            if (dao.getById(id) != null) {
                dao.setSaved(id, true)
            } else {
                val drink = cocktails.getDrinkById(id).drinks.orEmpty().firstOrNull()?.toDrink() ?: continue
                dao.saveDrink(drink.toEntity().copy(saved = true))
            }
        }
        for (id in local - remote) {
            if (!id.startsWith(Drink.USER_RECIPE_PREFIX)) dao.setSaved(id, false)
        }
        prefs.edit().putString(KEY_ACCOUNT, account).apply()
    }

    private suspend fun send(id: String, saved: Boolean) {
        if (saved) service.save(id) else service.unsave(id)
        synchronized(pending) {
            if (pending[id] == saved) pending.remove(id)
        }
    }

    private suspend fun runCatchingNetwork(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Offline, signed out or past the trial: keep the local list and try again later.
            Log.w(TAG, "Saved drinks sync failed", error)
        }
    }

    private companion object {
        const val TAG = "SavedSync"
        const val PREFS = "saved_sync"
        const val KEY_ACCOUNT = "account"
    }
}
