package com.capstone.nik.mixology.ui.recipes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarRecommendations
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

data class RecipesHomeUiState(
    val mine: List<Drink> = emptyList(),
    val saved: List<Drink> = emptyList(),
    val loading: Boolean = false,
    /** My recipes have come back from the server at least once. */
    val loaded: Boolean = false,
    /** The server's message or the connection problem, or empty for a generic one. */
    val error: String? = null,
    /** What My Bar can make, or nearly make, for the hero carousel. */
    val bar: BarRecommendations = BarRecommendations(),
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

/** Most makeable drinks the hero carousel shows. */
internal const val HERO_MAKEABLE_LIMIT = 8

/** Most "almost" drinks the hero carousel shows, after the makeable ones. */
internal const val HERO_ALMOST_LIMIT = 2

/** One page of the Recipes hero carousel. */
sealed interface HeroSlide {
    val drink: Drink

    data class CanMake(override val drink: Drink) : HeroSlide

    data class Almost(override val drink: Drink, val missing: List<String>) : HeroSlide
}

/**
 * The hero carousel from My Bar: makeable drinks first, then a couple that are one or two
 * ingredients away. Each list starts at a different drink every day, steady within the day.
 */
internal fun heroSlides(recs: BarRecommendations, date: LocalDate = LocalDate.now()): List<HeroSlide> {
    val makeable = rotatedForDay(recs.makeable, date).take(HERO_MAKEABLE_LIMIT).map { HeroSlide.CanMake(it) }
    val almost = rotatedForDay(recs.almost, date).take(HERO_ALMOST_LIMIT).map { HeroSlide.Almost(it.drink, it.missing) }
    return makeable + almost
}

private fun <T> rotatedForDay(items: List<T>, date: LocalDate): List<T> {
    if (items.isEmpty()) return items
    val start = Math.floorMod(date.toEpochDay(), items.size.toLong()).toInt()
    return items.drop(start) + items.take(start)
}

/** "Lime, Mint" for an almost slide: the first one or two missing ingredients. */
internal fun missingSummary(missing: List<String>): String =
    missing.map { it.trim() }.filter { it.isNotEmpty() }.take(2).joinToString(", ")

internal fun dayPart(time: LocalTime = LocalTime.now()): DayPart = when (time.hour) {
    in 5..11 -> DayPart.MORNING
    in 12..16 -> DayPart.AFTERNOON
    in 17..21 -> DayPart.EVENING
    else -> DayPart.NIGHT
}

/** The Recipes tab's top bar title, e.g. "Sunday morning". */
@Composable
internal fun recipesGreeting(): String {
    val locale = Locale.getDefault()
    val weekday = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val greeting = when (dayPart()) {
        DayPart.MORNING -> R.string.recipes_home_greeting_morning
        DayPart.AFTERNOON -> R.string.recipes_home_greeting_afternoon
        DayPart.EVENING -> R.string.recipes_home_greeting_evening
        DayPart.NIGHT -> R.string.recipes_home_greeting_night
    }
    return stringResource(greeting, weekday.replaceFirstChar { it.titlecase(locale) })
}

/** Ingredient names for the featured card, e.g. "Tequila, Lime juice, Triple sec". */
internal fun ingredientSummary(drink: Drink): String =
    drink.ingredients.map { it.ingredient.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
