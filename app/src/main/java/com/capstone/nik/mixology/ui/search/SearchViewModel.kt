package com.capstone.nik.mixology.ui.search

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: DrinkRepository,
) : MviViewModel<SearchIntent, SearchUiState, SearchEffect>(SearchUiState()) {

    private val drinks = MutableStateFlow<List<Drink>>(emptyList())
    private val loading = MutableStateFlow(false)
    private val queried = MutableStateFlow(false)
    private val query = MutableStateFlow("")
    private val mode = MutableStateFlow(SearchMode.NAME)
    private var catalogKind: FilterKind? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                combine(
                    drinks,
                    repository.observeSavedIds(),
                    loading,
                    queried,
                    query,
                ) { drinkList, savedIds, isLoading, hasQueried, currentQuery ->
                    SearchUiState(
                        query = currentQuery,
                        loading = isLoading,
                        results = drinkList.map { it.copy(saved = it.id in savedIds) },
                        empty = hasQueried && !isLoading && drinkList.isEmpty(),
                    )
                },
                mode,
            ) { ui, currentMode -> ui.copy(mode = currentMode) }
                .collect { newState -> setState { newState } }
        }
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Search -> {
                if (intent.mode != null && mode.value != intent.mode) {
                    searchJob?.cancel()
                    mode.value = intent.mode
                }
                catalogKind = intent.filterKind
                search(intent.query, immediate = intent.filterKind != null)
            }
            is SearchIntent.SetMode -> setMode(intent.mode)
            is SearchIntent.ToggleSaved -> toggleSaved(intent.drink)
            is SearchIntent.OpenDrink -> sendEffect(SearchEffect.OpenDrink(intent.drink))
            SearchIntent.Back -> sendEffect(SearchEffect.NavigateBack)
        }
    }

    private fun setMode(next: SearchMode) {
        if (mode.value == next) return
        searchJob?.cancel()
        mode.value = next
        catalogKind = null
        search(query.value)
    }

    private fun search(rawQuery: String, immediate: Boolean = false) {
        val adjusted = rawQuery.replace("%20", " ").trim()
        searchJob?.cancel()
        query.value = adjusted
        drinks.value = emptyList()
        searchText(adjusted, immediate)
    }

    private fun searchText(adjusted: String, immediate: Boolean) {
        if (adjusted.length < SEARCH_MIN_CHARS) {
            queried.value = false
            drinks.value = emptyList()
            loading.value = false
            return
        }
        queried.value = true
        loading.value = true
        searchJob = viewModelScope.launch {
            if (!immediate) {
                delay(SEARCH_DEBOUNCE_MS)
            }
            try {
                val kind = catalogKind
                drinks.value = when {
                    kind != null -> repository.fetchAndCache(DrinkFilter.dynamic(kind, adjusted))
                    mode.value == SearchMode.INGREDIENT -> repository.searchByIngredient(adjusted)
                    else -> repository.search(adjusted)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
                recordCrash(e)
                drinks.value = emptyList()
                sendEffect(SearchEffect.ShowMessageRes(R.string.network_error))
            } finally {
                if (isActive) {
                    loading.value = false
                }
            }
        }
    }

    private fun toggleSaved(drink: Drink) {
        viewModelScope.launch {
            if (drink.saved) {
                repository.unsave(drink.id)
            } else {
                repository.save(drink)
            }
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
        private const val SEARCH_DEBOUNCE_MS = 250L
    }
}
