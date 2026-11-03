package com.capstone.nik.mixology.Network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

/** The signed-in account itself: its server session, its data and its deletion. */
interface AccountService {
    /** Ends this device's server session, so the token stops working. */
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    /** Everything the account holds, as JSON. */
    @GET("me/export")
    suspend fun export(): ResponseBody

    /** Deletes the account and all its data; every session ends with it. */
    @DELETE("me")
    suspend fun deleteAccount(): Response<Unit>
}
