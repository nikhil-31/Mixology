package com.capstone.nik.mixology.ui.search

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.components.DrinkViewPreferences
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
) : MviViewModel<SearchIntent, SearchUiState, SearchEffect>(SearchUiState()) {

    private val drinks = MutableStateFlow<List<Drink>>(emptyList())
    private val loading = MutableStateFlow(false)
    private val queried = MutableStateFlow(false)
    private val query = MutableStateFlow("")
    private val mode = MutableStateFlow(SearchMode.NAME)
    private val listView = MutableStateFlow(DrinkViewPreferences.listView(context))
    private var catalogKind: FilterKind? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                repository.refreshCatalogs()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh catalogs", e)
                recordCrash(e)
            }
        }
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
                listView,
                repository.observeCatalog(FilterKind.INGREDIENT),
            ) { ui, currentMode, isList, ingredients ->
                val suggestions = if (
                    currentMode == SearchMode.INGREDIENT &&
                    !ui.loading &&
                    ui.results.isEmpty()
                ) {
                    filterIngredientSuggestions(ingredients, ui.query)
                } else {
                    emptyList()
                }
                ui.copy(
                    mode = currentMode,
                    listView = isList,
                    empty = ui.empty && suggestions.isEmpty(),
                    suggestions = suggestions,
                )
            }.collect { newState -> setState { newState } }
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
                search(
                    intent.query,
                    immediate = intent.filterKind != null || intent.commit,
                )
            }
            is SearchIntent.SetMode -> setMode(intent.mode)
            is SearchIntent.SelectSuggestion -> selectSuggestion(intent.ingredient)
            is SearchIntent.ToggleSaved -> toggleSaved(intent.drink)
            is SearchIntent.OpenDrink -> sendEffect(SearchEffect.OpenDrink(intent.drink))
            SearchIntent.ToggleListView -> toggleListView()
            SearchIntent.Back -> sendEffect(SearchEffect.NavigateBack)
        }
    }

    private fun selectSuggestion(ingredient: String) {
        searchJob?.cancel()
        if (mode.value != SearchMode.INGREDIENT) {
            mode.value = SearchMode.INGREDIENT
        }
        catalogKind = null
        search(ingredient, immediate = true)
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
        if (mode.value == SearchMode.INGREDIENT && !immediate) {
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

    private fun toggleListView() {
        val next = !listView.value
        DrinkViewPreferences.setListView(context, next)
        listView.value = next
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
