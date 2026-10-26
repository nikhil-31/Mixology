package com.capstone.nik.mixology.Network.remoteModel

import com.capstone.nik.mixology.data.Drink
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
