package com.capstone.nik.mixology.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.search.SearchMode

sealed class DrawerDestination(val route: String) {
    data class Filter(val filter: DrinkFilter) : DrawerDestination(gridRoute(filter))
    data object Hot : DrawerDestination("hot")
    data object Settings : DrawerDestination("settings")
    data object Ada : DrawerDestination("ada")
    data object Catalog : DrawerDestination("catalog")
    data object Bar : DrawerDestination("bar")
    data object Shopping : DrawerDestination("shopping")
    data object Recipes : DrawerDestination("recipes")
    data object RecipesHome : DrawerDestination("recipes_home")
}

const val GRID_ROUTE = "grid/{filter}"
const val HOT_ROUTE = "hot"
const val SETTINGS_ROUTE = "settings"
const val ADA_ROUTE = "ada"
const val CATALOG_ROUTE = "catalog"
const val BAR_ROUTE = "bar"
const val SHOPPING_ROUTE = "shopping"
const val MY_RECIPES_ROUTE = "recipes"
const val RECIPES_HOME_ROUTE = "recipes_home"
const val SEARCH_ROUTE = "search?query={query}&mode={mode}&kind={kind}"
const val DETAILS_ROUTE = "details/{id}?name={name}&thumb={thumb}"
const val ADA_MORE_ROUTE = "ada_more?q={q}&exclude={exclude}"
const val RECIPE_EDITOR_ROUTE = "recipe_editor?id={id}&imported={imported}"
const val IMPORT_RECIPE_ROUTE = "recipe_import?text={text}"
const val SHOPPING_HISTORY_ROUTE = "shopping_history"
const val SHOPPING_TRIP_ROUTE = "shopping_history/{id}"

fun shoppingTripRoute(id: Long) = "shopping_history/$id"

fun gridRoute(filter: DrinkFilter) = "grid/${encodeRouteArg(filter.name)}"

fun searchRoute(
    query: String = "",
    mode: SearchMode = SearchMode.NAME,
    kind: FilterKind? = null,
) = "search?query=${encodeRouteArg(query)}&mode=${mode.name}&kind=${kind?.name.orEmpty()}"

fun searchModeFromRoute(name: String?): SearchMode =
    runCatching { SearchMode.valueOf(name.orEmpty()) }.getOrDefault(SearchMode.NAME)

fun filterKindFromRoute(name: String?): FilterKind? =
    name?.takeIf { it.isNotBlank() }?.let { runCatching { FilterKind.valueOf(it) }.getOrNull() }

fun detailsRoute(drink: Drink): String {
    val id = encodeRouteArg(drink.id)
    val name = encodeRouteArg(drink.name)
    val thumb = encodeRouteArg(drink.thumb)
    return "details/$id?name=$name&thumb=$thumb"
}

/** Ada's Show more list for request [query], leaving out the [exclude] ids. */
fun adaMoreRoute(query: String, exclude: List<String>): String {
    return "ada_more?q=${encodeRouteArg(query)}&exclude=${encodeRouteArg(exclude.joinToString(","))}"
}

/**
 * The recipe editor for recipe [id], or for a new recipe when null. [imported]
 * when it opens straight from an import, to remind that import is in beta.
 */
fun recipeEditorRoute(id: String? = null, imported: Boolean = false): String =
    "recipe_editor?id=${encodeRouteArg(id.orEmpty())}&imported=$imported"

/** Import, starting on [text] (shared from another app) when given. */
fun importRecipeRoute(text: String? = null): String = "recipe_import?text=${encodeRouteArg(text.orEmpty())}"

/**
 * The tab or Settings page that [route] (a NavDestination's route template)
 * shows, or null for overlays such as search and details, which sit on top of one.
 */
fun tabDestinationForRoute(route: String?, arguments: Bundle?): DrawerDestination? = when (route) {
    HOT_ROUTE -> DrawerDestination.Hot
    SETTINGS_ROUTE -> DrawerDestination.Settings
    ADA_ROUTE -> DrawerDestination.Ada
    CATALOG_ROUTE -> DrawerDestination.Catalog
    BAR_ROUTE -> DrawerDestination.Bar
    SHOPPING_ROUTE -> DrawerDestination.Shopping
    MY_RECIPES_ROUTE -> DrawerDestination.Recipes
    RECIPES_HOME_ROUTE -> DrawerDestination.RecipesHome
    GRID_ROUTE -> DrawerDestination.Filter(DrinkFilter.fromName(arguments?.getString("filter").orEmpty()))
    else -> null
}

/**
 * The tab or Settings page under the current screen. Overlays keep the one
 * beneath them, so the title and bottom bar don't change while details are open.
 */
@Composable
fun rememberTabDestination(route: String?, arguments: Bundle?): DrawerDestination {
    val last = remember { arrayOf<DrawerDestination>(DrawerDestination.RecipesHome) }
    tabDestinationForRoute(route, arguments)?.let { last[0] = it }
    return last[0]
}

fun isOverlayRoute(route: String?): Boolean {
    if (route == null) return false
    return isSearchRoute(route) || isDetailsRoute(route) || isAdaMoreRoute(route) ||
        isRecipeEditorRoute(route) || isImportRecipeRoute(route) || isShoppingHistoryRoute(route)
}

/** Shopping history and a trip in it: full-screen pages with their own back button. */
fun isShoppingHistoryRoute(route: String?): Boolean = route?.startsWith(SHOPPING_HISTORY_ROUTE) == true

fun isImportRecipeRoute(route: String?): Boolean = route?.startsWith("recipe_import") == true

fun isRecipeEditorRoute(route: String?): Boolean = route?.startsWith("recipe_editor") == true

fun isAdaMoreRoute(route: String?): Boolean = route?.startsWith("ada_more") == true

fun isSearchRoute(route: String?): Boolean = route?.startsWith("search") == true

fun isDetailsRoute(route: String?): Boolean = route?.startsWith("details") == true

fun showsTwoPaneDetails(
    expanded: Boolean,
    route: String?,
    destination: DrawerDestination,
): Boolean {
    if (!expanded || isDetailsRoute(route) || isRecipeEditorRoute(route) || isImportRecipeRoute(route)) {
        return false
    }
    if (isSearchRoute(route)) return true
    return when (destination) {
        DrawerDestination.Settings,
        DrawerDestination.Shopping,
        DrawerDestination.Catalog,
        DrawerDestination.Ada,
        DrawerDestination.Recipes,
        -> false
        DrawerDestination.Hot,
        DrawerDestination.Bar,
        DrawerDestination.RecipesHome,
        is DrawerDestination.Filter,
        -> true
    }
}

private fun encodeRouteArg(value: String): String = Uri.encode(value).orEmpty()
