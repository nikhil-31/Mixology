package com.capstone.nik.mixology.data

import com.capstone.nik.mixology.Network.CocktailURLs
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

    /** A recipe the user wrote. The server won't save, rate or record these as viewed. */
    val isUserRecipe: Boolean
        get() = id.startsWith(USER_RECIPE_PREFIX)

    /** [thumb] as a loadable URL; recipe photos come back relative to the API. */
    val thumbUrl: String?
        get() = resolveThumbUrl(thumb)

    companion object {
        const val USER_RECIPE_PREFIX = "u-"

        /**
         * Absolute thumbs (the catalog's https images) pass through; relative ones
         * such as `recipe-photos/abc.jpg` are resolved against [baseUrl].
         */
        fun resolveThumbUrl(thumb: String?, baseUrl: String = CocktailURLs.BASE_URL): String? {
            val value = thumb?.trim()
            if (value.isNullOrEmpty() || value == "null") return null
            if (value.contains("://")) return value
            return baseUrl.trimEnd('/') + "/" + value.trimStart('/')
        }
    }
}
