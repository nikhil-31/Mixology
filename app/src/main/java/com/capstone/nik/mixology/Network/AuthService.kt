package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.AuthTokenResponse
import com.capstone.nik.mixology.Network.remoteModel.LoginRequest
import com.capstone.nik.mixology.Network.remoteModel.SignupRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupRequest): AuthTokenResponse
}
