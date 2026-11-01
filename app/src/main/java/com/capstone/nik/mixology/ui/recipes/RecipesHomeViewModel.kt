package com.capstone.nik.mixology.ui.recipes

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.connectionMessage
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipesHomeViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val drinks: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context,
) : MviViewModel<RecipesHomeIntent, RecipesHomeUiState, RecipesHomeEffect>(RecipesHomeUiState()) {

    init {
        viewModelScope.launch {
            recipes.recipes.collect { list -> setState { copy(mine = list) } }
        }
        viewModelScope.launch {
            drinks.observeDrinks(DrinkFilter.SAVED).collect { list -> setState { copy(saved = list) } }
        }
        viewModelScope.launch {
            drinks.observeBarRecommendations().collect { recs -> setState { copy(bar = recs) } }
        }
        viewModelScope.launch {
            networkMonitor.retries.collect { load() }
        }
        load()
    }

    override fun onIntent(intent: RecipesHomeIntent) {
        when (intent) {
            RecipesHomeIntent.Retry -> load()
            is RecipesHomeIntent.OpenDrink -> sendEffect(RecipesHomeEffect.OpenDrink(intent.drink))
            RecipesHomeIntent.AddRecipe -> sendEffect(RecipesHomeEffect.OpenEditor)
            RecipesHomeIntent.SeeAllRecipes -> sendEffect(RecipesHomeEffect.OpenMyRecipes)
            RecipesHomeIntent.SeeAllSaved -> sendEffect(RecipesHomeEffect.OpenSaved)
        }
    }

    private fun load() {
        if (currentState.loading) return
        setState { copy(loading = true, error = null) }
        viewModelScope.launch { syncBar() }
        viewModelScope.launch {
            try {
                recipes.load()
                drinks.syncSaved()
                setState { copy(loading = false, loaded = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = networkMonitor.connectionMessage(context, error) ?: error.message.orEmpty()
                setState { copy(loading = false, loaded = true, error = message) }
            }
        }
    }

    /** The hero carousel is a bonus: a failed My Bar pull leaves the rest of the screen alone. */
    private suspend fun syncBar() {
        try {
            drinks.syncBar()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, "Failed to sync My Bar", error)
        }
    }

    private companion object {
        const val TAG = "RecipesHome"
    }
}
