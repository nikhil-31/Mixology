package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.data.Drink

data class BarAlmostDrink(
    val drink: Drink,
    val missing: List<String>,
)

data class BarRecommendations(
    val makeable: List<Drink> = emptyList(),
    val almost: List<BarAlmostDrink> = emptyList(),
)

object BarMatcher {
    private val pantry = setOf("ice", "ice cubes", "water")
    private val extraSpaces = Regex("\\s+")

    fun normalize(name: String): String = name.trim().lowercase().replace(extraSpaces, " ")

    fun inBar(ingredient: String, bar: Set<String>): Boolean {
        val needle = normalize(ingredient)
        if (needle.isEmpty()) return true
        return bar.any { owned -> owned == needle || owned.contains(needle) || needle.contains(owned) }
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
