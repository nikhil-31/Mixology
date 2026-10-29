package com.capstone.nik.mixology.billing

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.remoteModel.BillingStatusDto
import com.capstone.nik.mixology.Network.remoteModel.ChatCreditsDto
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
    fun chatCreditsFollowTheBackendAndAdaReplies() = runTest {
        assertNull(repository.chatCredits.value)
        backend.status = BillingStatusDto(
            access = "trial",
            trialDaysLeft = 3,
            chatCredits = ChatCreditsDto(500, 12, 488, "2026-11-01T00:00:00+00:00"),
        )

        repository.refresh()

        val credits = repository.chatCredits.value!!
        assertEquals(488, credits.remaining)
        assertEquals(500, credits.limit)
        assertEquals(1_793_491_200_000L, credits.resetsAt?.time)

        repository.updateChatCredits(ChatCreditsDto(500, 15, 485, "2026-11-01T00:00:00+00:00"))
        assertEquals(485, repository.chatCredits.value?.remaining)

        // A reply without credits (an older server) keeps what we had.
        repository.updateChatCredits(null)
        assertEquals(485, repository.chatCredits.value?.remaining)

        sessions.clear()
        assertNull(repository.chatCredits.value)
    }

    @Test
    fun unmeteredChatHasNoCredits() = runTest {
        backend.status = BillingStatusDto(
            access = "subscribed",
            chatCredits = ChatCreditsDto(unlimited = true, resetsAt = "2026-11-01T00:00:00+00:00"),
        )

        repository.refresh()

        assertNull(repository.chatCredits.value)
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
