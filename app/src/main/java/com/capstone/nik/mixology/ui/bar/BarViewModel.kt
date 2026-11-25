package com.capstone.nik.mixology.ui.bar

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.ConnectionError
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.connectionError
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.ada.AdaInbox
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
    private val analytics: AnalyticsTracker = AnalyticsTracker.forTests(),
    private val inbox: AdaInbox = AdaInbox(),
) : MviViewModel<BarIntent, BarUiState, BarEffect>(BarUiState()) {

    private var observeJob: Job? = null

    init {
        onIntent(BarIntent.Load)
        viewModelScope.launch {
            networkMonitor.retries.collect {
                repository.retryBarRecommendations()
                refreshCatalog()
            }
        }
    }

    override fun onIntent(intent: BarIntent) {
        when (intent) {
            BarIntent.Load -> load()
            BarIntent.OpenPicker -> setState {
                copy(picking = true, query = "", pickerCategory = null, visibleTerms = filterBarTerms(catalogTerms, ""))
            }
            BarIntent.ClosePicker -> setState {
                copy(picking = false, query = "", pickerCategory = null, visibleTerms = filterBarTerms(catalogTerms, ""))
            }
            is BarIntent.QueryChanged -> setState {
                // A search looks through every category.
                val category = pickerCategory.takeIf { intent.query.isBlank() }
                copy(
                    query = intent.query,
                    pickerCategory = category,
                    visibleTerms = filterBarTerms(catalogTerms, intent.query, category),
                )
            }
            is BarIntent.PickerCategoryChanged -> setState {
                val category = intent.category.takeIf { it != pickerCategory }
                copy(pickerCategory = category, visibleTerms = filterBarTerms(catalogTerms, query, category))
            }
            is BarIntent.ToggleIngredient -> toggleIngredient(intent.name)
            is BarIntent.RemoveIngredient -> removeIngredient(intent.name)
            is BarIntent.AddIngredient -> {
                setState { copy(addChoice = null) }
                viewModelScope.launch { repository.addToBar(intent.name) }
            }
            is BarIntent.ChooseAdd -> setState { copy(addChoice = intent.choice) }
            BarIntent.DismissAddChoice -> setState { copy(addChoice = null) }
            is BarIntent.AddToShopping -> {
                setState { copy(addChoice = null) }
                viewModelScope.launch {
                    val added = repository.addIngredientToShoppingList(intent.name, intent.drinkId)
                    sendEffect(
                        if (added) {
                            BarEffect.ShowAddedToShopping(intent.name)
                        } else {
                            BarEffect.ShowAlreadyOnShopping(intent.name)
                        },
                    )
                }
            }
            BarIntent.Refresh -> refresh()
            is BarIntent.ToggleSaved -> toggleSaved(intent.drink)
            is BarIntent.OpenDrink -> sendEffect(BarEffect.OpenDrink(intent.drink))
            BarIntent.Retry -> {
                setState { copy(error = null) }
                repository.retryBarRecommendations()
                networkMonitor.retry()
            }
            is BarIntent.AskAda -> {
                inbox.post(intent.prompt)
                sendEffect(BarEffect.OpenAda)
            }
            is BarIntent.SendPhotoToAda -> {
                inbox.postPhoto(intent.uri, intent.caption)
                sendEffect(BarEffect.OpenAda)
            }
        }
    }

    private fun load() {
        if (observeJob == null) {
            observeJob = viewModelScope.launch {
                combine(
                    repository.observeBar(),
                    repository.observeBarTerms(),
                    repository.observeBarRecommendations(),
                ) { bar, terms, recommendations ->
                    Triple(bar, terms, recommendations)
                }.collect { (bar, terms, recommendations) ->
                    setState {
                        copy(
                            loading = false,
                            bar = bar,
                            barGroups = groupBar(bar, recommendations.categories, terms),
                            catalogTerms = terms,
                            visibleTerms = filterBarTerms(terms, query, pickerCategory),
                            makeable = recommendations.makeable,
                            almost = recommendations.almost,
                            unlocks = recommendations.unlocks,
                            // Any failure counts, so a server error isn't read as "nothing matches".
                            recommendationsError = recommendations.error?.let {
                                networkMonitor.connectionError(it) ?: ConnectionError.SERVER_UNREACHABLE
                            },
                        )
                    }
                }
            }
        }
        refreshCatalog()
    }

    private fun refreshCatalog() {
        viewModelScope.launch { repository.syncBar() }
        viewModelScope.launch { repository.refreshBarTerms() }
        viewModelScope.launch {
            try {
                repository.refreshCatalogs()
                setState { copy(error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh catalogs", e)
                recordCrash(e)
                setState { copy(error = networkMonitor.connectionError(e)) }
            }
        }
    }

    private fun refresh() {
        if (currentState.refreshing) return
        setState { copy(refreshing = true) }
        viewModelScope.launch {
            try {
                repository.retryBarRecommendations()
                repository.syncBar()
                repository.refreshBarTerms()
                repository.refreshCatalogs()
                setState { copy(error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh bar", e)
                setState { copy(error = networkMonitor.connectionError(e)) }
            } finally {
                setState { copy(refreshing = false) }
            }
        }
    }

    private fun removeIngredient(name: String) {
        val stored = currentState.bar.firstOrNull { it.equals(name, ignoreCase = true) } ?: return
        viewModelScope.launch { repository.removeFromBar(stored) }
        sendEffect(BarEffect.ShowRemoved(stored))
    }

    private fun toggleIngredient(name: String) {
        viewModelScope.launch {
            val stored = currentState.bar.firstOrNull { it.equals(name, ignoreCase = true) }
            if (stored != null) {
                repository.removeFromBar(stored)
                sendEffect(BarEffect.ShowRemoved(stored))
            } else {
                repository.addToBar(name)
            }
        }
    }

    private fun toggleSaved(item: Drink) {
        viewModelScope.launch {
            if (item.saved) {
                repository.unsave(item.id)
                analytics.logSaveDrink(item.id, item.name, saved = false)
            } else {
                repository.save(item)
                analytics.logSaveDrink(item.id, item.name, saved = true)
            }
        }
    }

    companion object {
        private const val TAG = "BarViewModel"
    }
}
