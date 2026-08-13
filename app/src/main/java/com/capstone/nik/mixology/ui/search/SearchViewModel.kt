package com.capstone.nik.mixology.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.mvi.MviAndroidViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) :
    MviAndroidViewModel<SearchIntent, SearchUiState, SearchEffect>(
        application,
        SearchUiState(),
    ) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val drinks = MutableStateFlow<List<Drink>>(emptyList())
    private val loading = MutableStateFlow(false)
    private val queried = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                drinks,
                repository.observeSavedIds(),
                loading,
                queried,
                query,
            ) { drinkList, savedIds, isLoading, hasQueried, currentQuery ->
                val results = drinkList.map { SearchResultItem(it, it.idDrink in savedIds) }
                SearchUiState(
                    query = currentQuery,
                    loading = isLoading,
                    results = results,
                    empty = hasQueried && !isLoading && results.isEmpty(),
                )
            }.collect { setState { it } }
        }
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Search -> search(intent.query)
            is SearchIntent.ToggleSaved -> toggleSaved(intent.item)
            is SearchIntent.OpenDrink -> sendEffect(SearchEffect.OpenDrink(intent.cocktail))
            SearchIntent.Back -> sendEffect(SearchEffect.NavigateBack)
        }
    }

    private fun search(rawQuery: String) {
        val adjusted = rawQuery.replace("%20", " ").trim()
        if (adjusted.isEmpty()) return
        query.value = adjusted
        queried.value = true
        viewModelScope.launch {
            loading.value = true
            try {
                drinks.value = repository.search(adjusted)
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                drinks.value = emptyList()
                sendEffect(SearchEffect.ShowMessageRes(R.string.network_error))
            } finally {
                loading.value = false
            }
        }
    }

    private fun toggleSaved(item: SearchResultItem) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.drink.idDrink)
                sendEffect(SearchEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(item.toCocktail())
                sendEffect(SearchEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}
