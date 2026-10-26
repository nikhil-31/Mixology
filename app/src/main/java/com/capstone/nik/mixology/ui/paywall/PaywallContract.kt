package com.capstone.nik.mixology.ui.paywall

import android.app.Activity

data class PaywallPlan(
    val productId: String,
    val price: String,
    val yearly: Boolean,
)

/** What the paywall can tell the user, resolved to text by the screen. */
enum class PaywallNotice { PENDING, NOTHING_TO_RESTORE, STORE_UNAVAILABLE, PURCHASE_FAILED }

data class PaywallUiState(
    val plans: List<PaywallPlan> = emptyList(),
    val selected: String? = null,
    val loadingPlans: Boolean = true,
    /** A purchase or restore is in progress. */
    val busy: Boolean = false,
    val notice: PaywallNotice? = null,
    /** True while the trial is still running: the user opened the paywall themselves. */
    val canClose: Boolean = false,
) {
    val canSubscribe: Boolean get() = selected != null && !busy && !loadingPlans
}

sealed interface PaywallIntent {
    data object Load : PaywallIntent
    data class Select(val productId: String) : PaywallIntent
    /** The Play purchase sheet needs the current activity; it isn't kept. */
    data class Subscribe(val activity: Activity) : PaywallIntent
    data object Restore : PaywallIntent
    data object SignOut : PaywallIntent
    data object Close : PaywallIntent
}
