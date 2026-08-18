package com.capstone.nik.mixology.ui.grid

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrinkGridViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
) : MviViewModel<DrinkGridIntent, DrinkGridUiState, DrinkGridEffect>(DrinkGridUiState()) {

    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            networkMonitor.retries.collect {
                refresh(currentState.filter)
            }
        }
    }

    override fun onIntent(intent: DrinkGridIntent) {
        when (intent) {
            is DrinkGridIntent.Bind -> bind(intent.filter)
            is DrinkGridIntent.ToggleSaved -> toggleSaved(intent.item)
            is DrinkGridIntent.OpenDrink -> sendEffect(DrinkGridEffect.OpenDrink(intent.drink))
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

    private fun toggleSaved(item: Drink) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.id)
                sendEffect(DrinkGridEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(item)
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
                recordCrash(e, "Failed to refresh ${filter.name}")
                sendEffect(DrinkGridEffect.ShowMessageRes(R.string.network_error))
            }
        }
    }

    companion object {
        private const val TAG = "DrinkGridViewModel"
    }
}
