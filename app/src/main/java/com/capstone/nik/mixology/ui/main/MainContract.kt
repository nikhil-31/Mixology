package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink

data class MainUiState(
    val menuExpanded: Boolean = false,
    val selectedDrink: Drink? = null,
    val destination: DrawerDestination = DrawerDestination.Hot,
)

sealed interface MainIntent {
    data object OpenDrawer : MainIntent
    data class SelectDestination(val destination: DrawerDestination) : MainIntent
    data object ToggleSearch : MainIntent
    data object OpenMenu : MainIntent
    data object DismissMenu : MainIntent
    data class DrinkSelected(val drink: Drink, val twoPane: Boolean) : MainIntent
}

sealed interface MainEffect {
    data class Navigate(val destination: DrawerDestination) : MainEffect
    data object OpenDrawer : MainEffect
    data object CloseDrawer : MainEffect
    data class OpenSearch(val query: String) : MainEffect
    data class OpenDetails(val drink: Drink) : MainEffect
}
