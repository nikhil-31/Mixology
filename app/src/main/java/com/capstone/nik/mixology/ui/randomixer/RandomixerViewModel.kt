package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.Network.remoteModel.Drink
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
    private var loadJob: Job? = null

    init {
        onIntent(RandomixerIntent.Refresh)
    }

    override fun onIntent(intent: RandomixerIntent) {
        when (intent) {
            RandomixerIntent.Refresh -> refresh()
            RandomixerIntent.SwipeSave -> swipeSave()
            RandomixerIntent.SwipeDiscard -> swipeDiscard()
        }
    }

    private fun swipeSave() {
        val drink = currentState.drink ?: return
        if (currentState.loading) return
        viewModelScope.launch {
            try {
                val id = drink.idDrink
                if (id != null) {
                    repository.save(Cocktail(id, drink.strDrink, drink.strDrinkThumb))
                    sendEffect(RandomixerEffect.ShowMessageRes(R.string.drink_added))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save drink", e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            refresh(avoidId = drink.idDrink)
        }
    }

    private fun swipeDiscard() {
        if (currentState.loading) return
        refresh(avoidId = currentState.drink?.idDrink)
    }

    private fun refresh(avoidId: String? = null) {
        loadJob?.cancel()
        savedJob?.cancel()
        loadJob = viewModelScope.launch {
            setState {
                copy(
                    loading = true,
                    drink = null,
                    ingredients = emptyList(),
                    saved = false,
                )
            }
            try {
                val drink = nextDrink(avoidId)
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

    private suspend fun nextDrink(avoidId: String?): Drink? {
        val first = repository.randomDrink()
        if (avoidId == null || first?.idDrink != avoidId) return first
        return repository.randomDrink() ?: first
    }

    companion object {
        private const val TAG = "RandomixerViewModel"
    }
}
