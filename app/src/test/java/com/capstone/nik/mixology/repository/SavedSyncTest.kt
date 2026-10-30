package com.capstone.nik.mixology.repository

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.Network.SavedService
import com.capstone.nik.mixology.Network.SavedSyncDto
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.data.toEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SavedSyncTest {

    private class FakeSavedService : SavedService {
        val saved = mutableSetOf<String>()
        override suspend fun sync() = SavedSyncDto(saved.toList())
        override suspend fun save(drinkId: String) {
            saved += drinkId
        }
        override suspend fun unsave(drinkId: String) {
            saved -= drinkId
        }
    }

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val service = FakeSavedService()
    private val cocktails = FakeCocktailService()
    private val prefs = context.getSharedPreferences("saved_sync_test", Context.MODE_PRIVATE)
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
        NetworkSavedSync(service, cocktails, database.drinkDao(), sessions, prefs, backgroundScope)

    private suspend fun localSaved() = database.drinkDao().observeSavedIds().first().toSet()

    private suspend fun saveLocally(id: String) {
        database.drinkDao().saveDrink(Drink(id = id, name = id, thumb = "").toEntity().copy(saved = true))
    }

    @Test
    fun signingInMergesThisDevicesSavesWithTheAccount() = runTest {
        saveLocally("local")
        service.saved += "remote"
        cocktails.lookupsById = mapOf("remote" to CocktailDbResponse(listOf(cocktailDrink("remote", "Remote"))))

        newSync().sync()
        runCurrent()

        assertEquals(setOf("local", "remote"), localSaved())
        assertEquals(setOf("local", "remote"), service.saved)
        assertEquals("Remote", database.drinkDao().getById("remote")?.name)
    }

    @Test
    fun anotherAccountReplacesThisDevicesSaves() = runTest {
        prefs.edit().putString("account", "uid-0").commit()
        saveLocally("theirs")
        saveLocally("both")
        service.saved += "both"

        newSync().sync()
        runCurrent()

        assertEquals(setOf("both"), localSaved())
        assertEquals(setOf("both"), service.saved)
    }

    @Test
    fun savesAndUnsavesReachTheServer() = runTest {
        val sync = newSync()
        runCurrent()

        sync.push("negroni", saved = true)
        runCurrent()
        assertEquals(setOf("negroni"), service.saved)

        sync.push("negroni", saved = false)
        sync.push("u-1", saved = true)
        runCurrent()
        assertEquals(emptySet<String>(), service.saved)
    }
}
