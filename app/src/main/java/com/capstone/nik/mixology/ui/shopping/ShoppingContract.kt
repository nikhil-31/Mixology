package com.capstone.nik.mixology.ui.shopping

import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection

data class ShoppingUiState(
    val sections: List<ShoppingSection> = emptyList(),
    /** Show one row per ingredient, merged across drinks, instead of grouping by drink. */
    val byIngredient: Boolean = false,
    val refreshing: Boolean = false,
) {
    val items: List<ShoppingItemEntity> get() = sections.flatMap { it.items }
    val total: Int get() = items.size
    val checkedCount: Int get() = items.count { it.checked }
    val combined: List<CombinedShoppingItem> get() = combineByIngredient(sections)
}

sealed interface ShoppingIntent {
    data class Toggle(val item: ShoppingItemEntity) : ShoppingIntent
    data class Remove(val item: ShoppingItemEntity) : ShoppingIntent
    data object ClearChecked : ShoppingIntent
    data class SetByIngredient(val byIngredient: Boolean) : ShoppingIntent
    /** Removes every item added from this drink. */
    data class RemoveDrink(val drinkId: String) : ShoppingIntent
    /** Checks (or unchecks) every item behind a merged row. */
    data class ToggleCombined(val row: CombinedShoppingItem) : ShoppingIntent
    data class RemoveCombined(val row: CombinedShoppingItem) : ShoppingIntent
    /** Puts back what the last remove or clear took off the list. */
    data object Undo : ShoppingIntent
    data object Refresh : ShoppingIntent
}

sealed interface ShoppingEffect {
    /** [messageRes] is a plurals resource taking [count]. */
    data class ShowUndo(val messageRes: Int, val count: Int) : ShoppingEffect
}
