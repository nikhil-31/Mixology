package com.capstone.nik.mixology.data

import com.capstone.nik.mixology.ui.model.IngredientMeasure

data class Drink(
    val id: String,
    val name: String,
    val thumb: String,
    val saved: Boolean = false,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val iba: String? = null,
    val instructions: String? = null,
    val video: String? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
    val notes: String = "",
) {
    val hasRecipe: Boolean
        get() = !instructions.isNullOrBlank() || ingredients.isNotEmpty()

    fun hasUsableThumb(): Boolean = thumb.isNotEmpty() && thumb != "null"
}
