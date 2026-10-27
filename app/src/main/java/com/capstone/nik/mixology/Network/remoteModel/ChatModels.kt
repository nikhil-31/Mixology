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
)
