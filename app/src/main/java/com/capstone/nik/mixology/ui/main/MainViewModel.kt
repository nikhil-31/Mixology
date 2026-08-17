package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : MviViewModel<MainIntent, MainUiState, MainEffect>(MainUiState()) {

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            MainIntent.OpenDrawer -> sendEffect(MainEffect.OpenDrawer)
            is MainIntent.SelectDestination -> {
                setState {
                    copy(
                        destination = intent.destination,
                        selectedDrink = if (intent.destination is DrawerDestination.Randomixer) {
                            selectedDrink
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
            is MainIntent.DrinkSelected -> {
                if (intent.twoPane) {
                    setState { copy(selectedDrink = intent.drink) }
                } else {
                    sendEffect(MainEffect.OpenDetails(intent.drink))
                }
            }
        }
    }
}
