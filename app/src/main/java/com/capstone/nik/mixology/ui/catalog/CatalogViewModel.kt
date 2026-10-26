package com.capstone.nik.mixology.ui.catalog

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.recordCrash
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
) : MviViewModel<CatalogIntent, CatalogUiState, CatalogEffect>(CatalogUiState()) {

    private var observeJob: Job? = null

    init {
        onIntent(CatalogIntent.Load)
        viewModelScope.launch {
            networkMonitor.retries.collect { refresh() }
        }
    }

    override fun onIntent(intent: CatalogIntent) {
        when (intent) {
            CatalogIntent.Load -> {
                observe(currentState.kind)
                refresh()
            }
            is CatalogIntent.SelectKind -> {
                if (currentState.kind == intent.kind) return
                setState {
                    copy(
                        kind = intent.kind,
                        query = "",
                        terms = emptyList(),
                        visibleTerms = emptyList(),
                        loading = true,
                    )
                }
                observe(intent.kind)
            }
            is CatalogIntent.QueryChanged -> setState { withQuery(intent.query) }
            is CatalogIntent.OpenTerm -> sendEffect(
                CatalogEffect.OpenFilter(DrinkFilter.dynamic(currentState.kind, intent.term)),
            )
            CatalogIntent.Refresh -> refresh(fromUser = true)
        }
    }

    private fun observe(kind: FilterKind) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(repository.observeCatalog(kind), repository.observeBar()) { terms, bar ->
                terms to bar.map { it.lowercase() }.toSet()
            }.collect { (terms, bar) ->
                setState {
                    if (this.kind != kind) {
                        this
                    } else {
                        withCatalogTerms(terms).copy(bar = bar)
                    }
                }
            }
        }
    }

    private fun refresh(fromUser: Boolean = false) {
        viewModelScope.launch {
            if (fromUser) setState { copy(refreshing = true) }
            try {
                repository.refreshCatalogs()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh catalogs", e)
                recordCrash(e)
                setState { copy(loading = false) }
            } finally {
                if (fromUser) setState { copy(refreshing = false) }
            }
        }
    }

    companion object {
        private const val TAG = "CatalogViewModel"
    }
}