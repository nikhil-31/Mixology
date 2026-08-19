package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.cocktailDrink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.repository.DrinkRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
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
    private lateinit var service: FakeCocktailService
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
        service = FakeCocktailService().apply { failRandom = true }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun load_usesLocalRecipesWithoutHittingEndpoint() = runTest {
        database.drinkDao().upsertRecipe(cocktailDrink("1", "Gin Fizz").toDrink()!!.toEntity())
        database.drinkDao().upsertRecipe(cocktailDrink("2", "Negroni").toDrink()!!.toEntity())
        viewModel = createViewModel()
        viewModel.state.test {
            val loaded = awaitItemUntil { it.drink != null }
            assertTrue(loaded.drink!!.id in setOf("1", "2"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(): RandomixerViewModel {
        val context = ApplicationProvider.getApplicationContext<Application>()
        return RandomixerViewModel(
            DrinkRepository(
                database.drinkDao(),
                database.shoppingDao(),
                database.barDao(),
                service,
                context,
            ),
            context,
        )
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
