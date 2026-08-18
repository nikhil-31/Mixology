package com.capstone.nik.mixology.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.catalog.CatalogRoute
import com.capstone.nik.mixology.ui.details.DrinkDetailsRoute
import com.capstone.nik.mixology.ui.grid.DrinkGridRoute
import com.capstone.nik.mixology.ui.hot.HotRoute
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.randomixer.RandomixerRoute
import com.capstone.nik.mixology.ui.search.SearchRoute
import com.capstone.nik.mixology.ui.settings.SettingsRoute
import com.capstone.nik.mixology.ui.shopping.ShoppingRoute
import com.capstone.nik.mixology.di.AppEntryPoint
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MixologyApp(
    windowSizeClass: WindowSizeClass,
    pendingDrink: Drink? = null,
    onPendingDrinkConsumed: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val twoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val overlay = isOverlayRoute(navBackStackEntry?.destination?.route)
    val context = LocalContext.current
    val networkMonitor = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java,
        ).networkMonitor()
    }
    val online by networkMonitor.online.collectAsStateWithLifecycle()

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
            is MainEffect.OpenSearch -> navController.navigate(searchRoute(effect.query))
            is MainEffect.OpenDetails -> navController.navigate(detailsRoute(effect.drink)) {
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

    val showSearch = !overlay &&
        state.destination !is DrawerDestination.Settings &&
        state.destination !is DrawerDestination.Shopping
    val showUp = !overlay && !state.destination.isBottomNavTab()
    val title = when (val destination = state.destination) {
        DrawerDestination.Hot -> stringResource(R.string.nav_item_hot)
        DrawerDestination.Randomixer -> stringResource(R.string.nav_item_randomixer)
        DrawerDestination.Settings -> stringResource(R.string.nav_bottom_settings)
        DrawerDestination.Catalog -> stringResource(R.string.nav_item_browse_catalog)
        DrawerDestination.Shopping -> stringResource(R.string.nav_item_shopping_list)
        is DrawerDestination.Filter -> destination.filter.titleRes?.let { stringResource(it) }
            ?: destination.filter.displayName()
    }

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
                if (!online) {
                    OfflineBanner(onRetry = { networkMonitor.retry() })
                }
                if (!overlay) {
                    ScreenHeader(
                        title = title,
                        showUp = showUp,
                        showSearch = showSearch,
                        onUp = {
                            val parent = if (state.destination is DrawerDestination.Shopping) {
                                DrawerDestination.Settings
                            } else {
                                DrawerDestination.Hot
                            }
                            viewModel.onIntent(MainIntent.SelectDestination(parent))
                        },
                        onSearch = { viewModel.onIntent(MainIntent.ToggleSearch) },
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    NavHost(
                        navController = navController,
                        startDestination = HOT_ROUTE,
                        modifier = Modifier.weight(1f),
                    ) {
                        composable(
                            route = GRID_ROUTE,
                            arguments = listOf(
                                navArgument("filter") { type = NavType.StringType },
                            ),
                        ) { entry ->
                            val filter = DrinkFilter.fromName(
                                entry.arguments?.getString("filter").orEmpty(),
                            )
                            DrinkGridRoute(
                                filter = filter,
                                snackbarHostState = snackbarHostState,
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, twoPane && !overlay))
                                },
                            )
                        }
                        composable(CATALOG_ROUTE) {
                            CatalogRoute(
                                onOpenFilter = { filter ->
                                    viewModel.onIntent(
                                        MainIntent.SelectDestination(DrawerDestination.Filter(filter)),
                                    )
                                },
                            )
                        }
                        composable(HOT_ROUTE) {
                            HotRoute(
                                snackbarHostState = snackbarHostState,
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, twoPane && !overlay))
                                },
                                onSeeAll = { filter ->
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Filter(filter)))
                                },
                                onBrowseCatalog = {
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Catalog))
                                },
                            )
                        }
                        composable(RANDOMIXER_ROUTE) {
                            RandomixerRoute(snackbarHostState = snackbarHostState)
                        }
                        composable(SETTINGS_ROUTE) {
                            SettingsRoute(
                                onShoppingList = {
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Shopping))
                                },
                            )
                        }
                        composable(SHOPPING_ROUTE) {
                            ShoppingRoute()
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
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, twoPane = false))
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
                            val drink = Drink(
                                id = entry.arguments?.getString("id").orEmpty(),
                                name = entry.arguments?.getString("name").orEmpty(),
                                thumb = entry.arguments?.getString("thumb").orEmpty(),
                            )
                            DrinkDetailsRoute(
                                drink = drink,
                                showUpNavigation = true,
                                onBack = { navController.navigateUp() },
                                wrapInScaffold = true,
                            )
                        }
                    }
                    if (twoPane &&
                        !overlay &&
                        state.destination !is DrawerDestination.Randomixer &&
                        state.destination !is DrawerDestination.Settings &&
                        state.destination !is DrawerDestination.Catalog &&
                        state.destination !is DrawerDestination.Shopping
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            val drink = state.selectedDrink
                            if (drink != null) {
                                DrinkDetailsRoute(
                                    drink = drink,
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

@Composable
private fun ScreenHeader(
    title: String,
    showUp: Boolean,
    showSearch: Boolean,
    onUp: () -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showUp) {
            IconButton(onClick = onUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.content_desc_up_navigation),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(start = if (showUp) 0.dp else 12.dp),
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

@Composable
private fun OfflineBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.network_error_no_network_available),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}
