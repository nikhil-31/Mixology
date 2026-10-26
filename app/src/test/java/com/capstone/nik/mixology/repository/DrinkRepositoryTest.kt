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
