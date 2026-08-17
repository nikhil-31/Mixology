package com.capstone.nik.mixology.ui.grid

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter

data class DrinkGridUiState(
    val filter: DrinkFilter = DrinkFilter.ALCOHOLIC,
    val drinks: List<Drink> = emptyList(),
) {
    val showEmpty: Boolean get() = filter.showEmptySaved && drinks.isEmpty()
}

sealed interface DrinkGridIntent {
    data class Bind(val filter: DrinkFilter) : DrinkGridIntent
    data class ToggleSaved(val item: Drink) : DrinkGridIntent
    data class OpenDrink(val drink: Drink) : DrinkGridIntent
}

sealed interface DrinkGridEffect {
    data class ShowMessage(val text: String) : DrinkGridEffect
    data class ShowMessageRes(val resId: Int) : DrinkGridEffect
    data class OpenDrink(val drink: Drink) : DrinkGridEffect
}
