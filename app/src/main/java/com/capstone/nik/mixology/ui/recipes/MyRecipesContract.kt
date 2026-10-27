package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.data.Drink

data class MyRecipesUiState(
    val recipes: List<Drink> = emptyList(),
    val loading: Boolean = false,
    /** The list has come back from the server at least once. */
    val loaded: Boolean = false,
    val listView: Boolean = false,
    /** The server's message, or empty for a generic one. */
    val error: String? = null,
) {
    val showEmpty: Boolean get() = loaded && !loading && recipes.isEmpty() && error == null
}

sealed interface MyRecipesIntent {
    data object Retry : MyRecipesIntent
    data class OpenRecipe(val recipe: Drink) : MyRecipesIntent
    data object AddRecipe : MyRecipesIntent
    data object ToggleListView : MyRecipesIntent
}

sealed interface MyRecipesEffect {
    data class OpenRecipe(val recipe: Drink) : MyRecipesEffect
    data object OpenEditor : MyRecipesEffect
}
