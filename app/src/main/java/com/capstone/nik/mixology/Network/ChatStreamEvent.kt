package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ChatCreditsDto
import com.capstone.nik.mixology.Network.remoteModel.ChatRecipeDto
import com.capstone.nik.mixology.data.Drink

sealed interface ChatStreamEvent {
    data class Delta(val text: String) : ChatStreamEvent
    /** [hasMore]: more drinks match than were shown; sending "Show more" pages them. */
    data class Recommendations(val drinks: List<Drink>, val hasMore: Boolean = false) : ChatStreamEvent
    /** [credits]: the chat allowance left after this reply, when the server meters chat. */
    data class Done(val content: String, val credits: ChatCreditsDto? = null) : ChatStreamEvent
    data class Error(val detail: String) : ChatStreamEvent
    /** A recipe Ada read from a link or photo, or the save or drop of one. */
    data class Recipe(val recipe: ChatRecipeDto) : ChatStreamEvent
}

class ChatHttpException(
    val code: Int,
    val detail: String,
) : Exception(detail)
