package com.capstone.nik.mixology.ui.shopping

import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.data.withRecipes
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val recipes: RecipeRepository = RecipeRepository.None,
    private val preferences: ShoppingPreferences = ShoppingPreferences.inMemory(),
) : MviViewModel<ShoppingIntent, ShoppingUiState, ShoppingEffect>(
    ShoppingUiState(byIngredient = preferences.byIngredient),
) {

    /** What the last remove or clear took off, for Undo. */
    private var lastRemoved: List<ShoppingItemEntity> = emptyList()

    /** My recipes are fetched at most once here when the list needs one that isn't loaded yet. */
    private var requestedRecipes = false

    init {
        viewModelScope.launch { repository.syncShopping() }
        viewModelScope.launch {
            combine(repository.observeShoppingSections(), recipes.recipes) { sections, mine ->
                loadRecipesIfMissing(sections, mine)
                withRecipes(sections, mine)
            }.collect { sections ->
                setState { copy(sections = sections) }
            }
        }
    }

    override fun onIntent(intent: ShoppingIntent) {
        when (intent) {
            is ShoppingIntent.Toggle -> viewModelScope.launch {
                repository.setShoppingChecked(intent.item.id, !intent.item.checked)
            }
            is ShoppingIntent.ToggleCombined -> viewModelScope.launch {
                val checked = !intent.row.checked
                intent.row.items.forEach { repository.setShoppingChecked(it.id, checked) }
            }
            is ShoppingIntent.Remove -> removeItems(listOf(intent.item), R.plurals.shopping_removed_items)
            is ShoppingIntent.RemoveCombined -> removeItems(intent.row.items, R.plurals.shopping_removed_items)
            is ShoppingIntent.RemoveDrink -> {
                val items = currentState.items.filter { it.drinkId == intent.drinkId }
                if (items.isEmpty()) return
                remember(items, R.plurals.shopping_removed_items)
                viewModelScope.launch { repository.removeFromShoppingList(intent.drinkId) }
            }
            ShoppingIntent.ClearChecked -> {
                val items = currentState.items.filter { it.checked }
                if (items.isEmpty()) return
                remember(items, R.plurals.shopping_cleared)
                viewModelScope.launch { repository.clearCheckedShoppingItems() }
            }
            ShoppingIntent.Undo -> {
                val items = lastRemoved
                lastRemoved = emptyList()
                if (items.isNotEmpty()) viewModelScope.launch { repository.restoreShoppingItems(items) }
            }
            is ShoppingIntent.SetByIngredient -> {
                preferences.byIngredient = intent.byIngredient
                setState { copy(byIngredient = intent.byIngredient) }
            }
            ShoppingIntent.Refresh -> viewModelScope.launch {
                setState { copy(refreshing = true) }
                try {
                    repository.syncShopping()
                    runCatching { recipes.load() }
                } finally {
                    setState { copy(refreshing = false) }
                }
            }
        }
    }

    private fun loadRecipesIfMissing(sections: List<ShoppingSection>, mine: List<Drink>) {
        if (requestedRecipes) return
        val known = mine.mapTo(HashSet()) { it.id }
        val needed = sections.any { it.drinkId.startsWith(Drink.USER_RECIPE_PREFIX) && it.drinkId !in known }
        if (!needed) return
        requestedRecipes = true
        viewModelScope.launch { runCatching { recipes.load() } }
    }

    private fun removeItems(items: List<ShoppingItemEntity>, messageRes: Int) {
        if (items.isEmpty()) return
        remember(items, messageRes)
        viewModelScope.launch { items.forEach { repository.removeShoppingItem(it.id) } }
    }

    private fun remember(items: List<ShoppingItemEntity>, messageRes: Int) {
        lastRemoved = items
        sendEffect(ShoppingEffect.ShowUndo(messageRes, items.size))
    }
}
