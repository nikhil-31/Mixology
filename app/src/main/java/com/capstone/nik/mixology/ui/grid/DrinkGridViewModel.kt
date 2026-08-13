package com.capstone.nik.mixology.ui.grid

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
import com.capstone.nik.mixology.ui.mvi.MviAndroidViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DrinkGridViewModel(application: Application) :
    MviAndroidViewModel<DrinkGridIntent, DrinkGridUiState, DrinkGridEffect>(
        application,
        DrinkGridUiState(),
    ) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()
    private var observeJob: Job? = null

    override fun onIntent(intent: DrinkGridIntent) {
        when (intent) {
            is DrinkGridIntent.Bind -> bind(intent.filter)
            is DrinkGridIntent.ToggleSaved -> toggleSaved(intent.item)
            is DrinkGridIntent.OpenDrink -> sendEffect(DrinkGridEffect.OpenDrink(intent.cocktail))
        }
    }

    private fun bind(filter: DrinkFilter) {
        if (currentState.filter != filter || observeJob == null) {
            setState { copy(filter = filter) }
            observeJob?.cancel()
            observeJob = viewModelScope.launch {
                repository.observeDrinks(filter).collect { items ->
                    setState { copy(drinks = items) }
                }
            }
        }
        refresh(filter)
    }

    private fun toggleSaved(item: DrinkListItem) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.id)
                sendEffect(DrinkGridEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(item.toCocktail())
                sendEffect(DrinkGridEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    private fun refresh(filter: DrinkFilter) {
        if (filter.kind == null || filter.query == null) return
        viewModelScope.launch {
            try {
                repository.fetchAndCache(filter)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh ${filter.name}", e)
                FirebaseCrashlytics.getInstance().log("Failed to refresh ${filter.name}")
                FirebaseCrashlytics.getInstance().recordException(e)
                sendEffect(
                    DrinkGridEffect.ShowMessage(
                        getApplication<Application>().getString(R.string.network_error),
                    ),
                )
            }
        }
    }

    companion object {
        private const val TAG = "DrinkGridViewModel"
    }
}
