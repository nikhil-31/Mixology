package com.capstone.nik.mixology.ui.hot

import android.app.Application
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class FeaturedDrinkTest {

    @Test
    fun featuredDrink_prefersLatestRecentlyViewed() {
        val recent = Drink("1", "Negroni", "")
        val firstRail = Drink("2", "Mojito", "")
        val featured = featuredDrink(
            listOf(
                HotCategory(DrinkFilter.RECENTLY_VIEWED, listOf(recent)),
                HotCategory(DrinkFilter.GIN, listOf(firstRail)),
            ),
        )
        assertEquals("1", featured?.id)
    }

    @Test
    fun featuredDrink_fallsBackToFirstRail() {
        val firstRail = Drink("2", "Mojito", "")
        assertEquals("2", featuredDrink(listOf(HotCategory(DrinkFilter.GIN, listOf(firstRail))))?.id)
        assertNull(featuredDrink(emptyList()))
    }
}
