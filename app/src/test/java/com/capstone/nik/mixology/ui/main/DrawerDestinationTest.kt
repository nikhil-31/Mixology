package com.capstone.nik.mixology.ui.main

import android.app.Application
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DrawerDestinationTest {

    @Test
    fun gridRoute_usesFilterName() {
        assertEquals("grid/ALCOHOLIC", gridRoute(DrinkFilter.ALCOHOLIC))
        assertEquals("grid/TEQUILA", gridRoute(DrinkFilter.TEQUILA))
        assertEquals(
            "grid/INGREDIENT%3AChartreuse",
            gridRoute(DrinkFilter.dynamic(com.capstone.nik.mixology.repository.FilterKind.INGREDIENT, "Chartreuse")),
        )
        assertEquals("grid/SAVED", DrawerDestination.Filter(DrinkFilter.SAVED).route)
        assertEquals("hot", DrawerDestination.Hot.route)
        assertEquals("settings", DrawerDestination.Settings.route)
        assertEquals("ada", DrawerDestination.Ada.route)
        assertEquals("catalog", DrawerDestination.Catalog.route)
        assertEquals("bar", DrawerDestination.Bar.route)
        assertEquals("shopping", DrawerDestination.Shopping.route)
        assertEquals("search?query=gin&mode=NAME&kind=", searchRoute("gin"))
        assertEquals(
            "search?query=Coffee%20liqueur&mode=INGREDIENT&kind=INGREDIENT",
            searchRoute(
                "Coffee liqueur",
                com.capstone.nik.mixology.ui.search.SearchMode.INGREDIENT,
                com.capstone.nik.mixology.repository.FilterKind.INGREDIENT,
            ),
        )
        assertEquals(
            com.capstone.nik.mixology.ui.search.SearchMode.INGREDIENT,
            searchModeFromRoute("INGREDIENT"),
        )
        assertEquals(
            com.capstone.nik.mixology.repository.FilterKind.GLASS,
            filterKindFromRoute("GLASS"),
        )
        assertEquals(
            "details/11007?name=Margarita&thumb=https%3A%2F%2Fexample.com%2Fa.jpg",
            detailsRoute(Drink("11007", "Margarita", "https://example.com/a.jpg")),
        )
        assertTrue(isOverlayRoute("search?query={query}&mode={mode}&kind={kind}"))
        assertTrue(isOverlayRoute("details/{id}?name={name}&thumb={thumb}"))
        assertFalse(isOverlayRoute("hot"))
        assertTrue(isSearchRoute("search?query=gin&mode=NAME&kind="))
        assertTrue(isDetailsRoute("details/11007?name=Margarita&thumb="))
    }

    @Test
    fun showsTwoPaneDetails_includesBarCatalogAndSearch() {
        assertTrue(showsTwoPaneDetails(true, "bar", DrawerDestination.Bar))
        assertFalse(showsTwoPaneDetails(true, "catalog", DrawerDestination.Catalog))
        assertTrue(
            showsTwoPaneDetails(
                true,
                "search?query=Gin&mode=INGREDIENT&kind=INGREDIENT",
                DrawerDestination.Catalog,
            ),
        )
        assertTrue(showsTwoPaneDetails(true, "hot", DrawerDestination.Hot))
        assertTrue(
            showsTwoPaneDetails(true, "grid/SAVED", DrawerDestination.Filter(DrinkFilter.SAVED)),
        )
        assertFalse(showsTwoPaneDetails(false, "bar", DrawerDestination.Bar))
        assertFalse(showsTwoPaneDetails(true, "settings", DrawerDestination.Settings))
        assertFalse(showsTwoPaneDetails(true, "ada", DrawerDestination.Ada))
        assertFalse(showsTwoPaneDetails(true, "shopping", DrawerDestination.Shopping))
        assertFalse(
            showsTwoPaneDetails(
                true,
                "details/11007?name=Margarita&thumb=",
                DrawerDestination.Bar,
            ),
        )
        assertTrue(
            showsTwoPaneDetails(
                true,
                "search?query=&mode=NAME&kind=",
                DrawerDestination.Settings,
            ),
        )
    }

    @Test
    fun bottomNavTab_includesCatalogAndExcludesShoppingAndFilterGrids() {
        assertTrue(DrawerDestination.Hot.isBottomNavTab())
        assertTrue(DrawerDestination.Catalog.isBottomNavTab())
        assertTrue(DrawerDestination.Bar.isBottomNavTab())
        assertTrue(DrawerDestination.Filter(DrinkFilter.SAVED).isBottomNavTab())
        assertTrue(DrawerDestination.Ada.isBottomNavTab())
        assertFalse(DrawerDestination.Settings.isBottomNavTab())
        assertFalse(DrawerDestination.Shopping.isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.GIN).isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.ALCOHOLIC).isBottomNavTab())
    }
}
