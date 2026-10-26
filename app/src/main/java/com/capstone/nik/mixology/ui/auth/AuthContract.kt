package com.capstone.nik.mixology.ui.auth

import android.app.Activity

enum class AuthMode { SIGN_IN, SIGN_UP }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !submitting
}

sealed interface AuthIntent {
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data class DisplayNameChanged(val value: String) : AuthIntent
    data object TogglePasswordVisible : AuthIntent
    data object SwitchMode : AuthIntent
    data object Submit : AuthIntent

    /** The Google account picker needs the current activity; it is not kept after the flow. */
    data class SignInWithGoogle(val activity: Activity) : AuthIntent

    /** Apple's sign-in page opens over the current activity. */
    data class SignInWithApple(val activity: Activity) : AuthIntent
}
