package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.CatalogListResponse
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CocktailService {

    @GET("filter.php")
    suspend fun getAlcoholFilter(@Query("a") filter: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun getGlassFilter(@Query("g") filter: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun getIngredientFilter(@Query("i") filter: String): CocktailDbResponse

    @GET("filter.php")
    suspend fun getDrinkTypeFilter(@Query("c") filter: String): CocktailDbResponse

    @GET("search.php")
    suspend fun getSearchResults(@Query("s") search: String): CocktailDbResponse

    @GET("random.php")
    suspend fun getRandomixer(): CocktailDbResponse

    @GET("lookup.php")
    suspend fun getDrinkById(@Query("i") id: String): CocktailDbResponse

    @GET("search.php")
    suspend fun getSearchByLetter(@Query("f") letter: String): CocktailDbResponse

    @GET("list.php")
    suspend fun listCategories(@Query("c") list: String = "list"): CatalogListResponse

    @GET("list.php")
    suspend fun listGlasses(@Query("g") list: String = "list"): CatalogListResponse

    @GET("list.php")
    suspend fun listIngredients(@Query("i") list: String = "list"): CatalogListResponse

    @GET("list.php")
    suspend fun listAlcoholic(@Query("a") list: String = "list"): CatalogListResponse
}
