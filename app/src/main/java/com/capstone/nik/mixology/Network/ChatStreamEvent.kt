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
    /**
     * Ada changed the user's data: [kind] is `bar_added`, `shopping_added` and so on, and
     * [names] what was added or removed. The app refreshes that data.
     */
    data class Action(val kind: String, val names: List<String> = emptyList()) : ChatStreamEvent {
        val changesBar: Boolean get() = kind.startsWith("bar_") || kind == "trip_recorded"
        val changesShopping: Boolean get() = kind.startsWith("shopping_") || kind == "trip_recorded"
    }
    /** A recipe Ada read from a link or photo, or the save or drop of one. */
    data class Recipe(val recipe: ChatRecipeDto) : ChatStreamEvent
}

class ChatHttpException(
    val code: Int,
    val detail: String,
) : Exception(detail)
