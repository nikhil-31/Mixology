package com.capstone.nik.mixology.ui.components

import android.app.Application
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SourceLinkTest {

    @Test
    fun socialSitesGetTheirNames() {
        assertEquals("Instagram", sourceLabel("instagram", "https://www.instagram.com/p/1/"))
        assertEquals("TikTok", sourceLabel("tiktok", null))
        assertEquals("YouTube", sourceLabel("YouTube", null))
        assertEquals("X", sourceLabel("x", null))
    }

    @Test
    fun otherPagesShowTheirHost() {
        assertEquals("liquor.com", sourceLabel("www.liquor.com", null))
        assertEquals("liquor.com", sourceLabel(null, "https://www.liquor.com/recipes/paper-plane/"))
        assertNull(sourceLabel(null, null))
        assertNull(sourceLabel(" ", "not a link"))
    }

    @Test
    fun theServersSourceFieldsReachTheDrink() {
        val json = """{"id":"u-1","name":"Paper Plane","source_url":"https://youtu.be/xyz","source":"youtube"}"""
        val drink = Gson().fromJson(json, ApiDrink::class.java).toDrink()

        assertEquals("https://youtu.be/xyz", drink?.sourceUrl)
        assertEquals("youtube", drink?.source)
    }
}
