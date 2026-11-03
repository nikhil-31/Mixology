package com.capstone.nik.mixology.ui.auth

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.FakeAuthRepository
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.SessionStore
import kotlinx.coroutines.flow.first
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
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()
    private lateinit var sessions: SessionStore

    @Before
    fun setUp() {
        sessions = SessionStore(ApplicationProvider.getApplicationContext())
        sessions.clear()
        sessions.save("token", 3600, "ada@example.com", "Ada", uid = "uid-1")
    }

    private fun viewModel(online: Boolean = true) =
        SessionViewModel(sessions, auth, NetworkMonitor.forTests(online = online))

    @Test
    fun signOut_goesThroughTheAuthRepository() {
        viewModel().signOut()
        assertTrue(auth.signedOut)
    }

    @Test
    fun deleteAccount_deletesAndEndsBusy() {
        val viewModel = viewModel()
        viewModel.deleteAccount()
        assertTrue(auth.deleted)
        assertFalse(viewModel.account.value.busy)
        assertNull(viewModel.account.value.errorRes)
    }

    @Test
    fun deleteAccount_offlineShowsTheConnectionMessage() {
        auth.deleteError = IOException("no route")
        val viewModel = viewModel(online = false)
        viewModel.deleteAccount()
        assertFalse(auth.deleted)
        assertFalse(viewModel.account.value.busy)
        assertEquals(R.string.error_no_connection_body, viewModel.account.value.errorRes)
    }

    @Test
    fun deleteAccount_serverErrorShowsTheDeleteMessage() {
        auth.deleteError = IllegalStateException("500")
        val viewModel = viewModel()
        viewModel.deleteAccount()
        assertEquals(R.string.settings_delete_account_failed, viewModel.account.value.errorRes)
    }

    @Test
    fun exportData_handsTheJsonToTheScreen() = runTest {
        auth.export = """{"uid":"uid-1","bar":[]}"""
        val viewModel = viewModel()
        viewModel.exportData()
        assertEquals(auth.export, viewModel.exports.first())
        assertFalse(viewModel.account.value.busy)
    }

    @Test
    fun exportData_failureShowsTheExportMessage() {
        auth.exportError = IllegalStateException("500")
        val viewModel = viewModel()
        viewModel.exportData()
        assertEquals(R.string.settings_export_failed, viewModel.account.value.errorRes)
    }
}
