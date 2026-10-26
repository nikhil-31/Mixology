package com.capstone.nik.mixology.Network.remoteModel

data class ChatRequest(
    val message: String,
)

data class ChatHistoryResponse(
    val messages: List<ChatMessageDto> = emptyList(),
)

data class ChatMessageDto(
    val role: String = "",
    val content: String = "",
    val at: String = "",
    val drinks: List<ApiDrink>? = null,
)

data class DeltaPayload(
    val text: String = "",
)

data class RecommendationsPayload(
    val drinks: List<ApiDrink> = emptyList(),
)

data class DonePayload(
    val content: String = "",
)
