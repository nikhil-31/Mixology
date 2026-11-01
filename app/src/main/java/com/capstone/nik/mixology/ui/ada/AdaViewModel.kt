package com.capstone.nik.mixology.ui.ada

import android.content.Context
import android.net.Uri
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.Network.remoteModel.ChatRecipeDto
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.recipes.RecipePhotoLoader
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.connectionMessage
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdaViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val drinkRepository: DrinkRepository,
    private val networkMonitor: NetworkMonitor,
    @ApplicationContext private val context: Context,
    private val recipeRepository: RecipeRepository,
    private val photos: RecipePhotoLoader,
    private val inbox: AdaInbox,
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
        // A link sent from Import ("Ask Ada"): sent once Ada is ready for it.
        viewModelScope.launch {
            inbox.pending.collect { if (it != null) deliverInbox() }
        }
    }

    override fun onIntent(intent: AdaIntent) {
        when (intent) {
            AdaIntent.Load -> load()
            AdaIntent.LoadOlder -> loadOlder()
            is AdaIntent.DraftChanged -> setState { copy(draft = intent.value) }
            AdaIntent.Send -> send(currentState.draft)
            is AdaIntent.UseSuggestion -> setState { copy(draft = intent.text) }
            is AdaIntent.OpenMore -> openMore(intent.messageId)
            is AdaIntent.OpenDrink -> sendEffect(AdaEffect.OpenDrink(intent.drink))
            is AdaIntent.ToggleSaved -> toggleSaved(intent.drink)
            AdaIntent.ToggleVoice -> toggleVoice()
            is AdaIntent.VoicePermission -> onVoicePermission(intent.granted)
            is AdaIntent.VoiceTranscript -> onVoiceTranscript(intent.text, intent.final)
            is AdaIntent.VoiceEnded -> onVoiceEnded(intent.failed)
            AdaIntent.StopVoice -> stopVoice()
            is AdaIntent.SaveRecipe -> answerDraft(intent.messageId, R.string.ada_recipe_save_answer)
            is AdaIntent.DropRecipe -> answerDraft(intent.messageId, R.string.ada_recipe_drop_answer)
            is AdaIntent.OpenRecipe -> transcript.firstOrNull { it.id == intent.messageId }
                ?.recipe?.toDrink()?.let { sendEffect(AdaEffect.OpenDrink(it)) }
            is AdaIntent.SendPhoto -> sendPhoto(intent.uri)
            is AdaIntent.UseImportSuggestion -> {
                setState { copy(draft = intent.clipboard?.trim().orEmpty()) }
                sendEffect(AdaEffect.FocusComposer)
            }
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
        // Ada picks from the account's saved list; match the hearts to it.
        viewModelScope.launch { drinkRepository.syncSaved() }
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
                transcript = foldRecipes(page.turns.map { it.toAdaMessage() })
                oldestServerId = page.turns.firstOrNull()?.serverId?.takeIf { it > 0 }
                publishMessages()
                setState {
                    copy(
                        loadingHistory = false,
                        loadingOlder = false,
                        hasOlder = page.hasMore && oldestServerId != null,
                    )
                }
                deliverInbox()
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                onHttpFailure(error, assistantId = null)
            } catch (error: Exception) {
                setState { copy(loadingHistory = false, error = message(error)) }
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
                transcript = foldRecipes(page.turns.map { it.toAdaMessage() } + transcript)
                oldestServerId = page.turns.firstOrNull()?.serverId?.takeIf { it > 0 } ?: oldestServerId
                publishMessages()
                setState { copy(loadingOlder = false, hasOlder = page.hasMore && page.turns.isNotEmpty()) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                setState { copy(loadingOlder = false) }
                onHttpFailure(error, assistantId = null)
            } catch (error: Exception) {
                setState { copy(loadingOlder = false, error = message(error)) }
            }
        }
    }

    private fun send(draft: String) {
        val text = draft.trim()
        if (text.isEmpty() || !canSend()) return
        startReply(text, photo = null) { chatRepository.send(text) }
    }

    private fun canSend(): Boolean =
        !currentState.sending && !currentState.loadingHistory && currentState.signedIn

    /** Sends a photo of recipe notes, with whatever is typed as what the user said. */
    private fun sendPhoto(uri: Uri) {
        if (!canSend()) return
        val text = currentState.draft.trim()
        stopVoice()
        setState { copy(sending = true, error = null, outOfCredits = false) }
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            val jpeg = try {
                photos.load(uri)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(sending = false, error = context.getString(R.string.ada_photo_unreadable)) }
                return@launch
            }
            setState { copy(sending = false) }
            startReply(text, photo = uri) { chatRepository.sendPhoto(jpeg, text) }
        }
    }

    /** The yes or no to the recipe drafted in [messageId], as a message Ada understands. */
    private fun answerDraft(messageId: Long, answer: Int) {
        val message = transcript.firstOrNull { it.id == messageId } ?: return
        if (message.recipe?.state != AdaRecipe.State.Draft) return
        send(context.getString(answer))
    }

    /** Adds the user's message and Ada's empty reply, then fills the reply from [stream]. */
    private fun startReply(text: String, photo: Uri?, stream: () -> Flow<ChatStreamEvent>) {
        stopVoice()
        val userId = freshId()
        val assistantId = freshId()
        transcript = transcript + AdaMessage(
            id = userId,
            role = "user",
            content = text,
            photo = photo,
        ) + AdaMessage(
            id = assistantId,
            role = "assistant",
            content = "",
            streaming = true,
        )
        setState { copy(draft = "", sending = true, error = null, outOfCredits = false) }
        publishMessages()
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            try {
                stream().collect { event ->
                    when (event) {
                        is ChatStreamEvent.Delta -> updateAssistant(assistantId) {
                            it.copy(content = it.content + event.text)
                        }
                        is ChatStreamEvent.Recommendations -> updateAssistant(assistantId) {
                            it.copy(drinks = event.drinks, hasMore = event.hasMore)
                        }
                        is ChatStreamEvent.Recipe -> onRecipe(assistantId, event.recipe)
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
                deliverInbox()
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                onHttpFailure(error, assistantId)
            } catch (error: Exception) {
                updateAssistant(assistantId) { it.copy(streaming = false) }
                setState { copy(sending = false, error = message(error)) }
            }
        }
    }

    /**
     * A draft goes on Ada's reply. A save or drop answers the draft before it,
     * so that card takes the result instead of a second card appearing.
     */
    private fun onRecipe(assistantId: Long, dto: ChatRecipeDto) {
        val recipe = AdaRecipe.from(dto)
        transcript = foldRecipes(
            transcript.map { if (it.id == assistantId) it.copy(recipe = recipe) else it },
        )
        publishMessages()
        if (recipe.state == AdaRecipe.State.Saved) {
            // So My recipes and Recipes show it without a pull.
            viewModelScope.launch {
                try {
                    recipeRepository.load()
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // The recipe is saved either way; the list catches up next time.
                }
            }
        }
    }

    /** Sends a message another screen left for Ada, once nothing else is going on. */
    private fun deliverInbox() {
        if (inbox.pending.value == null || !canSend()) return
        inbox.take()?.let { send(it) }
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
                setState { copy(error = message(error)) }
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
        if (error.code == 402) {
            // Ada never answered, so drop the empty reply bubble.
            if (assistantId != null) {
                transcript = transcript.filterNot { it.id == assistantId && it.content.isEmpty() }
                publishMessages()
            }
            setState { copy(loadingHistory = false, sending = false, error = null, outOfCredits = true) }
            return
        }
        if (assistantId != null) {
            updateAssistant(assistantId) { it.copy(streaming = false) }
        }
        setState { copy(loadingHistory = false, sending = false, error = message(error)) }
    }

    /** A connection problem's message, or what the server or exception said. */
    private fun message(error: Exception): String =
        networkMonitor.connectionMessage(context, error)
            ?: (error as? ChatHttpException)?.detail
            ?: error.message.orEmpty()

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
            recipe = recipe?.let(AdaRecipe::from),
        )
    }
}

/**
 * Moves each saved or dropped result onto the draft card it answers (the
 * latest draft before it), so a recipe shows once, with what became of it.
 */
internal fun foldRecipes(messages: List<AdaMessage>): List<AdaMessage> {
    val result = messages.toMutableList()
    for (index in result.indices) {
        val answer = result[index].recipe ?: continue
        if (answer.state == AdaRecipe.State.Draft) continue
        val draftIndex = (index - 1 downTo 0).firstOrNull { result[it].recipe?.state == AdaRecipe.State.Draft }
            ?: continue
        val draft = result[draftIndex].recipe!!
        result[draftIndex] = result[draftIndex].copy(
            recipe = draft.copy(
                state = answer.state,
                recipeId = answer.recipeId ?: draft.recipeId,
                duplicate = answer.duplicate,
                photoUrl = draft.photoUrl ?: answer.photoUrl,
            ),
        )
        result[index] = result[index].copy(recipe = null)
    }
    return result
}

private fun AdaMessage.withSaved(ids: Set<String>): AdaMessage {
    if (drinks.isEmpty()) return this
    return copy(drinks = drinks.map { drink -> drink.copy(saved = drink.id in ids) })
}
