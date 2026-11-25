package com.capstone.nik.mixology.ui.ada

import com.capstone.nik.mixology.data.Drink

data class AdaMoreUiState(
    /** The user's request the list answers. */
    val query: String = "",
    val drinks: List<Drink> = emptyList(),
    val loading: Boolean = false,
    /** More pages remain on the server. */
    val hasMore: Boolean = true,
    val listView: Boolean = false,
    val error: String? = null,
) {
    val showEmpty: Boolean get() = !loading && !hasMore && drinks.isEmpty() && error == null
}

sealed interface AdaMoreIntent {
    /** Lists drinks for [query], leaving out the [exclude] ids Ada already showed. */
    data class Bind(val query: String, val exclude: List<String>) : AdaMoreIntent
    /** The user scrolled near the end of the list. */
    data object LoadMore : AdaMoreIntent
    data object Retry : AdaMoreIntent
    data class OpenDrink(val drink: Drink) : AdaMoreIntent
    data class ToggleSaved(val drink: Drink) : AdaMoreIntent
    data object ToggleListView : AdaMoreIntent
}

sealed interface AdaMoreEffect {
    data class OpenDrink(val drink: Drink) : AdaMoreEffect
}
