package com.capstone.nik.mixology.ui.details

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
import kotlinx.coroutines.flow.first
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

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DrinkDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var service: FakeCocktailService
    private lateinit var repository: DrinkRepository
    private lateinit var viewModel: DrinkDetailsViewModel

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
            service,
            context,
        )
        viewModel = DrinkDetailsViewModel(repository, NetworkMonitor.forTests(), context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_usesLookupRecipe() = runTest {
        service.lookup = CocktailDbResponse(drinks = listOf(cocktailDrink("11007", "Margarita")))
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11007", "Margarita", "")))
        viewModel.state.test {
            val loaded = awaitItemUntil { it.drink?.hasRecipe == true }
            assertEquals("Shake.", loaded.drink?.instructions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addToShoppingList_insertsIngredients() = runTest {
        service.lookup = CocktailDbResponse(drinks = listOf(cocktailDrink("11007", "Margarita")))
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11007", "Margarita", "")))
        viewModel.state.test {
            awaitItemUntil { it.drink?.ingredients?.isNotEmpty() == true }
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.effects.test {
            viewModel.onIntent(DrinkDetailsIntent.AddToShoppingList)
            assertEquals(R.string.shopping_added, (awaitItem() as DrinkDetailsEffect.ShowMessageRes).resId)
        }
        assertEquals(listOf("Gin"), repository.observeShopping().first().map { it.name })
    }

    @Test
    fun openVideo_emitsUrl() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(DrinkDetailsIntent.OpenVideo("https://example.com/v"))
            assertEquals("https://example.com/v", (awaitItem() as DrinkDetailsEffect.OpenUrl).url)
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
