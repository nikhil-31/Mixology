package com.capstone.nik.mixology.ui.search

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.FilterKind

const val SEARCH_MIN_CHARS = 2
const val SEARCH_SUGGESTION_LIMIT = 10

enum class SearchMode {
    NAME,
    INGREDIENT,
}

data class SearchUiState(
    val query: String = "",
    val mode: SearchMode = SearchMode.NAME,
    val loading: Boolean = false,
    val results: List<Drink> = emptyList(),
    val empty: Boolean = false,
    val listView: Boolean = false,
    val suggestions: List<String> = emptyList(),
    /** The user's own recipes that match, shown above the catalog's results. */
    val myRecipes: List<Drink> = emptyList(),
)

/** Recipes whose name contains [query], or in ingredient mode one of whose ingredients does. */
internal fun filterMyRecipes(recipes: List<Drink>, query: String, mode: SearchMode): List<Drink> {
    val needle = query.trim()
    if (needle.length < SEARCH_MIN_CHARS) return emptyList()
    return recipes.filter { recipe ->
        when (mode) {
            SearchMode.NAME -> recipe.name.contains(needle, ignoreCase = true)
            SearchMode.INGREDIENT -> recipe.ingredients.any { it.ingredient.contains(needle, ignoreCase = true) }
        }
    }
}

internal fun filterIngredientSuggestions(
    terms: List<String>,
    query: String,
    limit: Int = SEARCH_SUGGESTION_LIMIT,
): List<String> {
    val needle = query.trim()
    if (needle.isEmpty()) return emptyList()
    return terms.asSequence()
        .filter { it.contains(needle, ignoreCase = true) }
        .sortedWith(
            compareBy<String> { !it.startsWith(needle, ignoreCase = true) }
                .thenBy { it.lowercase() },
        )
        .take(limit)
        .toList()
}

sealed interface SearchIntent {
    data class Search(
        val query: String,
        val mode: SearchMode? = null,
        val filterKind: FilterKind? = null,
        val commit: Boolean = false,
    ) : SearchIntent
    data class SetMode(val mode: SearchMode) : SearchIntent
    data class SelectSuggestion(val ingredient: String) : SearchIntent
    data class ToggleSaved(val drink: Drink) : SearchIntent
    data class OpenDrink(val drink: Drink) : SearchIntent
    data object ToggleListView : SearchIntent
    data object Back : SearchIntent
}

sealed interface SearchEffect {
    data class ShowMessageRes(val resId: Int) : SearchEffect
    data class OpenDrink(val drink: Drink) : SearchEffect
    data object NavigateBack : SearchEffect
}
