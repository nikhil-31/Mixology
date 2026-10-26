package com.capstone.nik.mixology.ui.paywall

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.billing.Access
import com.capstone.nik.mixology.billing.PurchaseOutcome
import com.capstone.nik.mixology.billing.StoreBilling
import com.capstone.nik.mixology.billing.StoreUnavailableException
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptions: SubscriptionRepository,
    private val storeBilling: StoreBilling,
    private val chatRepository: ChatRepository,
) : MviViewModel<PaywallIntent, PaywallUiState, Nothing>(PaywallUiState()) {

    init {
        viewModelScope.launch {
            subscriptions.access.collect { access ->
                setState { copy(canClose = access !is Access.Free) }
            }
        }
        onIntent(PaywallIntent.Load)
    }

    override fun onIntent(intent: PaywallIntent) {
        when (intent) {
            PaywallIntent.Load -> load()
            is PaywallIntent.Select -> setState { copy(selected = intent.productId) }
            is PaywallIntent.Subscribe -> subscribe(intent.activity)
            PaywallIntent.Restore -> restore()
            PaywallIntent.SignOut -> chatRepository.signOut()
            PaywallIntent.Close -> subscriptions.closePaywall()
        }
    }

    private fun load() {
        setState { copy(loadingPlans = true, notice = null) }
        viewModelScope.launch {
            try {
                val plans = storeBilling.plans(subscriptions.products)
                    .map { PaywallPlan(it.productId, it.price, yearly = it.billingPeriod == "P1Y") }
                    .sortedBy { it.yearly }
                setState {
                    copy(
                        plans = plans,
                        // Yearly is the better deal, so it starts selected.
                        selected = selected ?: (plans.firstOrNull { it.yearly } ?: plans.firstOrNull())?.productId,
                        loadingPlans = false,
                        notice = if (plans.isEmpty()) PaywallNotice.STORE_UNAVAILABLE else null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                setState { copy(loadingPlans = false, notice = PaywallNotice.STORE_UNAVAILABLE) }
            }
        }
    }

    private fun subscribe(activity: Activity) {
        val productId = currentState.selected ?: return
        if (!currentState.canSubscribe) return
        setState { copy(busy = true, notice = null) }
        viewModelScope.launch {
            val notice = try {
                when (val outcome = storeBilling.purchase(activity, productId, subscriptions.accountId())) {
                    is PurchaseOutcome.Purchased -> {
                        // On success the paywall disappears as access becomes subscribed.
                        val access = subscriptions.recordPurchase(outcome.purchase)
                        if (access is Access.Subscribed) null else PaywallNotice.PURCHASE_FAILED
                    }
                    PurchaseOutcome.Pending -> PaywallNotice.PENDING
                    PurchaseOutcome.Cancelled -> null
                    is PurchaseOutcome.Failed -> PaywallNotice.PURCHASE_FAILED
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: StoreUnavailableException) {
                PaywallNotice.STORE_UNAVAILABLE
            } catch (_: Exception) {
                PaywallNotice.PURCHASE_FAILED
            }
            setState { copy(busy = false, notice = notice) }
        }
    }

    private fun restore() {
        if (currentState.busy) return
        setState { copy(busy = true, notice = null) }
        viewModelScope.launch {
            val notice = try {
                if (subscriptions.restore()) null else PaywallNotice.NOTHING_TO_RESTORE
            } catch (error: CancellationException) {
                throw error
            } catch (_: StoreUnavailableException) {
                PaywallNotice.STORE_UNAVAILABLE
            } catch (_: Exception) {
                PaywallNotice.NOTHING_TO_RESTORE
            }
            setState { copy(busy = false, notice = notice) }
        }
    }
}
