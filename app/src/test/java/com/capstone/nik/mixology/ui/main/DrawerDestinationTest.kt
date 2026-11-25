package com.capstone.nik.mixology.ui.main

import android.app.Application
import android.os.Bundle
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertEquals("recipes", DrawerDestination.Recipes.route)
        assertEquals(MY_RECIPES_ROUTE, DrawerDestination.Recipes.route)
        assertEquals("recipe_editor?id=&imported=false", recipeEditorRoute())
        assertEquals("recipe_editor?id=u-abc&imported=false", recipeEditorRoute("u-abc"))
        assertEquals("recipe_editor?id=u-abc&imported=true", recipeEditorRoute("u-abc", imported = true))
        assertTrue(isOverlayRoute(RECIPE_EDITOR_ROUTE))
        assertTrue(isRecipeEditorRoute("recipe_editor?id=u-abc"))
        assertFalse(isOverlayRoute(MY_RECIPES_ROUTE))
        assertFalse(isRecipeEditorRoute("recipes"))
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
        assertFalse(showsTwoPaneDetails(true, "recipes", DrawerDestination.Recipes))
        assertFalse(showsTwoPaneDetails(true, "recipe_editor?id=u-1", DrawerDestination.Hot))
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
    fun bottomNavTab_excludesCatalogShoppingAndFilterGrids() {
        assertTrue(DrawerDestination.Bar.isBottomNavTab())
        assertTrue(DrawerDestination.RecipesHome.isBottomNavTab())
        assertTrue(DrawerDestination.Ada.isBottomNavTab())
        assertTrue(DrawerDestination.Shopping.isBottomNavTab())
        // Saved and Discover (the old home) open from Settings now.
        assertFalse(DrawerDestination.Hot.isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.SAVED).isBottomNavTab())
        assertFalse(DrawerDestination.Settings.isBottomNavTab())
        assertFalse(DrawerDestination.Catalog.isBottomNavTab())
        assertFalse(DrawerDestination.Recipes.isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.GIN).isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.ALCOHOLIC).isBottomNavTab())
    }

    @Test
    fun tabDestinationForRoute_mapsTabsAndSettingsPages() {
        assertEquals(DrawerDestination.RecipesHome, tabDestinationForRoute(RECIPES_HOME_ROUTE, null))
        assertEquals(DrawerDestination.Bar, tabDestinationForRoute(BAR_ROUTE, null))
        assertEquals(DrawerDestination.Shopping, tabDestinationForRoute(SHOPPING_ROUTE, null))
        assertEquals(DrawerDestination.Ada, tabDestinationForRoute(ADA_ROUTE, null))
        assertEquals(DrawerDestination.Settings, tabDestinationForRoute(SETTINGS_ROUTE, null))
        assertEquals(DrawerDestination.Hot, tabDestinationForRoute(HOT_ROUTE, null))
        assertEquals(DrawerDestination.Catalog, tabDestinationForRoute(CATALOG_ROUTE, null))
        assertEquals(DrawerDestination.Recipes, tabDestinationForRoute(MY_RECIPES_ROUTE, null))
    }

    @Test
    fun tabDestinationForRoute_readsGridFilterFromArguments() {
        val arguments = Bundle().apply { putString("filter", "SAVED") }
        assertEquals(
            DrawerDestination.Filter(DrinkFilter.SAVED),
            tabDestinationForRoute(GRID_ROUTE, arguments),
        )
    }

    @Test
    fun tabDestinationForRoute_isNullForOverlays() {
        assertNull(tabDestinationForRoute(SEARCH_ROUTE, null))
        assertNull(tabDestinationForRoute(DETAILS_ROUTE, null))
        assertNull(tabDestinationForRoute(ADA_MORE_ROUTE, null))
        assertNull(tabDestinationForRoute(RECIPE_EDITOR_ROUTE, null))
        assertNull(tabDestinationForRoute(IMPORT_RECIPE_ROUTE, null))
        assertNull(tabDestinationForRoute(SHOPPING_HISTORY_ROUTE, null))
        assertNull(tabDestinationForRoute(SHOPPING_TRIP_ROUTE, null))
        assertNull(tabDestinationForRoute(null, null))
    }
}
