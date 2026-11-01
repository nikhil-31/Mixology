package com.capstone.nik.mixology.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.Network.BarIngredientBody
import com.capstone.nik.mixology.Network.BarIngredientsBody
import com.capstone.nik.mixology.Network.BarService
import com.capstone.nik.mixology.Network.BarStateDto
import com.capstone.nik.mixology.data.BarIngredientEntity
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class BarSyncTest {

    /** Behaves like the server: names dedupe ignoring case; removing a missing name is a 404. */
    private class FakeBarService : BarService {
        val ingredients = mutableListOf<String>()
        var failing = false
        var puts = 0

        private fun state(): BarStateDto {
            if (failing) throw java.io.IOException("offline")
            return BarStateDto(ingredients.toList())
        }

        override suspend fun bar() = state()

        override suspend fun replace(body: BarIngredientsBody): BarStateDto {
            state()
            puts++
            ingredients.clear()
            body.ingredients.forEach { name -> if (ingredients.none { it.equals(name, true) }) ingredients += name }
            return state()
        }

        override suspend fun add(body: BarIngredientBody): BarStateDto {
            state()
            if (ingredients.none { it.equals(body.name, true) }) ingredients += body.name
            return state()
        }

        override suspend fun remove(name: String): BarStateDto {
            state()
            if (!ingredients.removeAll { it.equals(name, true) }) {
                throw HttpException(
                    retrofit2.Response.error<Any>(404, "{\"detail\":\"Bar ingredient not found\"}".toResponseBody()),
                )
            }
            return state()
        }
    }

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val service = FakeBarService()
    private val prefs = context.getSharedPreferences("bar_sync_test", Context.MODE_PRIVATE)
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
        NetworkBarSync(service, database.barDao(), sessions, prefs, backgroundScope)

    private suspend fun localBar() = database.barDao().getAll().map { it.name }.toSet()

    private suspend fun stock(vararg names: String) {
        names.forEach { database.barDao().insert(BarIngredientEntity(it)) }
    }

    @Test
    fun signingInUploadsThisDevicesIngredients() = runTest {
        stock("Gin", "Campari")
        service.ingredients += "Sweet Vermouth"

        newSync().pull()
        runCurrent()

        assertEquals(setOf("Gin", "Campari", "Sweet Vermouth"), localBar())
        assertEquals(setOf("Gin", "Campari", "Sweet Vermouth"), service.ingredients.toSet())
        assertEquals("uid-1", prefs.getString("account", null))
    }

    @Test
    fun mergeIgnoresCase() = runTest {
        stock("gin", "Lime Juice")
        service.ingredients += listOf("Gin", "Campari")

        newSync().pull()
        runCurrent()

        assertEquals(setOf("gin", "Lime Juice", "Campari"), localBar())
        assertEquals(listOf("Gin", "Campari", "Lime Juice"), service.ingredients)
    }

    @Test
    fun anotherAccountReplacesThisDevicesBar() = runTest {
        prefs.edit().putString("account", "uid-0").commit()
        stock("Theirs", "Gin")
        service.ingredients += listOf("GIN", "Mezcal")

        newSync().pull()
        runCurrent()

        assertEquals(setOf("Gin", "Mezcal"), localBar())
        assertEquals(listOf("GIN", "Mezcal"), service.ingredients)
        assertEquals(0, service.puts)
    }

    @Test
    fun sameAccountTakesRemovalsFromOtherDevices() = runTest {
        prefs.edit().putString("account", "uid-1").commit()
        stock("Gin", "Campari")
        service.ingredients += "Gin"

        newSync().pull()
        runCurrent()

        assertEquals(setOf("Gin"), localBar())
        assertEquals(listOf("Gin"), service.ingredients)
    }

    @Test
    fun addsAndRemovesReachTheServer() = runTest {
        val sync = newSync()
        runCurrent()

        sync.push("Lime / Lemon Juice", owned = true)
        sync.push("Gin", owned = true)
        runCurrent()
        assertEquals(listOf("Lime / Lemon Juice", "Gin"), service.ingredients)

        sync.push("lime / lemon juice", owned = false)
        sync.push("Never Had It", owned = false)
        runCurrent()
        assertEquals(listOf("Gin"), service.ingredients)
    }

    @Test
    fun signedOutPushesStayLocal() = runTest {
        sessions.clear()
        val sync = newSync()
        sync.push("Gin", owned = true)
        sync.pull()
        runCurrent()
        assertEquals(emptyList<String>(), service.ingredients)
    }

    @Test
    fun serverErrorKeepsLocalBarAndRetriesLater() = runTest {
        prefs.edit().putString("account", "uid-1").commit()
        service.failing = true
        val sync = newSync()
        runCurrent()
        stock("Gin")
        sync.push("Gin", owned = true)
        runCurrent()

        sync.pull()
        runCurrent()
        assertEquals(setOf("Gin"), localBar())

        service.failing = false
        sync.pull()
        runCurrent()
        assertEquals(listOf("Gin"), service.ingredients)
        assertEquals(setOf("Gin"), localBar())
    }

    @Test
    fun removePathEncodesTheNameAsOneSegment() = runTest {
        val urls = mutableListOf<String>()
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                urls += chain.request().url.encodedPath
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{\"ingredients\":[]}".toResponseBody("application/json".toMediaType()))
                    .build()
            })
            .build()
        val api = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BarService::class.java)

        api.remove("Lime / Lemon Juice")
        api.remove("50%")

        assertEquals(listOf("/bar/ingredients/Lime%20%2F%20Lemon%20Juice", "/bar/ingredients/50%25"), urls)
    }
}
