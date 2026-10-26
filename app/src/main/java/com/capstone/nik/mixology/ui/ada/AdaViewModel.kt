package com.capstone.nik.mixology.ui.ada

import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.ChatHttpException
import com.capstone.nik.mixology.Network.ChatStreamEvent
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.AuthResult
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
) : MviViewModel<AdaIntent, AdaUiState, AdaEffect>(AdaUiState()) {

    private var transcript: List<AdaMessage> = emptyList()
    private var savedIds: Set<String> = emptySet()
    private var nextId = 1L
    private var historyJob: Job? = null
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
            is AdaIntent.EmailChanged -> setState { copy(email = intent.value, authError = null) }
            is AdaIntent.PasswordChanged -> setState { copy(password = intent.value, authError = null) }
            is AdaIntent.DisplayNameChanged -> setState { copy(displayName = intent.value, authError = null) }
            AdaIntent.SignIn -> authenticate(create = false)
            AdaIntent.SignUp -> authenticate(create = true)
            AdaIntent.SignOut -> signOut()
            is AdaIntent.DraftChanged -> setState { copy(draft = intent.value) }
            AdaIntent.Send -> send()
            is AdaIntent.UseSuggestion -> setState { copy(draft = intent.text) }
            is AdaIntent.OpenDrink -> sendEffect(AdaEffect.OpenDrink(intent.drink))
            is AdaIntent.ToggleSaved -> toggleSaved(intent.drink)
        }
    }

    private fun load() {
        val session = chatRepository.currentSession()
        if (session == null) {
            historyJob?.cancel()
            sendJob?.cancel()
            transcript = emptyList()
            setState {
                copy(
                    signedIn = false,
                    loadingHistory = false,
                    sending = false,
                    messages = emptyList(),
                    error = null,
                )
            }
            return
        }
        if (currentState.loadingHistory || currentState.sending) return
        setState {
            copy(
                signedIn = true,
                email = session.email.ifBlank { email },
                displayName = session.displayName.ifBlank { displayName },
            )
        }
        loadHistory()
    }

    private fun loadHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            setState { copy(loadingHistory = true, error = null) }
            try {
                val turns = chatRepository.history()
                if (!currentState.signedIn || currentState.sending) {
                    setState { copy(loadingHistory = false) }
                    return@launch
                }
                transcript = turns.map { it.toAdaMessage() }
                publishMessages()
                setState { copy(loadingHistory = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                onHttpFailure(error, assistantId = null)
            } catch (error: Exception) {
                setState { copy(loadingHistory = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun authenticate(create: Boolean) {
        val email = currentState.email.trim()
        val password = currentState.password
        val name = currentState.displayName
        if (email.isEmpty() || password.isEmpty() || currentState.signingIn) return
        setState { copy(signingIn = true, authError = null) }
        viewModelScope.launch {
            val result = if (create) {
                chatRepository.signUp(email, password, name)
            } else {
                chatRepository.signIn(email, password)
            }
            when (result) {
                is AuthResult.Success -> {
                    setState {
                        copy(
                            signingIn = false,
                            signedIn = true,
                            password = "",
                            authError = null,
                            email = result.session.email.ifBlank { email },
                            displayName = result.session.displayName.ifBlank { displayName },
                        )
                    }
                    loadHistory()
                }
                is AuthResult.Failure -> setState {
                    copy(signingIn = false, authError = result.detail)
                }
            }
        }
    }

    private fun signOut() {
        historyJob?.cancel()
        sendJob?.cancel()
        chatRepository.signOut()
        transcript = emptyList()
        setState {
            AdaUiState(
                email = currentState.email,
                displayName = currentState.displayName,
            )
        }
    }

    private fun send() {
        val text = currentState.draft.trim()
        if (text.isEmpty() || currentState.sending || currentState.loadingHistory || !currentState.signedIn) {
            return
        }
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
                            it.copy(drinks = event.drinks)
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
            sendJob?.cancel()
            chatRepository.signOut()
            transcript = emptyList()
            setState {
                copy(
                    signedIn = false,
                    loadingHistory = false,
                    sending = false,
                    messages = emptyList(),
                    error = null,
                    authError = error.detail,
                    password = "",
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
        )
    }
}

private fun AdaMessage.withSaved(ids: Set<String>): AdaMessage {
    if (drinks.isEmpty()) return this
    return copy(drinks = drinks.map { drink -> drink.copy(saved = drink.id in ids) })
}
