package com.capstone.nik.mixology.Network.remoteModel

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.google.gson.annotations.SerializedName

data class CocktailDbResponse(
    @SerializedName("drinks") val drinks: List<CocktailDbDrink>? = null,
)

data class CocktailDbDrink(
    @SerializedName("idDrink") val idDrink: String? = null,
    @SerializedName("strDrink") val strDrink: String? = null,
    @SerializedName("strVideo") val strVideo: String? = null,
    @SerializedName("strCategory") val strCategory: String? = null,
    @SerializedName("strIBA") val strIBA: String? = null,
    @SerializedName("strAlcoholic") val strAlcoholic: String? = null,
    @SerializedName("strGlass") val strGlass: String? = null,
    @SerializedName("strInstructions") val strInstructions: String? = null,
    @SerializedName("strInstructionsES") val strInstructionsES: String? = null,
    @SerializedName("strInstructionsDE") val strInstructionsDE: String? = null,
    @SerializedName("strInstructionsFR") val strInstructionsFR: String? = null,
    @SerializedName("strInstructionsIT") val strInstructionsIT: String? = null,
    @SerializedName("strDrinkThumb") val strDrinkThumb: String? = null,
    @SerializedName("strIngredient1") val strIngredient1: String? = null,
    @SerializedName("strIngredient2") val strIngredient2: String? = null,
    @SerializedName("strIngredient3") val strIngredient3: String? = null,
    @SerializedName("strIngredient4") val strIngredient4: String? = null,
    @SerializedName("strIngredient5") val strIngredient5: String? = null,
    @SerializedName("strIngredient6") val strIngredient6: String? = null,
    @SerializedName("strIngredient7") val strIngredient7: String? = null,
    @SerializedName("strIngredient8") val strIngredient8: String? = null,
    @SerializedName("strIngredient9") val strIngredient9: String? = null,
    @SerializedName("strIngredient10") val strIngredient10: String? = null,
    @SerializedName("strIngredient11") val strIngredient11: String? = null,
    @SerializedName("strIngredient12") val strIngredient12: String? = null,
    @SerializedName("strIngredient13") val strIngredient13: String? = null,
    @SerializedName("strIngredient14") val strIngredient14: String? = null,
    @SerializedName("strIngredient15") val strIngredient15: String? = null,
    @SerializedName("strMeasure1") val strMeasure1: String? = null,
    @SerializedName("strMeasure2") val strMeasure2: String? = null,
    @SerializedName("strMeasure3") val strMeasure3: String? = null,
    @SerializedName("strMeasure4") val strMeasure4: String? = null,
    @SerializedName("strMeasure5") val strMeasure5: String? = null,
    @SerializedName("strMeasure6") val strMeasure6: String? = null,
    @SerializedName("strMeasure7") val strMeasure7: String? = null,
    @SerializedName("strMeasure8") val strMeasure8: String? = null,
    @SerializedName("strMeasure9") val strMeasure9: String? = null,
    @SerializedName("strMeasure10") val strMeasure10: String? = null,
    @SerializedName("strMeasure11") val strMeasure11: String? = null,
    @SerializedName("strMeasure12") val strMeasure12: String? = null,
    @SerializedName("strMeasure13") val strMeasure13: String? = null,
    @SerializedName("strMeasure14") val strMeasure14: String? = null,
    @SerializedName("strMeasure15") val strMeasure15: String? = null,
) {
    fun hasUsableThumb(): Boolean {
        val thumb = strDrinkThumb
        return !thumb.isNullOrEmpty() && thumb != "null"
    }

    fun ingredientMeasures(): List<IngredientMeasure> {
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

    fun toDrink(saved: Boolean = false): Drink? {
        val id = idDrink?.takeIf { it.isNotBlank() } ?: return null
        return Drink(
            id = id,
            name = strDrink.orEmpty(),
            thumb = strDrinkThumb.orEmpty(),
            saved = saved,
            alcoholic = strAlcoholic,
            glass = strGlass,
            category = strCategory,
            iba = strIBA,
            instructions = localizedInstructions(),
            video = strVideo,
            ingredients = ingredientMeasures(),
        )
    }

    fun localizedInstructions(language: String = java.util.Locale.getDefault().language): String? {
        val localized = when (language.lowercase(java.util.Locale.ROOT)) {
            "es" -> strInstructionsES
            "de" -> strInstructionsDE
            "fr" -> strInstructionsFR
            "it" -> strInstructionsIT
            else -> null
        }
        return localized?.takeIf { it.isNotBlank() } ?: strInstructions
    }
}
