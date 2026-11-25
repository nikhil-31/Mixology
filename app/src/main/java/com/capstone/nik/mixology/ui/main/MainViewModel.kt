package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : MviViewModel<MainIntent, MainUiState, MainEffect>(MainUiState()) {

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectDestination -> {
                setState { copy(selectedDrink = null) }
                sendEffect(MainEffect.Navigate(intent.destination))
            }
            MainIntent.ToggleSearch -> sendEffect(MainEffect.OpenSearch(""))
            is MainIntent.OpenSearch -> sendEffect(
                MainEffect.OpenSearch(intent.query, intent.mode, intent.filterKind),
            )
            is MainIntent.DrinkSelected -> {
                if (intent.twoPane) {
                    setState { copy(selectedDrink = intent.drink) }
                } else {
                    sendEffect(MainEffect.OpenDetails(intent.drink))
                }
            }
            MainIntent.CloseDetails -> setState { copy(selectedDrink = null) }
        }
    }
}
