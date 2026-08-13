package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.capstone.nik.mixology.ui.model.ingredientMeasures
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class RandomixerUiState(
    val loading: Boolean = false,
    val drink: Drink? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
    val saved: Boolean = false,
)

class RandomixerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val _state = MutableStateFlow(RandomixerUiState())
    val state: StateFlow<RandomixerUiState> = _state.asStateFlow()

    private val messages = Channel<Int>(Channel.BUFFERED)
    val userMessages = messages.receiveAsFlow()

    private var savedJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            try {
                val drink = repository.randomDrink()
                savedJob?.cancel()
                if (drink?.idDrink != null) {
                    savedJob = viewModelScope.launch {
                        repository.observeSavedIds().collect { ids ->
                            _state.value = _state.value.copy(saved = drink.idDrink in ids)
                        }
                    }
                }
                _state.value = _state.value.copy(
                    loading = false,
                    drink = drink,
                    ingredients = drink?.ingredientMeasures().orEmpty(),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load random drink", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _state.value = _state.value.copy(loading = false)
                messages.send(R.string.network_error)
            }
        }
    }

    fun toggleSaved() {
        val drink = _state.value.drink ?: return
        val id = drink.idDrink ?: return
        viewModelScope.launch {
            if (_state.value.saved) {
                repository.unsave(id)
                messages.send(R.string.drink_deleted)
            } else {
                repository.save(Cocktail(id, drink.strDrink, drink.strDrinkThumb))
                messages.send(R.string.drink_added)
            }
        }
    }

    companion object {
        private const val TAG = "RandomixerViewModel"
    }
}
