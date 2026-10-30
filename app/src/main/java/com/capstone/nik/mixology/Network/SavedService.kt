package com.capstone.nik.mixology.Network

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

data class SavedSyncDto(
    val saved: List<String> = emptyList(),
)

interface SavedService {
    /** The account's synced state; only `saved` is read here. */
    @GET("sync")
    suspend fun sync(): SavedSyncDto

    @PUT("me/saved/{id}")
    suspend fun save(@Path("id") drinkId: String)

    @DELETE("me/saved/{id}")
    suspend fun unsave(@Path("id") drinkId: String)
}
