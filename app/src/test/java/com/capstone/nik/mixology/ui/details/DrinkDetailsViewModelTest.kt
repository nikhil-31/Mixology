package com.capstone.nik.mixology.ui.details

import android.app.Application
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_REMOVE_FROM_WISHLIST
import com.capstone.nik.mixology.analytics.PARAM_SAVED
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.repository.DrinkRepository
import com.google.firebase.analytics.FirebaseAnalytics
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
    private lateinit var repository: DrinkRepository
    private lateinit var analytics: AnalyticsTracker
    private lateinit var viewModel: DrinkDetailsViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DrinkRepository(
            database.drinkDao(),
            database.shoppingDao(),
            database.barDao(),
            context,
        )
        analytics = AnalyticsTracker.forTests()
        viewModel = DrinkDetailsViewModel(repository, NetworkMonitor.forTests(), context, analytics)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_usesLookupRecipe() = runTest {
        seedMargarita()
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11007", "Margarita", "")))
        viewModel.state.test {
            val loaded = awaitItemUntil { it.drink?.hasRecipe == true }
            assertEquals("Shake.", loaded.drink?.instructions)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("11007"), repository.observeRecentlyViewed().first().map { it.id })
        assertEquals(FirebaseAnalytics.Event.VIEW_ITEM, analytics.recorded.single().name)
        assertEquals("11007", analytics.recorded.single().params[FirebaseAnalytics.Param.ITEM_ID])
    }

    @Test
    fun addToShoppingList_insertsIngredients() = runTest {
        seedMargarita()
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
        assertTrue(analytics.recorded.any { it.name == FirebaseAnalytics.Event.ADD_TO_CART })
    }

    @Test
    fun openVideo_emitsUrl() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(DrinkDetailsIntent.OpenVideo("https://example.com/v"))
            assertEquals("https://example.com/v", (awaitItem() as DrinkDetailsEffect.OpenUrl).url)
        }
    }

    @Test
    fun reloadSameDrink_logsViewOnce() = runTest {
        val drink = Drink("11007", "Margarita", "")
        viewModel.onIntent(DrinkDetailsIntent.Load(drink))
        viewModel.onIntent(DrinkDetailsIntent.Load(drink))
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11728", "Martini", "")))
        assertEquals(
            listOf("11007", "11728"),
            analytics.recorded.filter { it.name == FirebaseAnalytics.Event.VIEW_ITEM }
                .map { it.params[FirebaseAnalytics.Param.ITEM_ID] },
        )
    }

    @Test
    fun toggleSaved_logsWishlistEvents() = runTest {
        seedMargarita()
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11007", "Margarita", "")))
        viewModel.state.test {
            awaitItemUntil { it.drink?.hasRecipe == true }
            viewModel.onIntent(DrinkDetailsIntent.ToggleSaved)
            awaitItemUntil { it.saved }
            viewModel.onIntent(DrinkDetailsIntent.ToggleSaved)
            awaitItemUntil { !it.saved }
            cancelAndIgnoreRemainingEvents()
        }
        val wishlist = analytics.recorded.filter {
            it.name == FirebaseAnalytics.Event.ADD_TO_WISHLIST || it.name == EVENT_REMOVE_FROM_WISHLIST
        }
        assertEquals(
            listOf(FirebaseAnalytics.Event.ADD_TO_WISHLIST, EVENT_REMOVE_FROM_WISHLIST),
            wishlist.map { it.name },
        )
        assertEquals(true, wishlist[0].params[PARAM_SAVED])
        assertEquals(false, wishlist[1].params[PARAM_SAVED])
    }

    @Test
    fun share_logsShareEvent() = runTest {
        seedMargarita()
        viewModel.onIntent(DrinkDetailsIntent.Load(Drink("11007", "Margarita", "")))
        viewModel.state.test {
            awaitItemUntil { it.drink != null }
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.effects.test {
            viewModel.onIntent(DrinkDetailsIntent.Share)
            val share = awaitItem() as DrinkDetailsEffect.ShareRecipe
            assertEquals(Intent.ACTION_SEND, share.intent.action)
        }
        val event = analytics.recorded.single { it.name == FirebaseAnalytics.Event.SHARE }
        assertEquals("11007", event.params[FirebaseAnalytics.Param.ITEM_ID])
        assertEquals("drink", event.params[FirebaseAnalytics.Param.CONTENT_TYPE])
    }

    private suspend fun seedMargarita() {
        database.drinkDao().upsertRecipe(cocktailDrink("11007", "Margarita").toDrink()!!.toEntity())
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
