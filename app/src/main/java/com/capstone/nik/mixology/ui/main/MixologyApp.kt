package com.capstone.nik.mixology.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.details.DrinkDetailsRoute
import com.capstone.nik.mixology.ui.grid.DrinkGridRoute
import com.capstone.nik.mixology.ui.hot.HotRoute
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.randomixer.RandomixerRoute
import com.capstone.nik.mixology.ui.search.SearchRoute
import com.capstone.nik.mixology.ui.settings.SettingsRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MixologyApp(
    windowSizeClass: WindowSizeClass,
    pendingDrink: Cocktail? = null,
    onPendingDrinkConsumed: () -> Unit = {},
    viewModel: MainViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val twoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val overlay = isOverlayRoute(navBackStackEntry?.destination?.route)

    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is MainEffect.Navigate -> {
                navController.navigate(effect.destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            MainEffect.OpenDrawer -> scope.launch { drawerState.open() }
            MainEffect.CloseDrawer -> scope.launch { drawerState.close() }
            is MainEffect.OpenSearch -> navController.navigate(searchRoute(effect.query))
            is MainEffect.OpenDetails -> navController.navigate(detailsRoute(effect.cocktail)) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingDrink) {
        val drink = pendingDrink ?: return@LaunchedEffect
        navController.navigate(detailsRoute(drink)) {
            launchSingleTop = true
        }
        onPendingDrinkConsumed()
    }

    val showSideNav = state.destination.showsSideNav() && !overlay
    val showSearch = !overlay && state.destination !is DrawerDestination.Settings
    val title = when (val destination = state.destination) {
        DrawerDestination.Hot -> stringResource(R.string.nav_item_hot)
        DrawerDestination.Randomixer -> stringResource(R.string.nav_item_randomixer)
        DrawerDestination.Settings -> stringResource(R.string.nav_bottom_settings)
        is DrawerDestination.Filter -> when (destination.filter) {
            DrinkFilter.SAVED -> stringResource(R.string.nav_bottom_saved)
            else -> stringResource(destination.filter.titleRes)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showSideNav,
        drawerContent = {
            if (showSideNav) {
                MixologyDrawer(
                    selectedRoute = state.destination.route,
                    onDestinationSelected = { viewModel.onIntent(MainIntent.SelectDestination(it)) },
                )
            }
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (!overlay) {
                    MixologyBottomBar(
                        currentDestination = state.destination,
                        onDestinationSelected = { viewModel.onIntent(MainIntent.SelectDestination(it)) },
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (!overlay) {
                    ScreenHeader(
                        title = title,
                        showSearch = showSearch,
                        onSearch = { viewModel.onIntent(MainIntent.ToggleSearch) },
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    NavHost(
                        navController = navController,
                        startDestination = HOT_ROUTE,
                        modifier = Modifier.weight(1f),
                    ) {
                        DrinkFilter.entries.forEach { filter ->
                            composable(gridRoute(filter)) {
                                DrinkGridRoute(
                                    filter = filter,
                                    snackbarHostState = snackbarHostState,
                                    onDrinkClick = { cocktail ->
                                        viewModel.onIntent(MainIntent.DrinkSelected(cocktail, twoPane && !overlay))
                                    },
                                )
                            }
                        }
                        composable(HOT_ROUTE) {
                            HotRoute(
                                snackbarHostState = snackbarHostState,
                                onDrinkClick = { cocktail ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(cocktail, twoPane && !overlay))
                                },
                                onSeeAll = { filter ->
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Filter(filter)))
                                },
                            )
                        }
                        composable(RANDOMIXER_ROUTE) {
                            RandomixerRoute(snackbarHostState = snackbarHostState)
                        }
                        composable(SETTINGS_ROUTE) {
                            SettingsRoute()
                        }
                        composable(
                            route = SEARCH_ROUTE,
                            arguments = listOf(
                                navArgument("query") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                        ) { entry ->
                            SearchRoute(
                                initialQuery = entry.arguments?.getString("query").orEmpty(),
                                onBack = { navController.navigateUp() },
                                onDrinkClick = { cocktail ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(cocktail, twoPane = false))
                                },
                            )
                        }
                        composable(
                            route = DETAILS_ROUTE,
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType },
                                navArgument("name") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("thumb") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                        ) { entry ->
                            val cocktail = Cocktail(
                                entry.arguments?.getString("id").orEmpty(),
                                entry.arguments?.getString("name").orEmpty(),
                                entry.arguments?.getString("thumb").orEmpty(),
                            )
                            DrinkDetailsRoute(
                                cocktail = cocktail,
                                showUpNavigation = true,
                                onBack = { navController.navigateUp() },
                                wrapInScaffold = true,
                            )
                        }
                    }
                    if (twoPane &&
                        !overlay &&
                        state.destination !is DrawerDestination.Randomixer &&
                        state.destination !is DrawerDestination.Settings
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            val cocktail = state.selectedCocktail
                            if (cocktail != null) {
                                DrinkDetailsRoute(
                                    cocktail = cocktail,
                                    showUpNavigation = false,
                                    onBack = {},
                                    wrapInScaffold = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    showSearch: Boolean,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (showSearch) {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.action_search),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
