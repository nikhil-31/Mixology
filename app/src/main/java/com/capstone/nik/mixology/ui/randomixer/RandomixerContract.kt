package com.capstone.nik.mixology.ui.randomixer

import com.capstone.nik.mixology.data.Drink

data class RandomixerUiState(
    val loading: Boolean = false,
    val drink: Drink? = null,
    val saved: Boolean = false,
    val hideSaved: Boolean = false,
)

sealed interface RandomixerIntent {
    data object Refresh : RandomixerIntent
    data object SwipeSave : RandomixerIntent
    data object SwipeDiscard : RandomixerIntent
    data object ToggleHideSaved : RandomixerIntent
    data object Undo : RandomixerIntent
}

sealed interface RandomixerEffect {
    data class ShowMessageRes(val resId: Int) : RandomixerEffect
    data class ShowUndo(val resId: Int) : RandomixerEffect
}
