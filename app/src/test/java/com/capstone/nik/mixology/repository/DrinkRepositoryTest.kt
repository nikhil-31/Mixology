package com.capstone.nik.mixology.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.play.MemoryReviewPromptStore
import com.capstone.nik.mixology.play.NoOpPlayReviewClient
import com.capstone.nik.mixology.play.PlayReviewController
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertFalse
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import com.capstone.nik.mixology.data.DrinkEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DrinkRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var service: FakeCocktailService
    private lateinit var repository: DrinkRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = FakeCocktailService()
        repository = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fetchAndCache_returnsIngredientMatchesFromApi() = runTest {
        service.ingredient = CocktailDbResponse(listOf(cocktailDrink("2", "Gin Fizz")))
        val drinks = repository.fetchAndCache(DrinkFilter.GIN)
        assertEquals(listOf("Gin Fizz"), drinks.map { it.name })
        assertEquals(listOf("Gin Fizz"), repository.observeDrinks(DrinkFilter.GIN).first().map { it.name })
        assertTrue(database.drinkDao().getRecipes().isEmpty())
    }

    @Test
    fun fetchAndCache_observesFilterResultsAndSkipsBadThumbs() = runTest {
        service.ingredient = CocktailDbResponse(
            listOf(
                cocktailDrink("1", "Gin Fizz"),
                cocktailDrink("2", "No Thumb", thumb = "null"),
            ),
        )
        repository.fetchAndCache(DrinkFilter.GIN)
        val drinks = repository.observeDrinks(DrinkFilter.GIN).first()
        assertEquals(listOf("Gin Fizz"), drinks.map { it.name })
        assertTrue(database.drinkDao().getRecipes().isEmpty())
    }

    @Test
    fun saveAndUnsave_updatesSavedFlow() = runTest {
        val drink = cocktailDrink("11007", "Margarita").toDrink()!!
        repository.save(drink)
        assertEquals(setOf("11007"), repository.observeSavedIds().first())
        repository.unsave("11007")
        assertTrue(repository.observeSavedIds().first().isEmpty())
    }

    @Test
    fun savedList_comesFromTheSavedSetNotRoomsFlag() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val ids = MutableStateFlow(setOf("11007"))
        val savedSync = object : SavedSync {
            override val savedIds: StateFlow<Set<String>> = ids
            override val notes: StateFlow<Map<String, String>> = MutableStateFlow(emptyMap())
            override fun push(id: String, saved: Boolean) {
                ids.value = if (saved) ids.value + id else ids.value - id
            }
            override fun pushNotes(id: String, notes: String) = Unit
            override suspend fun sync() = Unit
        }
        val repo = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
            savedSync = savedSync,
        )
        database.drinkDao().insertDrinks(
            listOf(
                DrinkEntity("11007", "Margarita", ""),
                DrinkEntity("legacy", "Old Fashioned", "", saved = true),
            ),
        )

        val saved = repo.observeDrinks(DrinkFilter.SAVED).first()
        assertEquals(listOf("11007"), saved.map { it.id })
        assertTrue(saved.single().saved)
        assertEquals(listOf("11007"), repo.getSavedSync().map { it.id })

        repo.unsave("11007")
        assertTrue(repo.observeDrinks(DrinkFilter.SAVED).first().isEmpty())
        assertFalse(database.drinkDao().getById("11007")!!.saved)

        repo.save(cocktailDrink("11000", "Mojito").toDrink()!!)
        assertEquals(setOf("11000"), ids.value)
        assertEquals(listOf("Mojito"), repo.observeDrinks(DrinkFilter.SAVED).first().map { it.name })
        assertFalse(database.drinkDao().getById("11000")!!.saved)
    }

    @Test
    fun save_recordsPlayReviewSave() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val store = MemoryReviewPromptStore()
        val playReviews = PlayReviewController(
            store = store,
            client = NoOpPlayReviewClient,
            enabled = true,
            now = { 0L },
        )
        val repo = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
            playReviews,
        )
        repo.save(cocktailDrink("11007", "Margarita").toDrink()!!)
        assertEquals(1, store.state().saveCount)
    }

    @Test
    fun lookupDrink_readsEndpointWithoutSaving() = runTest {
        service.lookupsById = mapOf(
            "9" to CocktailDbResponse(listOf(cocktailDrink("9", "Negroni"))),
        )
        val drink = repository.lookupDrink("9")
        assertEquals("Negroni", drink?.name)
        assertEquals("Shake.", drink?.instructions)
        assertNull(repository.cachedDrink("9"))
        assertNull(repository.lookupDrink("missing"))
    }

    @Test
    fun notes_comeFromSavedSyncAndNeverReachRoom() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val notes = MutableStateFlow(mapOf("11007" to "Salt the rim"))
        val pushed = mutableListOf<Pair<String, String>>()
        val ids = MutableStateFlow(setOf("11007"))
        val savedSync = object : SavedSync {
            override val savedIds: StateFlow<Set<String>> = ids
            override val notes: StateFlow<Map<String, String>> = notes
            override fun push(id: String, saved: Boolean) {
                ids.value = if (saved) ids.value + id else ids.value - id
            }
            override fun pushNotes(id: String, notes: String) {
                pushed += id to notes
            }
            override suspend fun sync() = Unit
        }
        val repo = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
            savedSync = savedSync,
        )
        service.lookupsById = mapOf("11007" to CocktailDbResponse(listOf(cocktailDrink("11007", "Margarita"))))
        // A note an older version left in Room is not shown.
        database.drinkDao().insertDrinks(listOf(DrinkEntity("11007", "Margarita", "", notes = "Room copy")))

        assertEquals("Salt the rim", repo.lookupDrink("11007")?.notes)
        assertEquals("Salt the rim", repo.cachedDrink("11007")?.notes)
        assertEquals("Salt the rim", repo.observeDrinks(DrinkFilter.SAVED).first().single().notes)
        assertEquals("Salt the rim", repo.getSavedSync().single().notes)

        notes.value = mapOf("11007" to "No salt")
        assertEquals("No salt", repo.observeDrinks(DrinkFilter.SAVED).first().single().notes)

        repo.updateNotes("11007", "Less salt")
        assertEquals(listOf("11007" to "Less salt"), pushed)
        assertEquals("Room copy", database.drinkDao().getById("11007")!!.notes)

        // Saving or viewing a drink that carries notes doesn't write them to Room.
        repo.save(cocktailDrink("11000", "Mojito").toDrink()!!.copy(notes = "Extra mint"))
        assertEquals("", database.drinkDao().getById("11000")!!.notes)
    }

    @Test
    fun search_returnsNameMatchesFromApi() = runTest {
        service.search = CocktailDbResponse(listOf(cocktailDrink("1", "Mojito")))
        assertEquals(listOf("Mojito"), repository.search("moj").map { it.name })
        assertTrue(database.drinkDao().getRecipes().isEmpty())
    }

    @Test
    fun shoppingList_groupsIngredientsByDrinkAndKeepsMeasures() = runTest {
        val margarita = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "https://example.com/m.jpg",
            instructions = "Shake.",
            ingredients = listOf(
                IngredientMeasure("Tequila", "1 1/2 oz"),
                IngredientMeasure("Lime juice", "1 oz"),
                IngredientMeasure("tequila", "skip"),
            ),
        )
        val mojito = Drink(
            id = "11000",
            name = "Mojito",
            thumb = "",
            ingredients = listOf(IngredientMeasure("Lime juice", "1/2 oz")),
        )
        database.drinkDao().upsertRecipe(margarita.toEntity())
        database.drinkDao().upsertRecipe(mojito.toEntity())
        repository.addToShoppingList(margarita)
        repository.addToShoppingList(mojito)
        val sections = repository.observeShoppingSections().first()
        assertEquals(listOf("11007", "11000"), sections.map { it.drinkId })
        assertEquals(listOf("Tequila", "Lime juice"), sections[0].items.map { it.name })
        assertEquals("1 1/2 oz", sections[0].measure(sections[0].items.first()))
        assertEquals(listOf("Lime juice"), sections[1].items.map { it.name })
        assertTrue(repository.observeShopping().first().count { it.name.equals("Lime juice", true) } == 2)
        repository.removeFromShoppingList("11007")
        assertEquals(listOf("11000"), repository.observeShoppingSections().first().map { it.drinkId })
    }

    @Test
    fun bar_addRemoveAndRecommendFromApi() = runTest {
        val negroni = cocktailDrink("11003", "Negroni").toDrink()!!.copy(
            instructions = "Stir.",
            ingredients = listOf(
                IngredientMeasure("Gin", "1 oz"),
                IngredientMeasure("Campari", "1 oz"),
            ),
        )
        service.recommendationDrinks = listOf(negroni)
        repository.addToBar("Gin")
        assertEquals(listOf("Gin"), repository.observeBar().first())
        val almost = repository.observeBarRecommendations().first()
        assertTrue(almost.makeable.isEmpty())
        assertEquals(listOf("Campari"), almost.almost.single().missing)
        repository.addToBar("Campari")
        val makeable = repository.observeBarRecommendations().first()
        assertEquals(listOf("Negroni"), makeable.makeable.map { it.name })
        repository.removeFromBar("Gin")
        assertEquals(listOf("Campari"), repository.observeBar().first())
    }

    @Test
    fun bar_changesArePushedAndSyncPulls() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val sync = RecordingBarSync()
        val repo = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
            barSync = sync,
        )
        repo.addToBar("  Gin ")
        repo.addToBar("   ")
        repo.removeFromBar("Gin")
        repo.syncBar()
        assertEquals(listOf("Gin" to true, "Gin" to false), sync.pushes)
        assertEquals(1, sync.pulls)
        assertTrue(repo.observeBar().first().isEmpty())
    }

    private class RecordingBarSync : BarSync {
        val pushes = mutableListOf<Pair<String, Boolean>>()
        var pulls = 0
        override fun push(name: String, owned: Boolean) {
            pushes += name to owned
        }
        override suspend fun pull() {
            pulls++
        }
    }

    private class SignedInBarSync(recs: BarRecommendations?) : BarSync {
        override val recommendations = MutableStateFlow(recs)
        override fun push(name: String, owned: Boolean) = Unit
        override suspend fun pull() = Unit
    }

    private fun savedSyncOf(vararg ids: String) = object : SavedSync {
        override val savedIds: StateFlow<Set<String>> = MutableStateFlow(ids.toSet())
        override val notes: StateFlow<Map<String, String>> = MutableStateFlow(emptyMap())
        override fun push(id: String, saved: Boolean) = Unit
        override fun pushNotes(id: String, notes: String) = Unit
        override suspend fun sync() = Unit
    }

    @Test
    fun barRecommendations_signedInComeFromTheAccountWithSavedFlags() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val own = Drink(id = "u-sour", name = "My Sour", thumb = "")
        val negroni = Drink(id = "11003", name = "Negroni", thumb = "")
        val mojito = Drink(id = "11000", name = "Mojito", thumb = "")
        val sync = SignedInBarSync(
            BarRecommendations(
                makeable = listOf(own, negroni),
                almost = listOf(BarAlmostDrink(mojito, listOf("Mint"))),
            ),
        )
        val repo = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
            savedSync = savedSyncOf("11003", "11000"),
            barSync = sync,
        )
        repo.addToBar("Gin")

        val recs = repo.observeBarRecommendations().first()
        assertEquals(listOf("u-sour", "11003"), recs.makeable.map { it.id })
        assertEquals(listOf(false, true), recs.makeable.map { it.saved })
        assertTrue(recs.almost.single().drink.saved)
        assertEquals(0, service.recommendationCalls)
    }

    @Test
    fun barRecommendations_signedOutUseThePublicPreview() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val negroni = cocktailDrink("11003", "Negroni").toDrink()!!.copy(
            ingredients = listOf(IngredientMeasure("Gin", "1 oz")),
        )
        service.recommendationDrinks = listOf(negroni)
        val repo = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            service,
            context,
            savedSync = savedSyncOf("11003"),
            barSync = SignedInBarSync(null),
        )
        repo.addToBar("Gin")

        val recs = repo.observeBarRecommendations().first()
        assertEquals(listOf("11003"), recs.makeable.map { it.id })
        assertTrue(recs.makeable.single().saved)
        assertEquals(1, service.recommendationCalls)
    }

    @Test
    fun barRecommendations_networkFailureReturnsEmpty() = runTest {
        service.failRecommendations = true
        repository.addToBar("Gin")
        val recommendations = repository.observeBarRecommendations().first()
        assertTrue(recommendations.makeable.isEmpty())
        assertTrue(recommendations.almost.isEmpty())
        assertEquals(listOf("Gin"), repository.observeBar().first())
    }

    @Test
    fun save_addsToRecentlyViewedNewestFirst() = runTest {
        val first = cocktailDrink("1", "Gin Fizz").toDrink()!!
        val second = cocktailDrink("2", "Negroni").toDrink()!!
        repository.save(first)
        repository.save(second)
        assertEquals(listOf("2", "1"), repository.observeRecentlyViewed().first().map { it.id })
        repository.unsave("2")
        assertEquals(listOf("2", "1"), repository.observeRecentlyViewed().first().map { it.id })
    }

    @Test
    fun recordViewed_movesExistingToFrontAndCapsAtLimit() = runTest {
        val drinks = (1..31).map { index ->
            cocktailDrink(index.toString(), "Drink $index").toDrink()!!
        }
        drinks.forEach { repository.recordViewed(it) }
        val recent = repository.observeRecentlyViewed().first()
        assertEquals(30, recent.size)
        assertEquals("31", recent.first().id)
        assertEquals("2", recent.last().id)
        repository.recordViewed(drinks.first())
        val moved = repository.observeRecentlyViewed().first()
        assertEquals("1", moved.first().id)
        assertEquals(30, moved.size)
        assertTrue(moved.none { it.id == "2" })
    }

}
