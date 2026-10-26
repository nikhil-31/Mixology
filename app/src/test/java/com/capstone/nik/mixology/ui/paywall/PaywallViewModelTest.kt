package com.capstone.nik.mixology.ui.paywall

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.billing.Access
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.PurchaseOutcome
import com.capstone.nik.mixology.billing.StorePurchase
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val store = FakeStoreBilling()
    private val backend = FakeBillingService()
    private val chat = FakeChatRepository()
    private lateinit var subscriptions: SubscriptionRepository
    private val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    @Before
    fun setUp() {
        val sessions = SessionStore(ApplicationProvider.getApplicationContext())
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "free")
        subscriptions = SubscriptionRepository(backend, store, sessions)
    }

    private fun viewModel() = PaywallViewModel(subscriptions, store, chat)

    @Test
    fun plansLoadWithYearlySelectedAndThePaywallCannotBeClosed() {
        val state = viewModel().state.value

        assertEquals(listOf("mixology_premium_monthly", "mixology_premium_yearly"), state.plans.map { it.productId })
        assertTrue(state.plans.last().yearly)
        assertEquals("mixology_premium_yearly", state.selected)
        assertFalse(state.canClose)
        assertTrue(state.canSubscribe)
    }

    @Test
    fun subscribingVerifiesWithTheBackendAndUnlocksTheApp() {
        val viewModel = viewModel()
        viewModel.onIntent(PaywallIntent.Select("mixology_premium_monthly"))

        viewModel.onIntent(PaywallIntent.Subscribe(activity))

        assertEquals(listOf("mixology_premium_monthly" to "uid-1"), store.purchases)
        assertEquals(listOf("tok-new"), backend.verified)
        assertTrue(subscriptions.access.value is Access.Subscribed)
        assertFalse(subscriptions.showPaywall.value)
        assertNull(viewModel.state.value.notice)
    }

    @Test
    fun pendingAndFailedPurchasesExplainThemselves() {
        val viewModel = viewModel()
        store.outcome = PurchaseOutcome.Pending
        viewModel.onIntent(PaywallIntent.Subscribe(activity))
        assertEquals(PaywallNotice.PENDING, viewModel.state.value.notice)

        store.outcome = PurchaseOutcome.Cancelled
        viewModel.onIntent(PaywallIntent.Subscribe(activity))
        assertNull(viewModel.state.value.notice)

        // A purchase the backend refuses (e.g. another account's) does not unlock.
        store.outcome = PurchaseOutcome.Purchased(StorePurchase("tok-foreign", "mixology_premium_yearly"))
        viewModel.onIntent(PaywallIntent.Subscribe(activity))
        assertEquals(PaywallNotice.PURCHASE_FAILED, viewModel.state.value.notice)
        assertTrue(subscriptions.showPaywall.value)
    }

    @Test
    fun restoreFindsOwnedSubscriptionsOrSaysThereAreNone() {
        val viewModel = viewModel()
        viewModel.onIntent(PaywallIntent.Restore)
        assertEquals(PaywallNotice.NOTHING_TO_RESTORE, viewModel.state.value.notice)

        store.owned = listOf(StorePurchase("tok-owned", "mixology_premium_yearly"))
        viewModel.onIntent(PaywallIntent.Restore)
        assertNull(viewModel.state.value.notice)
        assertFalse(subscriptions.showPaywall.value)
    }

    @Test
    fun storeOutageOffersARetry() {
        store.unavailable = true
        val viewModel = viewModel()
        assertEquals(PaywallNotice.STORE_UNAVAILABLE, viewModel.state.value.notice)
        assertTrue(viewModel.state.value.plans.isEmpty())

        store.unavailable = false
        viewModel.onIntent(PaywallIntent.Load)
        assertEquals(2, viewModel.state.value.plans.size)
    }

    @Test
    fun signOutLeavesThePaywall() {
        viewModel().onIntent(PaywallIntent.SignOut)
        assertTrue(chat.signedOut)
    }
}
