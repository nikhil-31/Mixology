package com.capstone.nik.mixology.ui.shopping

import android.content.Context
import android.content.SharedPreferences
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Unchecked items first, then checked ones; each group keeps its incoming (recipe) order. */
fun sortCheckedLast(items: List<ShoppingItemEntity>): List<ShoppingItemEntity> =
    items.filterNot { it.checked } + items.filter { it.checked }

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

/**
 * The items still to buy as a plain-text checklist, grouped under drink names (or flat when
 * combined). Null when everything is checked.
 */
fun shoppingShareText(
    sections: List<ShoppingSection>,
    byIngredient: Boolean,
    otherLabel: String,
): String? {
    fun line(name: String, measure: String) =
        if (measure.isBlank()) "☐ ${name.trim()}" else "☐ ${name.trim()} (${measure.trim()})"

    val lines = if (byIngredient) {
        combineByIngredient(sections)
            .filterNot { it.checked }
            .map { row -> line(row.name, row.measure) }
    } else {
        sections.mapNotNull { section ->
            val open = section.items.filterNot { it.checked }
            if (open.isEmpty()) return@mapNotNull null
            val title = section.drink?.name ?: otherLabel
            (listOf(title) + open.map { line(it.name, section.measure(it)) }).joinToString("\n")
        }
    }
    if (lines.isEmpty()) return null
    return lines.joinToString(if (byIngredient) "\n" else "\n\n")
}

/** Remembers the By drink / By ingredient choice on this device. */
class ShoppingPreferences private constructor(private val prefs: SharedPreferences?) {

    @Inject
    constructor(@ApplicationContext context: Context) :
        this(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE))

    private var memory = false

    var byIngredient: Boolean
        get() = prefs?.getBoolean(KEY_BY_INGREDIENT, false) ?: memory
        set(value) {
            if (prefs != null) prefs.edit().putBoolean(KEY_BY_INGREDIENT, value).apply() else memory = value
        }

    companion object {
        private const val PREFS = "mixology"
        private const val KEY_BY_INGREDIENT = "shopping_by_ingredient"

        /** For tests and previews: nothing is written to disk. */
        fun inMemory() = ShoppingPreferences(null)
    }
}
