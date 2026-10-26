package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.AuthTokenResponse
import com.capstone.nik.mixology.Network.remoteModel.ProviderLoginRequest
import com.capstone.nik.mixology.Network.remoteModel.LoginRequest
import com.capstone.nik.mixology.Network.remoteModel.SignupRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthTokenResponse

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupRequest): AuthTokenResponse

    /** Exchanges a Firebase ID token from Google sign-in for a backend session. */
    @POST("auth/google")
    suspend fun google(@Body body: ProviderLoginRequest): AuthTokenResponse

    /** Exchanges a Firebase ID token from Sign in with Apple for a backend session. */
    @POST("auth/apple")
    suspend fun apple(@Body body: ProviderLoginRequest): AuthTokenResponse
}
