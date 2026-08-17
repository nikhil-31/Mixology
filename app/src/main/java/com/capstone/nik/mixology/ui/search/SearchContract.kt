package com.capstone.nik.mixology.ui.search

import com.capstone.nik.mixology.data.Drink

const val SEARCH_MIN_CHARS = 2

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<Drink> = emptyList(),
    val empty: Boolean = false,
)

sealed interface SearchIntent {
    data class Search(val query: String) : SearchIntent
    data class ToggleSaved(val drink: Drink) : SearchIntent
    data class OpenDrink(val drink: Drink) : SearchIntent
    data object Back : SearchIntent
}

sealed interface SearchEffect {
    data class ShowMessageRes(val resId: Int) : SearchEffect
    data class OpenDrink(val drink: Drink) : SearchEffect
    data object NavigateBack : SearchEffect
}
