package com.capstone.nik.mixology.ui.ada

import android.net.Uri
import com.capstone.nik.mixology.Network.remoteModel.ChatRecipeDto
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure

/** A recipe Ada read from a link or photo, shown as a card under her reply. */
data class AdaRecipe(
    val name: String,
    /** The post's photo (absolute), or none for a photo of notes. */
    val photoUrl: String?,
    /** The site's name or host; null when it came from a photo. */
    val source: String?,
    val sourceUrl: String?,
    val ingredients: List<IngredientMeasure>,
    val instructions: String?,
    val state: State,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    /** The saved recipe, once there is one. */
    val recipeId: String? = null,
    /** [recipeId] was already in My recipes from this link. */
    val duplicate: Boolean = false,
) {
    enum class State { Draft, Saved, Dropped }

    /** The recipe as the details screen opens it. */
    fun toDrink(): Drink? = recipeId?.let { id -> Drink(id = id, name = name, thumb = "") }

    companion object {
        fun from(dto: ChatRecipeDto): AdaRecipe = AdaRecipe(
            name = dto.recipe.name,
            photoUrl = dto.photoUrl?.takeIf { it.isNotBlank() },
            source = dto.source?.takeIf { it.isNotBlank() },
            sourceUrl = dto.recipe.sourceUrl?.takeIf { it.isNotBlank() },
            ingredients = dto.recipe.ingredients
                .filter { it.ingredient.isNotBlank() }
                .map { IngredientMeasure(it.ingredient, it.measure.orEmpty()) },
            instructions = dto.recipe.instructions?.takeIf { it.isNotBlank() },
            state = when (dto.state) {
                ChatRecipeDto.STATE_SAVED -> State.Saved
                ChatRecipeDto.STATE_DROPPED -> State.Dropped
                else -> State.Draft
            },
            recipeId = dto.recipeId,
            duplicate = dto.duplicate,
            alcoholic = dto.recipe.alcoholic?.takeIf { it.isNotBlank() },
            glass = dto.recipe.glass?.takeIf { it.isNotBlank() },
            category = dto.recipe.category?.takeIf { it.isNotBlank() },
        )
    }
}

data class AdaMessage(
    val id: Long,
    val role: String,
    val content: String,
    val drinks: List<Drink> = emptyList(),
    val streaming: Boolean = false,
    /** More drinks match than this reply shows. */
    val hasMore: Boolean = false,
    /** A recipe Ada read in this reply; its state follows the yes or no to it. */
    val recipe: AdaRecipe? = null,
    /** The photo the user sent with this message, shown from the device. */
    val photo: Uri? = null,
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
    /** The last message didn't get through and can be sent again with [AdaIntent.Retry]. */
    val retryAvailable: Boolean = false,
    /** The server refused the last message: this month's Ada credits are spent. */
    val outOfCredits: Boolean = false,
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
    /** Send the message that just failed again. */
    data object Retry : AdaIntent
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
    /** Save the recipe drafted in reply [messageId]. */
    data class SaveRecipe(val messageId: Long) : AdaIntent
    /** Don't save the recipe drafted in reply [messageId]. */
    data class DropRecipe(val messageId: Long) : AdaIntent
    data class OpenRecipe(val messageId: Long) : AdaIntent
    /** "Edit draft": open the editor filled in with the card's recipe, unsaved. */
    data class EditRecipe(val messageId: Long) : AdaIntent
    /** A photo of recipe notes to send with whatever is in the draft. */
    data class SendPhoto(val uri: Uri) : AdaIntent
    /** The "Import a recipe" starter: [clipboard] is a link on the clipboard, if any. */
    data class UseImportSuggestion(val clipboard: String?) : AdaIntent
}

sealed interface AdaEffect {
    data class OpenDrink(val drink: Drink) : AdaEffect
    /** Lists drinks for [query], leaving out the [exclude] ids the reply showed. */
    data class OpenMore(val query: String, val exclude: List<String>) : AdaEffect
    /** Start listening once RECORD_AUDIO is granted; answer with [AdaIntent.VoicePermission]. */
    data object RequestMicrophone : AdaEffect
    /** Put the cursor in the composer. */
    data object FocusComposer : AdaEffect
    /** Open the recipe editor on the draft just handed to [RecipeDraftHandoff]. */
    data object OpenDraftEditor : AdaEffect
}
