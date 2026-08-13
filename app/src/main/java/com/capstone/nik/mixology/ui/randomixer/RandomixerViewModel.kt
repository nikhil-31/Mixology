package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.model.ingredientMeasures
import com.capstone.nik.mixology.ui.mvi.MviAndroidViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RandomixerViewModel(application: Application) :
    MviAndroidViewModel<RandomixerIntent, RandomixerUiState, RandomixerEffect>(
        application,
        RandomixerUiState(),
    ) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()
    private var savedJob: Job? = null

    init {
        onIntent(RandomixerIntent.Refresh)
    }

    override fun onIntent(intent: RandomixerIntent) {
        when (intent) {
            RandomixerIntent.Refresh -> refresh()
            RandomixerIntent.ToggleSaved -> toggleSaved()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val drink = repository.randomDrink()
                savedJob?.cancel()
                if (drink?.idDrink != null) {
                    savedJob = viewModelScope.launch {
                        repository.observeSavedIds().collect { ids ->
                            setState { copy(saved = drink.idDrink in ids) }
                        }
                    }
                }
                setState {
                    copy(
                        loading = false,
                        drink = drink,
                        ingredients = drink?.ingredientMeasures().orEmpty(),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load random drink", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                setState { copy(loading = false) }
                sendEffect(RandomixerEffect.ShowMessageRes(R.string.network_error))
            }
        }
    }

    private fun toggleSaved() {
        val drink = currentState.drink ?: return
        val id = drink.idDrink ?: return
        viewModelScope.launch {
            if (currentState.saved) {
                repository.unsave(id)
                sendEffect(RandomixerEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(Cocktail(id, drink.strDrink, drink.strDrinkThumb))
                sendEffect(RandomixerEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    companion object {
        private const val TAG = "RandomixerViewModel"
    }
}
