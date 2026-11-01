package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ApiBarAlmost
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * The account's bar and what it can make: the user's own recipes first, then catalog
 * drinks ranked by taste. Drinks have the same shape as the public recommendations.
 */
data class BarStateDto(
    val ingredients: List<String> = emptyList(),
    val makeable: List<ApiDrink> = emptyList(),
    val almost: List<ApiBarAlmost> = emptyList(),
)

data class BarIngredientsBody(
    val ingredients: List<String>,
)

data class BarIngredientBody(
    val name: String,
)

interface BarService {
    @GET("bar")
    suspend fun bar(): BarStateDto

    /** Replaces the whole bar. */
    @PUT("bar")
    suspend fun replace(@Body body: BarIngredientsBody): BarStateDto

    /** Adds one ingredient; the server ignores case when deduping. */
    @POST("bar/ingredients")
    suspend fun add(@Body body: BarIngredientBody): BarStateDto

    /** Removes one ingredient; 404 when the bar never had it. The name is encoded as one path segment. */
    @DELETE("bar/ingredients/{name}")
    suspend fun remove(@Path("name") name: String): BarStateDto
}
