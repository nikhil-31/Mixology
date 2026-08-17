package com.capstone.nik.mixology.ui.search

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
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
            }.collect { newState -> setState { newState } }
        }
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Search -> search(intent.query)
            is SearchIntent.ToggleSaved -> toggleSaved(intent.drink)
            is SearchIntent.OpenDrink -> sendEffect(SearchEffect.OpenDrink(intent.drink))
            SearchIntent.Back -> sendEffect(SearchEffect.NavigateBack)
        }
    }

    private fun search(rawQuery: String) {
        val adjusted = rawQuery.replace("%20", " ").trim()
        searchJob?.cancel()
        query.value = adjusted
        if (adjusted.length < SEARCH_MIN_CHARS) {
            queried.value = false
            drinks.value = emptyList()
            loading.value = false
            return
        }
        queried.value = true
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loading.value = true
            try {
                drinks.value = repository.search(adjusted)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
                FirebaseCrashlytics.getInstance().recordException(e)
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
                sendEffect(SearchEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(drink)
                sendEffect(SearchEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
        private const val SEARCH_DEBOUNCE_MS = 250L
    }
}
