package com.capstone.nik.mixology.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResultItem(
    val drink: Drink,
    val saved: Boolean,
) {
    fun toCocktail(): Cocktail = Cocktail(drink.idDrink, drink.strDrink, drink.strDrinkThumb)
}

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<SearchResultItem> = emptyList(),
    val empty: Boolean = false,
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val drinks = MutableStateFlow<List<Drink>>(emptyList())
    private val loading = MutableStateFlow(false)
    private val queried = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    val state: StateFlow<SearchUiState> = combine(
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    private val messages = Channel<Int>(Channel.BUFFERED)
    val userMessages = messages.receiveAsFlow()

    fun search(rawQuery: String) {
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
                messages.send(R.string.network_error)
            } finally {
                loading.value = false
            }
        }
    }

    fun toggleSaved(item: SearchResultItem) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.drink.idDrink)
                messages.send(R.string.drink_deleted)
            } else {
                repository.save(item.toCocktail())
                messages.send(R.string.drink_added)
            }
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}
