package com.capstone.nik.mixology.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.Network.NoteBody
import com.capstone.nik.mixology.Network.SavedService
import com.capstone.nik.mixology.Network.SavedSyncDto
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkEntity
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SavedSyncTest {

    private class FakeSavedService : SavedService {
        val saved = mutableSetOf<String>()
        val notes = mutableMapOf<String, String>()
        val calls = mutableListOf<String>()
        var failWrites = false
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun sync(): SavedSyncDto {
            calls += "GET"
            gate?.await()
            return SavedSyncDto(saved.toList(), notes.toMap())
        }

        override suspend fun setNotes(id: String, body: NoteBody) {
            calls += "PUT notes $id ${body.notes}"
            if (failWrites) throw IOException("offline")
            val text = body.notes.trim()
            if (text.isEmpty()) notes -= id else notes[id] = text
        }

        override suspend fun save(drinkId: String) {
            calls += "PUT $drinkId"
            if (failWrites) throw IOException("offline")
            saved += drinkId
        }

        override suspend fun unsave(drinkId: String) {
            calls += "DELETE $drinkId"
            if (failWrites) throw IOException("offline")
            saved -= drinkId
        }
    }

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val service = FakeSavedService()
    private val cocktails = FakeCocktailService()
    private lateinit var database: MixologyDatabase
    private lateinit var sessions: SessionStore

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        sessions = SessionStore(context)
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun TestScope.newSync() =
        NetworkSavedSync(service, cocktails, database.drinkDao(), sessions, backgroundScope)

    private suspend fun cache(id: String, saved: Boolean = false, notes: String = "") {
        database.drinkDao().insertDrinks(
            listOf(DrinkEntity(id = id, name = id, thumb = "", saved = saved, notes = notes)),
        )
    }

    @Test
    fun theServersListReplacesTheSavedSet() = runTest {
        cache("cached")
        service.saved += setOf("cached", "remote")
        cocktails.lookupsById = mapOf("remote" to CocktailDbResponse(listOf(cocktailDrink("remote", "Remote"))))

        val sync = newSync()
        runCurrent()

        assertEquals(setOf("cached", "remote"), sync.savedIds.value)

        // Unsaved on another device: it stays gone here.
        service.saved -= "cached"
        sync.sync()
        assertEquals(setOf("remote"), sync.savedIds.value)
        assertEquals(setOf("remote"), service.saved)
    }

    @Test
    fun missingDrinkDetailsAreFetchedAndCachedWithoutASavedFlag() = runTest {
        service.saved += "remote"
        cocktails.lookupsById = mapOf("remote" to CocktailDbResponse(listOf(cocktailDrink("remote", "Remote"))))

        newSync()
        runCurrent()

        val cached = database.drinkDao().getById("remote")
        assertEquals("Remote", cached?.name)
        assertFalse(cached!!.saved)
    }

    @Test
    fun legacyLocalSavesAreClearedAndNeverUploaded() = runTest {
        cache("old", saved = true)

        val sync = newSync()
        runCurrent()

        assertFalse(database.drinkDao().getById("old")!!.saved)
        assertTrue(sync.savedIds.value.isEmpty())
        assertTrue(service.saved.isEmpty())
        assertEquals(listOf("GET"), service.calls)
    }

    @Test
    fun savesAndUnsavesReachTheServer() = runTest {
        val sync = newSync()
        runCurrent()
        service.calls.clear()

        sync.push("negroni", saved = true)
        assertEquals(setOf("negroni"), sync.savedIds.value)
        runCurrent()
        assertEquals(setOf("negroni"), service.saved)

        sync.push("negroni", saved = false)
        sync.push("u-1", saved = true)
        assertTrue(sync.savedIds.value.isEmpty())
        runCurrent()
        assertTrue(service.saved.isEmpty())
        assertEquals(listOf("PUT negroni", "DELETE negroni"), service.calls)
    }

    @Test
    fun aFailedSaveIsUndone() = runTest {
        val sync = newSync()
        runCurrent()
        service.failWrites = true

        sync.push("negroni", saved = true)
        assertEquals(setOf("negroni"), sync.savedIds.value)
        runCurrent()

        assertTrue(sync.savedIds.value.isEmpty())
    }

    @Test
    fun aFailedUnsaveIsUndone() = runTest {
        service.saved += "negroni"
        val sync = newSync()
        runCurrent()
        service.failWrites = true

        sync.push("negroni", saved = false)
        assertTrue(sync.savedIds.value.isEmpty())
        runCurrent()

        assertEquals(setOf("negroni"), sync.savedIds.value)
    }

    @Test
    fun aChangeMadeDuringAPullSurvivesIt() = runTest {
        val gate = CompletableDeferred<Unit>()
        service.gate = gate
        val sync = newSync()
        runCurrent() // The startup pull waits in GET sync.

        sync.push("late", saved = true)
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(setOf("late"), sync.savedIds.value)
        assertEquals(setOf("late"), service.saved)
    }

    @Test
    fun signingOutForgetsTheList() = runTest {
        service.saved += "remote"
        val sync = newSync()
        runCurrent()
        assertEquals(setOf("remote"), sync.savedIds.value)

        sessions.clear()
        runCurrent()

        assertTrue(sync.savedIds.value.isEmpty())
    }

    @Test
    fun signedOutChangesStayInMemory() = runTest {
        sessions.clear()
        val sync = newSync()
        runCurrent()

        sync.push("negroni", saved = true)
        runCurrent()

        assertEquals(setOf("negroni"), sync.savedIds.value)
        assertTrue(service.calls.isEmpty())
    }

    @Test
    fun aPullAppliesTheServersNotes() = runTest {
        service.notes["11007"] = "Salt the rim"
        val sync = newSync()
        runCurrent()

        assertEquals(mapOf("11007" to "Salt the rim"), sync.notes.value)

        service.notes["11007"] = "No salt"
        service.notes["11000"] = "Extra mint"
        sync.sync()
        assertEquals(mapOf("11007" to "No salt", "11000" to "Extra mint"), sync.notes.value)
    }

    @Test
    fun legacyLocalNotesAreClearedAndNeverUploaded() = runTest {
        cache("old", notes = "Kept on the device")

        val sync = newSync()
        runCurrent()

        assertEquals("", database.drinkDao().getById("old")!!.notes)
        assertTrue(sync.notes.value.isEmpty())
        assertTrue(service.notes.isEmpty())
        assertEquals(listOf("GET"), service.calls)
    }

    @Test
    fun pushNotesSendsThePutAndShowsAtOnce() = runTest {
        val sync = newSync()
        runCurrent()
        service.calls.clear()

        sync.pushNotes("11007", "Salt the rim")
        assertEquals(mapOf("11007" to "Salt the rim"), sync.notes.value)
        runCurrent()

        assertEquals(listOf("PUT notes 11007 Salt the rim"), service.calls)
        assertEquals(mapOf("11007" to "Salt the rim"), service.notes)
        assertEquals("", database.drinkDao().getById("11007")?.notes.orEmpty())
    }

    @Test
    fun pushNotesAlsoSendsNotesOnTheUsersOwnRecipes() = runTest {
        val sync = newSync()
        runCurrent()
        service.calls.clear()
        val recipeId = "${Drink.USER_RECIPE_PREFIX}42"

        sync.pushNotes(recipeId, "Less sugar next time")
        runCurrent()

        assertEquals(listOf("PUT notes $recipeId Less sugar next time"), service.calls)
        assertEquals(mapOf(recipeId to "Less sugar next time"), sync.notes.value)
    }

    @Test
    fun aBlankNoteSendsAnEmptyStringAndIsRemoved() = runTest {
        service.notes["11007"] = "Salt the rim"
        val sync = newSync()
        runCurrent()
        service.calls.clear()

        sync.pushNotes("11007", "   ")
        assertTrue(sync.notes.value.isEmpty())
        runCurrent()

        assertEquals(listOf("PUT notes 11007 "), service.calls)
        assertTrue(service.notes.isEmpty())
    }

    @Test
    fun aFailedNoteIsKeptAndSentByTheNextPull() = runTest {
        val sync = newSync()
        runCurrent()
        service.failWrites = true

        sync.pushNotes("11007", "Salt the rim")
        runCurrent()
        assertEquals(mapOf("11007" to "Salt the rim"), sync.notes.value)
        assertTrue(service.notes.isEmpty())

        service.failWrites = false
        service.calls.clear()
        sync.sync()

        assertEquals(listOf("PUT notes 11007 Salt the rim", "GET"), service.calls)
        assertEquals(mapOf("11007" to "Salt the rim"), service.notes)
        assertEquals(mapOf("11007" to "Salt the rim"), sync.notes.value)
    }

    @Test
    fun anUnsentNoteOverridesThePulledOne() = runTest {
        service.notes["11007"] = "Old"
        val gate = CompletableDeferred<Unit>()
        service.gate = gate
        val sync = newSync()
        runCurrent() // The startup pull waits in GET sync.

        sync.pushNotes("11007", "New")
        runCurrent()
        gate.complete(Unit)
        runCurrent()

        assertEquals(mapOf("11007" to "New"), sync.notes.value)
        assertEquals(mapOf("11007" to "New"), service.notes)
    }

    @Test
    fun signingOutForgetsTheNotes() = runTest {
        service.notes["11007"] = "Salt the rim"
        val sync = newSync()
        runCurrent()
        service.failWrites = true
        sync.pushNotes("11000", "Unsent")
        runCurrent()

        sessions.clear()
        runCurrent()
        assertTrue(sync.notes.value.isEmpty())

        // The unsent edit was forgotten too: signing back in doesn't send it.
        service.failWrites = false
        service.calls.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        runCurrent()
        assertEquals(listOf("GET"), service.calls)
        assertEquals(mapOf("11007" to "Salt the rim"), sync.notes.value)
    }
}
