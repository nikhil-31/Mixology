package com.capstone.nik.mixology.ui.auth

import com.capstone.nik.mixology.FakeAppleAuth
import com.capstone.nik.mixology.FakeChatRepository
import com.capstone.nik.mixology.FakeGoogleAuth
import com.capstone.nik.mixology.MainDispatcherRule
import com.capstone.nik.mixology.repository.AuthResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val chat = FakeChatRepository()
    private val viewModel = AuthViewModel(chat, FakeGoogleAuth(), FakeAppleAuth())

    @Test
    fun submit_disabledUntilEmailAndPasswordEntered() {
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onIntent(AuthIntent.EmailChanged("ada@mixology.test"))
        assertFalse(viewModel.state.value.canSubmit)

        viewModel.onIntent(AuthIntent.Submit)

        assertNull(chat.session)
    }

    @Test
    fun signIn_successStoresSessionAndClearsPassword() {
        viewModel.onIntent(AuthIntent.EmailChanged("  ada@mixology.test "))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret"))

        viewModel.onIntent(AuthIntent.Submit)

        val state = viewModel.state.value
        assertNotNull(chat.session)
        assertFalse(state.submitting)
        assertEquals("", state.password)
        assertNull(state.error)
    }

    @Test
    fun signIn_failureShowsDetailUntilEdited() {
        chat.signInResult = AuthResult.Failure("Invalid email or password")
        viewModel.onIntent(AuthIntent.EmailChanged("ada@mixology.test"))
        viewModel.onIntent(AuthIntent.PasswordChanged("wrong"))

        viewModel.onIntent(AuthIntent.Submit)

        assertEquals("Invalid email or password", viewModel.state.value.error)
        assertEquals("wrong", viewModel.state.value.password)

        viewModel.onIntent(AuthIntent.PasswordChanged("right"))

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun signUp_sendsTrimmedNameAndEmail() {
        viewModel.onIntent(AuthIntent.SwitchMode)
        viewModel.onIntent(AuthIntent.DisplayNameChanged(" Ada "))
        viewModel.onIntent(AuthIntent.EmailChanged("ada@mixology.test "))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret"))

        viewModel.onIntent(AuthIntent.Submit)

        assertEquals(AuthMode.SIGN_UP, viewModel.state.value.mode)
        assertEquals(Triple("ada@mixology.test", "secret", "Ada"), chat.signUps.single())
        assertNotNull(chat.session)
    }

    @Test
    fun switchMode_togglesAndClearsError() {
        chat.signInResult = AuthResult.Failure("Nope")
        viewModel.onIntent(AuthIntent.EmailChanged("ada@mixology.test"))
        viewModel.onIntent(AuthIntent.PasswordChanged("secret"))
        viewModel.onIntent(AuthIntent.Submit)

        viewModel.onIntent(AuthIntent.SwitchMode)

        assertEquals(AuthMode.SIGN_UP, viewModel.state.value.mode)
        assertNull(viewModel.state.value.error)
        viewModel.onIntent(AuthIntent.SwitchMode)
        assertEquals(AuthMode.SIGN_IN, viewModel.state.value.mode)
    }

    @Test
    fun togglePasswordVisible_flips() {
        viewModel.onIntent(AuthIntent.TogglePasswordVisible)
        assertTrue(viewModel.state.value.passwordVisible)
        viewModel.onIntent(AuthIntent.TogglePasswordVisible)
        assertFalse(viewModel.state.value.passwordVisible)
    }
}
