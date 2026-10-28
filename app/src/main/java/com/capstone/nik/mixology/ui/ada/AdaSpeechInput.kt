package com.capstone.nik.mixology.ui.ada

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

sealed interface AdaSpeechEvent {
    data class Transcript(val text: String, val final: Boolean) : AdaSpeechEvent
    /** Recognition stopped without words; [failed] is false when the user just said nothing. */
    data class Ended(val failed: Boolean) : AdaSpeechEvent
}

/** Speech-to-text for Ada's composer. Call from the main thread. */
interface AdaSpeechInput {
    val available: Boolean
    fun start(onEvent: (AdaSpeechEvent) -> Unit)
    /** Stops without reporting anything further. */
    fun stop()
    fun release()
}

class AndroidAdaSpeechInput(private val context: Context) : AdaSpeechInput {

    private var recognizer: SpeechRecognizer? = null
    private var onEvent: ((AdaSpeechEvent) -> Unit)? = null

    override val available: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    override fun start(onEvent: (AdaSpeechEvent) -> Unit) {
        stop()
        this.onEvent = onEvent
        val recognizer = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        recognizer.startListening(intent)
    }

    override fun stop() {
        if (onEvent == null) return
        onEvent = null
        recognizer?.cancel()
    }

    override fun release() {
        stop()
        recognizer?.destroy()
        recognizer = null
    }

    private fun emit(event: AdaSpeechEvent) {
        val callback = onEvent ?: return
        val final = event is AdaSpeechEvent.Ended || (event as AdaSpeechEvent.Transcript).final
        if (final) onEvent = null
        callback(event)
    }

    private val listener = object : RecognitionListener {
        override fun onPartialResults(partialResults: Bundle?) {
            partialResults.bestText()?.let { emit(AdaSpeechEvent.Transcript(it, final = false)) }
        }

        override fun onResults(results: Bundle?) {
            val text = results.bestText()
            emit(if (text != null) AdaSpeechEvent.Transcript(text, final = true) else AdaSpeechEvent.Ended(failed = false))
        }

        override fun onError(error: Int) {
            val silent = error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            emit(AdaSpeechEvent.Ended(failed = !silent))
        }

        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}

private fun Bundle?.bestText(): String? =
    this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        ?.firstOrNull()
        ?.takeIf { it.isNotBlank() }
