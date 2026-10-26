package com.capstone.nik.mixology.billing

import android.app.Activity
import com.capstone.nik.mixology.Network.BillingService
import com.capstone.nik.mixology.Network.remoteModel.BillingStatusDto
import com.capstone.nik.mixology.Network.remoteModel.PlayVerifyRequest
import com.capstone.nik.mixology.Network.remoteModel.SubscriptionDto
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeStoreBilling : StoreBilling {
    val updates = MutableSharedFlow<StorePurchase>(extraBufferCapacity = 4)
    override val purchaseUpdates = updates

    var plans = listOf(
        StorePlan("mixology_premium_monthly", "$4.99", "P1M"),
        StorePlan("mixology_premium_yearly", "$39.99", "P1Y"),
    )
    var unavailable = false
    var outcome: PurchaseOutcome = PurchaseOutcome.Purchased(StorePurchase("tok-new", "mixology_premium_yearly"))
    var owned = emptyList<StorePurchase>()
    val purchases = mutableListOf<Pair<String, String>>()

    override suspend fun plans(productIds: List<String>): List<StorePlan> {
        if (unavailable) throw StoreUnavailableException("offline")
        return plans.filter { it.productId in productIds }
    }

    override suspend fun purchase(activity: Activity, productId: String, accountId: String): PurchaseOutcome {
        purchases += productId to accountId
        return outcome
    }

    override suspend fun ownedPurchases(): List<StorePurchase> {
        if (unavailable) throw StoreUnavailableException("offline")
        return owned
    }
}

/** A backend that subscribes the account once it verifies a token from [validTokens]. */
class FakeBillingService : BillingService {
    var status = BillingStatusDto(access = "free", products = SubscriptionRepository.DEFAULT_PRODUCTS)
    val validTokens = mutableSetOf("tok-new", "tok-owned")
    val verified = mutableListOf<String>()

    override suspend fun status(): BillingStatusDto = status

    override suspend fun verifyPlay(body: PlayVerifyRequest) {
        verified += body.purchaseToken
        if (body.purchaseToken !in validTokens) error("Purchase not found")
        status = status.copy(
            access = "subscribed",
            subscription = SubscriptionDto("play", "active", body.productId, "2030-01-01T00:00:00+00:00"),
        )
    }
}
