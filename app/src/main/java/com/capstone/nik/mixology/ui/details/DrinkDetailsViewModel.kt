package com.capstone.nik.mixology.ui.details

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrinkDetailsViewModel @Inject constructor(
    private val repository: DrinkRepository,
    @ApplicationContext private val appContext: Context,
) : MviViewModel<DrinkDetailsIntent, DrinkDetailsUiState, DrinkDetailsEffect>(
    DrinkDetailsUiState(),
) {

    private var loadedId: String? = null
    private var savedJob: Job? = null
    private var lookupJob: Job? = null

    override fun onIntent(intent: DrinkDetailsIntent) {
        when (intent) {
            is DrinkDetailsIntent.Load -> load(intent.drink)
            DrinkDetailsIntent.ToggleSaved -> toggleSaved()
            DrinkDetailsIntent.Share -> share()
            DrinkDetailsIntent.Back -> sendEffect(DrinkDetailsEffect.NavigateBack)
        }
    }

    private fun load(drink: Drink) {
        if (loadedId == drink.id && currentState.drink?.hasRecipe == true) return
        loadedId = drink.id
        setState {
            copy(
                loading = !drink.hasRecipe,
                drink = drink,
            )
        }
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            repository.observeSavedIds().collect { ids ->
                setState { copy(saved = drink.id in ids) }
            }
        }
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            val cached = repository.cachedDrink(drink.id)
            if (cached != null && cached.hasRecipe) {
                setState { copy(loading = false, drink = cached) }
            }
            try {
                val fresh = repository.lookupDrink(drink.id)
                if (fresh != null) {
                    setState { copy(loading = false, drink = fresh) }
                } else {
                    setState { copy(loading = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load drink ${drink.id}", e)
                FirebaseCrashlytics.getInstance().recordException(e)
                setState { copy(loading = false) }
                if (currentState.drink?.hasRecipe != true) {
                    sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.network_error))
                }
            }
        }
    }

    private fun toggleSaved() {
        val drink = currentState.drink ?: return
        viewModelScope.launch {
            if (currentState.saved) {
                repository.unsave(drink.id)
                sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.drink_deleted))
            } else {
                repository.save(drink)
                sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.drink_added))
            }
        }
    }

    private fun share() {
        val drink = currentState.drink ?: return
        val builder = StringBuilder()
            .append(appContext.getString(R.string.detail_share_sent_from_mixology)).append(" \n")
            .append(appContext.getString(R.string.detail_share_name)).append(" ").append(drink.name).append("\n")
            .append(appContext.getString(R.string.detail_share_alcoholic)).append(" ").append(drink.alcoholic).append("\n")
            .append(appContext.getString(R.string.detail_share_instructions)).append(" \n")
            .append(drink.instructions).append("\n")
            .append(appContext.getString(R.string.detail_share_ingredients)).append("\n")
        drink.ingredients.forEach { item ->
            builder.append(item.ingredient).append(" -- ").append(item.measure).append("\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, appContext.getString(R.string.detail_share_sent_from_mixology))
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
        sendEffect(DrinkDetailsEffect.ShareRecipe(intent))
    }

    companion object {
        private const val TAG = "DrinkDetailsViewModel"
    }
}
