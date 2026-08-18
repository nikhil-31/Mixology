package com.capstone.nik.mixology.ui.hot

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
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
            .build()
        val service = FakeCocktailService().apply {
            ingredient = CocktailDbResponse(drinks = listOf(cocktailDrink("1", "Negroni")))
        }
        viewModel = HotViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), service, context),
            NetworkMonitor.forTests(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_populatesIngredientRailsInOrder() = runTest {
        viewModel.state.test {
            val loaded = awaitItemUntil { state ->
                state.visibleCategories.map { it.filter } == DrinkFilter.hotFilters
            }
            assertEquals(
                listOf("VODKA", "GIN", "RUM", "TEQUILA", "WHISKEY", "VERMOUTH", "COFFEE_LIQUEUR", "BITTERS", "APEROL"),
                loaded.visibleCategories.map { it.filter.name },
            )
            assertEquals("Negroni", loaded.visibleCategories.first().drinks.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
