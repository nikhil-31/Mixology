package com.capstone.nik.mixology.ui.shopping

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.capstone.nik.mixology.FakeCocktailService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.MixologyDatabase
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.ImportedRecipe
import com.capstone.nik.mixology.repository.RecipeDraft
import com.capstone.nik.mixology.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.capstone.nik.mixology.ui.model.IngredientMeasure
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
            FakeCocktailService(),
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
            repository.addToShoppingList(
                Drink(
                    id = "1",
                    name = "Margarita",
                    thumb = "",
                    ingredients = listOf(
                        IngredientMeasure("Lime", "1"),
                        IngredientMeasure("Salt", "1 pinch"),
                    ),
                ),
            )
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

    private val margarita = Drink(
        id = "1",
        name = "Margarita",
        thumb = "",
        ingredients = listOf(IngredientMeasure("Lime juice", "1 oz"), IngredientMeasure("Tequila", "2 oz")),
    )
    private val daiquiri = Drink(
        id = "2",
        name = "Daiquiri",
        thumb = "",
        ingredients = listOf(IngredientMeasure("Lime juice", "3/4 oz"), IngredientMeasure("Rum", "2 oz")),
    )

    private val mySour = Drink(
        id = "u-1",
        name = "My Sour",
        thumb = "",
        ingredients = listOf(IngredientMeasure("Lemon", "1 oz"), IngredientMeasure("Honey", "1/2 oz")),
    )

    @Test
    fun myRecipe_showsUnderItsOwnName() = runTest {
        val fake = FakeRecipes(listOf(mySour))
        val vm = ShoppingViewModel(repository, fake)
        vm.state.test {
            repository.addToShoppingList(mySour)
            val shown = awaitItemUntil { s -> s.sections.any { it.drink?.name == "My Sour" } }
            assertEquals(listOf("Lemon", "Honey"), shown.sections.single().items.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(0, fake.loads)
    }

    @Test
    fun myRecipe_notLoadedYet_isFetchedOnce() = runTest {
        val fake = FakeRecipes(initial = emptyList(), onLoad = listOf(mySour))
        val vm = ShoppingViewModel(repository, fake)
        vm.state.test {
            repository.addToShoppingList(mySour)
            awaitItemUntil { s -> s.sections.any { it.drink?.name == "My Sour" } }
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, fake.loads)
    }

    @Test
    fun removeThenUndo_putsTheItemBack_withItsCheckedState() = runTest {
        viewModel.effects.test {
            viewModel.state.test {
                repository.addToShoppingList(margarita)
                val added = awaitItemUntil { it.items.size == 2 }
                val lime = added.items.first { it.name == "Lime juice" }
                viewModel.onIntent(ShoppingIntent.Toggle(lime))
                val checked = awaitItemUntil { s -> s.items.any { it.name == "Lime juice" && it.checked } }

                viewModel.onIntent(ShoppingIntent.Remove(checked.items.first { it.name == "Lime juice" }))
                awaitItemUntil { it.items.size == 1 }
                viewModel.onIntent(ShoppingIntent.Undo)
                val restored = awaitItemUntil { it.items.size == 2 }
                assertTrue(restored.items.first { it.name == "Lime juice" }.checked)
                cancelAndIgnoreRemainingEvents()
            }
            val undo = awaitItem() as ShoppingEffect.ShowUndo
            assertEquals(1, undo.count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeDrink_onlyRemovesThatDrink() = runTest {
        viewModel.state.test {
            repository.addToShoppingList(margarita)
            repository.addToShoppingList(daiquiri)
            awaitItemUntil { it.items.size == 4 }
            viewModel.onIntent(ShoppingIntent.RemoveDrink("1"))
            val left = awaitItemUntil { it.items.size == 2 }
            assertTrue(left.items.all { it.drinkId == "2" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleCombined_checksEveryDrinksItem_andByIngredientIsRemembered() = runTest {
        viewModel.state.test {
            repository.addToShoppingList(margarita)
            repository.addToShoppingList(daiquiri)
            val both = awaitItemUntil { it.items.size == 4 }
            val lime = both.combined.first { it.name == "Lime juice" }
            assertEquals(2, lime.items.size)

            viewModel.onIntent(ShoppingIntent.ToggleCombined(lime))
            val done = awaitItemUntil { s -> s.items.filter { it.name == "Lime juice" }.all { it.checked } }
            assertEquals(2, done.checkedCount)

            viewModel.onIntent(ShoppingIntent.SetByIngredient(true))
            assertTrue(awaitItemUntil { it.byIngredient }.byIngredient)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/** Only [recipes] and [load] matter to the shopping list. */
private class FakeRecipes(initial: List<Drink>, private val onLoad: List<Drink> = initial) : RecipeRepository {
    private val flow = MutableStateFlow(initial)
    var loads = 0
    override val recipes: StateFlow<List<Drink>> = flow
    override suspend fun load(): List<Drink> {
        loads += 1
        flow.value = onLoad
        return onLoad
    }
    override suspend fun get(id: String): Drink = error("unused")
    override suspend fun create(draft: RecipeDraft): Drink = error("unused")
    override suspend fun update(id: String, draft: RecipeDraft): Drink = error("unused")
    override suspend fun delete(id: String) = error("unused")
    override suspend fun uploadPhoto(id: String, jpeg: ByteArray): Drink = error("unused")
    override suspend fun deletePhoto(id: String): Drink = error("unused")
    override suspend fun importRecipe(text: String): ImportedRecipe = error("unused")
}

private suspend fun <T> app.cash.turbine.ReceiveTurbine<T>.awaitItemUntil(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}
