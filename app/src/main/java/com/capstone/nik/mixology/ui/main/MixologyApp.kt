package com.capstone.nik.mixology.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.details.DrinkDetailsRoute
import com.capstone.nik.mixology.ui.grid.DrinkGridRoute
import com.capstone.nik.mixology.ui.hot.HotRoute
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.randomixer.RandomixerRoute
import com.capstone.nik.mixology.ui.settings.SettingsRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MixologyApp(
    windowSizeClass: WindowSizeClass,
    onOpenSearch: (String) -> Unit,
    onOpenDetails: (Cocktail) -> Unit,
    onSignOut: () -> Unit,
    viewModel: MainViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val twoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

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
            is MainEffect.OpenSearch -> onOpenSearch(effect.query)
            is MainEffect.OpenDetails -> onOpenDetails(effect.cocktail)
            MainEffect.SignOut -> onSignOut()
        }
    }

    val title = when (val destination = state.destination) {
        DrawerDestination.Hot -> stringResource(R.string.nav_item_hot)
        DrawerDestination.Randomixer -> stringResource(R.string.nav_item_randomixer)
        DrawerDestination.Settings -> stringResource(R.string.nav_bottom_settings)
        is DrawerDestination.Filter -> when (destination.filter) {
            DrinkFilter.SAVED -> stringResource(R.string.nav_bottom_saved)
            else -> stringResource(destination.filter.titleRes)
        }
    }

    val showSideNav = state.destination.showsSideNav()
    val showSearch = state.destination !is DrawerDestination.Settings

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
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (showSideNav) {
                            IconButton(onClick = { viewModel.onIntent(MainIntent.OpenDrawer) }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.navigation_drawer_open),
                                )
                            }
                        }
                    },
                    actions = {
                        if (showSearch) {
                            IconButton(onClick = { viewModel.onIntent(MainIntent.ToggleSearch) }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(R.string.action_search),
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.onIntent(MainIntent.OpenMenu) }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.action_sign_out),
                            )
                        }
                        DropdownMenu(
                            expanded = state.menuExpanded,
                            onDismissRequest = { viewModel.onIntent(MainIntent.DismissMenu) },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_sign_out)) },
                                onClick = { viewModel.onIntent(MainIntent.SignOut) },
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                MixologyBottomBar(
                    currentDestination = state.destination,
                    onDestinationSelected = { viewModel.onIntent(MainIntent.SelectDestination(it)) },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
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
                                    viewModel.onIntent(MainIntent.DrinkSelected(cocktail, twoPane))
                                },
                            )
                        }
                    }
                    composable(HOT_ROUTE) {
                        HotRoute(
                            snackbarHostState = snackbarHostState,
                            onDrinkClick = { cocktail ->
                                viewModel.onIntent(MainIntent.DrinkSelected(cocktail, twoPane))
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
                }
                if (twoPane &&
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
