package com.capstone.nik.mixology.ui.model

import com.capstone.nik.mixology.Network.CocktailURLs

data class IngredientMeasure(
    val ingredient: String,
    val measure: String,
) {
    val imageUrl: String
        get() = CocktailURLs.COCKTAIL_INGREDIENTS_URL +
            ingredient.replace(" ", "%20") +
            CocktailURLs.COCKTAIL_INGREDIENT_PNG_SMALL
}
