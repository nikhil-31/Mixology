package com.capstone.nik.mixology.ui.hot

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter

data class HotCategory(
    val filter: DrinkFilter,
    val drinks: List<Drink>,
)

data class HotUiState(
    val loading: Boolean = true,
    val categories: List<HotCategory> = emptyList(),
) {
    val visibleCategories: List<HotCategory> get() = categories.filter { it.drinks.isNotEmpty() }
}

sealed interface HotIntent {
    data object Load : HotIntent
    data class ToggleSaved(val item: Drink) : HotIntent
    data class OpenDrink(val drink: Drink) : HotIntent
    data class SeeAll(val filter: DrinkFilter) : HotIntent
    data object BrowseCatalog : HotIntent
}

sealed interface HotEffect {
    data class ShowMessageRes(val resId: Int) : HotEffect
    data class OpenDrink(val drink: Drink) : HotEffect
    data class OpenFilter(val filter: DrinkFilter) : HotEffect
    data object OpenCatalog : HotEffect
}
