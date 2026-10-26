package com.capstone.nik.mixology.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Google Play Billing (Play Billing Library 8). */
@Singleton
class PlayStoreBilling @Inject constructor(
    @ApplicationContext context: Context,
) : StoreBilling {

    private val updates = MutableSharedFlow<StorePurchase>(extraBufferCapacity = 8)
    override val purchaseUpdates: Flow<StorePurchase> = updates.asSharedFlow()

    private var pendingFlow: CompletableDeferred<PurchaseOutcome>? = null
    private val details = mutableMapOf<String, ProductDetails>()

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener { result, purchases -> onPurchasesUpdated(result, purchases.orEmpty()) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans().build())
        .enableAutoServiceReconnection()
        .build()

    override suspend fun plans(productIds: List<String>): List<StorePlan> {
        connect()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                productIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                },
            )
            .build()
        val result = client.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingResponseCode.OK) {
            throw StoreUnavailableException(result.billingResult.debugMessage)
        }
        return result.productDetailsList.orEmpty().mapNotNull { product ->
            details[product.productId] = product
            val offer = baseOffer(product) ?: return@mapNotNull null
            // The last phase is the recurring price, after any intro phases.
            val phase = offer.pricingPhases.pricingPhaseList.lastOrNull() ?: return@mapNotNull null
            StorePlan(product.productId, phase.formattedPrice, phase.billingPeriod)
        }
    }

    override suspend fun purchase(activity: Activity, productId: String, accountId: String): PurchaseOutcome {
        val product = details[productId]
            ?: run {
                plans(listOf(productId))
                details[productId]
            }
            ?: return PurchaseOutcome.Failed("Unknown product $productId")
        val offer = baseOffer(product) ?: return PurchaseOutcome.Failed("No offer for $productId")
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offer.offerToken)
                        .build(),
                ),
            )
            .setObfuscatedAccountId(accountId)
            .build()
        val outcome = CompletableDeferred<PurchaseOutcome>()
        pendingFlow = outcome
        val launch = client.launchBillingFlow(activity, params)
        if (launch.responseCode != BillingResponseCode.OK) {
            pendingFlow = null
            return outcomeFor(launch, emptyList())
        }
        return outcome.await()
    }

    override suspend fun ownedPurchases(): List<StorePurchase> {
        connect()
        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
        )
        if (result.billingResult.responseCode != BillingResponseCode.OK) {
            throw StoreUnavailableException(result.billingResult.debugMessage)
        }
        return result.purchasesList
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .map { it.toStorePurchase() }
    }

    private fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>) {
        val outcome = outcomeFor(result, purchases)
        val waiting = pendingFlow
        pendingFlow = null
        if (waiting != null) {
            waiting.complete(outcome)
        } else if (outcome is PurchaseOutcome.Purchased) {
            // A pending purchase that completed later.
            updates.tryEmit(outcome.purchase)
        }
    }

    private fun outcomeFor(result: BillingResult, purchases: List<Purchase>): PurchaseOutcome {
        return when (result.responseCode) {
            BillingResponseCode.OK -> {
                val purchased = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                when {
                    purchased != null -> PurchaseOutcome.Purchased(purchased.toStorePurchase())
                    purchases.any { it.purchaseState == Purchase.PurchaseState.PENDING } -> PurchaseOutcome.Pending
                    else -> PurchaseOutcome.Failed(result.debugMessage)
                }
            }
            BillingResponseCode.USER_CANCELED -> PurchaseOutcome.Cancelled
            else -> PurchaseOutcome.Failed(result.debugMessage)
        }
    }

    private suspend fun connect() {
        if (client.isReady) return
        val result = suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        if (continuation.isActive) continuation.resume(result)
                    }

                    override fun onBillingServiceDisconnected() {
                        // Auto reconnection handles later calls; fail this one.
                        if (continuation.isActive) {
                            continuation.resume(
                                BillingResult.newBuilder()
                                    .setResponseCode(BillingResponseCode.SERVICE_DISCONNECTED)
                                    .build(),
                            )
                        }
                    }
                },
            )
        }
        if (result.responseCode != BillingResponseCode.OK) {
            throw StoreUnavailableException(result.debugMessage)
        }
    }

    /** The base plan offer: no intro or developer-defined offers. */
    private fun baseOffer(product: ProductDetails): ProductDetails.SubscriptionOfferDetails? {
        val offers = product.subscriptionOfferDetails.orEmpty()
        return offers.firstOrNull { it.offerId == null } ?: offers.firstOrNull()
    }

    private fun Purchase.toStorePurchase() = StorePurchase(purchaseToken, products.firstOrNull())
}
