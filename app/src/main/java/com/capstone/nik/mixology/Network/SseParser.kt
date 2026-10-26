package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.Network.remoteModel.ApiDetail
import com.capstone.nik.mixology.Network.remoteModel.DeltaPayload
import com.capstone.nik.mixology.Network.remoteModel.DonePayload
import com.capstone.nik.mixology.Network.remoteModel.RecommendationsPayload
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class SseEvent(
    val event: String,
    val data: String,
)

class SseParser {
    private var pending: String = ""

    fun push(chunk: String): List<SseEvent> {
        if (chunk.isEmpty()) return emptyList()
        pending += chunk
        val events = mutableListOf<SseEvent>()
        while (true) {
            val separator = findSeparator(pending) ?: break
            val block = pending.substring(0, separator.first)
            pending = pending.substring(separator.second)
            parseSseBlock(block)?.let(events::add)
        }
        return events
    }

    fun finish(): List<SseEvent> {
        if (pending.isBlank()) {
            pending = ""
            return emptyList()
        }
        val block = pending
        pending = ""
        return listOfNotNull(parseSseBlock(block))
    }
}

internal fun parseSseBlock(block: String): SseEvent? {
    var event = "message"
    val data = mutableListOf<String>()
    var sawField = false
    block.split('\n').forEach { raw ->
        val line = raw.removeSuffix("\r")
        if (line.isEmpty() || line.startsWith(":")) return@forEach
        sawField = true
        val colon = line.indexOf(':')
        val field = if (colon == -1) line else line.substring(0, colon)
        var value = if (colon == -1) "" else line.substring(colon + 1)
        if (value.startsWith(" ")) value = value.drop(1)
        when (field) {
            "event" -> event = value
            "data" -> data += value
        }
    }
    if (!sawField) return null
    return SseEvent(event, data.joinToString("\n"))
}

private fun findSeparator(text: String): Pair<Int, Int>? {
    val lf = text.indexOf("\n\n")
    val crlf = text.indexOf("\r\n\r\n")
    return when {
        lf < 0 && crlf < 0 -> null
        lf < 0 -> crlf to crlf + 4
        crlf < 0 -> lf to lf + 2
        crlf < lf -> crlf to crlf + 4
        else -> lf to lf + 2
    }
}

private val chatGson = Gson()

fun SseEvent.toChatStreamEvent(): ChatStreamEvent? {
    return try {
        when (event) {
            "delta" -> {
                val text = chatGson.fromJson(data, DeltaPayload::class.java)?.text.orEmpty()
                text.takeIf { it.isNotEmpty() }?.let(ChatStreamEvent::Delta)
            }
            "recommendations" -> {
                val drinks = chatGson.fromJson(data, RecommendationsPayload::class.java)
                    ?.drinks
                    .orEmpty()
                    .mapNotNull { it.toDrink() }
                ChatStreamEvent.Recommendations(drinks)
            }
            "done" -> {
                val content = chatGson.fromJson(data, DonePayload::class.java)?.content.orEmpty()
                ChatStreamEvent.Done(content)
            }
            "error" -> {
                val detail = chatGson.fromJson(data, ApiDetail::class.java)?.detail.orEmpty()
                ChatStreamEvent.Error(detail)
            }
            else -> null
        }
    } catch (_: JsonSyntaxException) {
        ChatStreamEvent.Error("Malformed event")
    }
}
