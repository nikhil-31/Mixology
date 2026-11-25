package com.capstone.nik.mixology.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/** A subscription plan as the store prices it for this user. */
data class StorePlan(
    val productId: String,
    /** Localized price, e.g. "$4.99". */
    val price: String,
    /** ISO 8601 period: "P1M" for monthly, "P1Y" for yearly. */
    val billingPeriod: String,
)

/** A store purchase the backend still has to verify. */
data class StorePurchase(
    val token: String,
    val productId: String?,
)

sealed interface PurchaseOutcome {
    data class Purchased(val purchase: StorePurchase) : PurchaseOutcome
    /** Paid with a delayed method; it completes later through [StoreBilling.purchaseUpdates]. */
    data object Pending : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data class Failed(val detail: String) : PurchaseOutcome
}

class StoreUnavailableException(message: String) : Exception(message)

/** The app store's purchase API, kept behind an interface so it can be faked in tests. */
interface StoreBilling {
    /** Purchases that complete outside a [purchase] call, such as approved pending payments. */
    val purchaseUpdates: Flow<StorePurchase>

    /** Plans for [productIds]; throws [StoreUnavailableException] when the store can't be reached. */
    suspend fun plans(productIds: List<String>): List<StorePlan>

    /**
     * Shows the store's purchase sheet. [accountId] ties the purchase to the
     * signed-in account, and the backend refuses it for anyone else.
     */
    suspend fun purchase(activity: Activity, productId: String, accountId: String): PurchaseOutcome

    /** Active subscriptions owned by the store account on this device, for restore. */
    suspend fun ownedPurchases(): List<StorePurchase>
}
