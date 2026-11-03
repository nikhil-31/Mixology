package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.search.SearchMode

/**
 * The shell's own state. Which tab or page is showing is read from the
 * navigation back stack, so it stays right after the system Back button.
 */
data class MainUiState(
    val selectedDrink: Drink? = null,
)

sealed interface MainIntent {
    data class SelectDestination(val destination: DrawerDestination) : MainIntent
    data object ToggleSearch : MainIntent
    data class OpenSearch(
        val query: String = "",
        val mode: SearchMode = SearchMode.NAME,
        val filterKind: FilterKind? = null,
    ) : MainIntent
    data class DrinkSelected(val drink: Drink, val twoPane: Boolean) : MainIntent
    /** The two-pane details went away, e.g. the user deleted their recipe. */
    data object CloseDetails : MainIntent
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
