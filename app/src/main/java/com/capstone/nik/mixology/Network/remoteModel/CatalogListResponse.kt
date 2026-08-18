package com.capstone.nik.mixology.Network.remoteModel

import com.google.gson.annotations.SerializedName

data class CatalogListResponse(
    @SerializedName("drinks") val drinks: List<CatalogListItem>? = null,
)

data class CatalogListItem(
    @SerializedName("strCategory") val strCategory: String? = null,
    @SerializedName("strGlass") val strGlass: String? = null,
    @SerializedName("strIngredient1") val strIngredient1: String? = null,
    @SerializedName("strAlcoholic") val strAlcoholic: String? = null,
) {
    fun term(): String? = listOf(strCategory, strGlass, strIngredient1, strAlcoholic)
        .firstOrNull { !it.isNullOrBlank() }
        ?.trim()
}
