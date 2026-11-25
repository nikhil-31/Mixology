package com.capstone.nik.mixology.Network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class RatingDto(
    /** Community average to one decimal; null when nobody has rated the drink. */
    val average: Double? = null,
    val count: Int = 0,
    @SerializedName("my_rating") val myRating: Int? = null,
)

data class RatingWrite(val rating: Int)

interface RatingService {
    @GET("drinks/{id}/rating")
    suspend fun rating(@Path("id") drinkId: String): RatingDto

    @PUT("me/ratings/{id}")
    suspend fun rate(@Path("id") drinkId: String, @Body body: RatingWrite): RatingDto

    @DELETE("me/ratings/{id}")
    suspend fun clear(@Path("id") drinkId: String): RatingDto
}
