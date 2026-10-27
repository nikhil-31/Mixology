package com.capstone.nik.mixology.ui.details

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.DrinkRating
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.NetworkRecipeRepository
import com.capstone.nik.mixology.repository.RatingRepository
import com.capstone.nik.mixology.repository.RecipeDraft
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Fails the test if details ever asks for a rating. */
private class CountingRatings : RatingRepository {
    val calls = mutableListOf<String>()
    override suspend fun rating(drinkId: String): DrinkRating {
        calls += drinkId
        return DrinkRating(null, 0, null)
    }
    override suspend fun rate(drinkId: String, stars: Int?): DrinkRating {
        calls += drinkId
        return DrinkRating(null, 0, stars)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DrinkDetailsRecipeTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private val catalog = FakeCocktailService()
    private val recipeService = FakeRecipeService()
    private val ratings = CountingRatings()
    private lateinit var drinks: DrinkRepository
    private lateinit var recipes: NetworkRecipeRepository
    private lateinit var viewModel: DrinkDetailsViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        drinks = DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), catalog, context)
        val sessions = SessionStore(context)
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        recipes = NetworkRecipeRepository(
            recipeService,
            SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions),
            sessions,
        )
        viewModel = DrinkDetailsViewModel(
            drinks,
            NetworkMonitor.forTests(),
            context,
            AnalyticsTracker.forTests(),
            ratings,
            recipes,
        )
        recipeService.recipes += ApiDrink(
            id = "u-7",
            name = "House Sour",
            thumb = "recipe-photos/u-7.jpg",
            instructions = "Shake.",
            ingredients = listOf(IngredientMeasure("Whiskey", "2 oz")),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun aRecipeLoadsFromMyRecipesWithoutTheCatalogRatingsOrHistory() = runTest {
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("u-7", "House Sour", "")))

        val state = viewModel.state.value
        assertEquals("Shake.", state.drink?.instructions)
        assertEquals("recipe-photos/u-7.jpg", state.drink?.thumb)
        assertTrue(state.isUserRecipe)
        assertFalse(state.loading)
        assertNull(state.rating)
        assertEquals(listOf("get u-7"), recipeService.calls)
        assertTrue(catalog.lookedUp.isEmpty())
        assertTrue(ratings.calls.isEmpty())
        assertTrue(drinks.observeRecentlyViewed().first().isEmpty())

        // Saving and rating do nothing for a recipe.
        viewModel.onIntent(DrinkDetailsIntent.ToggleSaved)
        viewModel.onIntent(DrinkDetailsIntent.Rate(5))
        assertTrue(drinks.observeSavedIds().first().isEmpty())
        assertTrue(ratings.calls.isEmpty())
    }

    @Test
    fun editOpensTheEditorAndEditsShowUp() = runTest {
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("u-7", "House Sour", "")))
        viewModel.effects.test {
            viewModel.onIntent(DrinkDetailsIntent.EditRecipe)
            assertEquals(DrinkDetailsEffect.OpenRecipeEditor("u-7"), awaitItem())
        }

        recipes.update("u-7", RecipeDraft(name = "House Sour No. 2", instructions = "Stir."))

        assertEquals("House Sour No. 2", viewModel.state.value.drink?.name)
    }

    @Test
    fun deletingTheRecipeClosesDetails() = runTest {
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("u-7", "House Sour", "")))
        viewModel.effects.test {
            recipes.delete("u-7")
            assertEquals(DrinkDetailsEffect.NavigateBack, awaitItem())
        }
    }
}
