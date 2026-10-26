package com.capstone.nik.mixology.Network.remoteModel

import com.google.gson.annotations.SerializedName

/** `GET /billing/status`: what the account may use and what to sell. */
data class BillingStatusDto(
    /** `trial`, `subscribed` or `free`; the app shows the paywall for `free`. */
    val access: String = "",
    @SerializedName("trial_days_left") val trialDaysLeft: Int = 0,
    val subscription: SubscriptionDto? = null,
    val products: List<String> = emptyList(),
)

data class SubscriptionDto(
    val store: String = "",
    val status: String = "",
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
)

data class PlayVerifyRequest(
    @SerializedName("purchase_token") val purchaseToken: String,
    @SerializedName("product_id") val productId: String? = null,
)
