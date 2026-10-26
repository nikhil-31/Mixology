package com.capstone.nik.mixology.Network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SseParserTest {

    @Test
    fun splitChunks_parseDeltaRecommendationsDoneAndError() {
        val parser = SseParser()
        assertTrue(parser.push("event: delta\ndata: {\"te").isEmpty())

        val delta = parser.push("xt\":\"A good fit.\"}\n\n")
        val recommendations = parser.push(
            "event: recommendations\n" +
                "data: {\"drinks\":[{\"id\":\"11014\",\"name\":\"Alexander\",\"thumb\":\"https://example.com/a.jpg\"}]}\n\n",
        )
        val done = parser.push("event: done\ndata: {\"content\":\"A good fit.\"}\n\n")
        val error = parser.push("event: error\ndata: {\"detail\":\"save failed\"}\n\n")
        val events = (delta + recommendations + done + error).map { it.toChatStreamEvent() }

        assertEquals(ChatStreamEvent.Delta("A good fit."), events[0])
        val drinks = (events[1] as ChatStreamEvent.Recommendations).drinks
        assertEquals("11014", drinks.single().id)
        assertEquals("Alexander", drinks.single().name)
        assertEquals("https://example.com/a.jpg", drinks.single().thumb)
        assertEquals(ChatStreamEvent.Done("A good fit."), events[2])
        assertEquals(ChatStreamEvent.Error("save failed"), events[3])
    }

    @Test
    fun finish_flushesEventWithoutTrailingBlankLine() {
        val parser = SseParser()
        assertTrue(parser.push("event: error\ndata: {\"detail\":\"cut off\"}").isEmpty())
        assertEquals(ChatStreamEvent.Error("cut off"), parser.finish().single().toChatStreamEvent())
    }
}
