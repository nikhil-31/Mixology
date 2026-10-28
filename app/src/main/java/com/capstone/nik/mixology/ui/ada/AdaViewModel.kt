package com.capstone.nik.mixology.ui.ada

import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.ChatHttpException
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.analytics.VOICE_CANCELLED
import com.capstone.nik.mixology.analytics.VOICE_COMPLETED
import com.capstone.nik.mixology.analytics.VOICE_DENIED
import com.capstone.nik.mixology.analytics.VOICE_ERROR
import com.capstone.nik.mixology.analytics.VOICE_STARTED
import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.ChatRepository
import com.capstone.nik.mixology.repository.ChatTurn
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdaViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val drinkRepository: DrinkRepository,
    private val analytics: AnalyticsTracker = AnalyticsTracker.forTests(),
) : MviViewModel<AdaIntent, AdaUiState, AdaEffect>(AdaUiState()) {

    private var transcript: List<AdaMessage> = emptyList()
    private var savedIds: Set<String> = emptySet()
    private var nextId = 1L
    private var historyJob: Job? = null
    private var olderJob: Job? = null
    /** Server id of the oldest loaded message, the cursor for older pages. */
    private var oldestServerId: Long? = null
    private var sendJob: Job? = null

    init {
        viewModelScope.launch {
            drinkRepository.observeSavedIds().collect { ids ->
                savedIds = ids
                publishMessages()
            }
        }
        onIntent(AdaIntent.Load)
    }

    override fun onIntent(intent: AdaIntent) {
        when (intent) {
            AdaIntent.Load -> load()
            AdaIntent.LoadOlder -> loadOlder()
            is AdaIntent.DraftChanged -> setState { copy(draft = intent.value) }
            AdaIntent.Send -> send()
            is AdaIntent.UseSuggestion -> setState { copy(draft = intent.text) }
            is AdaIntent.OpenMore -> openMore(intent.messageId)
            is AdaIntent.OpenDrink -> sendEffect(AdaEffect.OpenDrink(intent.drink))
            is AdaIntent.ToggleSaved -> toggleSaved(intent.drink)
            AdaIntent.ToggleVoice -> toggleVoice()
            is AdaIntent.VoicePermission -> onVoicePermission(intent.granted)
            is AdaIntent.VoiceTranscript -> onVoiceTranscript(intent.text, intent.final)
            is AdaIntent.VoiceEnded -> onVoiceEnded(intent.failed)
            AdaIntent.StopVoice -> stopVoice()
        }
    }

    private fun load() {
        val session = chatRepository.currentSession()
        if (session == null) {
            historyJob?.cancel()
            olderJob?.cancel()
            oldestServerId = null
            sendJob?.cancel()
            transcript = emptyList()
            setState {
                copy(
                    signedIn = false,
                    loadingHistory = false,
                    sending = false,
                    messages = emptyList(),
                    hasOlder = false,
                    loadingOlder = false,
                    error = null,
                )
            }
            return
        }
        if (currentState.loadingHistory || currentState.sending) return
        setState { copy(signedIn = true) }
        loadHistory()
    }

    private fun loadHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            setState { copy(loadingHistory = true, error = null) }
            try {
                olderJob?.cancel()
                val page = chatRepository.history()
                if (!currentState.signedIn || currentState.sending) {
                    setState { copy(loadingHistory = false) }
                    return@launch
                }
                transcript = page.turns.map { it.toAdaMessage() }
                oldestServerId = page.turns.firstOrNull()?.serverId?.takeIf { it > 0 }
                publishMessages()
                setState {
                    copy(
                        loadingHistory = false,
                        loadingOlder = false,
                        hasOlder = page.hasMore && oldestServerId != null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                onHttpFailure(error, assistantId = null)
            } catch (error: Exception) {
                setState { copy(loadingHistory = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun loadOlder() {
        val before = oldestServerId ?: return
        val state = currentState
        if (!state.hasOlder || state.loadingOlder || state.loadingHistory || !state.signedIn) return
        setState { copy(loadingOlder = true) }
        olderJob = viewModelScope.launch {
            try {
                val page = chatRepository.history(before = before)
                transcript = page.turns.map { it.toAdaMessage() } + transcript
                oldestServerId = page.turns.firstOrNull()?.serverId?.takeIf { it > 0 } ?: oldestServerId
                publishMessages()
                setState { copy(loadingOlder = false, hasOlder = page.hasMore && page.turns.isNotEmpty()) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                setState { copy(loadingOlder = false) }
                onHttpFailure(error, assistantId = null)
            } catch (error: Exception) {
                setState { copy(loadingOlder = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun send() {
        val text = currentState.draft.trim()
        if (text.isEmpty() || currentState.sending || currentState.loadingHistory || !currentState.signedIn) {
            return
        }
        stopVoice()
        val userId = freshId()
        val assistantId = freshId()
        transcript = transcript + AdaMessage(
            id = userId,
            role = "user",
            content = text,
        ) + AdaMessage(
            id = assistantId,
            role = "assistant",
            content = "",
            streaming = true,
        )
        setState { copy(draft = "", sending = true, error = null) }
        publishMessages()
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            try {
                chatRepository.send(text).collect { event ->
                    when (event) {
                        is ChatStreamEvent.Delta -> updateAssistant(assistantId) {
                            it.copy(content = it.content + event.text)
                        }
                        is ChatStreamEvent.Recommendations -> updateAssistant(assistantId) {
                            it.copy(drinks = event.drinks, hasMore = event.hasMore)
                        }
                        is ChatStreamEvent.Done -> updateAssistant(assistantId) {
                            it.copy(content = event.content, streaming = false)
                        }
                        is ChatStreamEvent.Error -> {
                            updateAssistant(assistantId) { it.copy(streaming = false) }
                            setState { copy(error = event.detail) }
                        }
                    }
                }
                updateAssistant(assistantId) { it.copy(streaming = false) }
                setState { copy(sending = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                onHttpFailure(error, assistantId)
            } catch (error: Exception) {
                updateAssistant(assistantId) { it.copy(streaming = false) }
                setState { copy(sending = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun toggleVoice() {
        if (currentState.listening) {
            stopVoice()
        } else if (!currentState.sending) {
            sendEffect(AdaEffect.RequestMicrophone)
        }
    }

    private fun onVoicePermission(granted: Boolean) {
        if (!granted) {
            analytics.logAdaVoiceInput(VOICE_DENIED)
            setState { copy(voiceProblem = AdaVoiceProblem.Denied) }
            return
        }
        if (currentState.sending || currentState.listening) return
        analytics.logAdaVoiceInput(VOICE_STARTED)
        // Dictation replaces whatever was typed.
        setState { copy(listening = true, draft = "", voiceProblem = null, error = null) }
    }

    private fun onVoiceTranscript(text: String, final: Boolean) {
        if (!currentState.listening) return
        setState { copy(draft = text, listening = !final) }
        if (final) analytics.logAdaVoiceInput(VOICE_COMPLETED)
    }

    private fun onVoiceEnded(failed: Boolean) {
        if (!currentState.listening) return
        analytics.logAdaVoiceInput(if (failed) VOICE_ERROR else VOICE_CANCELLED)
        setState { copy(listening = false, voiceProblem = AdaVoiceProblem.Failed.takeIf { failed }) }
    }

    /** Keeps the words heard so far in the draft. */
    private fun stopVoice() {
        if (!currentState.listening) return
        analytics.logAdaVoiceInput(if (currentState.draft.isBlank()) VOICE_CANCELLED else VOICE_COMPLETED)
        setState { copy(listening = false) }
    }

    private fun openMore(messageId: Long) {
        val index = transcript.indexOfFirst { it.id == messageId }
        if (index < 0) return
        val query = transcript.subList(0, index).lastOrNull { it.role == "user" }?.content ?: return
        sendEffect(AdaEffect.OpenMore(query, transcript[index].drinks.map { it.id }))
    }

    private fun toggleSaved(drink: Drink) {
        viewModelScope.launch {
            try {
                if (drink.saved) drinkRepository.unsave(drink.id) else drinkRepository.save(drink)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(error = error.message.orEmpty()) }
            }
        }
    }

    private fun onHttpFailure(error: ChatHttpException, assistantId: Long?) {
        if (error.code == 401) {
            historyJob?.cancel()
            olderJob?.cancel()
            oldestServerId = null
            sendJob?.cancel()
            chatRepository.signOut()
            transcript = emptyList()
            setState {
                copy(
                    signedIn = false,
                    loadingHistory = false,
                    sending = false,
                    messages = emptyList(),
                    hasOlder = false,
                    loadingOlder = false,
                    error = null,
                )
            }
            return
        }
        if (assistantId != null) {
            updateAssistant(assistantId) { it.copy(streaming = false) }
        }
        setState { copy(loadingHistory = false, sending = false, error = error.detail) }
    }

    private fun updateAssistant(id: Long, block: (AdaMessage) -> AdaMessage) {
        transcript = transcript.map { message ->
            if (message.id == id) block(message) else message
        }
        publishMessages()
    }

    private fun publishMessages() {
        val ids = savedIds
        setState { copy(messages = transcript.map { it.withSaved(ids) }) }
    }

    private fun freshId(): Long = nextId++

    private fun ChatTurn.toAdaMessage(): AdaMessage {
        return AdaMessage(
            id = freshId(),
            role = role,
            content = content,
            drinks = drinks,
            hasMore = hasMore,
        )
    }
}

private fun AdaMessage.withSaved(ids: Set<String>): AdaMessage {
    if (drinks.isEmpty()) return this
    return copy(drinks = drinks.map { drink -> drink.copy(saved = drink.id in ids) })
}
