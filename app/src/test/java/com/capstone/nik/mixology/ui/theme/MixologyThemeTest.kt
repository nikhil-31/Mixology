package com.capstone.nik.mixology.ui.theme

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [34])
class MixologyThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lightTheme_usesLightPalette() {
        var background = Color.Unspecified
        var onSurface = Color.Unspecified
        composeRule.setContent {
            MixologyTheme(darkTheme = false) {
                background = MaterialTheme.colorScheme.background
                onSurface = MaterialTheme.colorScheme.onSurface
            }
        }
        assertEquals(MixologyGray, background)
        assertEquals(MixologyText, onSurface)
    }

    @Test
    fun darkTheme_usesDarkPalette() {
        var background = Color.Unspecified
        var onSurface = Color.Unspecified
        var primary = Color.Unspecified
        var secondary = Color.Unspecified
        composeRule.setContent {
            MixologyTheme(darkTheme = true) {
                background = MaterialTheme.colorScheme.background
                onSurface = MaterialTheme.colorScheme.onSurface
                primary = MaterialTheme.colorScheme.primary
                secondary = MaterialTheme.colorScheme.secondary
            }
        }
        assertEquals(MixologyDarkBackground, background)
        assertEquals(MixologyOnDark, onSurface)
        assertEquals(MixologyDarkPrimary, primary)
        assertEquals(MixologyDarkSecondary, secondary)
    }
}
