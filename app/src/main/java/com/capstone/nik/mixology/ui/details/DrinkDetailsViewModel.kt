package com.capstone.nik.mixology.ui.details

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.isOnShoppingList
import com.capstone.nik.mixology.repository.DrinkRating
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.RatingRepository
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrinkDetailsViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val appContext: Context,
    private val analytics: AnalyticsTracker = AnalyticsTracker.forTests(),
    private val ratings: RatingRepository = RatingRepository.None,
    private val recipes: RecipeRepository = RecipeRepository.None,
) : MviViewModel<DrinkDetailsIntent, DrinkDetailsUiState, DrinkDetailsEffect>(
    DrinkDetailsUiState(),
) {

    private var loadedId: String? = null
    private var savedJob: Job? = null
    private var lookupJob: Job? = null
    private var retryJob: Job? = null
    private var ratingJob: Job? = null
    private var recipeJob: Job? = null

    init {
        retryJob = viewModelScope.launch {
            networkMonitor.retries.collect {
                loadedId?.let { lookup(it) }
            }
        }
        viewModelScope.launch {
            repository.observeShopping().collect { items ->
                setState {
                    val current = drink
                    copy(onShoppingList = current != null && isOnShoppingList(current, items))
                }
            }
        }
    }

    override fun onIntent(intent: DrinkDetailsIntent) {
        when (intent) {
            is DrinkDetailsIntent.Load -> load(intent.drink)
            DrinkDetailsIntent.ToggleSaved -> toggleSaved()
            DrinkDetailsIntent.Share -> share()
            DrinkDetailsIntent.Back -> sendEffect(DrinkDetailsEffect.NavigateBack)
            is DrinkDetailsIntent.UpdateNotes -> updateNotes(intent.notes)
            DrinkDetailsIntent.AddToShoppingList -> addToShoppingList()
            DrinkDetailsIntent.RemoveFromShoppingList -> removeFromShoppingList()
            is DrinkDetailsIntent.OpenVideo -> sendEffect(DrinkDetailsEffect.OpenUrl(intent.url))
            is DrinkDetailsIntent.Rate -> rate(intent.stars)
            DrinkDetailsIntent.EditRecipe -> currentState.drink?.takeIf { it.isUserRecipe }?.let {
                sendEffect(DrinkDetailsEffect.OpenRecipeEditor(it.id))
            }
        }
    }

    private fun load(drink: Drink) {
        if (drink.isUserRecipe) {
            loadRecipe(drink)
            return
        }
        viewModelScope.launch { repository.recordViewed(drink) }
        if (loadedId != drink.id) {
            analytics.logViewDrink(drink.id, drink.name)
        }
        if (loadedId == drink.id && currentState.drink?.hasRecipe == true) return
        loadedId = drink.id
        recipeJob?.cancel()
        setState {
            copy(
                loading = !drink.hasRecipe,
                drink = drink,
                rating = null,
            )
        }
        loadRating(drink.id)
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            repository.observeSavedIds().collect { ids ->
                setState { copy(saved = drink.id in ids) }
            }
        }
        lookup(drink.id)
    }

    /**
     * The user's own recipe comes from My recipes, not the catalog. The server
     * refuses recipe ids for saving, ratings and recently viewed, so none of those run.
     */
    private fun loadRecipe(drink: Drink) {
        if (loadedId == drink.id) return
        analytics.logViewDrink(drink.id, drink.name)
        loadedId = drink.id
        savedJob?.cancel()
        ratingJob?.cancel()
        val cached = recipes.recipes.value.firstOrNull { it.id == drink.id }
        setState {
            copy(
                loading = cached == null && !drink.hasRecipe,
                drink = cached ?: drink,
                saved = false,
                rating = null,
            )
        }
        recipeJob?.cancel()
        recipeJob = viewModelScope.launch {
            var seen = cached != null
            recipes.recipes.collect { list ->
                val current = list.firstOrNull { it.id == drink.id }
                when {
                    current != null -> {
                        // Follows edits made in the editor.
                        seen = true
                        setState { copy(loading = false, drink = current) }
                    }
                    // It was there and now isn't: the user deleted it.
                    seen -> sendEffect(DrinkDetailsEffect.NavigateBack)
                }
            }
        }
        lookup(drink.id)
    }

    private fun lookup(id: String) {
        if (id.startsWith(Drink.USER_RECIPE_PREFIX)) {
            lookupRecipe(id)
            return
        }
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            val cached = repository.cachedDrink(id)
            if (cached != null && cached.hasRecipe) {
                setState { copy(loading = false, drink = cached) }
            }
            try {
                val fresh = repository.lookupDrink(id)
                if (fresh != null) {
                    setState { copy(loading = false, drink = fresh) }
                } else {
                    setState { copy(loading = false) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load drink $id", e)
                recordCrash(e)
                setState { copy(loading = false) }
                if (currentState.drink?.hasRecipe != true) {
                    sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.network_error))
                }
            }
        }
    }

    private fun lookupRecipe(id: String) {
        lookupJob?.cancel()
        lookupJob = viewModelScope.launch {
            try {
                // Updates the repository's list, which the recipe job shows.
                recipes.get(id)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load recipe $id", e)
                setState { copy(loading = false) }
                if (currentState.drink?.hasRecipe != true) {
                    sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.network_error))
                }
            }
        }
    }

    private fun loadRating(id: String) {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            val rating = runCatching { ratings.rating(id) }.getOrNull()
            if (loadedId == id) setState { copy(rating = rating) }
        }
    }

    private fun rate(stars: Int) {
        val id = loadedId ?: return
        if (currentState.isUserRecipe) return
        val before = currentState.rating ?: DrinkRating(null, 0, null)
        val next = if (before.mine == stars) null else stars.coerceIn(1, 5)
        // Show the new stars right away; the server answer brings the new average.
        setState { copy(rating = before.copy(mine = next)) }
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            try {
                val updated = ratings.rate(id, next)
                if (loadedId == id) setState { copy(rating = updated) }
            } catch (error: kotlinx.coroutines.CancellationException) {
                throw error
            } catch (e: Exception) {
                if (loadedId == id) setState { copy(rating = before) }
                sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.detail_rating_failed))
            }
        }
    }

    private fun updateNotes(notes: String) {
        val drink = currentState.drink ?: return
        setState { copy(drink = drink.copy(notes = notes)) }
        viewModelScope.launch {
            repository.updateNotes(drink.id, notes)
        }
    }

    private fun addToShoppingList() {
        val drink = currentState.drink ?: return
        viewModelScope.launch {
            repository.addToShoppingList(drink)
            analytics.logAddToShoppingList(drink.id, drink.name)
            sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.shopping_added))
        }
    }

    private fun removeFromShoppingList() {
        val drink = currentState.drink ?: return
        viewModelScope.launch {
            repository.removeFromShoppingList(drink.id)
            sendEffect(DrinkDetailsEffect.ShowMessageRes(R.string.shopping_removed))
        }
    }

    private fun toggleSaved() {
        val drink = currentState.drink?.takeUnless { it.isUserRecipe } ?: return
        viewModelScope.launch {
            if (currentState.saved) {
                repository.unsave(drink.id)
                analytics.logSaveDrink(drink.id, drink.name, saved = false)
            } else {
                repository.save(drink)
                analytics.logSaveDrink(drink.id, drink.name, saved = true)
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
        analytics.logShareDrink(drink.id, drink.name)
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
