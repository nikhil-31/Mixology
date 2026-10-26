package com.capstone.nik.mixology.Network.remoteModel

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
)

data class SignupRequest(
    val email: String,
    val password: String,
    @SerializedName("display_name") val displayName: String? = null,
)

data class AuthTokenResponse(
    @SerializedName("id_token") val idToken: String = "",
    @SerializedName("refresh_token") val refreshToken: String = "",
    @SerializedName("expires_in") val expiresIn: Long = 3600,
    val user: AuthUser = AuthUser(),
)

data class AuthUser(
    val uid: String = "",
    val email: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    val access: String = "",
)

data class ApiDetail(
    val detail: String? = null,
)
