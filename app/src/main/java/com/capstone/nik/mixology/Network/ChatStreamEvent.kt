package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.data.Drink

sealed interface ChatStreamEvent {
    data class Delta(val text: String) : ChatStreamEvent
    data class Recommendations(val drinks: List<Drink>) : ChatStreamEvent
    data class Done(val content: String) : ChatStreamEvent
    data class Error(val detail: String) : ChatStreamEvent
}

class ChatHttpException(
    val code: Int,
    val detail: String,
) : Exception(detail)
