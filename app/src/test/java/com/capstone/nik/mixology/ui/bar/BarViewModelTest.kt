package com.capstone.nik.mixology.ui.bar

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_REMOVE_FROM_WISHLIST
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.runBlocking
import com.google.firebase.analytics.FirebaseAnalytics
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
class BarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var analytics: AnalyticsTracker
    private lateinit var viewModel: BarViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        runBlocking {
            database.drinkDao().upsertRecipe(
                Drink(
                    id = "seed-skip",
                    name = "ZZZ Unmatchable",
                    thumb = "https://example.com/s.jpg",
                    ingredients = listOf(IngredientMeasure("SeedSkipIngredient", "1 oz")),
                ).toEntity(),
            )
            database.drinkDao().replaceCatalog(
                FilterKind.INGREDIENT.name,
                listOf("Gin", "Campari", "Sweet Vermouth"),
            )
        }
        analytics = AnalyticsTracker.forTests()
        viewModel = BarViewModel(
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                context,
            ),
            NetworkMonitor.forTests(),
            analytics,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_emptyBarHasNoRecommendations() = runTest {
        viewModel.state.test {
            val loaded = awaitItemUntil { !it.loading }
            assertTrue(loaded.bar.isEmpty())
            assertTrue(loaded.makeable.isEmpty())
            assertTrue(loaded.almost.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleIngredient_addsAndRemovesBarItem() = runTest {
        viewModel.state.test {
            awaitItemUntil { it.catalogTerms.contains("Gin") }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            awaitItemUntil { it.bar == listOf("Gin") }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            awaitItemUntil { it.bar.isEmpty() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun togglingIngredients_splitsMakeableAndAlmost() = runTest {
        database.drinkDao().upsertRecipe(negroni().toEntity())
        database.drinkDao().upsertRecipe(martini().toEntity())
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            viewModel.onIntent(BarIntent.ToggleIngredient("Campari"))
            viewModel.onIntent(BarIntent.ToggleIngredient("Sweet Vermouth"))
            val loaded = awaitItemUntil { it.makeable.map { drink -> drink.name } == listOf("Negroni") }
            assertEquals(listOf("Martini"), loaded.almost.map { it.drink.name })
            assertEquals(listOf("Dry Vermouth"), loaded.almost.single().missing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleSaved_logsWishlistEvents() = runTest {
        viewModel.onIntent(BarIntent.ToggleSaved(negroni()))
        viewModel.onIntent(BarIntent.ToggleSaved(negroni().copy(saved = true)))
        assertEquals(
            listOf(FirebaseAnalytics.Event.ADD_TO_WISHLIST, EVENT_REMOVE_FROM_WISHLIST),
            analytics.recorded.map { it.name },
        )
        assertEquals("11003", analytics.recorded[0].params[FirebaseAnalytics.Param.ITEM_ID])
    }
}

private fun negroni() = Drink(
    id = "11003",
    name = "Negroni",
    thumb = "https://example.com/n.jpg",
    instructions = "Stir.",
    ingredients = listOf(
        IngredientMeasure("Gin", "1 oz"),
        IngredientMeasure("Campari", "1 oz"),
        IngredientMeasure("Sweet Vermouth", "1 oz"),
    ),
)

private fun martini() = Drink(
    id = "11728",
    name = "Martini",
    thumb = "https://example.com/m.jpg",
    instructions = "Stir.",
    ingredients = listOf(
        IngredientMeasure("Gin", "1 oz"),
        IngredientMeasure("Dry Vermouth", "1 oz"),
    ),
)

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
