package com.capstone.nik.mixology.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.Network.RemoteShoppingTrip
import com.capstone.nik.mixology.Network.ShoppingTripService
import com.capstone.nik.mixology.Network.ShoppingTripWrite
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.ShoppingTripItem
import com.capstone.nik.mixology.data.parseIsoMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ShoppingTripRepositoryTest {

    private class FakeTripService : ShoppingTripService {
        val stored = mutableListOf<RemoteShoppingTrip>()
        val deleted = mutableListOf<Long>()
        var offline = false
        var nextId = 1L

        override suspend fun trips(): Response<List<RemoteShoppingTrip>> {
            if (offline) throw java.io.IOException("offline")
            return Response.success(stored.sortedByDescending { it.id })
        }

        override suspend fun add(trip: ShoppingTripWrite): Response<RemoteShoppingTrip> {
            if (offline) throw java.io.IOException("offline")
            val saved = RemoteShoppingTrip(nextId++, "2026-11-01T14:03:00+00:00", trip.items)
            stored += saved
            return Response.success(saved)
        }

        override suspend fun delete(id: Long): Response<Unit> {
            if (offline) throw java.io.IOException("offline")
            deleted += id
            return if (stored.removeAll { it.id == id }) {
                Response.success(204, Unit)
            } else {
                Response.error(404, """{"detail":"Trip not found"}""".toResponseBody())
            }
        }
    }

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val service = FakeTripService()
    private val prefs = context.getSharedPreferences("shopping_trips_test", Context.MODE_PRIVATE)
    private lateinit var database: MixologyDatabase
    private lateinit var sessions: SessionStore
    private val lime = ShoppingTripItem("Lime juice", "11007", "Margarita", "1 oz", addedToBar = true)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        prefs.edit().clear().commit()
        sessions = SessionStore(context)
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun TestScope.newRepo() = NetworkShoppingTripRepository(
        service, database.shoppingTripDao(), sessions, prefs, backgroundScope, now = { 1_000L },
    )

    @Test
    fun recordedOffline_staysPending_thenSyncGivesItTheServerId() = runTest {
        val repo = newRepo()
        runCurrent()
        service.offline = true
        val trip = repo.record(listOf(lime))
        runCurrent()
        assertNull(database.shoppingTripDao().get(trip.id)?.remoteId)
        assertEquals(listOf(lime), repo.trips.first().single().items)

        service.offline = false
        repo.sync()
        runCurrent()
        val row = database.shoppingTripDao().get(trip.id)!!
        assertEquals(1L, row.remoteId)
        assertEquals(parseIsoMillis("2026-11-01T14:03:00+00:00"), row.at)
        assertEquals(listOf(lime), service.stored.single().items)
    }

    @Test
    fun delete_removesHereAndFromTheAccount() = runTest {
        val repo = newRepo()
        val trip = repo.record(listOf(lime))
        runCurrent()
        assertEquals(1L, database.shoppingTripDao().get(trip.id)?.remoteId)

        repo.delete(trip.id)
        runCurrent()
        assertTrue(repo.trips.first().isEmpty())
        assertEquals(listOf(1L), service.deleted)
        assertTrue(service.stored.isEmpty())
    }

    @Test
    fun sync_pullsTripsFromOtherDevices_andDropsOnesDeletedThere() = runTest {
        service.stored += RemoteShoppingTrip(5, "2026-10-30T09:00:00Z", listOf(lime))
        service.stored += RemoteShoppingTrip(6, "2026-10-31T09:00:00.250Z", listOf(lime.copy(addedToBar = false)))
        val repo = newRepo()
        repo.sync()
        runCurrent()
        assertEquals(listOf(6L, 5L), repo.trips.first().map { it.remoteId })
        val keptId = repo.trips.first().first { it.remoteId == 6L }.id

        service.stored.removeAll { it.id == 5L }
        repo.sync()
        runCurrent()
        val trips = repo.trips.first()
        assertEquals(listOf(6L), trips.map { it.remoteId })
        // The row on screen keeps its local id.
        assertEquals(keptId, trips.single().id)
    }

    @Test
    fun anotherAccountsTripsAreClearedOnSync() = runTest {
        prefs.edit().putString("account", "uid-other").commit()
        database.shoppingTripDao().insert(
            com.capstone.nik.mixology.data.ShoppingTripEntity(remoteId = 9, at = 1, items = "[]"),
        )
        val repo = newRepo()
        repo.sync()
        runCurrent()
        assertTrue(repo.trips.first().isEmpty())
    }

    @Test
    fun signingOut_clearsTrips_evenUnsentOnes() = runTest {
        val repo = newRepo()
        runCurrent()
        service.offline = true
        repo.record(listOf(lime))
        runCurrent()
        assertEquals(1, repo.trips.first().size)
        sessions.clear()
        runCurrent()
        assertTrue(repo.trips.first().isEmpty())
    }

    @Test
    fun deletingATripTheServerNoLongerHas_isFine() = runTest {
        val repo = newRepo()
        val trip = repo.record(listOf(lime))
        runCurrent()
        service.stored.clear()
        repo.delete(trip.id)
        runCurrent()
        assertTrue(repo.trips.first().isEmpty())
        assertEquals(listOf(1L), service.deleted)
    }

    @Test
    fun parseIsoMillis_readsOffsetsAndFractions() {
        assertEquals(0L, parseIsoMillis("1970-01-01T00:00:00Z"))
        assertEquals(0L, parseIsoMillis("1970-01-01T01:00:00+01:00"))
        assertEquals(250L, parseIsoMillis("1970-01-01T00:00:00.25Z"))
        assertNull(parseIsoMillis("yesterday"))
    }
}
