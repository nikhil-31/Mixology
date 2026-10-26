package com.capstone.nik.mixology.ui.auth

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.data.AppleAuth
import com.capstone.nik.mixology.data.AppleAuthResult
import com.capstone.nik.mixology.data.GoogleAuth
import com.capstone.nik.mixology.data.GoogleAuthResult
import com.capstone.nik.mixology.repository.AuthResult
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val googleAuth: GoogleAuth,
    private val appleAuth: AppleAuth,
) : MviViewModel<AuthIntent, AuthUiState, Nothing>(AuthUiState()) {

    override fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EmailChanged -> setState { copy(email = intent.value, error = null) }
            is AuthIntent.PasswordChanged -> setState { copy(password = intent.value, error = null) }
            is AuthIntent.DisplayNameChanged -> setState { copy(displayName = intent.value, error = null) }
            AuthIntent.TogglePasswordVisible -> setState { copy(passwordVisible = !passwordVisible) }
            AuthIntent.SwitchMode -> setState {
                copy(
                    mode = if (mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN,
                    error = null,
                )
            }
            AuthIntent.Submit -> submit()
            is AuthIntent.SignInWithGoogle -> signInWithGoogle(intent.activity)
            is AuthIntent.SignInWithApple -> signInWithApple(intent.activity)
        }
    }

    private fun signInWithGoogle(activity: Activity) = signInWithProvider {
        when (val google = googleAuth.signIn(activity)) {
            GoogleAuthResult.Cancelled -> null
            is GoogleAuthResult.Failure -> AuthResult.Failure(google.detail)
            is GoogleAuthResult.Success -> chatRepository.signInWithGoogle(google.firebaseIdToken)
        }
    }

    private fun signInWithApple(activity: Activity) = signInWithProvider {
        when (val apple = appleAuth.signIn(activity)) {
            AppleAuthResult.Cancelled -> null
            is AppleAuthResult.Failure -> AuthResult.Failure(apple.detail)
            is AppleAuthResult.Success ->
                chatRepository.signInWithApple(apple.firebaseIdToken, apple.displayName)
        }
    }

    /**
     * Runs a provider sign-in. [attempt] returns null when the user cancelled,
     * which leaves the screen as it was without an error.
     */
    private fun signInWithProvider(attempt: suspend () -> AuthResult?) {
        if (currentState.submitting) return
        setState { copy(submitting = true, error = null) }
        viewModelScope.launch {
            val error = (attempt() as? AuthResult.Failure)?.detail
            setState { copy(submitting = false, error = error) }
        }
    }

    private fun submit() {
        val state = currentState
        if (!state.canSubmit) return
        val email = state.email.trim()
        setState { copy(submitting = true, error = null) }
        viewModelScope.launch {
            val result = when (state.mode) {
                AuthMode.SIGN_IN -> chatRepository.signIn(email, state.password)
                AuthMode.SIGN_UP -> chatRepository.signUp(email, state.password, state.displayName.trim())
            }
            // On success the session store updates and the app swaps this screen out.
            when (result) {
                is AuthResult.Success -> setState { copy(submitting = false, password = "") }
                is AuthResult.Failure -> setState { copy(submitting = false, error = result.detail) }
            }
        }
    }
}
