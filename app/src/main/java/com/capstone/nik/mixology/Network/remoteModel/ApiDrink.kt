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

/** Something in the bar that can stand in for a missing ingredient, and how the drink changes. */
data class ApiBarSubstitute(
    val missing: String = "",
    val use: String = "",
    val note: String = "",
)

data class ApiBarAlmost(
    val drink: ApiDrink = ApiDrink(),
    val missing: List<String> = emptyList(),
    val substitutes: List<ApiBarSubstitute>? = null,
)

/** A bar ingredient with the shelf it belongs on: spirit, liqueur, wine, mixer, syrup, bitters, fresh or other. */
data class ApiBarItem(
    val name: String = "",
    val category: String = "",
)

/** An ingredient worth buying: [unlocks] drinks need only it, [nearly] need it and one more thing. */
data class ApiBarBuyNext(
    val name: String = "",
    val unlocks: Int = 0,
    val nearly: Int = 0,
)

data class ApiBarRecommendations(
    val ingredients: List<String> = emptyList(),
    val makeable: List<ApiDrink> = emptyList(),
    val almost: List<ApiBarAlmost> = emptyList(),
    val items: List<ApiBarItem>? = null,
    /** Null from a server that doesn't send it yet; the app then works it out from [almost]. */
    @SerializedName("buy_next") val buyNext: List<ApiBarBuyNext>? = null,
)

/** One row of `GET /ingredients`: how many drinks use it and the shelf it belongs on. */
data class ApiIngredient(
    val name: String = "",
    @SerializedName("used_in") val usedIn: Int = 0,
    val category: String = "",
)
