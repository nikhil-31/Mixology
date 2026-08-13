package com.capstone.nik.mixology.ui.details

import android.app.Application
import android.content.Intent
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

data class DrinkDetailsUiState(
    val loading: Boolean = false,
    val cocktail: Cocktail? = null,
    val drink: Drink? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
    val saved: Boolean = false,
)

class DrinkDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val _state = MutableStateFlow(DrinkDetailsUiState())
    val state: StateFlow<DrinkDetailsUiState> = _state.asStateFlow()

    private val messages = Channel<Int>(Channel.BUFFERED)
    val userMessages = messages.receiveAsFlow()

    private var loadedId: String? = null
    private var savedJob: Job? = null
    private var lookupJob: Job? = null

    fun load(cocktail: Cocktail) {
        val id = cocktail.getmDrinkId() ?: return
        if (loadedId == id && _state.value.drink != null) return
        loadedId = id
        _state.value = DrinkDetailsUiState(
            loading = true,
            cocktail = cocktail,
            saved = _state.value.saved,
        )
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            repository.observeSavedIds().collect { ids ->
                _state.value = _state.value.copy(saved = id in ids)
            }
        }
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            try {
                val drink = repository.lookupDrink(id)
                _state.value = _state.value.copy(
                    loading = false,
                    drink = drink,
                    ingredients = drink?.ingredientMeasures().orEmpty(),
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load drink $id", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                _state.value = _state.value.copy(loading = false)
                messages.send(R.string.network_error)
            }
        }
    }

    fun toggleSaved() {
        val cocktail = _state.value.cocktail ?: return
        val id = cocktail.getmDrinkId() ?: return
        viewModelScope.launch {
            if (_state.value.saved) {
                repository.unsave(id)
                messages.send(R.string.drink_deleted)
            } else {
                repository.save(cocktail)
                messages.send(R.string.drink_added)
            }
        }
    }

    fun shareIntent(): Intent? {
        val drink = _state.value.drink ?: return null
        val app = getApplication<Application>()
        val builder = StringBuilder()
            .append(app.getString(R.string.detail_share_sent_from_mixology)).append(" \n")
            .append(app.getString(R.string.detail_share_name)).append(" ").append(drink.strDrink).append("\n")
            .append(app.getString(R.string.detail_share_alcoholic)).append(" ").append(drink.strAlcoholic).append("\n")
            .append(app.getString(R.string.detail_share_instructions)).append(" \n")
            .append(drink.strInstructions).append("\n")
            .append(app.getString(R.string.detail_share_ingredients)).append("\n")
        _state.value.ingredients.forEach { item ->
            builder.append(item.ingredient).append(" -- ").append(item.measure).append("\n")
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, app.getString(R.string.detail_share_sent_from_mixology))
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
    }

    companion object {
        private const val TAG = "DrinkDetailsViewModel"
    }
}
