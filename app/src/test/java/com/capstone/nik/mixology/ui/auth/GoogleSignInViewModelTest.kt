package com.capstone.nik.mixology.ui.auth

import android.app.Application
import androidx.activity.ComponentActivity
import com.capstone.nik.mixology.FakeAppleAuth
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.FakeGoogleAuth
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.data.GoogleAuthResult
import com.capstone.nik.mixology.repository.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class GoogleSignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chat = FakeChatRepository()
    private val google = FakeGoogleAuth()
    private val viewModel = AuthViewModel(chat, google, FakeAppleAuth())
    private val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    @Test
    fun google_successExchangesFirebaseTokenForSession() {
        viewModel.onIntent(AuthIntent.SignInWithGoogle(activity))

        assertEquals(listOf("firebase-id-token"), chat.googleTokens)
        assertNotNull(chat.session)
        assertFalse(viewModel.state.value.submitting)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun google_cancelLeavesScreenQuiet() {
        google.result = GoogleAuthResult.Cancelled

        viewModel.onIntent(AuthIntent.SignInWithGoogle(activity))

        assertTrue(chat.googleTokens.isEmpty())
        assertNull(chat.session)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun google_pickerFailureShowsMessage() {
        google.result = GoogleAuthResult.Failure("No Google account")

        viewModel.onIntent(AuthIntent.SignInWithGoogle(activity))

        assertEquals("No Google account", viewModel.state.value.error)
        assertTrue(chat.googleTokens.isEmpty())
    }

    @Test
    fun google_backendRejectionShowsDetail() {
        chat.signInResult = AuthResult.Failure("Not a Google sign-in")

        viewModel.onIntent(AuthIntent.SignInWithGoogle(activity))

        assertEquals("Not a Google sign-in", viewModel.state.value.error)
        assertNull(chat.session)
    }
}
