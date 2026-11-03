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
    private val handoff: RecipeDraftHandoff = RecipeDraftHandoff(),
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
    /** The message that last failed to send, so Retry can send the same thing. */
    private var failedSend: FailedSend? = null

    private class FailedSend(
        val userId: Long,
        val assistantId: Long,
        val text: String,
        val photo: Uri?,
        val stream: () -> Flow<ChatStreamEvent>,
    )

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
        // "Edit draft" saved from the editor: that card is saved now.
        viewModelScope.launch {
            handoff.saved.collect { saved -> markDraftSaved(saved.messageId, saved.recipeId) }
        }
        // Drafts saved earlier from the editor stay saved after a reload.
        viewModelScope.launch {
            recipeRepository.recipes.collect { recipes ->
                val reconciled = reconcileDrafts(transcript, recipes)
                if (reconciled != transcript) {
                    transcript = reconciled
                    publishMessages()
                }
            }
        }
    }

    override fun onIntent(intent: AdaIntent) {
        when (intent) {
            AdaIntent.Load -> load()
            AdaIntent.LoadOlder -> loadOlder()
            is AdaIntent.DraftChanged -> setState { copy(draft = intent.value) }
            AdaIntent.Send -> send(currentState.draft)
            AdaIntent.Retry -> retry()
            is AdaIntent.UseSuggestion -> setState { copy(draft = intent.text) }
            is AdaIntent.OpenMore -> openMore(intent.messageId)
            is AdaIntent.OpenDrink -> sendEffect(AdaEffect.OpenDrink(intent.drink))
            is AdaIntent.ToggleSaved -> toggleSaved(intent.drink)
            AdaIntent.ToggleVoice -> toggleVoice()
            is AdaIntent.VoicePermission -> onVoicePermission(intent.granted)
            is AdaIntent.VoiceTranscript -> onVoiceTranscript(intent.text, intent.final)
            is AdaIntent.VoiceEnded -> onVoiceEnded(intent.failed)
            AdaIntent.StopVoice -> stopVoice()
            is AdaIntent.SaveRecipe -> answerDraft(intent.messageId, save = true)
            is AdaIntent.DropRecipe -> answerDraft(intent.messageId, save = false)
            is AdaIntent.OpenRecipe -> transcript.firstOrNull { it.id == intent.messageId }
                ?.recipe?.toDrink()?.let { sendEffect(AdaEffect.OpenDrink(it)) }
            is AdaIntent.EditRecipe -> editDraft(intent.messageId)
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
                transcript = reconcileDrafts(
                    foldRecipes(page.turns.map { it.toAdaMessage() }),
                    recipeRepository.recipes.value,
                )
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
                transcript = reconcileDrafts(
                    foldRecipes(page.turns.map { it.toAdaMessage() } + transcript),
                    recipeRepository.recipes.value,
                )
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

    /** Sends the failed message again, in place of its failed bubbles. */
    private fun retry() {
        val failed = failedSend ?: return
        if (!canSend()) return
        failedSend = null
        transcript = transcript.filterNot { it.id == failed.userId || it.id == failed.assistantId }
        startReply(failed.text, failed.photo, failed.stream)
    }

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
    /**
     * Answers the recipe drafted in [messageId]. The words are the button's, in the user's
     * language; the action is what the server acts on.
     */
    private fun answerDraft(messageId: Long, save: Boolean) {
        val message = transcript.firstOrNull { it.id == messageId } ?: return
        if (message.recipe?.state != AdaRecipe.State.Draft || !canSend()) return
        val text = context.getString(if (save) R.string.ada_recipe_save_answer else R.string.ada_recipe_drop_answer)
        startReply(text, photo = null) { chatRepository.send(text, recipeAction = if (save) "save" else "drop") }
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
        setState { copy(draft = "", sending = true, error = null, outOfCredits = false, retryAvailable = false) }
        publishMessages()
        failedSend = null
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            val failed = FailedSend(userId, assistantId, text, photo, stream)
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
                            failedSend = failed
                            setState { copy(error = event.detail, retryAvailable = true) }
                        }
                    }
                }
                updateAssistant(assistantId) { it.copy(streaming = false) }
                setState { copy(sending = false) }
                deliverInbox()
            } catch (error: CancellationException) {
                throw error
            } catch (error: ChatHttpException) {
                if (error.code != 401 && error.code != 402) failedSend = failed
                onHttpFailure(error, assistantId)
            } catch (error: Exception) {
                updateAssistant(assistantId) { it.copy(streaming = false) }
                failedSend = failed
                setState { copy(sending = false, error = message(error), retryAvailable = true) }
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

    private fun editDraft(messageId: Long) {
        val recipe = transcript.firstOrNull { it.id == messageId }?.recipe ?: return
        if (recipe.state != AdaRecipe.State.Draft) return
        handoff.offer(RecipeDraftRequest(messageId, recipe))
        sendEffect(AdaEffect.OpenDraftEditor)
    }

    /**
     * The editor saved the draft in [messageId] as [recipeId]: the card shows it saved, and the
     * server is told when that draft was the one waiting, so a reload doesn't offer it again or
     * save it twice.
     */
    private fun markDraftSaved(messageId: Long, recipeId: String) {
        val wasPending = transcript.lastOrNull { it.recipe != null }
            ?.let { it.id == messageId && it.recipe?.state == AdaRecipe.State.Draft } == true
        transcript = transcript.map { message ->
            val recipe = message.recipe
            if (message.id == messageId && recipe != null) {
                message.copy(recipe = recipe.copy(state = AdaRecipe.State.Saved, recipeId = recipeId))
            } else {
                message
            }
        }
        publishMessages()
        if (!wasPending || !canSend()) return
        val text = context.getString(R.string.ada_recipe_save_answer)
        startReply(text, photo = null) { chatRepository.send(text, recipeAction = "saved", recipeId = recipeId) }
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
            // The server never took the message, so it would be gone after a reload:
            // drop both bubbles and put the text back in the draft to send later.
            val sent = assistantId?.let { id -> transcript.indexOfFirst { it.id == id } } ?: -1
            val user = transcript.getOrNull(sent - 1)?.takeIf { it.role == "user" }
            if (sent >= 0) {
                transcript = transcript.filterNot { it.id == assistantId || (user != null && it.id == user.id) }
                publishMessages()
            }
            setState {
                copy(
                    loadingHistory = false,
                    sending = false,
                    error = null,
                    outOfCredits = true,
                    draft = if (draft.isBlank()) user?.content.orEmpty() else draft,
                )
            }
            return
        }
        if (assistantId != null) {
            updateAssistant(assistantId) { it.copy(streaming = false) }
        }
        setState {
            copy(loadingHistory = false, sending = false, error = message(error), retryAvailable = failedSend != null)
        }
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

    /** Ids for messages made here are negative, so they never collide with the server's. */
    private fun freshId(): Long = -(nextId++)

    /** Keyed by the server's id when it has one, so a reload keeps the same list items in place. */
    private fun ChatTurn.toAdaMessage(): AdaMessage {
        return AdaMessage(
            id = if (serverId > 0) serverId else freshId(),
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

/**
 * Drafts already in My recipes, saved from the editor ("Edit draft"), show as
 * saved: matched by their link, or by name when they came from a photo.
 */
internal fun reconcileDrafts(messages: List<AdaMessage>, recipes: List<Drink>): List<AdaMessage> {
    if (recipes.isEmpty()) return messages
    return messages.map { message ->
        val recipe = message.recipe?.takeIf { it.state == AdaRecipe.State.Draft } ?: return@map message
        val match = recipe.sourceUrl
            ?.let { link -> recipes.firstOrNull { it.sourceUrl == link } }
            ?: recipes.firstOrNull { recipe.sourceUrl == null && it.name.trim().equals(recipe.name.trim(), ignoreCase = true) }
        if (match == null) {
            message
        } else {
            message.copy(recipe = recipe.copy(state = AdaRecipe.State.Saved, recipeId = match.id))
        }
    }
}

private fun AdaMessage.withSaved(ids: Set<String>): AdaMessage {
    if (drinks.isEmpty()) return this
    return copy(drinks = drinks.map { drink -> drink.copy(saved = drink.id in ids) })
}
