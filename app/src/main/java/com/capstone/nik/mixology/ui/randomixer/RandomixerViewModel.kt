package com.capstone.nik.mixology.ui.randomixer

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomixerViewModel @Inject constructor(
    private val repository: DrinkRepository,
) : MviViewModel<RandomixerIntent, RandomixerUiState, RandomixerEffect>(RandomixerUiState()) {

    private var savedJob: Job? = null
    private var loadJob: Job? = null
    private var prefetchJob: Job? = null
    private var prefetched: Drink? = null

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
                repository.save(drink)
                sendEffect(RandomixerEffect.ShowMessageRes(R.string.drink_added))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save drink", e)
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            refresh(avoidId = drink.id)
        }
    }

    private fun swipeDiscard() {
        if (currentState.loading) return
        refresh(avoidId = currentState.drink?.id)
    }

    private fun refresh(avoidId: String? = null) {
        loadJob?.cancel()
        savedJob?.cancel()
        loadJob = viewModelScope.launch {
            setState { copy(loading = true, drink = null, saved = false) }
            try {
                val drink = nextDrink(avoidId)
                val saved = drink?.id?.let { id ->
                    repository.observeSavedIds().first().contains(id)
                } ?: false
                if (drink != null) {
                    savedJob = viewModelScope.launch {
                        repository.observeSavedIds().collect { ids ->
                            setState { copy(saved = drink.id in ids) }
                        }
                    }
                    prefetch(avoidId = drink.id)
                }
                setState {
                    copy(
                        loading = false,
                        drink = drink,
                        saved = saved,
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
        val queued = prefetched?.takeIf { it.id != avoidId }
        prefetched = null
        if (queued != null) return queued
        return fetchRandom(avoidId)
    }

    private fun prefetch(avoidId: String) {
        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            try {
                prefetched = fetchRandom(avoidId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to prefetch random drink", e)
            }
        }
    }

    private suspend fun fetchRandom(avoidId: String?): Drink? {
        val first = repository.randomDrink()
        if (avoidId == null || first?.id != avoidId) return first
        return repository.randomDrink() ?: first
    }

    companion object {
        private const val TAG = "RandomixerViewModel"
    }
}
