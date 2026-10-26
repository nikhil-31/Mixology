package com.capstone.nik.mixology.ui.catalog

import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.FilterKind

data class CatalogUiState(
    val loading: Boolean = true,
    val kind: FilterKind = FilterKind.INGREDIENT,
    val terms: List<String> = emptyList(),
    val query: String = "",
    val visibleTerms: List<String> = emptyList(),
    val bar: Set<String> = emptySet(),
    val refreshing: Boolean = false,
)

data class CatalogLetterSection(
    val letter: String,
    val terms: List<String>,
)

internal const val CATALOG_GRID_THRESHOLD = 12

internal fun catalogUsesGrid(termCount: Int): Boolean = termCount in 1..CATALOG_GRID_THRESHOLD

internal fun groupCatalogTermsByLetter(terms: List<String>): List<CatalogLetterSection> {
    val grouped = terms.groupBy { term ->
        term.firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() }?.toString() ?: "#"
    }
    return grouped.toSortedMap(compareBy { if (it == "#") Char.MAX_VALUE.toString() else it })
        .map { (letter, items) -> CatalogLetterSection(letter, items) }
}

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
    data object Refresh : CatalogIntent
}

sealed interface CatalogEffect {
    data class OpenFilter(val filter: DrinkFilter) : CatalogEffect
}
