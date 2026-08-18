package com.capstone.nik.mixology.ui.model

import com.capstone.nik.mixology.Network.CocktailURLs

data class IngredientMeasure(
    val ingredient: String,
    val measure: String,
) {
    val imageUrl: String
        get() = ingredientImageUrl(ingredient)
}

fun ingredientImageUrl(name: String): String =
    CocktailURLs.COCKTAIL_INGREDIENTS_URL +
        name.replace(" ", "%20") +
        CocktailURLs.COCKTAIL_INGREDIENT_PNG_SMALL
