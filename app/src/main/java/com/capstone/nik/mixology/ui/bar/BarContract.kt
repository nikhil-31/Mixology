package com.capstone.nik.mixology.ui.bar

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink

data class BarUiState(
    val loading: Boolean = true,
    val picking: Boolean = false,
    val bar: List<String> = emptyList(),
    val catalogTerms: List<String> = emptyList(),
    val query: String = "",
    val visibleTerms: List<String> = emptyList(),
    val makeable: List<Drink> = emptyList(),
    val almost: List<BarAlmostDrink> = emptyList(),
)

internal fun filterBarTerms(terms: List<String>, query: String): List<String> {
    val needle = query.trim()
    if (needle.isEmpty()) return terms
    return terms.filter { it.contains(needle, ignoreCase = true) }
}

internal fun BarUiState.inBar(name: String): Boolean =
    bar.any { it.equals(name, ignoreCase = true) }

sealed interface BarIntent {
    data object Load : BarIntent
    data object OpenPicker : BarIntent
    data object ClosePicker : BarIntent
    data class QueryChanged(val query: String) : BarIntent
    data class ToggleIngredient(val name: String) : BarIntent
    data class ToggleSaved(val drink: Drink) : BarIntent
    data class OpenDrink(val drink: Drink) : BarIntent
}

sealed interface BarEffect {
    data class OpenDrink(val drink: Drink) : BarEffect
}
