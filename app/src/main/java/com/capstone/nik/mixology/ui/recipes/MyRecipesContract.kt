package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.data.Drink

data class MyRecipesUiState(
    val recipes: List<Drink> = emptyList(),
    val loading: Boolean = false,
    /** The list has come back from the server at least once. */
    val loaded: Boolean = false,
    val listView: Boolean = false,
    /** The server's message or the connection problem, or empty for a generic one. */
    val error: String? = null,
    val query: String = "",
) {
    val showEmpty: Boolean get() = loaded && !loading && recipes.isEmpty() && error == null

    /** The recipes the search leaves; all of them when the query is blank. */
    val visible: List<Drink> get() = filterRecipes(recipes, query)

    /** There are recipes, but none match the search. */
    val noMatches: Boolean get() = recipes.isNotEmpty() && visible.isEmpty()
}

/** Recipes whose name or any ingredient contains [query], ignoring case. */
internal fun filterRecipes(recipes: List<Drink>, query: String): List<Drink> {
    val needle = query.trim()
    if (needle.isEmpty()) return recipes
    return recipes.filter { recipe ->
        recipe.name.contains(needle, ignoreCase = true) ||
            recipe.ingredients.any { it.ingredient.contains(needle, ignoreCase = true) }
    }
}

sealed interface MyRecipesIntent {
    data object Retry : MyRecipesIntent
    data class QueryChanged(val query: String) : MyRecipesIntent
    data class OpenRecipe(val recipe: Drink) : MyRecipesIntent
    data object AddRecipe : MyRecipesIntent
    data object ToggleListView : MyRecipesIntent
}

sealed interface MyRecipesEffect {
    data class OpenRecipe(val recipe: Drink) : MyRecipesEffect
    data object OpenEditor : MyRecipesEffect
}
