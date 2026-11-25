package com.capstone.nik.mixology.ui.shopping

import android.content.Context
import android.content.SharedPreferences
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.repository.BarCategory
import com.capstone.nik.mixology.repository.BarMatcher
import com.capstone.nik.mixology.repository.BarTerm
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** A checked item, shown in the list's bottom "Checked" section with the drink it came from. */
data class CheckedShoppingRow(
    val item: ShoppingItemEntity,
    val drinkName: String?,
    val measure: String,
)

/**
 * Splits the list into drink sections holding only what's still to buy (drinks with nothing left
 * drop out) and the checked items, alphabetical, for the bottom of the list.
 */
fun splitChecked(sections: List<ShoppingSection>): Pair<List<ShoppingSection>, List<CheckedShoppingRow>> {
    val open = sections
        .map { section -> section.copy(items = section.items.filterNot { it.checked }) }
        .filter { it.items.isNotEmpty() }
    val checked = sections
        .flatMap { section ->
            section.items.filter { it.checked }.map { CheckedShoppingRow(it, section.drink?.name, section.measure(it)) }
        }
        .sortedWith(compareBy<CheckedShoppingRow> { normalizeIngredient(it.item.name) }.thenBy { it.drinkName.orEmpty() })
    return open to checked
}

/** One ingredient to buy, merged across every drink on the list that needs it. */
data class CombinedShoppingItem(
    val name: String,
    val items: List<ShoppingItemEntity>,
    val drinkNames: List<String>,
    /** The recipe measure, only when a single drink needs it. */
    val measure: String,
) {
    val key: String get() = normalizeIngredient(name)
    val checked: Boolean get() = items.all { it.checked }
}

internal fun normalizeIngredient(name: String): String =
    name.trim().lowercase().replace(Regex("\\s+"), " ")

/** Merges same-named items (ignoring case and spacing), alphabetical with unchecked rows first. */
fun combineByIngredient(sections: List<ShoppingSection>): List<CombinedShoppingItem> {
    data class Entry(val item: ShoppingItemEntity, val section: ShoppingSection)
    val grouped = LinkedHashMap<String, MutableList<Entry>>()
    sections.forEach { section ->
        section.items.forEach { item ->
            grouped.getOrPut(normalizeIngredient(item.name)) { mutableListOf() }.add(Entry(item, section))
        }
    }
    return grouped.values
        .map { entries ->
            val drinks = entries.mapNotNull { it.section.drink?.name }.distinct()
            CombinedShoppingItem(
                name = entries.first().item.name.trim(),
                items = entries.map { it.item },
                drinkNames = drinks,
                measure = if (entries.size == 1) entries.first().section.measure(entries.first().item) else "",
            )
        }
        .sortedWith(compareBy<CombinedShoppingItem> { it.checked }.thenBy { it.key })
}

/** A checked ingredient on the Done shopping review. */
data class ReviewRow(
    val key: String,
    val name: String,
    val drinkNames: List<String>,
    val items: List<ShoppingItemEntity>,
    /** Already in My Bar, so there's nothing to add; it only leaves the list. */
    val inBar: Boolean,
)

/** The checked items merged by ingredient: ones to add first, then ones already in the bar. */
fun reviewRows(sections: List<ShoppingSection>, bar: Collection<String>): List<ReviewRow> {
    // Same matching as My Bar's recommendations and iOS, so "Lemon" also covers "Lemon vodka".
    val owned = bar.map { BarMatcher.normalize(it) }.filterTo(HashSet()) { it.isNotEmpty() }
    val checkedOnly = sections.map { section -> section.copy(items = section.items.filter { it.checked }) }
        .filter { it.items.isNotEmpty() }
    return combineByIngredient(checkedOnly)
        .map { row ->
            ReviewRow(row.key, row.name, row.drinkNames, row.items, inBar = BarMatcher.inBar(row.name, owned))
        }
        .sortedWith(compareBy<ReviewRow> { it.inBar }.thenBy { it.key })
}

/**
 * The aisle (a [BarCategory], the same shelves as My Bar) an ingredient is bought from. [terms]
 * maps lowercase catalog names to categories. An exact name wins; otherwise the longest catalog
 * name found in it as whole words, so "Fresh lime juice" goes where "Lime juice" does.
 */
fun shoppingCategory(name: String, terms: Map<String, String>): String {
    val key = BarMatcher.normalize(name)
    if (key.isEmpty() || terms.isEmpty()) return BarCategory.OTHER
    terms[key]?.let { return BarCategory.of(it) }
    val best = terms.entries
        .filter { (term, _) -> term.isNotEmpty() && BarMatcher.containsWord(key, term) }
        .maxByOrNull { it.key.length }
    return BarCategory.of(best?.value)
}

/** The catalog's terms as [shoppingCategory] reads them, by normalised name. */
fun shoppingTerms(terms: List<BarTerm>): Map<String, String> =
    terms.filter { it.category != BarCategory.OTHER }
        .associate { BarMatcher.normalize(it.name) to it.category }

/** One aisle of the By ingredient list. */
data class AisleGroup(val category: String, val rows: List<CombinedShoppingItem>)

/** [rows] by aisle, in My Bar's shelf order, A–Z within each; empty aisles are left out. */
fun groupByAisle(rows: List<CombinedShoppingItem>, terms: Map<String, String>): List<AisleGroup> {
    val grouped = rows.groupBy { shoppingCategory(it.name, terms) }
    return BarCategory.all.mapNotNull { category ->
        grouped[category]?.let { AisleGroup(category, it.sortedBy { row -> row.key }) }
    }
}

/**
 * Ids of the unchecked items My Bar already stocks ([bar] holds normalised names), matched as
 * the Done shopping review does. Ice and water are never flagged.
 */
fun ownedOpen(sections: List<ShoppingSection>, bar: Collection<String>): Set<Long> {
    val owned = bar.map { BarMatcher.normalize(it) }.filterTo(HashSet()) { it.isNotEmpty() }
    if (owned.isEmpty()) return emptySet()
    return sections.flatMap { it.items }
        .filter { !it.checked && !BarMatcher.isPantry(it.name) && BarMatcher.inBar(it.name, owned) }
        .mapTo(HashSet()) { it.id }
}

/**
 * The items still to buy as a plain-text checklist, grouped under drink names, or when combined
 * under aisle names (flat when everything is in one aisle). Null when everything is checked.
 */
fun shoppingShareText(
    sections: List<ShoppingSection>,
    byIngredient: Boolean,
    otherLabel: String,
    terms: Map<String, String> = emptyMap(),
    aisleLabel: (String) -> String = { it },
): String? {
    fun line(name: String, measure: String) =
        if (measure.isBlank()) "☐ ${name.trim()}" else "☐ ${name.trim()} (${measure.trim()})"

    if (byIngredient) {
        val open = combineByIngredient(sections).filterNot { it.checked }
        if (open.isEmpty()) return null
        val aisles = groupByAisle(open, terms)
        if (aisles.size == 1) return open.joinToString("\n") { line(it.name, it.measure) }
        return aisles.joinToString("\n\n") { aisle ->
            (listOf(aisleLabel(aisle.category)) + aisle.rows.map { line(it.name, it.measure) }).joinToString("\n")
        }
    }
    val lines = run {
        sections.mapNotNull { section ->
            val open = section.items.filterNot { it.checked }
            if (open.isEmpty()) return@mapNotNull null
            val title = section.drink?.name ?: otherLabel
            (listOf(title) + open.map { line(it.name, section.measure(it)) }).joinToString("\n")
        }
    }
    if (lines.isEmpty()) return null
    return lines.joinToString("\n\n")
}

/** Remembers the By drink / By ingredient choice on this device. */
class ShoppingPreferences private constructor(private val prefs: SharedPreferences?) {

    @Inject
    constructor(@ApplicationContext context: Context) :
        this(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE))

    private var memory = DEFAULT_BY_INGREDIENT

    var byIngredient: Boolean
        get() = prefs?.getBoolean(KEY_BY_INGREDIENT, DEFAULT_BY_INGREDIENT) ?: memory
        set(value) {
            if (prefs != null) prefs.edit().putBoolean(KEY_BY_INGREDIENT, value).apply() else memory = value
        }

    companion object {
        private const val PREFS = "mixology"
        private const val KEY_BY_INGREDIENT = "shopping_by_ingredient"

        /** By ingredient is what you shop from; a choice made on the switch is kept. */
        const val DEFAULT_BY_INGREDIENT = true

        /** For tests and previews: nothing is written to disk. */
        fun inMemory() = ShoppingPreferences(null)
    }
}
