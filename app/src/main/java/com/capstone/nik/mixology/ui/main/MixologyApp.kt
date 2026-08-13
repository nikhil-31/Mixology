package com.capstone.nik.mixology.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.details.DrinkDetailsRoute
import com.capstone.nik.mixology.ui.grid.DrinkGridRoute
import com.capstone.nik.mixology.ui.randomixer.RandomixerRoute
import com.capstone.nik.mixology.ui.theme.MixologyGray
import com.capstone.nik.mixology.ui.theme.MixologyText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MixologyApp(
    windowSizeClass: WindowSizeClass,
    onOpenSearch: (String) -> Unit,
    onOpenDetails: (Cocktail) -> Unit,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val twoPane = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    var selectedCocktail by remember { mutableStateOf<Cocktail?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentFilterName = backStackEntry?.arguments?.getString("filter")
    val currentFilter = currentFilterName?.let { runCatching { DrinkFilter.valueOf(it) }.getOrNull() }
    val title = when {
        currentRoute == RANDOMIXER_ROUTE -> stringResource(R.string.nav_item_randomixer)
        currentFilter != null -> stringResource(currentFilter.titleRes)
        else -> stringResource(R.string.app_name)
    }

    fun navigateTo(destination: DrawerDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        if (destination is DrawerDestination.Filter) {
            selectedCocktail = null
        }
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MixologyDrawer(
                selectedRoute = currentFilter?.let { gridRoute(it) } ?: currentRoute.orEmpty(),
                onDestinationSelected = ::navigateTo,
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (searchOpen) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                placeholder = { Text(stringResource(R.string.action_search)) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        val query = searchQuery.trim()
                                        if (query.isNotEmpty()) {
                                            onOpenSearch(query.replace(" ", "%20"))
                                            searchOpen = false
                                            searchQuery = ""
                                        }
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text(title, color = MixologyText)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.navigation_drawer_open),
                                tint = MixologyText,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (searchOpen) {
                                    val query = searchQuery.trim()
                                    if (query.isNotEmpty()) {
                                        onOpenSearch(query.replace(" ", "%20"))
                                        searchOpen = false
                                        searchQuery = ""
                                    }
                                } else {
                                    searchOpen = true
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.action_search),
                                tint = MixologyText,
                            )
                        }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.action_sign_out),
                                tint = MixologyText,
                            )
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_sign_out)) },
                                onClick = {
                                    menuExpanded = false
                                    onSignOut()
                                },
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MixologyGray,
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = gridRoute(DrinkFilter.ALCOHOLIC),
                    modifier = Modifier.weight(1f),
                ) {
                    composable(GRID_ROUTE) { entry ->
                        val filter = DrinkFilter.valueOf(entry.arguments?.getString("filter")!!)
                        DrinkGridRoute(
                            filter = filter,
                            snackbarHostState = snackbarHostState,
                            onDrinkClick = { cocktail ->
                                if (twoPane) {
                                    selectedCocktail = cocktail
                                } else {
                                    onOpenDetails(cocktail)
                                }
                            },
                        )
                    }
                    composable(RANDOMIXER_ROUTE) {
                        RandomixerRoute(snackbarHostState = snackbarHostState)
                    }
                }
                if (twoPane && currentRoute != RANDOMIXER_ROUTE) {
                    Box(modifier = Modifier.weight(1f)) {
                        val cocktail = selectedCocktail
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
