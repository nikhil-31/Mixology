package com.capstone.nik.mixology.Network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertFalse((events[1] as ChatStreamEvent.Recommendations).hasMore)
        assertEquals(ChatStreamEvent.Done("A good fit."), events[2])
        assertEquals(ChatStreamEvent.Error("save failed"), events[3])
    }

    @Test
    fun done_readsCreditsWhenSent() {
        val withCredits = parseSseBlock(
            "event: done\ndata: {\"content\":\"Hi\",\"credits\":{\"limit\":500,\"used\":12," +
                "\"remaining\":488,\"resets_at\":\"2026-11-01T00:00:00+00:00\",\"unlimited\":false}}",
        )?.toChatStreamEvent() as ChatStreamEvent.Done
        assertEquals("Hi", withCredits.content)
        assertEquals(488, withCredits.credits?.remaining)
        assertEquals(500, withCredits.credits?.limit)
        assertEquals("2026-11-01T00:00:00+00:00", withCredits.credits?.resetsAt)

        val without = parseSseBlock("event: done\ndata: {\"content\":\"Hi\"}")?.toChatStreamEvent()
        assertEquals(ChatStreamEvent.Done("Hi"), without)
    }

    @Test
    fun recommendations_readHasMore() {
        val event = parseSseBlock(
            "event: recommendations\ndata: {\"drinks\":[{\"id\":\"1\",\"name\":\"Gin\"}],\"has_more\":true}",
        )?.toChatStreamEvent() as ChatStreamEvent.Recommendations

        assertEquals("1", event.drinks.single().id)
        assertTrue(event.hasMore)
    }

    @Test
    fun finish_flushesEventWithoutTrailingBlankLine() {
        val parser = SseParser()
        assertTrue(parser.push("event: error\ndata: {\"detail\":\"cut off\"}").isEmpty())
        assertEquals(ChatStreamEvent.Error("cut off"), parser.finish().single().toChatStreamEvent())
    }

    @Test
    fun recipe_readsTheDraftAndItsState() {
        val event = parseSseBlock(
            "event: recipe\ndata: {\"recipe\":{\"name\":\"Paloma\",\"instructions\":\"Build over ice.\"," +
                "\"ingredients\":[{\"ingredient\":\"Tequila\",\"measure\":\"2 oz\"}]," +
                "\"source_url\":\"https://www.instagram.com/p/abc/\"},\"photo_url\":\"https://cdn.example.com/p.jpg\"," +
                "\"method\":\"model\",\"source\":\"instagram\",\"state\":\"saved\",\"recipe_id\":\"u-9\"," +
                "\"duplicate\":true}",
        )?.toChatStreamEvent() as ChatStreamEvent.Recipe

        assertEquals("Paloma", event.recipe.recipe.name)
        assertEquals("2 oz", event.recipe.recipe.ingredients.single().measure)
        assertEquals("https://www.instagram.com/p/abc/", event.recipe.recipe.sourceUrl)
        assertEquals("https://cdn.example.com/p.jpg", event.recipe.photoUrl)
        assertEquals("instagram", event.recipe.source)
        assertEquals("saved", event.recipe.state)
        assertEquals("u-9", event.recipe.recipeId)
        assertTrue(event.recipe.duplicate)
    }

    @Test
    fun actionEvent_saysWhatAdaChanged() {
        val added = parseSseBlock(
            "event: action\ndata: {\"kind\":\"bar_added\",\"names\":[\"Gin\",\"Lime\"]}",
        )?.toChatStreamEvent() as ChatStreamEvent.Action
        assertEquals(ChatStreamEvent.Action("bar_added", listOf("Gin", "Lime")), added)
        assertEquals(true, added.changesBar)
        assertEquals(false, added.changesShopping)

        val shopped = parseSseBlock("event: action\ndata: {\"kind\":\"shopping_added\"}")
            ?.toChatStreamEvent() as ChatStreamEvent.Action
        assertEquals(true, shopped.changesShopping)
        assertEquals(false, shopped.changesBar)
        // A finished shop moves what was bought into the bar.
        assertEquals(true, ChatStreamEvent.Action("trip_recorded").changesBar)
        assertEquals(false, ChatStreamEvent.Action("saved").changesBar)
        assertEquals(null, parseSseBlock("event: action\ndata: {}")?.toChatStreamEvent())
    }
}
