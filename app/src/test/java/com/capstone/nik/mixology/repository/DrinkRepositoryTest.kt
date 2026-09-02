package com.capstone.nik.mixology.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
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
    private lateinit var repository: DrinkRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fetchAndCache_matchesLocalRecipesByIngredient() = runTest {
        seedRecipe(cocktailDrink("1", "Old Fashioned").toDrink()!!.copy(
            ingredients = listOf(IngredientMeasure("Whiskey", "2 oz")),
        ))
        seedRecipe(cocktailDrink("2", "Gin Fizz").toDrink()!!)
        repository.fetchAndCache(DrinkFilter.GIN)
        val drinks = repository.observeDrinks(DrinkFilter.GIN).first()
        assertEquals(listOf("Gin Fizz"), drinks.map { it.name })
    }

    @Test
    fun fetchAndCache_observesFilterResultsAndSkipsBadThumbs() = runTest {
        seedRecipe(cocktailDrink("1", "Gin Fizz").toDrink()!!)
        seedRecipe(cocktailDrink("2", "No Thumb", thumb = "null").toDrink()!!)
        repository.fetchAndCache(DrinkFilter.GIN)
        val drinks = repository.observeDrinks(DrinkFilter.GIN).first()
        assertEquals(listOf("Gin Fizz"), drinks.map { it.name })
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
    fun lookupDrink_readsCachedRecipe() = runTest {
        seedRecipe(cocktailDrink("9", "Negroni").toDrink()!!)
        val drink = repository.lookupDrink("9")
        assertEquals("Negroni", drink?.name)
        assertEquals("Shake.", drink?.instructions)
        assertEquals("Negroni", repository.cachedDrink("9")?.name)
        assertNull(repository.lookupDrink("missing"))
    }

    @Test
    fun search_filtersLocalRecipesByName() = runTest {
        seedRecipe(cocktailDrink("1", "Mojito").toDrink()!!)
        seedRecipe(cocktailDrink("2", "Margarita").toDrink()!!)
        assertEquals(listOf("Mojito"), repository.search("moj").map { it.name })
    }

    @Test
    fun shoppingList_addsDistinctIngredients() = runTest {
        repository.addToShoppingList(listOf("Gin", "gin", "Lime"))
        val names = repository.observeShopping().first().map { it.name }
        assertEquals(listOf("Gin", "Lime"), names)
    }

    @Test
    fun bar_addRemoveAndRecommendFromCachedRecipes() = runTest {
        val negroni = cocktailDrink("11003", "Negroni").toDrink()!!.copy(
            instructions = "Stir.",
            ingredients = listOf(
                IngredientMeasure("Gin", "1 oz"),
                IngredientMeasure("Campari", "1 oz"),
            ),
        )
        repository.save(negroni)
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

    @Test
    fun randomDrink_usesLocalRecipe_notEndpoint() = runTest {
        repository.save(cocktailDrink("11007", "Margarita").toDrink()!!)
        val drink = repository.randomDrink()
        assertEquals("11007", drink?.id)
        assertEquals("Margarita", drink?.name)
    }

    private suspend fun seedRecipe(drink: Drink) {
        database.drinkDao().upsertRecipe(drink.toEntity())
    }
}
