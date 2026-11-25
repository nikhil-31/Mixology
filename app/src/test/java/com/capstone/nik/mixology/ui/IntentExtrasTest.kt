package com.capstone.nik.mixology.ui

import android.app.Application
import android.content.Intent
import com.capstone.nik.mixology.data.Drink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class IntentExtrasTest {

    @Test
    fun drinkExtra_returnsIdNameAndThumb() {
        val drink = Drink("11007", "Margarita", "https://example.com/m.jpg")
        val intent = Intent().putDrinkExtra(drink)

        val restored = intent.drinkExtra()

        assertEquals("11007", restored?.id)
        assertEquals("Margarita", restored?.name)
        assertEquals("https://example.com/m.jpg", restored?.thumb)
    }

    @Test
    fun drinkExtra_returnsNullWhenIdMissing() {
        assertNull(Intent().drinkExtra())
        assertNull(Intent().putExtra(DrinkIntents.EXTRA_ID, " ").drinkExtra())
    }

    @Test
    fun drinkExtra_readsMixologyUri() {
        val intent = Intent(Intent.ACTION_VIEW).setData(
            android.net.Uri.parse("mixology://drink/11007?name=Margarita"),
        )
        val drink = intent.drinkExtra()
        assertEquals("11007", drink?.id)
        assertEquals("Margarita", drink?.name)
    }

    @Test
    fun sharedImportText_readsSharedText() {
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, "  https://www.instagram.com/reel/abc/  ")

        assertEquals("https://www.instagram.com/reel/abc/", intent.sharedImportText())
    }

    @Test
    fun sharedImportText_putsANewSubjectFirst() {
        val intent = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_SUBJECT, "Paper Plane")
            .putExtra(Intent.EXTRA_TEXT, "https://youtu.be/xyz")

        assertEquals("Paper Plane\nhttps://youtu.be/xyz", intent.sharedImportText())

        val repeated = Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_SUBJECT, "Paper Plane")
            .putExtra(Intent.EXTRA_TEXT, "Paper Plane https://youtu.be/xyz")
        assertEquals("Paper Plane https://youtu.be/xyz", repeated.sharedImportText())
    }

    @Test
    fun sharedImportText_isNullForOtherIntentsAndBlankText() {
        assertNull(Intent(Intent.ACTION_VIEW).putExtra(Intent.EXTRA_TEXT, "https://x.com/a").sharedImportText())
        assertNull(Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, "   ").sharedImportText())
        assertNull(Intent(Intent.ACTION_SEND).sharedImportText())
    }
}
