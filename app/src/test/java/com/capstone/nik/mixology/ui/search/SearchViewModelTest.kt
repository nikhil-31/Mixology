package com.capstone.nik.mixology.ui.search

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_REMOVE_FROM_WISHLIST
import com.capstone.nik.mixology.analytics.PARAM_RESULT_COUNT
import com.capstone.nik.mixology.analytics.PARAM_SEARCH_MODE
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import kotlinx.coroutines.runBlocking
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var database: MixologyDatabase
    private lateinit var analytics: AnalyticsTracker
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.getSharedPreferences("mixology", Context.MODE_PRIVATE)
            .edit()
            .remove("saved_list_view")
            .apply()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        runBlocking {
            database.drinkDao().upsertRecipe(
                cocktailDrink("seed-skip", "ZZZ Unmatchable", ingredient = "SeedSkipIngredient")
                    .toDrink()!!
                    .toEntity(),
            )
            database.drinkDao().replaceCatalog(
                FilterKind.INGREDIENT.name,
                listOf("Gin", "Ginger", "Vodka", "Virgin"),
            )
        }
        analytics = AnalyticsTracker.forTests()
        viewModel = SearchViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), context),
            context,
            analytics,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun search_debouncesThenLoadsResults() = runTest(dispatcher) {
        seedRecipe("1", "Mojito")
        viewModel.onIntent(SearchIntent.Search("mo"))
        runCurrent()
        assertTrue(viewModel.state.value.results.isEmpty())
        advanceTimeBy(249)
        runCurrent()
        assertTrue(viewModel.state.value.results.isEmpty())
        advanceTimeBy(1)
        advanceUntilIdle()
        assertEquals(listOf("Mojito"), viewModel.state.value.results.map { it.name })
        assertTrue(!viewModel.state.value.loading)
        val search = analytics.recorded.single { it.name == FirebaseAnalytics.Event.SEARCH }
        assertEquals("mo", search.params[FirebaseAnalytics.Param.SEARCH_TERM])
        assertEquals("NAME", search.params[PARAM_SEARCH_MODE])
        assertEquals(1, search.params[PARAM_RESULT_COUNT])
    }

    @Test
    fun ingredientSearch_matchesLocalRecipes() = runTest(dispatcher) {
        seedRecipe("3", "Bloody Mary", ingredient = "Vodka")
        viewModel.onIntent(SearchIntent.Search("Vodka", SearchMode.INGREDIENT, commit = true))
        advanceUntilIdle()
        assertEquals(SearchMode.INGREDIENT, viewModel.state.value.mode)
        assertEquals("Vodka", viewModel.state.value.query)
        assertEquals(listOf("Bloody Mary"), viewModel.state.value.results.map { it.name })
        assertTrue(viewModel.state.value.suggestions.isEmpty())
        val search = analytics.recorded.single { it.name == FirebaseAnalytics.Event.SEARCH }
        assertEquals("Vodka", search.params[FirebaseAnalytics.Param.SEARCH_TERM])
        assertEquals("INGREDIENT", search.params[PARAM_SEARCH_MODE])
    }

    @Test
    fun ingredientTyping_showsSuggestionsWithoutSearchingDrinks() = runTest(dispatcher) {
        seedRecipe("3", "Bloody Mary", ingredient = "Vodka")
        viewModel.onIntent(SearchIntent.Search("gin", SearchMode.INGREDIENT))
        advanceTimeBy(250)
        advanceUntilIdle()
        assertEquals(listOf("Gin", "Ginger", "Virgin"), viewModel.state.value.suggestions)
        assertTrue(viewModel.state.value.results.isEmpty())
        assertTrue(!viewModel.state.value.empty)
        assertTrue(!viewModel.state.value.loading)
        assertTrue(analytics.recorded.none { it.name == FirebaseAnalytics.Event.SEARCH })
    }

    @Test
    fun selectSuggestion_searchesDrinksImmediately() = runTest(dispatcher) {
        seedRecipe("3", "Bloody Mary", ingredient = "Vodka")
        viewModel.onIntent(SearchIntent.SelectSuggestion("Vodka"))
        runCurrent()
        assertEquals("Vodka", viewModel.state.value.query)
        advanceUntilIdle()
        assertEquals(SearchMode.INGREDIENT, viewModel.state.value.mode)
        assertEquals(listOf("Bloody Mary"), viewModel.state.value.results.map { it.name })
        assertTrue(viewModel.state.value.suggestions.isEmpty())
        val search = analytics.recorded.single { it.name == FirebaseAnalytics.Event.SEARCH }
        assertEquals("Vodka", search.params[FirebaseAnalytics.Param.SEARCH_TERM])
    }

    @Test
    fun filterIngredientSuggestions_ranksPrefixMatchesFirst() {
        assertEquals(
            listOf("Gin", "Ginger", "Virgin"),
            filterIngredientSuggestions(listOf("Virgin", "Ginger", "Gin", "Vodka"), "gin"),
        )
        assertEquals(
            10,
            filterIngredientSuggestions((1..12).map { "Gin $it" }, "gin").size,
        )
        assertTrue(filterIngredientSuggestions(listOf("Gin", "Vodka"), "").isEmpty())
    }

    @Test
    fun catalogTermSearch_replacesPreviousResultsImmediately() = runTest(dispatcher) {
        seedRecipe("1", "Gin Fizz", ingredient = "Gin")
        viewModel.onIntent(
            SearchIntent.Search("Gin", SearchMode.INGREDIENT, FilterKind.INGREDIENT),
        )
        advanceUntilIdle()
        assertEquals(listOf("Gin Fizz"), viewModel.state.value.results.map { it.name })

        seedRecipe("2", "Tequila Sunrise", ingredient = "Tequila")
        viewModel.onIntent(
            SearchIntent.Search("Tequila", SearchMode.INGREDIENT, FilterKind.INGREDIENT),
        )
        advanceUntilIdle()
        assertEquals(listOf("Tequila Sunrise"), viewModel.state.value.results.map { it.name })
    }

    @Test
    fun openDrink_emitsEffect() = runTest(dispatcher) {
        val drink = cocktailDrink("1", "Mojito").toDrink()!!
        viewModel.effects.test {
            viewModel.onIntent(SearchIntent.OpenDrink(drink))
            assertEquals("1", (awaitItem() as SearchEffect.OpenDrink).drink.id)
        }
    }

    @Test
    fun toggleListView_updatesStateAndPersists() = runTest(dispatcher) {
        advanceUntilIdle()
        assertTrue(!viewModel.state.value.listView)
        viewModel.onIntent(SearchIntent.ToggleListView)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.listView)
        val context = ApplicationProvider.getApplicationContext<Application>()
        assertTrue(
            context.getSharedPreferences("mixology", Context.MODE_PRIVATE)
                .getBoolean("saved_list_view", false),
        )
    }

    @Test
    fun toggleSaved_logsWishlistEvents() = runTest(dispatcher) {
        val drink = cocktailDrink("1", "Mojito").toDrink()!!
        viewModel.onIntent(SearchIntent.ToggleSaved(drink))
        advanceUntilIdle()
        viewModel.onIntent(SearchIntent.ToggleSaved(drink.copy(saved = true)))
        advanceUntilIdle()
        assertEquals(
            listOf(FirebaseAnalytics.Event.ADD_TO_WISHLIST, EVENT_REMOVE_FROM_WISHLIST),
            analytics.recorded.map { it.name },
        )
        assertEquals("1", analytics.recorded[0].params[FirebaseAnalytics.Param.ITEM_ID])
    }

    private suspend fun seedRecipe(id: String, name: String, ingredient: String = "Gin") {
        database.drinkDao().upsertRecipe(
            cocktailDrink(id, name, ingredient = ingredient).toDrink()!!.toEntity(),
        )
    }
}
