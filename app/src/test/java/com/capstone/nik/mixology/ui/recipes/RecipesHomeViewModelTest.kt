package com.capstone.nik.mixology.ui.recipes

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.BarSync
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.NetworkRecipeRepository
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class RecipesHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private lateinit var database: MixologyDatabase
    private val cocktails = FakeCocktailService()
    private lateinit var recipes: NetworkRecipeRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        val sessions = SessionStore(context)
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        val subscriptions = SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions)
        recipes = NetworkRecipeRepository(FakeRecipeService(), subscriptions, sessions)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun drinkRepository(barSync: BarSync = BarSync.None) = DrinkRepository(
        database.drinkDao(),
        database.shoppingDao(),
        database.barDao(),
        cocktails,
        context,
        barSync = barSync,
    )

    @Test
    fun importRecipe_opensImport() = runTest {
        val viewModel = RecipesHomeViewModel(recipes, drinkRepository(), NetworkMonitor.forTests(online = true), context)

        viewModel.effects.test {
            viewModel.onIntent(RecipesHomeIntent.ImportRecipe)
            assertEquals(RecipesHomeEffect.OpenImport, awaitItem())
        }
    }

    @Test
    fun barRecommendations_reachTheState() = runTest {
        cocktails.recommendationDrinks = listOf(negroni(), martini())
        val drinks = drinkRepository()
        val viewModel = RecipesHomeViewModel(recipes, drinks, NetworkMonitor.forTests(online = true), context)

        viewModel.state.test {
            drinks.addToBar("Gin")
            drinks.addToBar("Campari")
            drinks.addToBar("Sweet Vermouth")
            val loaded = awaitItemUntil { it.bar.makeable.isNotEmpty() }
            assertEquals(listOf("Negroni"), loaded.bar.makeable.map { it.name })
            assertEquals(listOf("Martini"), loaded.bar.almost.map { it.drink.name })
            assertEquals(listOf("Dry Vermouth"), loaded.bar.almost.single().missing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun barSyncFailure_leavesTheScreenAlone() = runTest {
        var pulls = 0
        val failing = object : BarSync {
            override fun push(name: String, owned: Boolean) = Unit
            override suspend fun pull() {
                pulls++
                error("bar sync failed")
            }
        }
        val viewModel = RecipesHomeViewModel(
            recipes,
            drinkRepository(failing),
            NetworkMonitor.forTests(online = true),
            context,
        )

        viewModel.state.test {
            val loaded = awaitItemUntil { it.loaded && !it.loading }
            assertNull(loaded.error)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, pulls)
    }
}

private fun negroni() = Drink(
    id = "11003",
    name = "Negroni",
    thumb = "https://example.com/n.jpg",
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
