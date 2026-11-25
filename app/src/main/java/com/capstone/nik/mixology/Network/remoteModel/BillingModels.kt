package com.capstone.nik.mixology.Network.remoteModel

import com.google.gson.annotations.SerializedName

/** `GET /billing/status`: what the account may use and what to sell. */
data class BillingStatusDto(
    /** `trial`, `subscribed` or `free`; the app shows the paywall for `free`. */
    val access: String = "",
    @SerializedName("trial_days_left") val trialDaysLeft: Int = 0,
    val subscription: SubscriptionDto? = null,
    val products: List<String> = emptyList(),
    /** This month's Ada allowance; missing from servers that don't meter chat. */
    @SerializedName("chat_credits") val chatCredits: ChatCreditsDto? = null,
)

/** Ada chat credits: model tokens used this month, shown as credits. */
data class ChatCreditsDto(
    val limit: Int = 0,
    val used: Int = 0,
    val remaining: Int = 0,
    @SerializedName("resets_at") val resetsAt: String? = null,
    /** Metering is off on the server; the counts mean nothing. */
    val unlimited: Boolean = false,
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
