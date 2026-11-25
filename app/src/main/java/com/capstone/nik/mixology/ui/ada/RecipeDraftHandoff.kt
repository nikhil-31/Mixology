package com.capstone.nik.mixology.ui.ada

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/** A recipe Ada read, opened in the editor from the card on [messageId]. */
data class RecipeDraftRequest(val messageId: Long, val recipe: AdaRecipe)

/** The editor saved the draft from [messageId] as [recipeId]. */
data class DraftSaved(val messageId: Long, val recipeId: String)

/**
 * Carries a draft from Ada's card to the recipe editor ("Edit draft") and the
 * saved recipe back, so the card can say it's saved.
 */
@Singleton
class RecipeDraftHandoff @Inject constructor() {
    private var pending: RecipeDraftRequest? = null
    private val _saved = MutableSharedFlow<DraftSaved>(extraBufferCapacity = 4)
    val saved: SharedFlow<DraftSaved> = _saved.asSharedFlow()

    fun offer(request: RecipeDraftRequest) {
        pending = request
    }

    /** The waiting draft, removed so only one editor fills from it. */
    fun take(): RecipeDraftRequest? = pending.also { pending = null }

    fun saved(messageId: Long, recipeId: String) {
        _saved.tryEmit(DraftSaved(messageId, recipeId))
    }
}
