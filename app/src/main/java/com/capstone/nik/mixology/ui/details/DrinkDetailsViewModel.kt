package com.capstone.nik.mixology.ui.details

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.model.ingredientMeasures
import com.capstone.nik.mixology.ui.mvi.MviAndroidViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DrinkDetailsViewModel(application: Application) :
    MviAndroidViewModel<DrinkDetailsIntent, DrinkDetailsUiState, DrinkDetailsEffect>(
        application,
        DrinkDetailsUiState(),
    ) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()
    private var loadedId: String? = null
    private var savedJob: Job? = null
    private var lookupJob: Job? = null

    override fun onIntent(intent: DrinkDetailsIntent) {
        when (intent) {
            is DrinkDetailsIntent.Load -> load(intent.cocktail)
            DrinkDetailsIntent.ToggleSaved -> toggleSaved()
            DrinkDetailsIntent.Share -> share()
            DrinkDetailsIntent.Back -> sendEffect(DrinkDetailsEffect.NavigateBack)
        }
    }

    private fun load(cocktail: Cocktail) {
        val id = cocktail.getmDrinkId() ?: return
        if (loadedId == id && currentState.drink != null) return
        loadedId = id
        setState {
            copy(
                loading = true,
                cocktail = cocktail,
            )
        }
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            repository.observeSavedIds().collect { ids ->
                setState { copy(saved = id in ids) }
            }
        }
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            try {
                val drink = repository.lookupDrink(id)
                setState {
                    copy(
                        loading = false,
                        drink = drink,
                        ingredients = drink?.ingredientMeasures().orEmpty(),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load drink $id", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                setState { copy(loading = false) }
                sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.network_error))
            }
        }
    }

    private fun toggleSaved() {
        val cocktail = currentState.cocktail ?: return
        val id = cocktail.getmDrinkId() ?: return
        viewModelScope.launch {
            if (currentState.saved) {
                repository.unsave(id)
                sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(cocktail)
                sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    private fun share() {
        val drink = currentState.drink ?: return
        val app = getApplication<Application>()
        val builder = StringBuilder()
            .append(app.getString(R.string.detail_share_sent_from_mixology)).append(" \n")
            .append(app.getString(R.string.detail_share_name)).append(" ").append(drink.strDrink).append("\n")
            .append(app.getString(R.string.detail_share_alcoholic)).append(" ").append(drink.strAlcoholic).append("\n")
            .append(app.getString(R.string.detail_share_instructions)).append(" \n")
            .append(drink.strInstructions).append("\n")
            .append(app.getString(R.string.detail_share_ingredients)).append("\n")
        currentState.ingredients.forEach { item ->
            builder.append(item.ingredient).append(" -- ").append(item.measure).append("\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, app.getString(R.string.detail_share_sent_from_mixology))
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
        sendEffect(DrinkDetailsEffect.ShareRecipe(intent))
    }

    companion object {
        private const val TAG = "DrinkDetailsViewModel"
    }
}
