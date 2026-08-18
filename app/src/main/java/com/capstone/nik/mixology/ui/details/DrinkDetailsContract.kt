package com.capstone.nik.mixology.ui.details

import android.content.Intent
import com.capstone.nik.mixology.data.Drink

data class DrinkDetailsUiState(
    val loading: Boolean = false,
    val drink: Drink? = null,
    val saved: Boolean = false,
)

sealed interface DrinkDetailsIntent {
    data class Load(val drink: Drink) : DrinkDetailsIntent
    data object ToggleSaved : DrinkDetailsIntent
    data object Share : DrinkDetailsIntent
    data object Back : DrinkDetailsIntent
    data class UpdateNotes(val notes: String) : DrinkDetailsIntent
    data object AddToShoppingList : DrinkDetailsIntent
    data class OpenVideo(val url: String) : DrinkDetailsIntent
}

sealed interface DrinkDetailsEffect {
    data class ShowMessageRes(val resId: Int) : DrinkDetailsEffect
    data class ShareRecipe(val intent: Intent) : DrinkDetailsEffect
    data class OpenUrl(val url: String) : DrinkDetailsEffect
    data object NavigateBack : DrinkDetailsEffect
}
