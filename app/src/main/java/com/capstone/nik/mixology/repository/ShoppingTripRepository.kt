package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.capstone.nik.mixology.Network.RemoteShoppingTrip
import com.capstone.nik.mixology.Network.ShoppingTripService
import com.capstone.nik.mixology.Network.ShoppingTripWrite
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.ShoppingTrip
import com.capstone.nik.mixology.data.ShoppingTripDao
import com.capstone.nik.mixology.data.ShoppingTripEntity
import com.capstone.nik.mixology.data.ShoppingTripItem
import com.capstone.nik.mixology.data.encodeTripItems
import com.capstone.nik.mixology.data.parseIsoMillis
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/** Shopping trips recorded by Done shopping, kept on the device and the account. */
interface ShoppingTripRepository {
    /** Newest first. */
    val trips: Flow<List<ShoppingTrip>>

    /** Saves the trip here straight away and sends it to the account in the background. */
    suspend fun record(items: List<ShoppingTripItem>): ShoppingTrip

    /** Removes the trip here and from the account. */
    suspend fun delete(id: Long)

    /** Sends trips recorded offline, then pulls the account's trips. */
    suspend fun sync()

    companion object {
        /** For tests and previews: trips stay in memory. */
        fun inMemory(): ShoppingTripRepository = InMemoryShoppingTrips()
    }
}

private class InMemoryShoppingTrips : ShoppingTripRepository {
    private val state = MutableStateFlow<List<ShoppingTrip>>(emptyList())
    private var nextId = 1L
    override val trips: Flow<List<ShoppingTrip>> = state.asStateFlow()

    override suspend fun record(items: List<ShoppingTripItem>): ShoppingTrip {
        val trip = ShoppingTrip(nextId++, null, System.currentTimeMillis(), items)
        state.value = listOf(trip) + state.value
        return trip
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun sync() = Unit
}

@Singleton
class NetworkShoppingTripRepository internal constructor(
    private val service: ShoppingTripService,
    private val dao: ShoppingTripDao,
    private val sessionStore: SessionStore,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis,
) : ShoppingTripRepository {

    @Inject
    constructor(
        service: ShoppingTripService,
        dao: ShoppingTripDao,
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

    init {
        scope.launch {
            sessionStore.session.collect { session ->
                if (session != null) {
                    sync()
                } else if (sessionStore.signedOut()) {
                    // Signed out: this account's trips, sent or not, leave the device.
                    // An expired session keeps them for when the same account signs back in.
                    lock.withLock {
                        dao.deleteAll()
                        prefs.edit().remove(KEY_ACCOUNT).apply()
                    }
                }
            }
        }
    }

    override val trips: Flow<List<ShoppingTrip>> = dao.observeEntities().map { rows -> rows.map { it.toTrip() } }

    override suspend fun record(items: List<ShoppingTripItem>): ShoppingTrip {
        val entity = ShoppingTripEntity(at = now(), items = encodeTripItems(items))
        val id = dao.insert(entity)
        scope.launch { lock.withLock { runCatchingNetwork { pushPending() } } }
        return entity.copy(id = id).toTrip()
    }

    override suspend fun delete(id: Long) {
        val row = dao.get(id) ?: return
        dao.delete(id)
        val remoteId = row.remoteId ?: return
        scope.launch { runCatchingNetwork { deleteRemote(remoteId) } }
    }

    override suspend fun sync() {
        val session = sessionStore.current() ?: return
        lock.withLock {
            runCatchingNetwork {
                val account = account(session)
                val claimed = prefs.getString(KEY_ACCOUNT, null)
                // Another account's history must not show, or be sent up as this one's.
                if (claimed != null && claimed != account) dao.deleteAll()
                prefs.edit().putString(KEY_ACCOUNT, account).apply()
                pushPending()
                val response = service.trips()
                if (!response.isSuccessful) throw HttpException(response)
                dao.applyRemote(response.body().orEmpty().map { it.toEntity() })
            }
        }
    }

    private suspend fun pushPending() {
        if (sessionStore.current() == null) return
        dao.pending().forEach { row ->
            val response = service.add(ShoppingTripWrite(row.toTrip().items))
            if (!response.isSuccessful) throw HttpException(response)
            val stored = response.body() ?: return@forEach
            if (dao.get(row.id) == null) {
                // Deleted (or undone) while it was on its way: take it back off the account.
                deleteRemote(stored.id)
            } else {
                dao.setRemoteId(row.id, stored.id)
            }
        }
    }

    private suspend fun deleteRemote(remoteId: Long) {
        val response = service.delete(remoteId)
        if (!response.isSuccessful && response.code() != HTTP_NOT_FOUND) throw HttpException(response)
    }

    private fun RemoteShoppingTrip.toEntity() = ShoppingTripEntity(
        remoteId = id,
        at = parseIsoMillis(at) ?: now(),
        items = encodeTripItems(items),
    )

    private fun account(session: Session) = session.uid.ifEmpty { session.email }

    private suspend fun runCatchingNetwork(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Offline or signed out: trips stay here, and the next sync sends them.
            Log.w(TAG, "Shopping trip sync failed", error)
        }
    }

    private companion object {
        const val TAG = "ShoppingTrips"
        const val PREFS = "shopping_trips"
        const val KEY_ACCOUNT = "account"
        const val HTTP_NOT_FOUND = 404
    }
}
