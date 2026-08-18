package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.search.SearchMode

data class MainUiState(
    val menuExpanded: Boolean = false,
    val selectedDrink: Drink? = null,
    val destination: DrawerDestination = DrawerDestination.Hot,
)

sealed interface MainIntent {
    data class SelectDestination(val destination: DrawerDestination) : MainIntent
    data object ToggleSearch : MainIntent
    data class OpenSearch(
        val query: String = "",
        val mode: SearchMode = SearchMode.NAME,
        val filterKind: FilterKind? = null,
    ) : MainIntent
    data object OpenMenu : MainIntent
    data object DismissMenu : MainIntent
    data class DrinkSelected(val drink: Drink, val twoPane: Boolean) : MainIntent
}

sealed interface MainEffect {
    data class Navigate(val destination: DrawerDestination) : MainEffect
    data class OpenSearch(
        val query: String,
        val mode: SearchMode = SearchMode.NAME,
        val filterKind: FilterKind? = null,
    ) : MainEffect
    data class OpenDetails(val drink: Drink) : MainEffect
}
