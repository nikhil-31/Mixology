package com.capstone.nik.mixology.billing

import com.capstone.nik.mixology.Network.BillingService
import com.capstone.nik.mixology.Network.remoteModel.BillingStatusDto
import com.capstone.nik.mixology.Network.remoteModel.PlayVerifyRequest
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** What the signed-in account may use. */
sealed interface Access {
    /** Not known yet (first launch before `/billing/status` answers). */
    data object Unknown : Access
    data class Trial(val daysLeft: Int) : Access
    data class Subscribed(val productId: String?, val expiresAt: String?) : Access
    /** The trial ended and there is no subscription: the app shows only the paywall. */
    data object Free : Access
}

/**
 * The account's subscription state. The backend decides access; this keeps
 * the app in sync with it and sends store purchases there to be verified.
 */
@Singleton
class SubscriptionRepository @Inject constructor(
    private val billingService: BillingService,
    private val storeBilling: StoreBilling,
    private val sessionStore: SessionStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _access = MutableStateFlow(accessFor(sessionStore.current()?.access))
    val access: StateFlow<Access> = _access.asStateFlow()

    /** Set when the user opens the paywall themselves, e.g. during the trial. */
    private val paywallRequested = MutableStateFlow(false)

    /** True when the paywall must cover the app: no access left, or the user asked for it. */
    val showPaywall: StateFlow<Boolean> = combine(_access, paywallRequested) { access, requested ->
        access is Access.Free || requested
    }.stateIn(scope, SharingStarted.Eagerly, _access.value is Access.Free)

    /** Store product ids to sell, from the backend. */
    var products: List<String> = DEFAULT_PRODUCTS
        private set

    init {
        scope.launch {
            sessionStore.session.collect { session ->
                if (session == null) {
                    _access.value = Access.Unknown
                    paywallRequested.value = false
                }
            }
        }
        scope.launch {
            storeBilling.purchaseUpdates.collect { purchase ->
                runCatching { recordPurchase(purchase) }
            }
        }
    }

    /** Reloads access from the backend; keeps the last known value when offline. */
    suspend fun refresh(): Access {
        if (sessionStore.current() == null) return Access.Unknown
        try {
            apply(billingService.status())
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Offline or a server error: keep what we had.
        }
        return _access.value
    }

    /**
     * On launch and on resume: refresh, and if the trial has ended, quietly
     * re-check this device's Play purchases in case a renewal was missed.
     */
    suspend fun refreshOnLaunch() {
        if (refresh() is Access.Free) {
            runCatching { restore() }
        }
    }

    /** Sends a completed store purchase to the backend, then reloads access. */
    suspend fun recordPurchase(purchase: StorePurchase): Access {
        billingService.verifyPlay(PlayVerifyRequest(purchase.token, purchase.productId))
        return refresh()
    }

    /**
     * Restore purchases: verifies every active subscription the store account
     * owns. Returns true when the account ends up subscribed.
     */
    suspend fun restore(): Boolean {
        for (purchase in storeBilling.ownedPurchases()) {
            try {
                billingService.verifyPlay(PlayVerifyRequest(purchase.token, purchase.productId))
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // One bad purchase (e.g. owned by another account) shouldn't stop the rest.
            }
        }
        return refresh() is Access.Subscribed
    }

    /** The server answered 403 "Subscription required": show the paywall now, then confirm. */
    fun requireSubscription() {
        _access.value = Access.Free
        scope.launch { refresh() }
    }

    fun openPaywall() {
        paywallRequested.value = true
    }

    fun closePaywall() {
        paywallRequested.value = false
    }

    fun accountId(): String = sessionStore.current()?.uid.orEmpty()

    private fun apply(status: BillingStatusDto) {
        val access = when (status.access) {
            "subscribed" -> Access.Subscribed(status.subscription?.productId, status.subscription?.expiresAt)
            "trial" -> Access.Trial(status.trialDaysLeft)
            "free" -> Access.Free
            else -> Access.Unknown
        }
        _access.value = access
        if (access is Access.Subscribed) paywallRequested.value = false
        if (status.products.isNotEmpty()) products = status.products
        sessionStore.updateAccess(status.access)
    }

    private fun accessFor(cached: String?): Access = when (cached) {
        "free" -> Access.Free
        "subscribed" -> Access.Subscribed(null, null)
        "trial" -> Access.Trial(0)
        else -> Access.Unknown
    }

    companion object {
        val DEFAULT_PRODUCTS = listOf("mixology_premium_monthly", "mixology_premium_yearly")
    }
}
