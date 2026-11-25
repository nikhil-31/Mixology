package com.capstone.nik.mixology.ui.recipes

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeRecipeService
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.billing.FakeBillingService
import com.capstone.nik.mixology.billing.FakeStoreBilling
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.NetworkRecipeRepository
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
class MyRecipesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val service = FakeRecipeService()
    private lateinit var repository: NetworkRecipeRepository

    @Before
    fun setUp() {
        val sessions = SessionStore(context)
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1", access = "trial")
        val subscriptions = SubscriptionRepository(FakeBillingService(), FakeStoreBilling(), sessions)
        repository = NetworkRecipeRepository(service, subscriptions, sessions)
    }

    @Test
    fun offlineLoad_showsTheOfflineMessageThenRetryLoads() = runTest {
        service.failWithIo = true
        val viewModel = MyRecipesViewModel(repository, NetworkMonitor.forTests(online = false), context)

        assertEquals("You’re offline. Check your connection and try again.", viewModel.state.value.error)
        assertFalse(viewModel.state.value.loading)
        assertFalse(viewModel.state.value.showEmpty)

        service.failWithIo = false
        viewModel.onIntent(MyRecipesIntent.Retry)
        assertNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.showEmpty)
    }

    @Test
    fun serverErrors_keepTheServerMessage() = runTest {
        service.failWith = 500
        val viewModel = MyRecipesViewModel(repository, NetworkMonitor.forTests(online = true), context)

        assertEquals("Error 500", viewModel.state.value.error)
    }

    @Test
    fun queryChanged_updatesTheSearch() = runTest {
        val viewModel = MyRecipesViewModel(repository, NetworkMonitor.forTests(online = true), context)

        viewModel.onIntent(MyRecipesIntent.QueryChanged("lime"))
        assertEquals("lime", viewModel.state.value.query)

        viewModel.onIntent(MyRecipesIntent.QueryChanged(""))
        assertEquals("", viewModel.state.value.query)
    }
}
