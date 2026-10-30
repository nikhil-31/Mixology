package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.Network.remoteModel.RecipeImportRequest
import com.capstone.nik.mixology.Network.remoteModel.RecipeImportResponse
import com.capstone.nik.mixology.Network.remoteModel.RecipeWrite
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** The user's own recipes. Each comes back as drink JSON with a `u-` id. */
interface RecipeService {
    /** Newest first. */
    @GET("me/recipes")
    suspend fun list(): List<ApiDrink>

    @POST("me/recipes")
    suspend fun create(@Body body: RecipeWrite): ApiDrink

    @GET("me/recipes/{id}")
    suspend fun get(@Path("id") id: String): ApiDrink

    /** Replaces the recipe's fields; the photo stays. */
    @PUT("me/recipes/{id}")
    suspend fun update(@Path("id") id: String, @Body body: RecipeWrite): ApiDrink

    @DELETE("me/recipes/{id}")
    suspend fun delete(@Path("id") id: String)

    /** [jpeg] is the raw image, sent as `image/jpeg`. */
    @PUT("me/recipes/{id}/photo")
    suspend fun uploadPhoto(@Path("id") id: String, @Body jpeg: RequestBody): ApiDrink

    @DELETE("me/recipes/{id}/photo")
    suspend fun deletePhoto(@Path("id") id: String): ApiDrink

    /** Reads a recipe from a social post, a page or pasted text; 201 when new, 200 when a duplicate. */
    @POST("me/recipes/import")
    suspend fun importRecipe(@Body body: RecipeImportRequest): RecipeImportResponse
}
