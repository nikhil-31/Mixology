package com.capstone.nik.mixology.ui.shopping

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
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
class ShoppingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: MixologyDatabase
    private lateinit var repository: DrinkRepository
    private lateinit var viewModel: ShoppingViewModel

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
        viewModel = ShoppingViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observesItems_toggleRemoveAndClearChecked() = runTest {
        viewModel.state.test {
            awaitItemUntil { it.items.isEmpty() }
            repository.addToShoppingList(listOf("Lime", "Salt"))
            val added = awaitItemUntil { it.items.size == 2 }
            assertEquals(listOf("Lime", "Salt"), added.items.map { it.name }.sorted())

            val lime = added.items.first { it.name == "Lime" }
            viewModel.onIntent(ShoppingIntent.Toggle(lime))
            val toggled = awaitItemUntil { it.items.any { item -> item.name == "Lime" && item.checked } }
            assertTrue(toggled.items.first { it.name == "Lime" }.checked)

            viewModel.onIntent(ShoppingIntent.ClearChecked)
            val remaining = awaitItemUntil { it.items.size == 1 }
            assertEquals(listOf("Salt"), remaining.items.map { it.name })

            viewModel.onIntent(ShoppingIntent.Remove(remaining.items.single()))
            awaitItemUntil { it.items.isEmpty() }
        }
    }
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
