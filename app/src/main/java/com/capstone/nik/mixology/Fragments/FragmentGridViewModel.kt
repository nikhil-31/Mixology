package com.capstone.nik.mixology.Fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FragmentGridViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val _drinks = MutableStateFlow<List<DrinkListItem>>(emptyList())
    val drinks: StateFlow<List<DrinkListItem>> = _drinks.asStateFlow()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var boundFilter: DrinkFilter? = null

    fun bind(filter: DrinkFilter) {
        if (boundFilter != filter) {
            boundFilter = filter
            viewModelScope.launch {
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
            } else {
                repository.save(item.toCocktail())
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
                _error.postValue(getApplication<Application>().getString(R.string.network_error))
            }
        }
    }

    fun errorShown() {
        _error.value = null
    }

    companion object {
        private const val TAG = "FragmentGridViewModel"
    }
}
