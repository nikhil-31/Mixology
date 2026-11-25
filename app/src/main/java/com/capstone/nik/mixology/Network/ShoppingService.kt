package com.capstone.nik.mixology.Network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

/** One `/me/shopping` item; `id` is null until the server has stored it. */
data class RemoteShoppingItem(
    val id: Long? = null,
    val name: String = "",
    val checked: Boolean = false,
    @SerializedName("drink_id") val drinkId: String? = null,
)

interface ShoppingService {
    /** The account's list; its `ETag` header is the list's version. */
    @GET("me/shopping")
    suspend fun items(): Response<List<RemoteShoppingItem>>

    /**
     * Replaces the account's whole list and returns it with server ids, in the
     * same order. With [ifMatch] the server answers 412 instead if the list has
     * moved past that version; a null [ifMatch] is left out and always writes.
     */
    @PUT("me/shopping")
    suspend fun replace(
        @Header("If-Match") ifMatch: String?,
        @Body items: List<RemoteShoppingItem>,
    ): Response<List<RemoteShoppingItem>>
}
