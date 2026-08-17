package com.capstone.nik.mixology.ui.hot

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HotViewModel @Inject constructor(
    private val repository: DrinkRepository,
) : MviViewModel<HotIntent, HotUiState, HotEffect>(HotUiState()) {

    private val catalogFilters = DrinkFilter.catalogFilters

    init {
        onIntent(HotIntent.Load)
    }

    override fun onIntent(intent: HotIntent) {
        when (intent) {
            HotIntent.Load -> load()
            is HotIntent.ToggleSaved -> toggleSaved(intent.item)
            is HotIntent.OpenDrink -> sendEffect(HotEffect.OpenDrink(intent.drink))
            is HotIntent.SeeAll -> sendEffect(HotEffect.OpenFilter(intent.filter))
        }
    }

    private fun load() {
        viewModelScope.launch {
            val flows = catalogFilters.map { filter ->
                repository.observeDrinks(filter).map { drinks -> HotCategory(filter, drinks) }
            }
            combine(flows) { rows -> rows.toList() }.collect { categories ->
                setState { copy(loading = false, categories = categories) }
            }
        }
        catalogFilters.forEach { filter ->
            viewModelScope.launch {
                try {
                    repository.fetchAndCache(filter)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh ${filter.name}", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    private fun toggleSaved(item: Drink) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.id)
                sendEffect(HotEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(item)
                sendEffect(HotEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    companion object {
        private const val TAG = "HotViewModel"
    }
}
