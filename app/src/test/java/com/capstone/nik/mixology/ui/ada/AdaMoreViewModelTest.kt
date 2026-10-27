package com.capstone.nik.mixology.ui.ada

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class AdaMoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private val chat = FakeChatRepository()
    private lateinit var viewModel: AdaMoreViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        chat.matchDrinks = (0 until 25).map { Drink(id = "gin-$it", name = "Gin Drink $it", thumb = "") }
        viewModel = AdaMoreViewModel(
            chat,
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                FakeCocktailService(),
                context,
            ),
            context,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun bind_loadsFirstPageWithoutShownDrinks() = runTest {
        viewModel.onIntent(AdaMoreIntent.Bind("drinks with gin", listOf("gin-0", "gin-1")))

        val state = viewModel.state.value
        assertEquals("drinks with gin", state.query)
        assertEquals(20, state.drinks.size)
        assertEquals("gin-2", state.drinks.first().id)
        assertTrue(state.hasMore)
        assertFalse(state.loading)
        assertEquals(listOf(Triple("drinks with gin", listOf("gin-0", "gin-1"), 0)), chat.matchRequests)
    }

    @Test
    fun loadMore_appendsNextPageUntilTheEnd() = runTest {
        viewModel.onIntent(AdaMoreIntent.Bind("drinks with gin", listOf("gin-0", "gin-1")))

        viewModel.onIntent(AdaMoreIntent.LoadMore)

        val state = viewModel.state.value
        assertEquals((2 until 25).map { "gin-$it" }, state.drinks.map { it.id })
        assertFalse(state.hasMore)
        assertEquals(20, chat.matchRequests.last().third)

        viewModel.onIntent(AdaMoreIntent.LoadMore)
        assertEquals(2, chat.matchRequests.size)
    }

    @Test
    fun failure_showsErrorAndRetryLoadsAgain() = runTest {
        chat.matchError = IllegalStateException("offline")
        viewModel.onIntent(AdaMoreIntent.Bind("drinks with gin", emptyList()))
        assertEquals("offline", viewModel.state.value.error)
        assertTrue(viewModel.state.value.drinks.isEmpty())
        assertFalse(viewModel.state.value.showEmpty)

        chat.matchError = null
        viewModel.onIntent(AdaMoreIntent.Retry)

        assertEquals(null, viewModel.state.value.error)
        assertEquals(20, viewModel.state.value.drinks.size)
    }

    @Test
    fun showMoreRequest_titlesTheSearchItPages() = runTest {
        chat.matchQuery = "drinks with gin"
        viewModel.onIntent(AdaMoreIntent.Bind("Show more", listOf("gin-0")))
        viewModel.onIntent(AdaMoreIntent.LoadMore)

        assertEquals("drinks with gin", viewModel.state.value.query)
        assertTrue(chat.matchRequests.all { it.first == "Show more" })
    }

    @Test
    fun noMatches_showsEmpty() = runTest {
        chat.matchDrinks = emptyList()
        viewModel.onIntent(AdaMoreIntent.Bind("something odd", emptyList()))

        assertTrue(viewModel.state.value.showEmpty)
    }
}
