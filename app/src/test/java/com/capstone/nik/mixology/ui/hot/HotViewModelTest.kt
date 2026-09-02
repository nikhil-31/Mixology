package com.capstone.nik.mixology.ui.hot

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
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
    private lateinit var viewModel: HotViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_usesDrinkTypeCatalogTerms() = runTest {
        database.drinkDao().replaceCatalog(FilterKind.DRINK_TYPE.name, listOf("Cocktail", "Shake"))
        database.drinkDao().upsertRecipe(
            cocktailDrink("1", "Margarita").toDrink()!!.copy(category = "Cocktail").toEntity(),
        )
        database.drinkDao().upsertRecipe(
            cocktailDrink("2", "Milk Shake").toDrink()!!.copy(category = "Shake").toEntity(),
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
        database.drinkDao().upsertRecipe(
            cocktailDrink("1", "Negroni").toDrink()!!.copy(category = "Cocktail").toEntity(),
        )
        viewModel = createViewModel()
        viewModel.state.test {
            val loaded = awaitItemUntil { state ->
                state.visibleCategories.map { it.filter } == listOf(DrinkFilter.COCKTAIL)
            }
            assertEquals("Negroni", loaded.visibleCategories.first().drinks.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun save_addsRecentlyViewedRowFirst() = runTest {
        database.drinkDao().upsertRecipe(
            cocktailDrink("1", "Negroni").toDrink()!!.copy(category = "Cocktail").toEntity(),
        )
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
            DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), context),
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
