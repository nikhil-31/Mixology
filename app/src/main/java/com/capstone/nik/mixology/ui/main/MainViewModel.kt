package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.ui.mvi.MviViewModel

class MainViewModel : MviViewModel<MainIntent, MainUiState, MainEffect>(MainUiState()) {

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            MainIntent.OpenDrawer -> sendEffect(MainEffect.OpenDrawer)
            is MainIntent.SelectDestination -> {
                setState {
                    copy(
                        destination = intent.destination,
                        selectedCocktail = if (intent.destination is DrawerDestination.Randomixer) {
                            selectedCocktail
                        } else {
                            null
                        },
                    )
                }
                sendEffect(MainEffect.Navigate(intent.destination))
                sendEffect(MainEffect.CloseDrawer)
            }
            MainIntent.ToggleSearch -> sendEffect(MainEffect.OpenSearch(""))
            MainIntent.OpenMenu -> setState { copy(menuExpanded = true) }
            MainIntent.DismissMenu -> setState { copy(menuExpanded = false) }
            MainIntent.SignOut -> {
                setState { copy(menuExpanded = false) }
                sendEffect(MainEffect.SignOut)
            }
            is MainIntent.DrinkSelected -> {
                if (intent.twoPane) {
                    setState { copy(selectedCocktail = intent.cocktail) }
                } else {
                    sendEffect(MainEffect.OpenDetails(intent.cocktail))
                }
            }
        }
    }
}
