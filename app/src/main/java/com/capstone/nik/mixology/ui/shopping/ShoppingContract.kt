package com.capstone.nik.mixology.ui.shopping

import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection

data class ShoppingUiState(
    val sections: List<ShoppingSection> = emptyList(),
    /** Show one row per ingredient, merged across drinks, instead of grouping by drink. */
    val byIngredient: Boolean = false,
    val refreshing: Boolean = false,
    /** Normalised names of the ingredients in My Bar. */
    val bar: Set<String> = emptySet(),
    /** The Done shopping review sheet, while it's open. */
    val review: ReviewUi? = null,
) {
    val items: List<ShoppingItemEntity> get() = sections.flatMap { it.items }
    val total: Int get() = items.size
    val checkedCount: Int get() = items.count { it.checked }
    val combined: List<CombinedShoppingItem> get() = combineByIngredient(sections)
}

data class ReviewUi(
    val rows: List<ReviewRow>,
    /** Keys of the rows to add to My Bar. */
    val selected: Set<String>,
) {
    val toAdd: List<ReviewRow> get() = rows.filter { !it.inBar && it.key in selected }
    val canConfirm: Boolean get() = toAdd.isNotEmpty()
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
    /** Opens the review of what was checked, to move it into My Bar. */
    data object OpenReview : ShoppingIntent
    data object DismissReview : ShoppingIntent
    data class ToggleReviewRow(val key: String) : ShoppingIntent
    /** Records the trip, adds the selected ingredients to My Bar and clears the checked items. */
    data object ConfirmReview : ShoppingIntent
    /** Not now: records the trip and clears the checked items, adding nothing to My Bar. */
    data object SkipReview : ShoppingIntent
}

sealed interface ShoppingEffect {
    /** [messageRes] is a plurals resource taking [count], or a plain string when [count] is null. */
    data class ShowUndo(val messageRes: Int, val count: Int?) : ShoppingEffect
}
