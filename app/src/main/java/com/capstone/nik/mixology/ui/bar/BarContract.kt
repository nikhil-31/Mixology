package com.capstone.nik.mixology.ui.bar

import android.net.Uri
import com.capstone.nik.mixology.Network.ConnectionError
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink
import com.capstone.nik.mixology.repository.BarCategory
import com.capstone.nik.mixology.repository.BarTerm
import com.capstone.nik.mixology.repository.BarUnlock

data class BarUiState(
    val loading: Boolean = true,
    /** Pull-to-refresh is running. */
    val refreshing: Boolean = false,
    val picking: Boolean = false,
    val bar: List<String> = emptyList(),
    /** [bar] by shelf, in [BarCategory.all] order; shelves with nothing on them are left out. */
    val barGroups: List<BarGroup> = emptyList(),
    val catalogTerms: List<BarTerm> = emptyList(),
    val query: String = "",
    /** The picker shows only this category while set; cleared by typing a query. */
    val pickerCategory: String? = null,
    /** [catalogTerms] that fit [query] and [pickerCategory], best match first. */
    val visibleTerms: List<BarTerm> = emptyList(),
    val makeable: List<Drink> = emptyList(),
    val almost: List<BarAlmostDrink> = emptyList(),
    /** The ingredients worth buying next, best first, for the "Buy next" strip. */
    val unlocks: List<BarUnlock> = emptyList(),
    /** The ingredient list couldn't be fetched; shown in full only while none is cached. */
    val error: ConnectionError? = null,
    /** The drinks couldn't be loaded; [makeable] and [almost] are from before it, or empty. */
    val recommendationsError: ConnectionError? = null,
    /** A missing ingredient was tapped; the sheet asks whether it goes on the bar or the shopping list. */
    val addChoice: BarAddChoice? = null,
) {
    /** The bar has ingredients and the server, when asked, matched no drinks to them. */
    val noMatches: Boolean
        get() = !loading && recommendationsError == null &&
            bar.isNotEmpty() && makeable.isEmpty() && almost.isEmpty()

    /** The drinks couldn't be loaded and there are none from before to show instead. */
    val recommendationsFailed: Boolean
        get() = !loading && recommendationsError != null &&
            bar.isNotEmpty() && makeable.isEmpty() && almost.isEmpty()

    /** Almost-drinks the bar can make tonight by standing in for everything missing. */
    val withSwaps: List<BarAlmostDrink>
        get() = almost.filter { it.makeableWithSwaps }

    /** The rest of the almost-drinks: something has to be bought. */
    val needsShopping: List<BarAlmostDrink>
        get() = almost.filterNot { it.makeableWithSwaps }
}

/** One shelf of the bar: a [BarCategory] and the ingredients on it. */
data class BarGroup(
    val category: String,
    val names: List<String>,
)

/**
 * A missing ingredient waiting on "bar or shopping list?". [drink] is the Almost drink it was
 * tapped on (the shopping item is filed under it); a Buy next card has no drink but [unlockCount]
 * (or, when it finishes nothing alone, [nearlyCount]).
 */
data class BarAddChoice(
    val ingredient: String,
    val drink: Drink? = null,
    val unlockCount: Int = 0,
    val nearlyCount: Int = 0,
)

/** One titled run of picker rows. [category] is null for the plain "All ingredients" list. */
data class BarPickerSection(
    val category: String?,
    val terms: List<BarTerm>,
)

/**
 * The picker's list. With no query: the ingredients already in the bar (easy to find and
 * untick), the most used ones not in it yet, then every ingredient by shelf. While searching
 * it is [results] alone, plus [custom] when the query names nothing in the catalog or the bar.
 */
data class BarPickerTerms(
    val selected: List<BarTerm> = emptyList(),
    val popular: List<BarTerm> = emptyList(),
    val sections: List<BarPickerSection> = emptyList(),
    val results: List<BarTerm> = emptyList(),
    val custom: String? = null,
) {
    val isEmpty: Boolean
        get() = selected.isEmpty() && popular.isEmpty() && sections.all { it.terms.isEmpty() } &&
            results.isEmpty() && custom == null
}

/** How many "Popular" rows the picker shows. */
internal const val BAR_POPULAR_COUNT = 12

internal fun BarUiState.pickerTerms(): BarPickerTerms {
    val needle = query.trim()
    if (needle.isNotEmpty()) {
        val listed = visibleTerms.any { it.name.equals(needle, ignoreCase = true) }
        // An ingredient of the user's own is in the bar but not the catalog: list it, to untick.
        val own = bar.firstOrNull { it.equals(needle, ignoreCase = true) }?.takeIf { !listed }
        return BarPickerTerms(
            results = listOfNotNull(own?.let { BarTerm(it) }) + visibleTerms,
            custom = needle.takeIf { !listed && own == null },
        )
    }
    if (pickerCategory != null) {
        return BarPickerTerms(sections = listOf(BarPickerSection(pickerCategory, visibleTerms)))
    }
    val byName = catalogTerms.associateBy { it.name.lowercase() }
    val rest = visibleTerms.filterNot { inBar(it.name) }
    val selected = bar.map { name -> byName[name.lowercase()] ?: BarTerm(name) }
    val popular = if (rest.any { it.usedIn > 0 }) rest.take(BAR_POPULAR_COUNT) else emptyList()
    val grouped = rest.groupBy { it.category }
    val sections = if (grouped.keys.size <= 1) {
        // No categories to speak of (the plain list): one alphabetical run, as before.
        listOf(BarPickerSection(null, rest.sortedBy { it.name.lowercase() }))
    } else {
        BarCategory.all.mapNotNull { category ->
            grouped[category]?.let { BarPickerSection(category, it) }
        }
    }
    return BarPickerTerms(selected = selected, popular = popular, sections = sections)
}

/** The categories the picker can narrow to: those [catalogTerms] has, when it has more than one. */
internal fun BarUiState.pickerCategories(): List<String> {
    val present = catalogTerms.mapTo(mutableSetOf()) { it.category }
    return if (present.size <= 1) emptyList() else BarCategory.all.filter { it in present }
}

/**
 * [terms] that fit [query] and [category]. Without a query: the most used first. With one:
 * names that start with it, then names with a word that starts with it, then the rest, each
 * by how many drinks use them. Ties go by name.
 */
internal fun filterBarTerms(terms: List<BarTerm>, query: String, category: String? = null): List<BarTerm> {
    val needle = query.trim()
    val byUse = compareByDescending<BarTerm> { it.usedIn }.thenBy { it.name.lowercase() }
    if (needle.isEmpty()) {
        return terms.filter { category == null || it.category == category }.sortedWith(byUse)
    }
    fun rank(term: BarTerm): Int {
        val at = term.name.indexOf(needle, ignoreCase = true)
        return when {
            at == 0 -> 0
            !term.name[at - 1].isLetterOrDigit() -> 1
            else -> 2
        }
    }
    return terms
        .filter { it.name.contains(needle, ignoreCase = true) }
        .sortedWith(compareBy<BarTerm> { rank(it) }.then(byUse))
}

/**
 * [bar] by shelf. A name's category comes from the server's bar ([categories], by lowercase
 * name), else from the picker's catalog, else it is [BarCategory.OTHER].
 */
internal fun groupBar(
    bar: List<String>,
    categories: Map<String, String>,
    terms: List<BarTerm>,
): List<BarGroup> {
    if (bar.isEmpty()) return emptyList()
    val fromCatalog = terms.associate { it.name.lowercase() to it.category }
    val grouped = bar.groupBy { name ->
        val key = name.lowercase()
        categories[key] ?: fromCatalog[key] ?: BarCategory.OTHER
    }
    return BarCategory.all.mapNotNull { category -> grouped[category]?.let { BarGroup(category, it) } }
}

internal fun BarUiState.inBar(name: String): Boolean =
    bar.any { it.equals(name, ignoreCase = true) }

sealed interface BarIntent {
    data object Load : BarIntent
    data object OpenPicker : BarIntent
    data object ClosePicker : BarIntent
    data class QueryChanged(val query: String) : BarIntent
    /** Narrows the picker to one category; null, or the same one again, shows them all. */
    data class PickerCategoryChanged(val category: String?) : BarIntent
    /** A picker row was tapped: adds it, or removes it and offers an undo. */
    data class ToggleIngredient(val name: String) : BarIntent
    /** A chip's remove button was tapped on the bar; offers an undo. */
    data class RemoveIngredient(val name: String) : BarIntent
    data class AddIngredient(val name: String) : BarIntent
    /** Opens the "bar or shopping list?" sheet for a missing ingredient. */
    data class ChooseAdd(val choice: BarAddChoice) : BarIntent
    data object DismissAddChoice : BarIntent
    data class AddToShopping(val name: String, val drinkId: String = "") : BarIntent
    data object Refresh : BarIntent
    data class ToggleSaved(val drink: Drink) : BarIntent
    data class OpenDrink(val drink: Drink) : BarIntent
    /** Retries every screen waiting on the server, this one included. */
    data object Retry : BarIntent
    /** Leaves [prompt] for Ada and opens her tab. */
    data class AskAda(val prompt: String) : BarIntent
    /** Leaves a photo of the shelf for Ada, with [caption] as what's said with it, and opens her tab. */
    data class SendPhotoToAda(val uri: Uri, val caption: String) : BarIntent
}

sealed interface BarEffect {
    data class OpenDrink(val drink: Drink) : BarEffect
    data class ShowRemoved(val name: String) : BarEffect
    data class ShowAddedToShopping(val name: String) : BarEffect
    /** The ingredient was on the shopping list already, so nothing was added. */
    data class ShowAlreadyOnShopping(val name: String) : BarEffect
    data object OpenAda : BarEffect
}
