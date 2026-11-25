package com.capstone.nik.mixology.ui.auth

import android.app.Application
import androidx.activity.ComponentActivity
import com.capstone.nik.mixology.FakeAppleAuth
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.FakeGoogleAuth
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.data.AppleAuthResult
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
class AppleSignInViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chat = FakeChatRepository()
    private val apple = FakeAppleAuth()
    private val google = FakeGoogleAuth()
    private val viewModel = AuthViewModel(chat, google, apple)
    private val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()

    @Test
    fun apple_successSendsTokenAndFirstTimeName() {
        viewModel.onIntent(AuthIntent.SignInWithApple(activity))

        assertEquals(listOf("apple-firebase-token" to "Ada Lovelace"), chat.appleSignIns)
        assertNotNull(chat.session)
        assertFalse(viewModel.state.value.submitting)
        assertNull(viewModel.state.value.error)
        assertEquals(0, google.signIns)
    }

    @Test
    fun apple_cancelLeavesScreenQuiet() {
        apple.result = AppleAuthResult.Cancelled

        viewModel.onIntent(AuthIntent.SignInWithApple(activity))

        assertTrue(chat.appleSignIns.isEmpty())
        assertNull(chat.session)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun apple_failureShowsMessage() {
        apple.result = AppleAuthResult.Failure("Sign in with Apple didn’t work")

        viewModel.onIntent(AuthIntent.SignInWithApple(activity))

        assertEquals("Sign in with Apple didn’t work", viewModel.state.value.error)
        assertTrue(chat.appleSignIns.isEmpty())
    }

    @Test
    fun apple_backendRejectionShowsDetail() {
        chat.signInResult = AuthResult.Failure("Not a apple.com sign-in")

        viewModel.onIntent(AuthIntent.SignInWithApple(activity))

        assertEquals("Not a apple.com sign-in", viewModel.state.value.error)
        assertNull(chat.session)
    }
}
