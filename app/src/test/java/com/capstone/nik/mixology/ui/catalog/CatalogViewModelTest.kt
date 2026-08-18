package com.capstone.nik.mixology.ui.catalog

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.catalog
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
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
class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var viewModel: CatalogViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val service = FakeCocktailService().apply {
            categories = catalog("Cocktail")
            glasses = catalog("Highball glass")
            ingredients = catalog("Tequila")
            alcoholic = catalog("Alcoholic")
        }
        viewModel = CatalogViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), service, context),
            NetworkMonitor.forTests(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_populatesIngredientTerms() = runTest {
        viewModel.state.test {
            val loaded = awaitItemUntil { it.terms.contains("Tequila") }
            assertEquals(FilterKind.INGREDIENT, loaded.kind)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryChanged_filtersTermsAcrossMultipleSearches() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val service = FakeCocktailService().apply {
            categories = catalog("Cocktail")
            glasses = catalog("Highball glass")
            ingredients = catalog("Tequila", "Rum", "Vodka")
            alcoholic = catalog("Alcoholic")
        }
        viewModel = CatalogViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), service, context),
            NetworkMonitor.forTests(),
        )

        viewModel.state.test {
            awaitItemUntil { it.kind == FilterKind.INGREDIENT && it.terms.contains("Tequila") }

            viewModel.onIntent(CatalogIntent.QueryChanged("rum"))
            val rumSearch = awaitItemUntil { it.query == "rum" && it.visibleTerms == listOf("Rum") }
            assertEquals(listOf("Rum"), rumSearch.visibleTerms)

            viewModel.onIntent(CatalogIntent.QueryChanged("vod"))
            val vodkaSearch = awaitItemUntil { it.query == "vod" && it.visibleTerms == listOf("Vodka") }
            assertEquals(listOf("Vodka"), vodkaSearch.visibleTerms)

            viewModel.onIntent(CatalogIntent.QueryChanged(""))
            val cleared = awaitItemUntil { it.query.isEmpty() && it.visibleTerms.size == 3 }
            assertEquals(listOf("Rum", "Tequila", "Vodka"), cleared.visibleTerms)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectSameKind_doesNotClearQuery() = runTest {
        viewModel.state.test {
            awaitItemUntil { it.kind == FilterKind.INGREDIENT && it.terms.contains("Tequila") }

            viewModel.onIntent(CatalogIntent.QueryChanged("rum"))
            awaitItemUntil { it.query == "rum" }

            viewModel.onIntent(CatalogIntent.SelectKind(FilterKind.INGREDIENT))
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun openTerm_emitsDynamicFilter() = runTest {
        val deferred = CompletableDeferred<CatalogEffect>()
        val job = launch {
            deferred.complete(viewModel.effects.first())
        }
        yield()
        viewModel.onIntent(CatalogIntent.OpenTerm("Tequila"))
        val filter = (deferred.await() as CatalogEffect.OpenFilter).filter
        job.cancel()
        assertEquals(FilterKind.INGREDIENT, filter.kind)
        assertEquals("Tequila", filter.query)
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
