package com.capstone.nik.mixology.ui.model

import com.capstone.nik.mixology.Network.CocktailURLs
import com.capstone.nik.mixology.Network.remoteModel.Drink

data class IngredientMeasure(
    val ingredient: String,
    val measure: String,
) {
    val imageUrl: String
        get() = CocktailURLs.COCKTAIL_INGREDIENTS_URL +
            ingredient.replace(" ", "%20") +
            CocktailURLs.COCKTAIL_INGREDIENT_PNG_SMALL
}

fun Drink.ingredientMeasures(): List<IngredientMeasure> {
    return listOf(
        strIngredient1 to strMeasure1,
        strIngredient2 to strMeasure2,
        strIngredient3 to strMeasure3,
        strIngredient4 to strMeasure4,
        strIngredient5 to strMeasure5,
        strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7,
        strIngredient8 to strMeasure8,
        strIngredient9 to strMeasure9,
        strIngredient10 to strMeasure10,
        strIngredient11 to strMeasure11,
        strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13,
        strIngredient14 to strMeasure14,
        strIngredient15 to strMeasure15,
    ).mapNotNull { (ingredient, measure) ->
        val name = ingredient?.trim().orEmpty()
        if (name.isEmpty()) null else IngredientMeasure(name, measure.orEmpty())
    }
}
