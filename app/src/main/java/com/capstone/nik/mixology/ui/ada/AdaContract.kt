package com.capstone.nik.mixology.ui.ada

import com.capstone.nik.mixology.data.Drink

data class AdaMessage(
    val id: Long,
    val role: String,
    val content: String,
    val drinks: List<Drink> = emptyList(),
    val streaming: Boolean = false,
    /** More drinks match than this reply shows. */
    val hasMore: Boolean = false,
)

data class AdaUiState(
    val signedIn: Boolean = false,
    val loadingHistory: Boolean = false,
    /** Oldest first; the screen opens at the newest. */
    val messages: List<AdaMessage> = emptyList(),
    /** Older messages exist on the server. */
    val hasOlder: Boolean = false,
    val loadingOlder: Boolean = false,
    val draft: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    /** The mic is open and transcripts are replacing [draft]. */
    val listening: Boolean = false,
    val voiceProblem: AdaVoiceProblem? = null,
)

enum class AdaVoiceProblem {
    /** The microphone permission was refused. */
    Denied,
    Failed,
}

sealed interface AdaIntent {
    data object Load : AdaIntent
    /** The user scrolled up to the oldest loaded message. */
    data object LoadOlder : AdaIntent
    data class DraftChanged(val value: String) : AdaIntent
    data object Send : AdaIntent
    data class UseSuggestion(val text: String) : AdaIntent
    /** Opens the full list of drinks that fit the request behind reply [messageId]. */
    data class OpenMore(val messageId: Long) : AdaIntent
    data class OpenDrink(val drink: Drink) : AdaIntent
    data class ToggleSaved(val drink: Drink) : AdaIntent
    /** The mic button: asks for the microphone, or stops listening. */
    data object ToggleVoice : AdaIntent
    data class VoicePermission(val granted: Boolean) : AdaIntent
    /** Recognised speech so far; [final] ends the session. */
    data class VoiceTranscript(val text: String, val final: Boolean) : AdaIntent
    /** Recognition ended without a transcript; [failed] is false when nothing was said. */
    data class VoiceEnded(val failed: Boolean) : AdaIntent
    /** The screen went away mid-dictation. */
    data object StopVoice : AdaIntent
}

sealed interface AdaEffect {
    data class OpenDrink(val drink: Drink) : AdaEffect
    /** Lists drinks for [query], leaving out the [exclude] ids the reply showed. */
    data class OpenMore(val query: String, val exclude: List<String>) : AdaEffect
    /** Start listening once RECORD_AUDIO is granted; answer with [AdaIntent.VoicePermission]. */
    data object RequestMicrophone : AdaEffect
}
