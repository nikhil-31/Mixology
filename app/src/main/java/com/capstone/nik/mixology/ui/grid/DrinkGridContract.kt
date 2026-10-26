package com.capstone.nik.mixology.ui.grid

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter

data class DrinkGridUiState(
    val filter: DrinkFilter = DrinkFilter.ALCOHOLIC,
    val drinks: List<Drink> = emptyList(),
    val listView: Boolean = false,
    val query: String = "",
) {
    val showEmpty: Boolean get() = filter.showEmptySaved && drinks.isEmpty()
}

internal fun filterSavedDrinks(drinks: List<Drink>, query: String): List<Drink> {
    val needle = query.trim()
    if (needle.isEmpty()) return drinks
    return drinks.filter { it.name.contains(needle, ignoreCase = true) }
}

sealed interface DrinkGridIntent {
    data class Bind(val filter: DrinkFilter) : DrinkGridIntent
    data class ToggleSaved(val item: Drink) : DrinkGridIntent
    data class OpenDrink(val drink: Drink) : DrinkGridIntent
    data object ToggleListView : DrinkGridIntent
    data class QueryChanged(val query: String) : DrinkGridIntent
}

sealed interface DrinkGridEffect {
    data class ShowMessage(val text: String) : DrinkGridEffect
    data class ShowMessageRes(val resId: Int) : DrinkGridEffect
    data class OpenDrink(val drink: Drink) : DrinkGridEffect
}
