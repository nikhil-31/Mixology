package com.capstone.nik.mixology.ui.ada

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.components.DrinkViewPreferences
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdaMoreViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val drinkRepository: DrinkRepository,
    @ApplicationContext private val context: Context,
) : MviViewModel<AdaMoreIntent, AdaMoreUiState, AdaMoreEffect>(AdaMoreUiState()) {

    /** The request the list was opened for; [AdaMoreUiState.query] shows what it searched. */
    private var request: String = ""
    private var exclude: List<String> = emptyList()
    /** Drinks as the server sent them; saved flags are applied on publish. */
    private var loaded: List<Drink> = emptyList()
    private var savedIds: Set<String> = emptySet()
    private var pageJob: Job? = null

    init {
        setState { copy(listView = DrinkViewPreferences.listView(context)) }
        viewModelScope.launch {
            drinkRepository.observeSavedIds().collect { ids ->
                savedIds = ids
                publish()
            }
        }
    }

    override fun onIntent(intent: AdaMoreIntent) {
        when (intent) {
            is AdaMoreIntent.Bind -> bind(intent.query, intent.exclude)
            AdaMoreIntent.LoadMore -> loadPage()
            AdaMoreIntent.Retry -> {
                setState { copy(error = null) }
                loadPage()
            }
            is AdaMoreIntent.OpenDrink -> sendEffect(AdaMoreEffect.OpenDrink(intent.drink))
            is AdaMoreIntent.ToggleSaved -> toggleSaved(intent.drink)
            AdaMoreIntent.ToggleListView -> toggleListView()
        }
    }

    private fun bind(query: String, exclude: List<String>) {
        if (query == request && exclude == this.exclude) return
        pageJob?.cancel()
        request = query
        this.exclude = exclude
        loaded = emptyList()
        setState { copy(query = query, drinks = emptyList(), hasMore = true, loading = false, error = null) }
        loadPage()
    }

    private fun loadPage() {
        val state = currentState
        if (request.isEmpty() || state.loading || !state.hasMore || state.error != null) return
        setState { copy(loading = true) }
        pageJob = viewModelScope.launch {
            try {
                val page = chatRepository.matches(request, exclude, offset = loaded.size)
                val seen = loaded.mapTo(HashSet()) { it.id }
                loaded = loaded + page.drinks.filter { seen.add(it.id) }
                publish()
                setState {
                    copy(
                        query = page.query,
                        loading = false,
                        hasMore = page.hasMore && page.drinks.isNotEmpty(),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(loading = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun publish() {
        val ids = savedIds
        setState { copy(drinks = loaded.map { it.copy(saved = it.id in ids) }) }
    }

    private fun toggleSaved(drink: Drink) {
        viewModelScope.launch {
            try {
                if (drink.saved) drinkRepository.unsave(drink.id) else drinkRepository.save(drink)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(error = error.message.orEmpty()) }
            }
        }
    }

    private fun toggleListView() {
        val listView = !currentState.listView
        DrinkViewPreferences.setListView(context, listView)
        setState { copy(listView = listView) }
    }
}
