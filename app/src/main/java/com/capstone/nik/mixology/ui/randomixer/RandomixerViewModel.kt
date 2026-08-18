package com.capstone.nik.mixology.ui.randomixer

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomixerViewModel @Inject constructor(
    private val repository: DrinkRepository,
    @ApplicationContext private val context: Context,
) : MviViewModel<RandomixerIntent, RandomixerUiState, RandomixerEffect>(RandomixerUiState()) {

    private val deck = ArrayDeque<Drink>()
    private var savedJob: Job? = null
    private var loadJob: Job? = null
    private var lastUndo: UndoAction? = null

    init {
        val hideSaved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(PREF_HIDE_SAVED, false)
        setState { copy(hideSaved = hideSaved) }
        onIntent(RandomixerIntent.Refresh)
    }

    override fun onIntent(intent: RandomixerIntent) {
        when (intent) {
            RandomixerIntent.Refresh -> refresh()
            RandomixerIntent.SwipeSave -> swipeSave()
            RandomixerIntent.SwipeDiscard -> swipeDiscard()
            RandomixerIntent.ToggleHideSaved -> toggleHideSaved()
            RandomixerIntent.Undo -> undo()
        }
    }

    private fun swipeSave() {
        val drink = currentState.drink ?: return
        if (currentState.loading) return
        viewModelScope.launch {
            try {
                repository.save(drink)
                lastUndo = UndoAction.Saved(drink)
                sendEffect(RandomixerEffect.ShowUndo(R.string.drink_added))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save drink", e)
                recordCrash(e)
            }
            refresh(avoidId = drink.id)
        }
    }

    private fun swipeDiscard() {
        val drink = currentState.drink ?: return
        if (currentState.loading) return
        lastUndo = UndoAction.Discarded(drink)
        sendEffect(RandomixerEffect.ShowUndo(R.string.randomixer_skipped))
        refresh(avoidId = drink.id)
    }

    private fun toggleHideSaved() {
        val hideSaved = !currentState.hideSaved
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_HIDE_SAVED, hideSaved)
            .apply()
        setState { copy(hideSaved = hideSaved) }
        if (hideSaved && currentState.saved) {
            refresh(avoidId = currentState.drink?.id)
        } else {
            viewModelScope.launch { refillDeck(currentState.drink?.id) }
        }
    }

    private fun undo() {
        val action = lastUndo ?: return
        lastUndo = null
        loadJob?.cancel()
        val restored = when (action) {
            is UndoAction.Saved -> {
                viewModelScope.launch {
                    try {
                        repository.unsave(action.drink.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to undo save", e)
                        recordCrash(e)
                    }
                }
                action.drink
            }
            is UndoAction.Discarded -> action.drink
        }
        currentState.drink?.let { current ->
            if (current.id != restored.id) {
                deck.addFirst(current)
            }
        }
        showDrink(restored)
    }

    private fun refresh(avoidId: String? = null) {
        loadJob?.cancel()
        savedJob?.cancel()
        loadJob = viewModelScope.launch {
            setState { copy(loading = true, drink = null, saved = false) }
            try {
                val drink = nextDrink(avoidId)
                if (drink != null) {
                    showDrink(drink)
                } else {
                    setState { copy(loading = false) }
                    sendEffect(RandomixerEffect.ShowMessageRes(R.string.network_error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load random drink", e)
                recordCrash(e)
                setState { copy(loading = false) }
                sendEffect(RandomixerEffect.ShowMessageRes(R.string.network_error))
            }
        }
    }

    private fun showDrink(drink: Drink) {
        savedJob?.cancel()
        savedJob = viewModelScope.launch {
            repository.observeSavedIds().collect { ids ->
                setState { copy(saved = drink.id in ids) }
            }
        }
        setState {
            copy(
                loading = false,
                drink = drink,
                saved = drink.saved,
            )
        }
        viewModelScope.launch { refillDeck(drink.id) }
    }

    private suspend fun nextDrink(avoidId: String?): Drink? {
        refillDeck(avoidId)
        while (deck.isNotEmpty() && deck.first().id == avoidId) {
            deck.removeFirst()
        }
        if (deck.isEmpty()) {
            refillDeck(avoidId)
        }
        return deck.removeFirstOrNull()
    }

    private suspend fun refillDeck(currentId: String?) {
        val savedIds = if (currentState.hideSaved) {
            repository.observeSavedIds().first()
        } else {
            emptySet()
        }
        val seen = deck.map { it.id }.toMutableSet()
        currentId?.let { seen.add(it) }
        var attempts = 0
        while (deck.size < DECK_SIZE && attempts < DECK_SIZE * 3) {
            attempts++
            val drink = repository.randomDrink() ?: break
            if (drink.id in seen) continue
            if (currentState.hideSaved && drink.id in savedIds) continue
            seen.add(drink.id)
            deck.addLast(drink)
        }
    }

    private sealed interface UndoAction {
        val drink: Drink
        data class Saved(override val drink: Drink) : UndoAction
        data class Discarded(override val drink: Drink) : UndoAction
    }

    companion object {
        private const val TAG = "RandomixerViewModel"
        private const val DECK_SIZE = 8
        private const val PREFS = "mixology"
        private const val PREF_HIDE_SAVED = "randomixer_hide_saved"
    }
}
