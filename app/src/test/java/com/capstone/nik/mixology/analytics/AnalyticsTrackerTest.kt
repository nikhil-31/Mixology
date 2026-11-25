package com.capstone.nik.mixology.analytics

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class AnalyticsTrackerTest {

    @Test
    fun screenName_mapsNavRoutes() {
        assertEquals(SCREEN_HOME, analyticsScreenName("hot"))
        assertEquals(SCREEN_SETTINGS, analyticsScreenName("settings"))
        assertEquals(SCREEN_ADA, analyticsScreenName("ada"))
        assertEquals(SCREEN_CATALOG, analyticsScreenName("catalog"))
        assertEquals(SCREEN_MY_BAR, analyticsScreenName("bar"))
        assertEquals(SCREEN_SHOPPING_LIST, analyticsScreenName("shopping"))
        assertEquals(SCREEN_MY_RECIPES, analyticsScreenName("recipes"))
        assertEquals(SCREEN_RECIPE_EDITOR, analyticsScreenName("recipe_editor?id={id}"))
        assertEquals(SCREEN_DRINK_GRID, analyticsScreenName("grid/{filter}"))
        assertEquals(SCREEN_SEARCH, analyticsScreenName("search?query={query}&mode={mode}&kind={kind}"))
        assertEquals(SCREEN_DRINK_DETAILS, analyticsScreenName("details/{id}?name={name}&thumb={thumb}"))
        assertNull(analyticsScreenName(null))
        assertNull(analyticsScreenName(""))
        assertEquals("custom", analyticsScreenName("custom/path"))
        assertEquals("foo", analyticsScreenName("foo"))
    }

    @Test
    fun forTests_recordsScreenSearchAndDrinkEvents() {
        val analytics = AnalyticsTracker.forTests()
        analytics.logScreenView(SCREEN_HOME)
        analytics.logSearch("margarita", "NAME", 3)
        analytics.logViewDrink("11007", "Margarita")
        analytics.logSaveDrink("11007", "Margarita", saved = true)
        analytics.logSaveDrink("11007", "Margarita", saved = false)
        analytics.logShareDrink("11007", "Margarita")
        analytics.logAddToShoppingList("11007", "Margarita")

        assertEquals(
            listOf(
                FirebaseAnalytics.Event.SCREEN_VIEW,
                FirebaseAnalytics.Event.SEARCH,
                FirebaseAnalytics.Event.VIEW_ITEM,
                FirebaseAnalytics.Event.ADD_TO_WISHLIST,
                EVENT_REMOVE_FROM_WISHLIST,
                FirebaseAnalytics.Event.SHARE,
                FirebaseAnalytics.Event.ADD_TO_CART,
            ),
            analytics.recorded.map { it.name },
        )
        val search = analytics.recorded[1]
        assertEquals("margarita", search.params[FirebaseAnalytics.Param.SEARCH_TERM])
        assertEquals("NAME", search.params[PARAM_SEARCH_MODE])
        assertEquals(3, search.params[PARAM_RESULT_COUNT])
        assertEquals("11007", analytics.recorded[2].params[FirebaseAnalytics.Param.ITEM_ID])
        assertEquals("Margarita", analytics.recorded[2].params[FirebaseAnalytics.Param.ITEM_NAME])
        assertTrue(analytics.recorded[3].params[PARAM_SAVED] as Boolean)
        assertEquals(false, analytics.recorded[4].params[PARAM_SAVED])
        assertEquals("drink", analytics.recorded[5].params[FirebaseAnalytics.Param.CONTENT_TYPE])
        assertEquals("11007", analytics.recorded[6].params[FirebaseAnalytics.Param.ITEM_ID])
    }

    @Test
    fun setCollectionEnabled_isNoOpWithoutFirebase() {
        AnalyticsTracker.forTests().setCollectionEnabled(true)
    }

    @Test
    fun forTests_recordsRecipeEvents() {
        val analytics = AnalyticsTracker.forTests()
        analytics.logRecipeCreated("u-1", "House Sour")
        analytics.logRecipeUpdated("u-1", "House Sour")
        analytics.logRecipeDeleted("u-1", "House Sour")

        assertEquals(
            listOf(EVENT_RECIPE_CREATED, EVENT_RECIPE_UPDATED, EVENT_RECIPE_DELETED),
            analytics.recorded.map { it.name },
        )
        assertEquals("u-1", analytics.recorded[0].params[FirebaseAnalytics.Param.ITEM_ID])
        assertEquals("House Sour", analytics.recorded[2].params[FirebaseAnalytics.Param.ITEM_NAME])
    }
}
