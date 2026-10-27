package com.capstone.nik.mixology.ui.details

import android.content.Intent
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.DrinkRating

data class DrinkDetailsUiState(
    val loading: Boolean = false,
    val drink: Drink? = null,
    val saved: Boolean = false,
    val onShoppingList: Boolean = false,
    /** Null until loaded, or when ratings couldn't be reached. */
    val rating: DrinkRating? = null,
) {
    /** The user's own recipe: editable, but never saved or rated. */
    val isUserRecipe: Boolean get() = drink?.isUserRecipe == true
}

sealed interface DrinkDetailsIntent {
    data class Load(val drink: Drink) : DrinkDetailsIntent
    data object ToggleSaved : DrinkDetailsIntent
    data object Share : DrinkDetailsIntent
    data object Back : DrinkDetailsIntent
    data class UpdateNotes(val notes: String) : DrinkDetailsIntent
    data object AddToShoppingList : DrinkDetailsIntent
    data object RemoveFromShoppingList : DrinkDetailsIntent
    data class OpenVideo(val url: String) : DrinkDetailsIntent
    /** Tapping the star that is already the user's rating clears it. */
    data class Rate(val stars: Int) : DrinkDetailsIntent
    /** Opens the editor for the user's own recipe. */
    data object EditRecipe : DrinkDetailsIntent
}

sealed interface DrinkDetailsEffect {
    data class ShowMessageRes(val resId: Int) : DrinkDetailsEffect
    data class ShareRecipe(val intent: Intent) : DrinkDetailsEffect
    data class OpenUrl(val url: String) : DrinkDetailsEffect
    data object NavigateBack : DrinkDetailsEffect
    data class OpenRecipeEditor(val recipeId: String) : DrinkDetailsEffect
}
