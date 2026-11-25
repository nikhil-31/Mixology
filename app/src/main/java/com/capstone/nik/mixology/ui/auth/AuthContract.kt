package com.capstone.nik.mixology.ui.auth

import android.app.Activity

enum class AuthMode { SIGN_IN, SIGN_UP }

/** Something to tell the user about a password reset, resolved to text by the screen. */
enum class AuthNotice { RESET_SENT, RESET_NEEDS_EMAIL }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val passwordVisible: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
    val notice: AuthNotice? = null,
) {
    /** A new password has to meet the rule the screen shows; an existing one only has to be there. */
    val canSubmit: Boolean
        get() = email.isNotBlank() && !submitting &&
            if (mode == AuthMode.SIGN_UP) password.length >= MIN_PASSWORD_LENGTH else password.isNotBlank()

    companion object {
        /** The length `auth_password_rule` asks for. */
        const val MIN_PASSWORD_LENGTH = 6
    }
}

sealed interface AuthIntent {
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data class DisplayNameChanged(val value: String) : AuthIntent
    data object TogglePasswordVisible : AuthIntent
    data object SwitchMode : AuthIntent
    data object Submit : AuthIntent

    /** Emails a reset link to the address in the email field. */
    data object ForgotPassword : AuthIntent

    /** The Google account picker needs the current activity; it is not kept after the flow. */
    data class SignInWithGoogle(val activity: Activity) : AuthIntent

    /** Apple's sign-in page opens over the current activity. */
    data class SignInWithApple(val activity: Activity) : AuthIntent
}
