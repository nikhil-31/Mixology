package com.capstone.nik.mixology.ui.bar

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
) : MviViewModel<BarIntent, BarUiState, BarEffect>(BarUiState()) {

    private var observeJob: Job? = null

    init {
        onIntent(BarIntent.Load)
        viewModelScope.launch {
            networkMonitor.retries.collect { refreshCatalog() }
        }
    }

    override fun onIntent(intent: BarIntent) {
        when (intent) {
            BarIntent.Load -> load()
            BarIntent.OpenPicker -> setState {
                copy(picking = true, query = "", visibleTerms = catalogTerms)
            }
            BarIntent.ClosePicker -> setState {
                copy(picking = false, query = "", visibleTerms = catalogTerms)
            }
            is BarIntent.QueryChanged -> setState {
                copy(query = intent.query, visibleTerms = filterBarTerms(catalogTerms, intent.query))
            }
            is BarIntent.ToggleIngredient -> toggleIngredient(intent.name)
            is BarIntent.ToggleSaved -> toggleSaved(intent.drink)
            is BarIntent.OpenDrink -> sendEffect(BarEffect.OpenDrink(intent.drink))
        }
    }

    private fun load() {
        if (observeJob == null) {
            observeJob = viewModelScope.launch {
                combine(
                    repository.observeBar(),
                    repository.observeCatalog(FilterKind.INGREDIENT),
                    repository.observeBarRecommendations(),
                ) { bar, terms, recommendations ->
                    Triple(bar, terms, recommendations)
                }.collect { (bar, terms, recommendations) ->
                    setState {
                        copy(
                            loading = false,
                            bar = bar,
                            catalogTerms = terms,
                            visibleTerms = filterBarTerms(terms, query),
                            makeable = recommendations.makeable,
                            almost = recommendations.almost,
                        )
                    }
                }
            }
        }
        refreshCatalog()
    }

    private fun refreshCatalog() {
        viewModelScope.launch {
            try {
                repository.refreshCatalogs()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh catalogs", e)
                recordCrash(e)
            }
        }
    }

    private fun toggleIngredient(name: String) {
        viewModelScope.launch {
            val stored = currentState.bar.firstOrNull { it.equals(name, ignoreCase = true) }
            if (stored != null) {
                repository.removeFromBar(stored)
            } else {
                repository.addToBar(name)
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
        private const val TAG = "BarViewModel"
    }
}
