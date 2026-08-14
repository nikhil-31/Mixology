package com.capstone.nik.mixology.ui.hot

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
import com.capstone.nik.mixology.ui.mvi.MviAndroidViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HotViewModel(application: Application) :
    MviAndroidViewModel<HotIntent, HotUiState, HotEffect>(
        application,
        HotUiState(),
    ) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()
    private val catalogFilters = DrinkFilter.catalogFilters

    init {
        onIntent(HotIntent.Load)
    }

    override fun onIntent(intent: HotIntent) {
        when (intent) {
            HotIntent.Load -> load()
            is HotIntent.ToggleSaved -> toggleSaved(intent.item)
            is HotIntent.OpenDrink -> sendEffect(HotEffect.OpenDrink(intent.cocktail))
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

    private fun toggleSaved(item: DrinkListItem) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.id)
                sendEffect(HotEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(item.toCocktail())
                sendEffect(HotEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    companion object {
        private const val TAG = "HotViewModel"
    }
}
