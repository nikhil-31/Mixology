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
    private var notesJob: Job? = null
    /** The account's notes as last seen, so a recipe reloaded from My recipes keeps its note. */
    private var latestNotes: Map<String, String> = emptyMap()

    /** The last note typed for a drink and not yet handed to the repository: (drink id, text). */
    private var unsentNote: Pair<String, String>? = null

    init {
        retryJob = viewModelScope.launch {
            networkMonitor.retries.collect {
                loadedId?.let { lookup(it) }
            }
        }
        viewModelScope.launch {
            repository.observeBar().collect { names ->
                setState { copy(inBar = names.mapTo(mutableSetOf()) { it.trim().lowercase() }) }
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
            DrinkDetailsIntent.SaveNotes -> flushNotes()
            DrinkDetailsIntent.AddToShoppingList -> addToShoppingList()
            DrinkDetailsIntent.RemoveFromShoppingList -> removeFromShoppingList()
            is DrinkDetailsIntent.OpenVideo -> sendEffect(DrinkDetailsEffect.OpenUrl(intent.url))
            is DrinkDetailsIntent.Rate -> rate(intent.stars)
            DrinkDetailsIntent.EditRecipe -> currentState.drink?.takeIf { it.isUserRecipe }?.let {
                sendEffect(DrinkDetailsEffect.OpenRecipeEditor(it.id))
            }
            DrinkDetailsIntent.Retry -> loadedId?.let { id ->
                setState { copy(loading = true, loadFailed = false) }
                lookup(id)
            }
        }
    }

    private fun load(drink: Drink) {
        if (loadedId != drink.id) {
            flushNotes()
            // Brings in a note (or save) made on another device since the last pull;
            // the notes collector shows it when it lands.
            viewModelScope.launch { repository.syncSaved() }
        }
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
                loadFailed = false,
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
        watchNotes(drink.id)
        lookup(drink.id)
    }

    /**
     * Notes can arrive later from a pull (another device, or a sign-in).
     * While the user has an unsent edit for this drink, the text field wins.
     */
    private fun watchNotes(id: String) {
        notesJob?.cancel()
        notesJob = viewModelScope.launch {
            repository.observeNotes().collect { notes ->
                latestNotes = notes
                if (unsentNote?.first == id) return@collect
                setState {
                    val shown = this.drink
                    if (shown?.id == id) copy(drink = shown.copy(notes = notes[id].orEmpty())) else this
                }
            }
        }
    }

    /** The account's note on [drink], or the user's unsent text for it. */
    private fun withNotes(drink: Drink): Drink =
        withUnsentNote(drink.copy(notes = latestNotes[drink.id].orEmpty()))

    /**
     * The user's own recipe comes from My recipes, not the catalog. The server
     * refuses recipe ids for saving, ratings and recently viewed, so none of those
     * run; notes work on recipes like on any drink.
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
                loadFailed = false,
                drink = withNotes(cached ?: drink),
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
                        setState { copy(loading = false, loadFailed = false, drink = withNotes(current)) }
                    }
                    // It was there and now isn't: the user deleted it.
                    seen -> sendEffect(DrinkDetailsEffect.NavigateBack)
                }
            }
        }
        watchNotes(drink.id)
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
                setState { copy(loading = false, loadFailed = false, drink = withUnsentNote(cached)) }
            }
            try {
                val fresh = repository.lookupDrink(id)
                if (fresh != null) {
                    setState { copy(loading = false, loadFailed = false, drink = withUnsentNote(fresh)) }
                } else {
                    setState { copy(loading = false, loadFailed = drink?.hasRecipe != true) }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load drink $id", e)
                recordCrash(e)
                // With nothing to show, the screen offers Retry in place of the recipe.
                setState { copy(loading = false, loadFailed = drink?.hasRecipe != true) }
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
                setState { copy(loading = false, loadFailed = drink?.hasRecipe != true) }
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

    /**
     * The field updates on every keystroke; the repository (and so the server)
     * gets the text when Save is tapped. Leaving the drink sends any unsaved
     * text too, so nothing typed is lost.
     */
    private fun updateNotes(notes: String) {
        val drink = currentState.drink ?: return
        unsentNote = drink.id to notes
        setState { copy(drink = drink.copy(notes = notes), unsavedNote = true) }
    }

    private fun flushNotes() {
        val (id, notes) = unsentNote ?: return
        unsentNote = null
        // Not suspending: SavedSync sends it from its own scope, so this works from onCleared too.
        repository.updateNotes(id, notes)
        if (currentState.unsavedNote) setState { copy(unsavedNote = false) }
    }

    /** Keeps the user's unsent text when a lookup replaces the shown drink. */
    private fun withUnsentNote(drink: Drink): Drink {
        val (id, notes) = unsentNote ?: return drink
        return if (id == drink.id) drink.copy(notes = notes) else drink
    }

    override fun onCleared() {
        flushNotes()
        super.onCleared()
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
            // Logged first: the saved set updates in memory at once, so the
            // next toggle can arrive before save() returns.
            if (currentState.saved) {
                analytics.logSaveDrink(drink.id, drink.name, saved = false)
                repository.unsave(drink.id)
            } else {
                analytics.logSaveDrink(drink.id, drink.name, saved = true)
                repository.save(drink)
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
