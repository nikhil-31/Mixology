package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.data.Drink
import java.time.LocalDate
import java.time.LocalTime

data class RecipesHomeUiState(
    val mine: List<Drink> = emptyList(),
    val saved: List<Drink> = emptyList(),
    val loading: Boolean = false,
    /** My recipes have come back from the server at least once. */
    val loaded: Boolean = false,
    /** The server's message or the connection problem, or empty for a generic one. */
    val error: String? = null,
) {
    val isEmpty: Boolean get() = mine.isEmpty() && saved.isEmpty()
    val showEmpty: Boolean get() = loaded && !loading && isEmpty && error == null
}

sealed interface RecipesHomeIntent {
    data object Retry : RecipesHomeIntent
    data class OpenDrink(val drink: Drink) : RecipesHomeIntent
    data object AddRecipe : RecipesHomeIntent
    data object SeeAllRecipes : RecipesHomeIntent
    data object SeeAllSaved : RecipesHomeIntent
}

sealed interface RecipesHomeEffect {
    data class OpenDrink(val drink: Drink) : RecipesHomeEffect
    data object OpenEditor : RecipesHomeEffect
    data object OpenMyRecipes : RecipesHomeEffect
    data object OpenSaved : RecipesHomeEffect
}

enum class DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

/** Rows each card shows before "See all". */
internal const val RECIPES_HOME_ROW_LIMIT = 3

/** The drink of the day: steady through a day, the next one tomorrow. */
internal fun featuredDrink(drinks: List<Drink>, date: LocalDate = LocalDate.now()): Drink? {
    if (drinks.isEmpty()) return null
    return drinks[Math.floorMod(date.toEpochDay(), drinks.size.toLong()).toInt()]
}

internal fun dayPart(time: LocalTime = LocalTime.now()): DayPart = when (time.hour) {
    in 5..11 -> DayPart.MORNING
    in 12..16 -> DayPart.AFTERNOON
    in 17..21 -> DayPart.EVENING
    else -> DayPart.NIGHT
}

/** Ingredient names for the featured card, e.g. "Tequila, Lime juice, Triple sec". */
internal fun ingredientSummary(drink: Drink): String =
    drink.ingredients.map { it.ingredient.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
