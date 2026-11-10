package com.capstone.nik.mixology.ui.shopping

import com.capstone.nik.mixology.Network.NetworkMonitor
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.data.ShoppingTrip
import com.capstone.nik.mixology.data.ShoppingTripItem
import com.capstone.nik.mixology.repository.ShoppingTripRepository
import com.capstone.nik.mixology.data.withRecipes
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val recipes: RecipeRepository = RecipeRepository.None,
    private val preferences: ShoppingPreferences = ShoppingPreferences.inMemory(),
    private val trips: ShoppingTripRepository = ShoppingTripRepository.inMemory(),
    private val networkMonitor: NetworkMonitor = NetworkMonitor.forTests(),
) : MviViewModel<ShoppingIntent, ShoppingUiState, ShoppingEffect>(
    ShoppingUiState(byIngredient = preferences.byIngredient),
) {

    /** What the last remove or clear took off, for Undo. */
    private var lastRemoved: List<ShoppingItemEntity> = emptyList()

    /** Bar ingredients the last Done shopping added, so Undo can take them back out. */
    private var lastAddedToBar: List<String> = emptyList()

    /** The trip the last Done shopping recorded (still saving, perhaps), so Undo can delete it. */
    private var lastTrip: Deferred<ShoppingTrip>? = null

    /** Done shopping's bar adds and removals, so a quick Undo waits for them instead of racing them. */
    private var finishJob: Job? = null

    /** My recipes are fetched at most once here when the list needs one that isn't loaded yet. */
    private var requestedRecipes = false

    init {
        viewModelScope.launch { repository.syncShopping() }
        // Retry, or the server answering again: pick up what changed on the account meanwhile.
        viewModelScope.launch {
            networkMonitor.retries.collect { repository.syncShopping() }
        }
        viewModelScope.launch {
            repository.observeBar().collect { names ->
                setState { copy(bar = names.mapTo(HashSet()) { normalizeIngredient(it) }) }
            }
        }
        viewModelScope.launch {
            combine(repository.observeShoppingSections(), recipes.recipes) { sections, mine ->
                loadRecipesIfMissing(sections, mine)
                withRecipes(sections, mine)
            }.collect { sections ->
                setState { copy(sections = sections) }
            }
        }
    }

    override fun onIntent(intent: ShoppingIntent) {
        when (intent) {
            is ShoppingIntent.Toggle -> viewModelScope.launch {
                repository.setShoppingChecked(intent.item.id, !intent.item.checked)
            }
            is ShoppingIntent.ToggleCombined -> viewModelScope.launch {
                val checked = !intent.row.checked
                intent.row.items.forEach { repository.setShoppingChecked(it.id, checked) }
            }
            is ShoppingIntent.Remove -> removeItems(listOf(intent.item), R.plurals.shopping_removed_items)
            is ShoppingIntent.RemoveCombined -> removeItems(intent.row.items, R.plurals.shopping_removed_items)
            is ShoppingIntent.RemoveDrink -> {
                val items = currentState.items.filter { it.drinkId == intent.drinkId }
                if (items.isEmpty()) return
                remember(items, R.plurals.shopping_removed_items)
                viewModelScope.launch { repository.removeFromShoppingList(intent.drinkId) }
            }
            ShoppingIntent.ClearChecked -> {
                val items = currentState.items.filter { it.checked }
                if (items.isEmpty()) return
                remember(items, R.plurals.shopping_cleared)
                viewModelScope.launch { repository.clearCheckedShoppingItems() }
            }
            ShoppingIntent.Undo -> {
                val items = lastRemoved
                val added = lastAddedToBar
                val trip = lastTrip
                lastRemoved = emptyList()
                lastAddedToBar = emptyList()
                lastTrip = null
                viewModelScope.launch {
                    finishJob?.join()
                    if (items.isNotEmpty()) repository.restoreShoppingItems(items)
                    added.forEach { repository.removeFromBar(it) }
                    trip?.let { trips.delete(it.await().id) }
                }
            }
            ShoppingIntent.OpenReview -> {
                val rows = reviewRows(currentState.sections, currentState.bar)
                if (rows.isEmpty()) return
                setState {
                    copy(review = ReviewUi(rows, rows.filterNot { it.inBar }.mapTo(HashSet()) { it.key }))
                }
            }
            ShoppingIntent.DismissReview -> setState { copy(review = null) }
            is ShoppingIntent.ToggleReviewRow -> setState {
                val current = review ?: return@setState this
                val row = current.rows.firstOrNull { it.key == intent.key }
                if (row == null || row.inBar) return@setState this
                val selected = if (intent.key in current.selected) {
                    current.selected - intent.key
                } else {
                    current.selected + intent.key
                }
                copy(review = current.copy(selected = selected))
            }
            ShoppingIntent.ConfirmReview -> finishShopping(addToBar = true)
            ShoppingIntent.SkipReview -> finishShopping(addToBar = false)
            is ShoppingIntent.SetByIngredient -> {
                preferences.byIngredient = intent.byIngredient
                setState { copy(byIngredient = intent.byIngredient) }
            }
            ShoppingIntent.Refresh -> viewModelScope.launch {
                setState { copy(refreshing = true) }
                try {
                    repository.syncShopping()
                    runCatching { recipes.load() }
                } finally {
                    setState { copy(refreshing = false) }
                }
            }
        }
    }

    /**
     * Records the trip, optionally moves the selected ingredients into My Bar, and takes every
     * checked item off the list; the trip keeps the record. Undo reverses all three.
     */
    private fun finishShopping(addToBar: Boolean) {
        val review = currentState.review ?: return
        if (addToBar && !review.canConfirm) return
        val toAdd = if (addToBar) review.toAdd else emptyList()
        val addedKeys = toAdd.mapTo(HashSet()) { it.key }
        if (review.rows.isEmpty()) {
            setState { copy(review = null) }
            return
        }
        val measures = currentState.sections
            .flatMap { section -> section.items.map { it.id to section.measure(it) } }
            .toMap()
        // One trip item per ingredient on the review, the same record iOS writes.
        val tripItems = review.rows.map { row -> tripItem(row, row.key in addedKeys, measures) }
        val removed = review.rows.flatMap { it.items }
        val added = toAdd.map { it.name }
        lastRemoved = removed
        lastAddedToBar = added
        lastTrip = viewModelScope.async { trips.record(tripItems) }
        setState { copy(review = null) }
        sendEffect(
            if (added.isNotEmpty()) {
                ShoppingEffect.ShowUndo(R.plurals.shopping_added_to_bar, added.size)
            } else {
                ShoppingEffect.ShowUndo(R.string.history_saved, count = null)
            },
        )
        finishJob = viewModelScope.launch {
            added.forEach { repository.addToBar(it) }
            removed.forEach { repository.removeShoppingItem(it.id) }
        }
    }

    private fun tripItem(row: ReviewRow, added: Boolean, measures: Map<Long, String>): ShoppingTripItem {
        val drinkIds = row.items.map { it.drinkId }.filter { it.isNotEmpty() }.distinct()
        val itemMeasures = row.items.mapNotNull { measures[it.id]?.trim()?.takeIf { m -> m.isNotEmpty() } }
        return ShoppingTripItem(
            name = row.name,
            drinkId = drinkIds.singleOrNull(),
            drinkName = row.drinkNames.takeIf { it.isNotEmpty() }?.joinToString(" · "),
            measure = itemMeasures.singleOrNull(),
            addedToBar = added,
        )
    }

    private fun loadRecipesIfMissing(sections: List<ShoppingSection>, mine: List<Drink>) {
        if (requestedRecipes) return
        val known = mine.mapTo(HashSet()) { it.id }
        val needed = sections.any { it.drinkId.startsWith(Drink.USER_RECIPE_PREFIX) && it.drinkId !in known }
        if (!needed) return
        requestedRecipes = true
        viewModelScope.launch { runCatching { recipes.load() } }
    }

    private fun removeItems(items: List<ShoppingItemEntity>, messageRes: Int) {
        if (items.isEmpty()) return
        remember(items, messageRes)
        viewModelScope.launch { items.forEach { repository.removeShoppingItem(it.id) } }
    }

    private fun remember(items: List<ShoppingItemEntity>, messageRes: Int) {
        lastRemoved = items
        lastAddedToBar = emptyList()
        lastTrip = null
        sendEffect(ShoppingEffect.ShowUndo(messageRes, items.size))
    }
}
