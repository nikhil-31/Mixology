package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.BillingStatusDto
import com.capstone.nik.mixology.Network.remoteModel.PlayVerifyRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BillingService {
    @GET("billing/status")
    suspend fun status(): BillingStatusDto

    /** Records a Play purchase on the account after the backend checks it with Google. */
    @POST("billing/play/verify")
    suspend fun verifyPlay(@Body body: PlayVerifyRequest)
}
