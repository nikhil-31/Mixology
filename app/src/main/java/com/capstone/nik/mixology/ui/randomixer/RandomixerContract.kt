package com.capstone.nik.mixology.ui.randomixer

import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure

data class RandomixerUiState(
    val loading: Boolean = false,
    val drink: Drink? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
    val saved: Boolean = false,
)

sealed interface RandomixerIntent {
    data object Refresh : RandomixerIntent
    data object ToggleSaved : RandomixerIntent
}

sealed interface RandomixerEffect {
    data class ShowMessageRes(val resId: Int) : RandomixerEffect
}
