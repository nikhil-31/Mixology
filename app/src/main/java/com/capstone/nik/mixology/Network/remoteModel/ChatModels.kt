package com.capstone.nik.mixology.Network.remoteModel

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val message: String,
)

data class ChatHistoryResponse(
    /** Oldest first within the page. */
    val messages: List<ChatMessageDto> = emptyList(),
    /** Older messages remain; request them with `before` = the first message id. */
    @SerializedName("has_more") val hasMore: Boolean = false,
)

data class ChatMessageDto(
    val id: Long = 0,
    val role: String = "",
    val content: String = "",
    val at: String = "",
    val drinks: List<ApiDrink>? = null,
    /** On assistant messages: more drinks match than this reply shows. */
    @SerializedName("has_more") val hasMore: Boolean = false,
    /** `model`, `fallback`, `more`, `list` or `import`. */
    val source: String? = null,
    /** A recipe Ada read from a link or photo in this reply, and what became of it. */
    val recipe: ChatRecipeDto? = null,
)

/**
 * A recipe Ada read from a link or photo: the `recipe` event, and `recipe` on
 * a history message. [state] is `draft` while Ada waits for a yes or no.
 */
data class ChatRecipeDto(
    val recipe: ChatRecipeDraftDto = ChatRecipeDraftDto(),
    @SerializedName("photo_url") val photoUrl: String? = null,
    /** `structured`, `model` or `photo`. */
    val method: String = "",
    /** The site's name or host; null for a photo. */
    val source: String? = null,
    val state: String = STATE_DRAFT,
    /** The saved recipe, new or already there. */
    @SerializedName("recipe_id") val recipeId: String? = null,
    /** [recipeId] is a recipe the user already had from this link. */
    val duplicate: Boolean = false,
) {
    companion object {
        const val STATE_DRAFT = "draft"
        const val STATE_SAVED = "saved"
        const val STATE_DROPPED = "dropped"
    }
}

data class ChatRecipeDraftDto(
    val name: String = "",
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val instructions: String? = null,
    val ingredients: List<ChatRecipeIngredientDto> = emptyList(),
    @SerializedName("source_url") val sourceUrl: String? = null,
)

data class ChatRecipeIngredientDto(
    val ingredient: String = "",
    val measure: String? = null,
)

data class ChatMatchesResponse(
    /** The search listed: the request, or the one a bare "Show more" pages. */
    val query: String = "",
    val drinks: List<ApiDrink> = emptyList(),
    /** More drinks follow this page. */
    @SerializedName("has_more") val hasMore: Boolean = false,
)

data class DeltaPayload(
    val text: String = "",
)

data class RecommendationsPayload(
    val drinks: List<ApiDrink> = emptyList(),
    @SerializedName("has_more") val hasMore: Boolean = false,
)

data class DonePayload(
    val content: String = "",
    val credits: ChatCreditsDto? = null,
)
