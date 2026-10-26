package com.capstone.nik.mixology.billing

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.remoteModel.BillingStatusDto
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SubscriptionRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sessions: SessionStore
    private val store = FakeStoreBilling()
    private val backend = FakeBillingService()
    private lateinit var repository: SubscriptionRepository

    @Before
    fun setUp() {
        sessions = SessionStore(ApplicationProvider.getApplicationContext())
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        repository = SubscriptionRepository(backend, store, sessions)
    }

    @Test
    fun cachedAccessIsUsedUntilTheBackendAnswers() {
        assertTrue(repository.access.value is Access.Trial)
        assertFalse(repository.showPaywall.value)
    }

    @Test
    fun endedTrialShowsThePaywallAndIsRemembered() = runTest {
        backend.status = BillingStatusDto(access = "free", products = listOf("mixology_premium_yearly"))

        repository.refresh()

        assertEquals(Access.Free, repository.access.value)
        assertTrue(repository.showPaywall.value)
        assertEquals(listOf("mixology_premium_yearly"), repository.products)
        assertEquals("free", sessions.current()?.access)
    }

    @Test
    fun launchRestoresAMissedRenewalQuietly() = runTest {
        store.owned = listOf(StorePurchase("tok-owned", "mixology_premium_monthly"))

        repository.refreshOnLaunch()

        assertEquals(listOf("tok-owned"), backend.verified)
        assertTrue(repository.access.value is Access.Subscribed)
        assertFalse(repository.showPaywall.value)
    }

    @Test
    fun forbiddenFromTheServerShowsThePaywallRightAway() = runTest {
        backend.status = BillingStatusDto(access = "trial", trialDaysLeft = 3)
        repository.refresh()
        assertFalse(repository.showPaywall.value)

        backend.status = BillingStatusDto(access = "free")
        repository.requireSubscription()

        assertTrue(repository.showPaywall.value)
    }

    @Test
    fun trialUsersCanOpenAndCloseThePaywall() = runTest {
        backend.status = BillingStatusDto(access = "trial", trialDaysLeft = 5)
        repository.refresh()
        assertEquals(Access.Trial(5), repository.access.value)

        repository.openPaywall()
        assertTrue(repository.showPaywall.value)
        repository.closePaywall()
        assertFalse(repository.showPaywall.value)
    }
}
