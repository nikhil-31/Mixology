package com.capstone.nik.mixology.ui.grid

import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem

data class DrinkGridUiState(
    val filter: DrinkFilter = DrinkFilter.ALCOHOLIC,
    val drinks: List<DrinkListItem> = emptyList(),
) {
    val showEmpty: Boolean get() = filter.showEmptySaved && drinks.isEmpty()
}

sealed interface DrinkGridIntent {
    data class Bind(val filter: DrinkFilter) : DrinkGridIntent
    data class ToggleSaved(val item: DrinkListItem) : DrinkGridIntent
    data class OpenDrink(val cocktail: Cocktail) : DrinkGridIntent
}

sealed interface DrinkGridEffect {
    data class ShowMessage(val text: String) : DrinkGridEffect
    data class ShowMessageRes(val resId: Int) : DrinkGridEffect
    data class OpenDrink(val cocktail: Cocktail) : DrinkGridEffect
}
