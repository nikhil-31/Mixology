package com.capstone.nik.mixology.ui.grid

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DrinkGridViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val _drinks = MutableStateFlow<List<DrinkListItem>>(emptyList())
    val drinks: StateFlow<List<DrinkListItem>> = _drinks.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val messages = Channel<Int>(Channel.BUFFERED)
    val userMessages = messages.receiveAsFlow()

    private var boundFilter: DrinkFilter? = null
    private var observeJob: Job? = null

    fun bind(filter: DrinkFilter) {
        if (boundFilter != filter) {
            boundFilter = filter
            observeJob?.cancel()
            observeJob = viewModelScope.launch {
                repository.observeDrinks(filter).collect { items ->
                    _drinks.value = items
                }
            }
        }
        refresh(filter)
    }

    fun toggleSaved(item: DrinkListItem) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.id)
                messages.send(R.string.drink_deleted)
            } else {
                repository.save(item.toCocktail())
                messages.send(R.string.drink_added)
            }
        }
    }

    fun refresh(filter: DrinkFilter) {
        if (filter.kind == null || filter.query == null) return
        viewModelScope.launch {
            try {
                repository.fetchAndCache(filter)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh ${filter.name}", e)
                FirebaseCrashlytics.getInstance().log("Failed to refresh ${filter.name}")
                FirebaseCrashlytics.getInstance().recordException(e)
                _error.value = getApplication<Application>().getString(R.string.network_error)
            }
        }
    }

    fun errorShown() {
        _error.value = null
    }

    companion object {
        private const val TAG = "DrinkGridViewModel"
    }
}
