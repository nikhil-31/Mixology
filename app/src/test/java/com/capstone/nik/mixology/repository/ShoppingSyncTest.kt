package com.capstone.nik.mixology.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.Network.RemoteShoppingItem
import com.capstone.nik.mixology.Network.ShoppingService
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.ShoppingItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ShoppingSyncTest {

    /** Models the server's list version: every write bumps it, and a stale If-Match gets a 412. */
    private class FakeShoppingService : ShoppingService {
        val stored = mutableListOf<RemoteShoppingItem>()
        val puts = mutableListOf<List<RemoteShoppingItem>>()
        var version = 1
        var rejected = 0
        var nextId = 100L
        var trialExpired = false

        /** Another client's write, such as the website or a second phone. */
        fun editElsewhere(change: MutableList<RemoteShoppingItem>.() -> Unit) {
            stored.change()
            version++
        }

        override suspend fun items(): Response<List<RemoteShoppingItem>> {
            if (trialExpired) return expired()
            return Response.success(stored.toList(), etag())
        }

        override suspend fun replace(
            ifMatch: String?,
            items: List<RemoteShoppingItem>,
        ): Response<List<RemoteShoppingItem>> {
            if (trialExpired) return expired()
            if (ifMatch != null && ifMatch != "\"$version\"") {
                rejected++
                return Response.error(412, """{"detail":"Shopping list changed"}""".toResponseBody())
            }
            puts += items
            stored.clear()
            stored += items.map { it.copy(id = it.id ?: nextId++) }
            version++
            return Response.success(stored.toList(), etag())
        }

        private fun etag() = Headers.headersOf("ETag", "\"$version\"")

        // Retrofit hands back error statuses rather than throwing when the call returns a Response.
        private fun expired() = Response.error<List<RemoteShoppingItem>>(
            403,
            """{"detail":"Subscription required"}""".toResponseBody(),
        )
    }

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val service = FakeShoppingService()
    private val prefs = context.getSharedPreferences("shopping_sync_test", Context.MODE_PRIVATE)
    private lateinit var database: MixologyDatabase
    private lateinit var sessions: SessionStore

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

    private fun TestScope.newSync() =
        NetworkShoppingSync(service, database.shoppingDao(), sessions, prefs, backgroundScope)

    private suspend fun local() = database.shoppingDao().getAll()

    private suspend fun addLocally(name: String, drinkId: String = "", remoteId: Long? = null) {
        database.shoppingDao().insert(ShoppingItemEntity(name = name, drinkId = drinkId, remoteId = remoteId))
    }

    @Test
    fun pullReplacesTheLocalListWithTheAccounts() = runTest {
        prefs.edit().putString("account", "uid-1").commit()
        addLocally("Lime", drinkId = "11007", remoteId = 1)
        addLocally("Gone elsewhere", remoteId = 2)
        val limeId = local().first { it.name == "Lime" }.id
        service.stored += RemoteShoppingItem(id = 1, name = "Lime", checked = true, drinkId = "11007")
        service.stored += RemoteShoppingItem(id = 3, name = "Mint", drinkId = "17207")

        newSync().sync()
        runCurrent()

        val items = local().associateBy { it.name }
        assertEquals(setOf("Lime", "Mint"), items.keys)
        assertEquals(limeId, items.getValue("Lime").id)
        assertEquals(true, items.getValue("Lime").checked)
        assertEquals("17207", items.getValue("Mint").drinkId)
        assertEquals(3L, items.getValue("Mint").remoteId)
        assertEquals(emptyList<List<RemoteShoppingItem>>(), service.puts)
    }

    @Test
    fun pushSendsTheDrinkAndStoresTheServersIds() = runTest {
        prefs.edit().putString("account", "uid-1").commit()
        val sync = newSync()
        runCurrent()

        addLocally("Lime", drinkId = "11007")
        sync.push()
        addLocally("Salt")
        sync.push()
        // Background work only moves when the clock does.
        advanceTimeBy(1_000)
        runCurrent()

        // Both edits went up in one PUT.
        assertEquals(1, service.puts.size)
        val sent = service.puts.single().associateBy { it.name }
        assertNull(sent.getValue("Lime").id)
        assertEquals("11007", sent.getValue("Lime").drinkId)
        assertNull(sent.getValue("Salt").drinkId)
        val items = local().associateBy { it.name }
        assertEquals(service.stored.first { it.name == "Lime" }.id, items.getValue("Lime").remoteId)
        assertEquals(service.stored.first { it.name == "Salt" }.id, items.getValue("Salt").remoteId)
    }

    @Test
    fun firstSyncMergesThisDevicesListIntoTheAccount() = runTest {
        addLocally("Lime", drinkId = "11007")
        addLocally("salt", drinkId = "11007")
        service.stored += RemoteShoppingItem(id = 1, name = "Salt", drinkId = "11007")

        newSync().sync()
        runCurrent()

        assertEquals(setOf("Salt", "Lime"), service.stored.map { it.name }.toSet())
        val items = local().associateBy { it.name }
        assertEquals(setOf("Lime", "Salt"), items.keys)
        assertEquals(1L, items.getValue("Salt").remoteId)
        assertEquals(service.stored.first { it.name == "Lime" }.id, items.getValue("Lime").remoteId)
        assertEquals("uid-1", prefs.getString("account", null))
    }

    @Test
    fun anotherAccountReplacesThisDevicesList() = runTest {
        prefs.edit().putString("account", "uid-0").commit()
        addLocally("Theirs", remoteId = 9)
        addLocally("Unsent")
        service.stored += RemoteShoppingItem(id = 1, name = "Mine")

        newSync().sync()
        runCurrent()

        assertEquals(listOf("Mine"), local().map { it.name })
        assertEquals(listOf("Mine"), service.stored.map { it.name })
        assertEquals(emptyList<List<RemoteShoppingItem>>(), service.puts)
    }

    @Test
    fun errorsKeepTheLocalListAndRetryLater() = runTest {
        prefs.edit().putString("account", "uid-1").commit()
        service.trialExpired = true
        addLocally("Lime")
        val sync = newSync()
        sync.push()
        advanceTimeBy(1_000)
        runCurrent()
        sync.sync()

        assertEquals(listOf("Lime"), local().map { it.name })
        assertEquals(emptyList<List<RemoteShoppingItem>>(), service.puts)

        service.trialExpired = false
        sync.sync()

        assertEquals(listOf("Lime"), service.stored.map { it.name })
        assertEquals(service.stored.single().id, local().single().remoteId)
    }

    @Test
    fun aPushThatLosesARaceMergesBothDevicesEdits() = runTest {
        service.stored += RemoteShoppingItem(id = 1, name = "Lime", drinkId = "11007")
        service.stored += RemoteShoppingItem(id = 2, name = "Salt", drinkId = "11007")
        val sync = newSync()
        runCurrent()
        assertEquals("\"1\"", service.items().headers()["ETag"])

        // This phone ticks Lime and adds Mint while another device swaps Salt for Sugar.
        val lime = local().first { it.name == "Lime" }
        database.shoppingDao().setChecked(lime.id, true)
        addLocally("Mint")
        service.editElsewhere {
            removeAll { it.name == "Salt" }
            add(RemoteShoppingItem(id = 3, name = "Sugar"))
        }
        sync.push()
        advanceTimeBy(1_000)
        runCurrent()

        assertEquals(1, service.rejected)
        val server = service.stored.associateBy { it.name }
        assertEquals(setOf("Lime", "Sugar", "Mint"), server.keys)
        assertEquals(true, server.getValue("Lime").checked)
        val items = local().associateBy { it.name }
        assertEquals(setOf("Lime", "Sugar", "Mint"), items.keys)
        assertEquals(lime.id, items.getValue("Lime").id)
        assertEquals(server.getValue("Mint").id, items.getValue("Mint").remoteId)

        // The next push is based on the version that PUT produced, so it goes straight through.
        database.shoppingDao().delete(items.getValue("Sugar").id)
        sync.push()
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(1, service.rejected)
        assertEquals(setOf("Lime", "Mint"), service.stored.map { it.name }.toSet())
    }
}
