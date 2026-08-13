package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.data.DrinkFilter

data class MainUiState(
    val searchOpen: Boolean = false,
    val searchQuery: String = "",
    val menuExpanded: Boolean = false,
    val selectedCocktail: Cocktail? = null,
    val destination: DrawerDestination = DrawerDestination.Filter(DrinkFilter.ALCOHOLIC),
)

sealed interface MainIntent {
    data object OpenDrawer : MainIntent
    data class SelectDestination(val destination: DrawerDestination) : MainIntent
    data object ToggleSearch : MainIntent
    data class SearchQueryChanged(val query: String) : MainIntent
    data object SubmitSearch : MainIntent
    data object OpenMenu : MainIntent
    data object DismissMenu : MainIntent
    data object SignOut : MainIntent
    data class DrinkSelected(val cocktail: Cocktail, val twoPane: Boolean) : MainIntent
}

sealed interface MainEffect {
    data class Navigate(val destination: DrawerDestination) : MainEffect
    data object OpenDrawer : MainEffect
    data object CloseDrawer : MainEffect
    data class OpenSearch(val query: String) : MainEffect
    data class OpenDetails(val cocktail: Cocktail) : MainEffect
    data object SignOut : MainEffect
}
