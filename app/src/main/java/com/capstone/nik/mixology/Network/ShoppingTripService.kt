package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.data.ShoppingTripItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** One `/me/shopping/trips` entry. */
data class RemoteShoppingTrip(
    val id: Long = 0,
    /** RFC 3339, set by the server when the trip was recorded. */
    val at: String = "",
    val items: List<ShoppingTripItem> = emptyList(),
)

data class ShoppingTripWrite(val items: List<ShoppingTripItem>)

interface ShoppingTripService {
    /** Newest first. */
    @GET("me/shopping/trips")
    suspend fun trips(): Response<List<RemoteShoppingTrip>>

    @POST("me/shopping/trips")
    suspend fun add(@Body trip: ShoppingTripWrite): Response<RemoteShoppingTrip>

    /** 204, or 404 when the trip is already gone. */
    @DELETE("me/shopping/trips/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}
