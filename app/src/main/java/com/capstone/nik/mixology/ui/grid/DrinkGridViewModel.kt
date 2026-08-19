package com.capstone.nik.mixology.ui.grid

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.ui.components.DrinkViewPreferences
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrinkGridViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context,
) : MviViewModel<DrinkGridIntent, DrinkGridUiState, DrinkGridEffect>(DrinkGridUiState()) {

    private var observeJob: Job? = null

    init {
        setState { copy(listView = DrinkViewPreferences.listView(context)) }
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
            DrinkGridIntent.ToggleListView -> toggleListView()
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
            } else {
                repository.save(item)
            }
        }
    }

    private fun toggleListView() {
        val listView = !currentState.listView
        DrinkViewPreferences.setListView(context, listView)
        setState { copy(listView = listView) }
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
