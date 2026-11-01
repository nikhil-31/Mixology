package com.capstone.nik.mixology.ui.bar

import com.capstone.nik.mixology.Network.ConnectionError
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink
import com.capstone.nik.mixology.repository.BarRecommendations
import com.capstone.nik.mixology.repository.BarUnlock
import com.capstone.nik.mixology.repository.topUnlocks

data class BarUiState(
    val loading: Boolean = true,
    /** Pull-to-refresh is running. */
    val refreshing: Boolean = false,
    val picking: Boolean = false,
    val bar: List<String> = emptyList(),
    val catalogTerms: List<String> = emptyList(),
    val query: String = "",
    val visibleTerms: List<String> = emptyList(),
    val makeable: List<Drink> = emptyList(),
    val almost: List<BarAlmostDrink> = emptyList(),
    /** The ingredient list couldn't be fetched; shown in full only while none is cached. */
    val error: ConnectionError? = null,
) {
    /** The ingredients that would unlock the most "almost" drinks, for the "Buy next" strip. */
    val unlocks: List<BarUnlock>
        get() = BarRecommendations(makeable, almost).topUnlocks()

    /** The bar has ingredients but the server matched no drinks to them. */
    val noMatches: Boolean
        get() = !loading && bar.isNotEmpty() && makeable.isEmpty() && almost.isEmpty()
}

/**
 * The picker's list. With no query, the ingredients already in the bar come first so they're
 * easy to find and untick; [BarPickerTerms.selected] is empty while searching.
 */
data class BarPickerTerms(
    val selected: List<String>,
    val rest: List<String>,
)

internal fun BarUiState.pickerTerms(): BarPickerTerms {
    if (query.isNotBlank()) return BarPickerTerms(emptyList(), visibleTerms)
    val (selected, rest) = visibleTerms.partition { inBar(it) }
    return BarPickerTerms(selected, rest)
}

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
    /** A chip was tapped on the bar; offers an undo. */
    data class RemoveIngredient(val name: String) : BarIntent
    data class AddIngredient(val name: String) : BarIntent
    data object Refresh : BarIntent
    data class ToggleSaved(val drink: Drink) : BarIntent
    data class OpenDrink(val drink: Drink) : BarIntent
    /** Retries every screen waiting on the server, this one included. */
    data object Retry : BarIntent
}

sealed interface BarEffect {
    data class OpenDrink(val drink: Drink) : BarEffect
    data class ShowRemoved(val name: String) : BarEffect
}
