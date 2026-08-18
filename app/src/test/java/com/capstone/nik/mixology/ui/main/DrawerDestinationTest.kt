package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertEquals("randomixer", DrawerDestination.Randomixer.route)
        assertEquals("hot", DrawerDestination.Hot.route)
        assertEquals("settings", DrawerDestination.Settings.route)
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
    }

    @Test
    fun bottomNavTab_includesCatalogAndExcludesShoppingAndFilterGrids() {
        assertTrue(DrawerDestination.Hot.isBottomNavTab())
        assertTrue(DrawerDestination.Catalog.isBottomNavTab())
        assertTrue(DrawerDestination.Bar.isBottomNavTab())
        assertTrue(DrawerDestination.Filter(DrinkFilter.SAVED).isBottomNavTab())
        assertTrue(DrawerDestination.Randomixer.isBottomNavTab())
        assertFalse(DrawerDestination.Settings.isBottomNavTab())
        assertFalse(DrawerDestination.Shopping.isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.GIN).isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.ALCOHOLIC).isBottomNavTab())
    }
}
