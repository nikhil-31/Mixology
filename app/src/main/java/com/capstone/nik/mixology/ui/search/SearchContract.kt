package com.capstone.nik.mixology.ui.search

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.FilterKind

const val SEARCH_MIN_CHARS = 2

enum class SearchMode {
    NAME,
    INGREDIENT,
}

data class SearchUiState(
    val query: String = "",
    val mode: SearchMode = SearchMode.NAME,
    val loading: Boolean = false,
    val results: List<Drink> = emptyList(),
    val empty: Boolean = false,
)

sealed interface SearchIntent {
    data class Search(
        val query: String,
        val mode: SearchMode? = null,
        val filterKind: FilterKind? = null,
    ) : SearchIntent
    data class SetMode(val mode: SearchMode) : SearchIntent
    data class ToggleSaved(val drink: Drink) : SearchIntent
    data class OpenDrink(val drink: Drink) : SearchIntent
    data object Back : SearchIntent
}

sealed interface SearchEffect {
    data class ShowMessageRes(val resId: Int) : SearchEffect
    data class OpenDrink(val drink: Drink) : SearchEffect
    data object NavigateBack : SearchEffect
}
