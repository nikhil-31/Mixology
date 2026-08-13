package com.capstone.nik.mixology.ui.details

import android.content.Intent
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure

data class DrinkDetailsUiState(
    val loading: Boolean = false,
    val cocktail: Cocktail? = null,
    val drink: Drink? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
    val saved: Boolean = false,
)

sealed interface DrinkDetailsIntent {
    data class Load(val cocktail: Cocktail) : DrinkDetailsIntent
    data object ToggleSaved : DrinkDetailsIntent
    data object Share : DrinkDetailsIntent
    data object Back : DrinkDetailsIntent
}

sealed interface DrinkDetailsEffect {
    data class ShowMessageRes(val resId: Int) : DrinkDetailsEffect
    data class ShareRecipe(val intent: Intent) : DrinkDetailsEffect
    data object NavigateBack : DrinkDetailsEffect
}
