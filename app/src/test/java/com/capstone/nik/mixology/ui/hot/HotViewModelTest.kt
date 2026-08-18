package com.capstone.nik.mixology.ui.hot

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.catalog
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class HotViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var service: FakeCocktailService
    private lateinit var viewModel: HotViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = FakeCocktailService()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_usesDrinkTypeCatalogTerms() = runTest {
        service.categories = catalog("Cocktail", "Shake")
        service.drinkTypesByQuery = mapOf(
            "Cocktail" to CocktailDbResponse(drinks = listOf(cocktailDrink("1", "Margarita"))),
            "Shake" to CocktailDbResponse(drinks = listOf(cocktailDrink("2", "Milk Shake"))),
        )
        viewModel = createViewModel()
        viewModel.state.test {
            val loaded = awaitItemUntil { state ->
                state.visibleCategories.map { it.filter.query } == listOf("Cocktail", "Shake") &&
                    state.visibleCategories.all { it.drinks.isNotEmpty() }
            }
            assertEquals(DrinkFilter.COCKTAIL, loaded.visibleCategories[0].filter)
            assertEquals(FilterKind.DRINK_TYPE, loaded.visibleCategories[1].filter.kind)
            assertEquals("Margarita", loaded.visibleCategories[0].drinks.single().name)
            assertEquals("Milk Shake", loaded.visibleCategories[1].drinks.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun load_fallsBackToPresetDrinkTypesWhenCatalogEmpty() = runTest {
        service.drinkType = CocktailDbResponse(drinks = listOf(cocktailDrink("1", "Negroni")))
        viewModel = createViewModel()
        viewModel.state.test {
            val loaded = awaitItemUntil { state ->
                state.visibleCategories.map { it.filter } ==
                    listOf(DrinkFilter.COCKTAIL, DrinkFilter.ORDINARY_DRINK)
            }
            assertEquals("Negroni", loaded.visibleCategories.first().drinks.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_addsRecentlyViewedRowFirst() = runTest {
        service.drinkType = CocktailDbResponse(drinks = listOf(cocktailDrink("1", "Negroni")))
        viewModel = createViewModel()
        val drink = cocktailDrink("9", "Negroni").toDrink()!!
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(HotIntent.ToggleSaved(drink))
            val withRecent = awaitItemUntil { state ->
                state.visibleCategories.firstOrNull()?.filter == DrinkFilter.RECENTLY_VIEWED
            }
            assertEquals("9", withRecent.visibleCategories.first().drinks.single().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(): HotViewModel {
        val context = ApplicationProvider.getApplicationContext<Application>()
        return HotViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), service, context),
            NetworkMonitor.forTests(),
        )
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
