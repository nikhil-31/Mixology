package com.capstone.nik.mixology.ui.bar

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.EVENT_REMOVE_FROM_WISHLIST
import com.capstone.nik.mixology.catalog
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import android.net.Uri
import com.capstone.nik.mixology.Network.remoteModel.ApiIngredient
import com.capstone.nik.mixology.repository.BarCategory
import com.capstone.nik.mixology.repository.BarSync
import com.capstone.nik.mixology.repository.BarTerm
import com.capstone.nik.mixology.repository.BarUnlock
import com.capstone.nik.mixology.ui.ada.AdaInbox
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.model.IngredientMeasure
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
    private val service = FakeCocktailService()
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
        service.ingredients = catalog("Gin", "Campari", "Sweet Vermouth")
        analytics = AnalyticsTracker.forTests()
        viewModel = BarViewModel(
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                service,
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
            awaitItemUntil { state -> state.catalogTerms.any { it.name == "Gin" } }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            awaitItemUntil { it.bar == listOf("Gin") }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            awaitItemUntil { it.bar.isEmpty() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun togglingIngredients_splitsMakeableAndAlmost() = runTest {
        service.recommendationDrinks = listOf(negroni(), martini())
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

    @Test
    fun syncsTheBarOnOpenAndOnRetry() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        var pulls = 0
        val sync = object : BarSync {
            override fun push(name: String, owned: Boolean) = Unit
            override suspend fun pull() {
                pulls++
            }
        }
        val monitor = NetworkMonitor.forTests()
        val synced = BarViewModel(
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                service,
                context,
                barSync = sync,
            ),
            monitor,
            analytics,
        )
        synced.state.test {
            awaitItemUntil { !it.loading }
            assertEquals(1, pulls)
            synced.onIntent(BarIntent.Retry)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, pulls)
    }

    @Test
    fun recommendationsFailure_keepsTheBarOnScreen() = runTest {
        service.failRecommendations = true
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            val loaded = awaitItemUntil { it.bar == listOf("Gin") && it.recommendationsError != null }
            assertTrue(loaded.makeable.isEmpty())
            assertTrue(loaded.almost.isEmpty())
            // "Couldn't ask" is not "nothing matches".
            assertTrue(loaded.recommendationsFailed)
            assertEquals(false, loaded.noMatches)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun retry_loadsTheDrinksAFailureMissed() = runTest {
        service.recommendationDrinks = listOf(martini())
        service.failRecommendations = true
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            awaitItemUntil { it.recommendationsFailed }
            service.failRecommendations = false
            viewModel.onIntent(BarIntent.Retry)
            val loaded = awaitItemUntil { it.almost.isNotEmpty() }
            assertEquals(null, loaded.recommendationsError)
            assertEquals(listOf(BarUnlock("Dry Vermouth", 1)), loaded.unlocks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedRefresh_keepsTheDrinksFromBefore() = runTest {
        service.recommendationDrinks = listOf(martini())
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            awaitItemUntil { it.almost.isNotEmpty() }
            service.failRecommendations = true
            viewModel.onIntent(BarIntent.ToggleIngredient("Campari"))
            val stale = awaitItemUntil { it.recommendationsError != null }
            assertEquals(listOf("Martini"), stale.almost.map { it.drink.name })
            assertEquals(false, stale.recommendationsFailed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun quickEdits_areAnsweredByOneRequest() = runTest {
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            val before = service.recommendationCalls
            viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
            viewModel.onIntent(BarIntent.ToggleIngredient("Campari"))
            viewModel.onIntent(BarIntent.ToggleIngredient("Sweet Vermouth"))
            awaitItemUntil { it.bar.size == 3 }
            testScheduler.advanceUntilIdle()
            assertEquals(1, service.recommendationCalls - before)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addingTheSameIngredientInAnotherCase_keepsOne() = runTest {
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(BarIntent.AddIngredient("Gin"))
            awaitItemUntil { it.bar == listOf("Gin") }
            viewModel.onIntent(BarIntent.AddIngredient("gin"))
            viewModel.onIntent(BarIntent.AddIngredient("Lime"))
            val loaded = awaitItemUntil { it.bar.contains("Lime") }
            assertEquals(listOf("Gin", "Lime"), loaded.bar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removingInThePicker_offersAnUndoToo() = runTest {
        viewModel.onIntent(BarIntent.ToggleIngredient("Gin"))
        viewModel.effects.test {
            viewModel.state.test {
                awaitItemUntil { it.bar == listOf("Gin") }
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.onIntent(BarIntent.ToggleIngredient("gin"))
            assertEquals(BarEffect.ShowRemoved("Gin"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theIngredientCatalog_givesThePickerAndTheBarTheirShelves() = runTest {
        service.barIngredients = listOf(
            ApiIngredient("Lime", usedIn = 40, category = "fresh"),
            ApiIngredient("Gin", usedIn = 900, category = "spirit"),
            ApiIngredient("Mystery", usedIn = 1, category = "somewhere new"),
        )
        viewModel.onIntent(BarIntent.Refresh)
        viewModel.state.test {
            val loaded = awaitItemUntil { it.catalogTerms.size == 3 }
            // Most used first; a category this app doesn't know is "other".
            assertEquals(
                listOf(
                    BarTerm("Gin", BarCategory.SPIRIT, 900),
                    BarTerm("Lime", BarCategory.FRESH, 40),
                    BarTerm("Mystery", BarCategory.OTHER, 1),
                ),
                loaded.visibleTerms,
            )
            viewModel.onIntent(BarIntent.AddIngredient("Lime"))
            viewModel.onIntent(BarIntent.AddIngredient("Gin"))
            val stocked = awaitItemUntil { it.bar.size == 2 }
            assertEquals(
                listOf(BarGroup(BarCategory.SPIRIT, listOf("Gin")), BarGroup(BarCategory.FRESH, listOf("Lime"))),
                stocked.barGroups,
            )
            viewModel.onIntent(BarIntent.PickerCategoryChanged(BarCategory.FRESH))
            assertEquals(listOf("Lime"), awaitItemUntil { it.pickerCategory != null }.visibleTerms.map { it.name })
            // The same category again shows them all; a search looks through every category.
            viewModel.onIntent(BarIntent.PickerCategoryChanged(BarCategory.FRESH))
            awaitItemUntil { it.pickerCategory == null }
            viewModel.onIntent(BarIntent.PickerCategoryChanged(BarCategory.FRESH))
            viewModel.onIntent(BarIntent.QueryChanged("g"))
            val searched = awaitItemUntil { it.query == "g" }
            assertEquals(null, searched.pickerCategory)
            assertEquals(listOf("Gin"), searched.visibleTerms.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun askAda_leavesThePromptForHerAndOpensHerTab() = runTest {
        val inbox = AdaInbox()
        val context = ApplicationProvider.getApplicationContext<Application>()
        val asking = BarViewModel(
            DrinkRepository(database.drinkDao(), database.shoppingDao(), database.barDao(), service, context),
            NetworkMonitor.forTests(),
            analytics,
            inbox,
        )
        asking.effects.test {
            asking.onIntent(BarIntent.AskAda("What can I make?"))
            assertEquals(BarEffect.OpenAda, awaitItem())
            val photo = Uri.parse("content://photos/1")
            asking.onIntent(BarIntent.SendPhotoToAda(photo, " My shelf "))
            assertEquals(BarEffect.OpenAda, awaitItem())
            assertEquals(AdaInbox.Photo(photo, "My shelf"), inbox.takePhoto())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("What can I make?", inbox.take())
    }

    @Test
    fun chooseAdd_opensTheSheet_andAddingToTheBarClosesIt() = runTest {
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            val choice = BarAddChoice("Dry Vermouth", drink = martini())
            viewModel.onIntent(BarIntent.ChooseAdd(choice))
            awaitItemUntil { it.addChoice == choice }
            viewModel.onIntent(BarIntent.AddIngredient("Dry Vermouth"))
            awaitItemUntil { it.addChoice == null && it.bar == listOf("Dry Vermouth") }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dismissAddChoice_closesTheSheetWithoutAdding() = runTest {
        viewModel.state.test {
            awaitItemUntil { !it.loading }
            viewModel.onIntent(BarIntent.ChooseAdd(BarAddChoice("Lime")))
            awaitItemUntil { it.addChoice != null }
            viewModel.onIntent(BarIntent.DismissAddChoice)
            val closed = awaitItemUntil { it.addChoice == null }
            assertTrue(closed.bar.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(database.shoppingDao().getAll().isEmpty())
    }

    @Test
    fun addToShopping_filesTheIngredientUnderTheDrinkOnce() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(BarIntent.ChooseAdd(BarAddChoice("Dry Vermouth", drink = martini())))
            viewModel.onIntent(BarIntent.AddToShopping("Dry Vermouth", "11728"))
            assertEquals(BarEffect.ShowAddedToShopping("Dry Vermouth"), awaitItem())
            viewModel.onIntent(BarIntent.AddToShopping("dry vermouth", "11728"))
            assertEquals(BarEffect.ShowAlreadyOnShopping("dry vermouth"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(null, viewModel.state.value.addChoice)
        val items = database.shoppingDao().getAll()
        assertEquals(listOf("Dry Vermouth" to "11728"), items.map { it.name to it.drinkId })
        assertTrue(viewModel.state.value.bar.isEmpty())
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
