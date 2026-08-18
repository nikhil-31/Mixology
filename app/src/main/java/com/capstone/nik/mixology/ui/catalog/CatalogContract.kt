package com.capstone.nik.mixology.ui.catalog

import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.FilterKind

data class CatalogUiState(
    val loading: Boolean = true,
    val kind: FilterKind = FilterKind.INGREDIENT,
    val terms: List<String> = emptyList(),
    val query: String = "",
    val visibleTerms: List<String> = emptyList(),
)

internal fun filterCatalogTerms(terms: List<String>, query: String): List<String> {
    val needle = query.trim()
    if (needle.isEmpty()) return terms
    return terms.filter { it.contains(needle, ignoreCase = true) }
}

internal fun CatalogUiState.withCatalogTerms(terms: List<String>): CatalogUiState =
    copy(loading = false, terms = terms, visibleTerms = filterCatalogTerms(terms, query))

internal fun CatalogUiState.withQuery(query: String): CatalogUiState =
    copy(query = query, visibleTerms = filterCatalogTerms(terms, query))

sealed interface CatalogIntent {
    data object Load : CatalogIntent
    data class SelectKind(val kind: FilterKind) : CatalogIntent
    data class QueryChanged(val query: String) : CatalogIntent
    data class OpenTerm(val term: String) : CatalogIntent
}

sealed interface CatalogEffect {
    data class OpenFilter(val filter: DrinkFilter) : CatalogEffect
}
