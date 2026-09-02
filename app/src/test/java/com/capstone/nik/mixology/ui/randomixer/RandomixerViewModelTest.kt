package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_RANDOMIXER_SKIP
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.repository.DrinkRepository
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
class RandomixerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var viewModel: RandomixerViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.getSharedPreferences("mixology", Context.MODE_PRIVATE)
            .edit()
            .remove("randomixer_hide_saved")
            .apply()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_usesLocalRecipesWithoutHittingEndpoint() = runTest {
        seedRecipes()
        viewModel = createViewModel()
        viewModel.state.test {
            val loaded = awaitItemUntil { it.drink != null }
            assertTrue(loaded.drink!!.id in setOf("1", "2"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun swipeSave_logsWishlistEvent() = runTest {
        seedRecipes()
        val analytics = AnalyticsTracker.forTests()
        viewModel = createViewModel(analytics)
        viewModel.state.test {
            val loaded = awaitItemUntil { it.drink != null }
            val drink = loaded.drink!!
            viewModel.onIntent(RandomixerIntent.SwipeSave)
            awaitItemUntil { it.drink != null && it.drink!!.id != drink.id }
            cancelAndIgnoreRemainingEvents()
            val event = analytics.recorded.single { it.name == FirebaseAnalytics.Event.ADD_TO_WISHLIST }
            assertEquals(drink.id, event.params[FirebaseAnalytics.Param.ITEM_ID])
            assertEquals(drink.name, event.params[FirebaseAnalytics.Param.ITEM_NAME])
        }
    }

    @Test
    fun swipeDiscard_logsSkipEvent() = runTest {
        seedRecipes()
        val analytics = AnalyticsTracker.forTests()
        viewModel = createViewModel(analytics)
        viewModel.state.test {
            val loaded = awaitItemUntil { it.drink != null }
            val drink = loaded.drink!!
            viewModel.onIntent(RandomixerIntent.SwipeDiscard)
            awaitItemUntil { it.drink != null && it.drink!!.id != drink.id }
            cancelAndIgnoreRemainingEvents()
            val event = analytics.recorded.single { it.name == EVENT_RANDOMIXER_SKIP }
            assertEquals(drink.id, event.params[FirebaseAnalytics.Param.ITEM_ID])
            assertEquals(drink.name, event.params[FirebaseAnalytics.Param.ITEM_NAME])
        }
    }

    private suspend fun seedRecipes() {
        database.drinkDao().upsertRecipe(cocktailDrink("1", "Gin Fizz").toDrink()!!.toEntity())
        database.drinkDao().upsertRecipe(cocktailDrink("2", "Negroni").toDrink()!!.toEntity())
    }

    private fun createViewModel(
        analytics: AnalyticsTracker = AnalyticsTracker.forTests(),
    ): RandomixerViewModel {
        val context = ApplicationProvider.getApplicationContext<Application>()
        return RandomixerViewModel(
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                context,
            ),
            context,
            analytics,
        )
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
