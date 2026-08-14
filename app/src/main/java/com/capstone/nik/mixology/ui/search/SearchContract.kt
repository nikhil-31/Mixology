package com.capstone.nik.mixology.ui.search

import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.data.DrinkListItem

const val SEARCH_MIN_CHARS = 2

data class SearchResultItem(
    val drink: Drink,
    val saved: Boolean,
) {
    fun toCocktail(): Cocktail = Cocktail(drink.idDrink, drink.strDrink, drink.strDrinkThumb)

    fun toListItem(): DrinkListItem = DrinkListItem(
        id = drink.idDrink.orEmpty(),
        name = drink.strDrink.orEmpty(),
        thumb = drink.strDrinkThumb.orEmpty(),
        saved = saved,
    )
}

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<SearchResultItem> = emptyList(),
    val empty: Boolean = false,
)

sealed interface SearchIntent {
    data class Search(val query: String) : SearchIntent
    data class ToggleSaved(val item: SearchResultItem) : SearchIntent
    data class OpenDrink(val cocktail: Cocktail) : SearchIntent
    data object Back : SearchIntent
}

sealed interface SearchEffect {
    data class ShowMessageRes(val resId: Int) : SearchEffect
    data class OpenDrink(val cocktail: Cocktail) : SearchEffect
    data object NavigateBack : SearchEffect
}
