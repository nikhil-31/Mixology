package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import java.net.URLEncoder

sealed class DrawerDestination(val route: String) {
    data class Filter(val filter: DrinkFilter) : DrawerDestination("grid/${filter.name}")
    data object Randomixer : DrawerDestination("randomixer")
    data object Hot : DrawerDestination("hot")
    data object Settings : DrawerDestination("settings")
}

data class DrawerSection(
    val titleRes: Int?,
    val items: List<DrawerNavItem>,
)

data class DrawerNavItem(
    val titleRes: Int,
    val destination: DrawerDestination,
)

val drawerSections = listOf(
    DrawerSection(
        titleRes = null,
        items = listOf(DrawerNavItem(R.string.nav_item_saved_cocktails, DrawerDestination.Filter(DrinkFilter.SAVED))),
    ),
    DrawerSection(
        titleRes = R.string.nav_title_inspire_me,
        items = listOf(
            DrawerNavItem(R.string.nav_item_hot, DrawerDestination.Hot),
            DrawerNavItem(R.string.nav_item_randomixer, DrawerDestination.Randomixer),
        ),
    ),
    DrawerSection(
        titleRes = R.string.nav_title_alcoholic,
        items = listOf(
            DrawerNavItem(R.string.nav_item_alcoholic, DrawerDestination.Filter(DrinkFilter.ALCOHOLIC)),
            DrawerNavItem(R.string.nav_item_non_alcoholic, DrawerDestination.Filter(DrinkFilter.NON_ALCOHOLIC)),
        ),
    ),
    DrawerSection(
        titleRes = R.string.nav_title_category,
        items = listOf(
            DrawerNavItem(R.string.nav_item_cocktail, DrawerDestination.Filter(DrinkFilter.COCKTAIL)),
            DrawerNavItem(R.string.nav_item_ordinary_drink, DrawerDestination.Filter(DrinkFilter.ORDINARY_DRINK)),
        ),
    ),
    DrawerSection(
        titleRes = R.string.nav_title_favourite_ingredients,
        items = listOf(
            DrawerNavItem(R.string.nav_item_gin, DrawerDestination.Filter(DrinkFilter.GIN)),
            DrawerNavItem(R.string.nav_item_vodka, DrawerDestination.Filter(DrinkFilter.VODKA)),
        ),
    ),
    DrawerSection(
        titleRes = R.string.nav_title_glass,
        items = listOf(
            DrawerNavItem(R.string.nav_item_cocktail_glass, DrawerDestination.Filter(DrinkFilter.COCKTAIL_GLASS)),
            DrawerNavItem(R.string.nav_item_highball_glass, DrawerDestination.Filter(DrinkFilter.HIGHBALL_GLASS)),
        ),
    ),
)

const val GRID_ROUTE = "grid/{filter}"
const val RANDOMIXER_ROUTE = "randomixer"
const val HOT_ROUTE = "hot"
const val SETTINGS_ROUTE = "settings"
const val SEARCH_ROUTE = "search?query={query}"
const val DETAILS_ROUTE = "details/{id}?name={name}&thumb={thumb}"

fun gridRoute(filter: DrinkFilter) = "grid/${filter.name}"

fun searchRoute(query: String = "") = "search?query=${encodeRouteArg(query)}"

fun detailsRoute(drink: Drink): String {
    val id = encodeRouteArg(drink.id)
    val name = encodeRouteArg(drink.name)
    val thumb = encodeRouteArg(drink.thumb)
    return "details/$id?name=$name&thumb=$thumb"
}

fun isOverlayRoute(route: String?): Boolean {
    if (route == null) return false
    return route.startsWith("search") || route.startsWith("details")
}

private fun encodeRouteArg(value: String): String {
    return URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}

fun DrawerDestination.showsSideNav(): Boolean {
    return when (this) {
        DrawerDestination.Hot -> false
        DrawerDestination.Randomixer -> false
        DrawerDestination.Settings -> false
        is DrawerDestination.Filter -> filter != DrinkFilter.SAVED
    }
}
