package com.capstone.nik.mixology.ui.ada

import com.capstone.nik.mixology.data.Drink

data class AdaMessage(
    val id: Long,
    val role: String,
    val content: String,
    val drinks: List<Drink> = emptyList(),
    val streaming: Boolean = false,
)

data class AdaUiState(
    val signedIn: Boolean = false,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val signingIn: Boolean = false,
    val authError: String? = null,
    val loadingHistory: Boolean = false,
    val messages: List<AdaMessage> = emptyList(),
    val draft: String = "",
    val sending: Boolean = false,
    val error: String? = null,
)

sealed interface AdaIntent {
    data object Load : AdaIntent
    data class EmailChanged(val value: String) : AdaIntent
    data class PasswordChanged(val value: String) : AdaIntent
    data class DisplayNameChanged(val value: String) : AdaIntent
    data object SignIn : AdaIntent
    data object SignUp : AdaIntent
    data object SignOut : AdaIntent
    data class DraftChanged(val value: String) : AdaIntent
    data object Send : AdaIntent
    data class UseSuggestion(val text: String) : AdaIntent
    data class OpenDrink(val drink: Drink) : AdaIntent
    data class ToggleSaved(val drink: Drink) : AdaIntent
}

sealed interface AdaEffect {
    data class OpenDrink(val drink: Drink) : AdaEffect
}
