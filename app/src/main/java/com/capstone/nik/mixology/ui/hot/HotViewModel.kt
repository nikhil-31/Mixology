package com.capstone.nik.mixology.ui.hot

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HotViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
) : MviViewModel<HotIntent, HotUiState, HotEffect>(HotUiState()) {

    private val hotFilters = DrinkFilter.hotFilters
    private var observeJob: Job? = null

    init {
        onIntent(HotIntent.Load)
        viewModelScope.launch {
            networkMonitor.retries.collect { refreshRemote() }
        }
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
        if (observeJob == null) {
            observeJob = viewModelScope.launch {
                val categoryFlows = hotFilters.map { filter ->
                    repository.observeDrinks(filter).map { drinks -> HotCategory(filter, drinks) }
                }
                val categoriesFlow = combine(categoryFlows) { rows -> rows.toList() }
                combine(
                    repository.observeRecentlyViewed(),
                    categoriesFlow,
                ) { recent, categories ->
                    buildList {
                        if (recent.isNotEmpty()) {
                            add(HotCategory(DrinkFilter.RECENTLY_VIEWED, recent))
                        }
                        addAll(categories)
                    }
                }.collect { categories ->
                    setState { copy(loading = false, categories = categories) }
                }
            }
        }
        refreshRemote()
    }

    private fun refreshRemote() {
        hotFilters.forEach { filter ->
            viewModelScope.launch {
                try {
                    repository.fetchAndCache(filter)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh ${filter.name}", e)
                    recordCrash(e)
                }
            }
        }
        viewModelScope.launch {
            try {
                repository.refreshCatalogs()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh catalogs", e)
                recordCrash(e)
            }
        }
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

    companion object {
        private const val TAG = "HotViewModel"
    }
}
