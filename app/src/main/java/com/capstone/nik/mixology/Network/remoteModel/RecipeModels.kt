package com.capstone.nik.mixology.Network.remoteModel

data class RecipeIngredientWrite(
    val ingredient: String,
    val measure: String? = null,
)

/** The body of a create or update; the photo is uploaded separately. */
data class RecipeWrite(
    val name: String,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val instructions: String? = null,
    val ingredients: List<RecipeIngredientWrite> = emptyList(),
)
