package com.capstone.nik.mixology.Network.remoteModel

import com.capstone.nik.mixology.data.Drink
import com.google.gson.annotations.SerializedName
import com.capstone.nik.mixology.ui.model.IngredientMeasure

data class ApiDrink(
    val id: String = "",
    val name: String = "",
    val thumb: String? = null,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val iba: String? = null,
    val instructions: String? = null,
    val video: String? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
    /** The page or post a user's recipe was imported from. */
    @SerializedName("source_url") val sourceUrl: String? = null,
    /** Its site, such as `instagram`, or the page's host. */
    val source: String? = null,
) {
    fun toDrink(saved: Boolean = false, notes: String = ""): Drink? {
        if (id.isBlank() || name.isBlank()) return null
        return Drink(
            id = id,
            name = name,
            thumb = thumb.orEmpty(),
            saved = saved,
            alcoholic = alcoholic,
            glass = glass,
            category = category,
            iba = iba,
            instructions = instructions,
            video = video,
            ingredients = ingredients,
            notes = notes,
            sourceUrl = sourceUrl?.takeIf { it.isNotBlank() },
            source = source?.takeIf { it.isNotBlank() },
        )
    }
}

data class ApiBarAlmost(
    val drink: ApiDrink = ApiDrink(),
    val missing: List<String> = emptyList(),
)

data class ApiBarRecommendations(
    val ingredients: List<String> = emptyList(),
    val makeable: List<ApiDrink> = emptyList(),
    val almost: List<ApiBarAlmost> = emptyList(),
)
