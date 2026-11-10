package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.remoteModel.ApiBarAlmost
import com.capstone.nik.mixology.Network.remoteModel.ApiBarBuyNext
import com.capstone.nik.mixology.Network.remoteModel.ApiBarItem
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.data.Drink

/** [use] is in the bar and can stand in for [missing]; [note] says how the drink changes. */
data class BarSubstitute(
    val missing: String,
    val use: String,
    val note: String,
)

data class BarAlmostDrink(
    val drink: Drink,
    val missing: List<String>,
    /** A stand-in the bar already has, for each missing ingredient that has one. */
    val substitutes: List<BarSubstitute> = emptyList(),
) {
    /** Everything missing has a stand-in in the bar, so the drink can be made tonight. */
    val makeableWithSwaps: Boolean
        get() = missing.isNotEmpty() && missing.all { name -> substituteFor(name) != null }

    fun substituteFor(name: String): BarSubstitute? =
        substitutes.firstOrNull { it.missing.equals(name, ignoreCase = true) }
}

data class BarRecommendations(
    val makeable: List<Drink> = emptyList(),
    val almost: List<BarAlmostDrink> = emptyList(),
    /** The server's "Buy next"; null when it sent none, and [unlocks] works it out instead. */
    val buyNext: List<BarUnlock>? = null,
    /** Each bar ingredient's category ([BarCategory]), by lowercase name; empty when unknown. */
    val categories: Map<String, String> = emptyMap(),
    /** The last load failed, so the drinks here are from before it (or none). */
    val error: Throwable? = null,
) {
    /** What to buy next, best first. */
    val unlocks: List<BarUnlock>
        get() = buyNext ?: topUnlocks()
}

/**
 * An ingredient worth buying: it is the only thing missing from [count] of the "almost"
 * drinks, and one of two things missing from [nearly] more.
 */
data class BarUnlock(
    val ingredient: String,
    val count: Int,
    val nearly: Int = 0,
)

/** The shelves a bar is grouped into, in the order they're shown. */
object BarCategory {
    const val SPIRIT = "spirit"
    const val LIQUEUR = "liqueur"
    const val WINE = "wine"
    const val MIXER = "mixer"
    const val SYRUP = "syrup"
    const val BITTERS = "bitters"
    const val FRESH = "fresh"
    const val OTHER = "other"

    val all = listOf(SPIRIT, LIQUEUR, WINE, MIXER, SYRUP, BITTERS, FRESH, OTHER)

    /** [value] when it is a category this app knows, else [OTHER]. */
    fun of(value: String?): String = value?.takeIf { it in all } ?: OTHER
}

/** One ingredient the picker offers. [usedIn] is how many drinks use it; 0 when unknown. */
data class BarTerm(
    val name: String,
    val category: String = BarCategory.OTHER,
    val usedIn: Int = 0,
)

/**
 * The ingredients that would turn the most "almost" drinks into ones you can make, best first.
 * Names are matched loosely (case and spacing) and keep the first spelling seen.
 */
fun BarRecommendations.topUnlocks(limit: Int = 5): List<BarUnlock> {
    val counts = linkedMapOf<String, Int>()
    val names = mutableMapOf<String, String>()
    almost.filter { it.missing.size == 1 }.forEach { item ->
        val name = item.missing.first()
        val key = BarMatcher.normalize(name)
        if (key.isEmpty() || BarMatcher.isPantry(name)) return@forEach
        counts[key] = (counts[key] ?: 0) + 1
        names.getOrPut(key) { name.trim() }
    }
    return counts.entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .take(limit.coerceAtLeast(0))
        .map { BarUnlock(names[it.key] ?: it.key, it.value) }
}

/** Maps a server recommendations payload (public or signed-in), keeping its order and dropping unusable drinks. */
internal fun barRecommendationsOf(
    makeable: List<ApiDrink>,
    almost: List<ApiBarAlmost>,
    saved: Set<String> = emptySet(),
    items: List<ApiBarItem>? = null,
    buyNext: List<ApiBarBuyNext>? = null,
): BarRecommendations = BarRecommendations(
    makeable = makeable.mapNotNull { drink -> drink.toDrink(saved = drink.id in saved) },
    almost = almost.mapNotNull { item ->
        val drink = item.drink.toDrink(saved = item.drink.id in saved) ?: return@mapNotNull null
        BarAlmostDrink(
            drink = drink,
            missing = item.missing,
            substitutes = item.substitutes.orEmpty()
                .filter { it.missing.isNotBlank() && it.use.isNotBlank() }
                .map { BarSubstitute(it.missing, it.use, it.note) },
        )
    },
    buyNext = buyNext
        ?.filter { it.name.isNotBlank() }
        ?.map { BarUnlock(it.name, it.unlocks, it.nearly) },
    categories = items.orEmpty()
        .filter { it.name.isNotBlank() }
        .associate { it.name.lowercase() to BarCategory.of(it.category) },
)

/** The same recommendations with each drink's saved flag taken from [saved]. */
internal fun BarRecommendations.withSaved(saved: Set<String>): BarRecommendations = copy(
    makeable = makeable.map { it.copy(saved = it.id in saved) },
    almost = almost.map { it.copy(drink = it.drink.copy(saved = it.drink.id in saved)) },
)

object BarMatcher {
    private val pantry = setOf("ice", "ice cubes", "water")
    private val extraSpaces = Regex("\\s+")

    fun normalize(name: String): String = name.trim().lowercase().replace(extraSpaces, " ")

    /**
     * Whether a bar of [normalize]d names stocks the ingredient: by name, or by whole words
     * either way, as the server matches. "Gin" covers "London dry gin" and "Bourbon" covers
     * "Bourbon whiskey", but "Gin" does not cover "Ginger ale".
     */
    fun inBar(ingredient: String, bar: Set<String>): Boolean {
        val needle = normalize(ingredient)
        if (needle.isEmpty()) return true
        return bar.any { owned ->
            owned == needle || containsWord(owned, needle) || containsWord(needle, owned)
        }
    }

    /** Whether [needle] appears in [haystack] with no letter or digit touching either end. */
    internal fun containsWord(haystack: String, needle: String): Boolean {
        if (needle.isEmpty() || haystack.length < needle.length) return false
        var from = haystack.indexOf(needle)
        while (from >= 0) {
            val end = from + needle.length
            val before = from == 0 || !haystack[from - 1].isLetterOrDigit()
            val after = end >= haystack.length || !haystack[end].isLetterOrDigit()
            if (before && after) return true
            from = haystack.indexOf(needle, from + 1)
        }
        return false
    }

    fun isPantry(name: String): Boolean = normalize(name) in pantry

    fun missingIngredients(drink: Drink, bar: Set<String>): List<String> {
        return drink.ingredients
            .map { it.ingredient }
            .filter { it.isNotBlank() && !isPantry(it) && !inBar(it, bar) }
            .distinctBy { normalize(it) }
    }

    fun usesBarIngredient(drink: Drink, bar: Set<String>): Boolean {
        return drink.ingredients.any { measure ->
            measure.ingredient.isNotBlank() &&
                !isPantry(measure.ingredient) &&
                inBar(measure.ingredient, bar)
        }
    }

    fun recommend(drinks: List<Drink>, barNames: List<String>): BarRecommendations {
        val bar = barNames.map(::normalize).filter { it.isNotEmpty() }.toSet()
        if (bar.isEmpty()) return BarRecommendations()
        val makeable = mutableListOf<Drink>()
        val almost = mutableListOf<BarAlmostDrink>()
        drinks.forEach { drink ->
            val realCount = drink.ingredients.count { it.ingredient.isNotBlank() && !isPantry(it.ingredient) }
            if (realCount == 0) return@forEach
            val missing = missingIngredients(drink, bar)
            when {
                missing.isEmpty() -> makeable.add(drink)
                missing.size in 1..2 && usesBarIngredient(drink, bar) ->
                    almost.add(BarAlmostDrink(drink, missing))
            }
        }
        return BarRecommendations(
            makeable = makeable.sortedBy { it.name.lowercase() },
            almost = almost.sortedWith(
                compareBy<BarAlmostDrink> { it.missing.size }.thenBy { it.drink.name.lowercase() },
            ),
        )
    }
}
