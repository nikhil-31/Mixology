package com.capstone.nik.mixology.ui.search

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
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
    private lateinit var service: FakeCocktailService
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
        service = FakeCocktailService()
        viewModel = SearchViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), service, context),
            context,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun search_debouncesThenLoadsResults() = runTest(dispatcher) {
        service.search = CocktailDbResponse(drinks = listOf(cocktailDrink("1", "Mojito")))
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
    }

    @Test
    fun ingredientSearch_usesFilterEndpoint() = runTest(dispatcher) {
        service.ingredient = CocktailDbResponse(drinks = listOf(cocktailDrink("3", "Bloody Mary")))
        viewModel.onIntent(SearchIntent.Search("Vodka", SearchMode.INGREDIENT))
        advanceTimeBy(250)
        advanceUntilIdle()
        assertEquals(SearchMode.INGREDIENT, viewModel.state.value.mode)
        assertEquals("Vodka", viewModel.state.value.query)
        assertEquals(listOf("Bloody Mary"), viewModel.state.value.results.map { it.name })
    }

    @Test
    fun catalogTermSearch_replacesPreviousResultsImmediately() = runTest(dispatcher) {
        service.ingredient = CocktailDbResponse(drinks = listOf(cocktailDrink("1", "Gin Fizz")))
        viewModel.onIntent(
            SearchIntent.Search("Gin", SearchMode.INGREDIENT, FilterKind.INGREDIENT),
        )
        advanceUntilIdle()
        assertEquals(listOf("Gin Fizz"), viewModel.state.value.results.map { it.name })

        service.ingredient = CocktailDbResponse(drinks = listOf(cocktailDrink("2", "Tequila Sunrise")))
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
}
