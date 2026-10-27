package com.capstone.nik.mixology.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.capstone.nik.mixology.BuildConfig
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.ada.AdaMoreRoute
import com.capstone.nik.mixology.ui.ada.AdaRoute
import com.capstone.nik.mixology.ui.bar.BarRoute
import com.capstone.nik.mixology.ui.catalog.CatalogRoute
import com.capstone.nik.mixology.ui.components.BannerAd
import com.capstone.nik.mixology.ui.details.DrinkDetailsRoute
import com.capstone.nik.mixology.ui.grid.DrinkGridRoute
import com.capstone.nik.mixology.ui.hot.HotRoute
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.recipes.MyRecipesRoute
import com.capstone.nik.mixology.ui.recipes.RecipeEditorRoute
import com.capstone.nik.mixology.ui.search.SearchMode
import com.capstone.nik.mixology.ui.search.SearchRoute
import com.capstone.nik.mixology.ui.settings.PRIVACY_POLICY_URL
import com.capstone.nik.mixology.ui.settings.SettingsRoute
import com.capstone.nik.mixology.ui.shopping.ShoppingRoute
import com.capstone.nik.mixology.play.PlayUpdateEvent
import com.capstone.nik.mixology.play.findActivity
import com.capstone.nik.mixology.play.openPlayStoreListing
import com.capstone.nik.mixology.di.AppEntryPoint
import com.capstone.nik.mixology.analytics.analyticsScreenName
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
    val route = navBackStackEntry?.destination?.route
    val overlay = isOverlayRoute(route)
    val showTwoPane = showsTwoPaneDetails(twoPane, route, state.destination)
    val selectDrinkTwoPane = rememberUpdatedState(showTwoPane)
    val context = LocalContext.current
    val appEntry = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppEntryPoint::class.java,
        )
    }
    val networkMonitor = remember { appEntry.networkMonitor() }
    val analytics = remember { appEntry.analyticsTracker() }
    val playUpdates = remember { appEntry.playUpdateController() }
    val playReviews = remember { appEntry.playReviewController() }
    val adConsent = remember { appEntry.adConsentController() }
    val online by networkMonitor.online.collectAsStateWithLifecycle()
    val canRequestAds by adConsent.canRequestAds.collectAsStateWithLifecycle()
    val privacyOptionsRequired by adConsent.privacyOptionsRequired.collectAsStateWithLifecycle()
    val activity = context.findActivity()
    val lifecycleOwner = LocalLifecycleOwner.current
    val playScope = rememberCoroutineScope()

    LaunchedEffect(activity, adConsent) {
        activity?.let { adConsent.start(it) }
    }
    LaunchedEffect(playReviews) {
        playReviews.onAppOpen()
    }
    LaunchedEffect(activity, playUpdates, lifecycleOwner) {
        val host = activity ?: return@LaunchedEffect
        launch {
            playUpdates.events.collect { event ->
                when (event) {
                    PlayUpdateEvent.Downloaded -> {
                        val result = snackbarHostState.showSnackbar(
                            message = context.getString(R.string.update_downloaded),
                            actionLabel = context.getString(R.string.update_restart),
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            playUpdates.completeUpdate()
                        }
                    }
                    PlayUpdateEvent.UpToDate -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.update_up_to_date))
                    }
                }
            }
        }
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            playUpdates.start(host)
        }
    }
    LaunchedEffect(activity, playReviews) {
        val host = activity ?: return@LaunchedEffect
        playReviews.prompts.collect { playReviews.launch(host) }
    }

    LaunchedEffect(navBackStackEntry?.destination?.route) {
        val screen = analyticsScreenName(navBackStackEntry?.destination?.route) ?: return@LaunchedEffect
        analytics.logScreenView(screen)
    }

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
            is MainEffect.OpenSearch -> {
                val route = searchRoute(effect.query, effect.mode, effect.filterKind)
                navController.navigate(route) {
                    popUpTo(SEARCH_ROUTE) { inclusive = true }
                }
            }
            is MainEffect.OpenDetails -> navController.navigate(detailsRoute(effect.drink)) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingDrink) {
        val drink = pendingDrink ?: return@LaunchedEffect
        navController.currentBackStackEntryFlow.first()
        navController.navigate(detailsRoute(drink)) {
            launchSingleTop = true
        }
        onPendingDrinkConsumed()
    }

    val showSearch = !overlay &&
        state.destination !is DrawerDestination.Settings &&
        state.destination !is DrawerDestination.Shopping &&
        state.destination !is DrawerDestination.Ada
    val showUp = !overlay &&
        !state.destination.isBottomNavTab() &&
        state.destination !is DrawerDestination.Settings
    val settingsSelected = state.destination is DrawerDestination.Settings ||
        state.destination is DrawerDestination.Shopping ||
        state.destination is DrawerDestination.Catalog ||
        state.destination is DrawerDestination.Recipes
    val title = when (val destination = state.destination) {
        DrawerDestination.Hot -> stringResource(R.string.nav_item_home)
        DrawerDestination.Settings -> stringResource(R.string.nav_bottom_settings)
        DrawerDestination.Ada -> stringResource(R.string.nav_bottom_ada)
        DrawerDestination.Catalog -> stringResource(R.string.nav_item_browse_catalog)
        DrawerDestination.Bar -> stringResource(R.string.nav_item_my_bar)
        DrawerDestination.Shopping -> stringResource(R.string.nav_item_shopping_list)
        DrawerDestination.Recipes -> stringResource(R.string.my_recipes_title)
        is DrawerDestination.Filter -> destination.filter.titleRes?.let { stringResource(it) }
            ?: destination.filter.displayName()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                if (online && canRequestAds && !BuildConfig.DEBUG) {
                    BannerAd(
                        details = navBackStackEntry?.destination?.route?.startsWith("details") == true,
                        enabled = true,
                    )
                }
                if (!overlay) {
                    MixologyBottomBar(
                        currentDestination = state.destination,
                        onDestinationSelected = { viewModel.onIntent(MainIntent.SelectDestination(it)) },
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding),
            ) {
                if (!overlay) {
                    ScreenHeader(
                        title = title,
                        showUp = showUp,
                        showSearch = showSearch,
                        settingsSelected = settingsSelected,
                        onUp = {
                            val parent = when (state.destination) {
                                DrawerDestination.Shopping,
                                DrawerDestination.Catalog,
                                DrawerDestination.Recipes,
                                -> DrawerDestination.Settings
                                is DrawerDestination.Filter -> DrawerDestination.Catalog
                                else -> DrawerDestination.Hot
                            }
                            viewModel.onIntent(MainIntent.SelectDestination(parent))
                        },
                        onSearch = { viewModel.onIntent(MainIntent.ToggleSearch) },
                        onSettings = {
                            viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Settings))
                        },
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
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
                                },
                            )
                        }
                        composable(CATALOG_ROUTE) {
                            CatalogRoute(
                                onOpenFilter = { filter ->
                                    viewModel.onIntent(
                                        MainIntent.OpenSearch(
                                            query = filter.query.orEmpty(),
                                            mode = if (filter.kind == FilterKind.INGREDIENT) {
                                                SearchMode.INGREDIENT
                                            } else {
                                                SearchMode.NAME
                                            },
                                            filterKind = filter.kind,
                                        ),
                                    )
                                },
                            )
                        }
                        composable(BAR_ROUTE) {
                            BarRoute(
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
                                },
                            )
                        }
                        composable(HOT_ROUTE) {
                            HotRoute(
                                snackbarHostState = snackbarHostState,
                                onOpenBar = { viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Bar)) },
                                onOpenAda = { viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Ada)) },
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
                                },
                                onSeeAll = { filter ->
                                    viewModel.onIntent(
                                        MainIntent.OpenSearch(
                                            query = filter.query.orEmpty(),
                                            mode = if (filter.kind == FilterKind.INGREDIENT) {
                                                SearchMode.INGREDIENT
                                            } else {
                                                SearchMode.NAME
                                            },
                                            filterKind = filter.kind,
                                        ),
                                    )
                                },
                            )
                        }
                        composable(ADA_ROUTE) {
                            AdaRoute(
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
                                },
                                onOpenMore = { query, exclude ->
                                    navController.navigate(adaMoreRoute(query, exclude)) {
                                        launchSingleTop = true
                                    }
                                },
                            )
                        }
                        composable(
                            route = ADA_MORE_ROUTE,
                            arguments = listOf(
                                navArgument("q") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("exclude") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                        ) { entry ->
                            AdaMoreRoute(
                                query = entry.arguments?.getString("q").orEmpty(),
                                exclude = entry.arguments?.getString("exclude").orEmpty()
                                    .split(',')
                                    .filter { it.isNotBlank() },
                                onBack = { navController.navigateUp() },
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
                                },
                            )
                        }
                        composable(SETTINGS_ROUTE) {
                            SettingsRoute(
                                onCatalog = {
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Catalog))
                                },
                                onShoppingList = {
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Shopping))
                                },
                                onMyRecipes = {
                                    viewModel.onIntent(MainIntent.SelectDestination(DrawerDestination.Recipes))
                                },
                                onCheckForUpdates = {
                                    activity?.let { host ->
                                        playScope.launch { playUpdates.start(host, userRequested = true) }
                                    }
                                },
                                onRateApp = {
                                    playReviews.onOpenedListing()
                                    openPlayStoreListing(context)
                                },
                                privacyOptionsRequired = privacyOptionsRequired,
                                onPrivacyPolicy = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)),
                                    )
                                },
                                onPrivacyOptions = {
                                    activity?.let { adConsent.presentPrivacyOptions(it) }
                                },
                            )
                        }
                        composable(SHOPPING_ROUTE) {
                            ShoppingRoute()
                        }
                        composable(MY_RECIPES_ROUTE) {
                            MyRecipesRoute(
                                onRecipeClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
                                },
                                onAddRecipe = {
                                    navController.navigate(recipeEditorRoute()) { launchSingleTop = true }
                                },
                            )
                        }
                        composable(
                            route = RECIPE_EDITOR_ROUTE,
                            arguments = listOf(
                                navArgument("id") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                        ) { entry ->
                            RecipeEditorRoute(
                                recipeId = entry.arguments?.getString("id")?.takeIf { it.isNotBlank() },
                                onClose = { navController.navigateUp() },
                                // Details for the deleted recipe closes itself once it's gone.
                                onDeleted = { navController.navigateUp() },
                            )
                        }
                        composable(
                            route = SEARCH_ROUTE,
                            arguments = listOf(
                                navArgument("query") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                                navArgument("mode") {
                                    type = NavType.StringType
                                    defaultValue = SearchMode.NAME.name
                                },
                                navArgument("kind") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                        ) { entry ->
                            SearchRoute(
                                initialQuery = entry.arguments?.getString("query").orEmpty(),
                                initialMode = searchModeFromRoute(entry.arguments?.getString("mode")),
                                initialFilterKind = filterKindFromRoute(entry.arguments?.getString("kind")),
                                onBack = { navController.navigateUp() },
                                onDrinkClick = { drink ->
                                    viewModel.onIntent(MainIntent.DrinkSelected(drink, selectDrinkTwoPane.value))
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
                                onEditRecipe = { id ->
                                    navController.navigate(recipeEditorRoute(id)) { launchSingleTop = true }
                                },
                                wrapInScaffold = true,
                            )
                        }
                    }
                    if (showTwoPane) {
                        Box(modifier = Modifier.weight(1f)) {
                            val drink = state.selectedDrink
                            if (drink != null) {
                                DrinkDetailsRoute(
                                    drink = drink,
                                    showUpNavigation = false,
                                    onBack = { viewModel.onIntent(MainIntent.CloseDetails) },
                                    onEditRecipe = { id ->
                                        navController.navigate(recipeEditorRoute(id)) { launchSingleTop = true }
                                    },
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
internal fun ScreenHeader(
    title: String,
    showUp: Boolean,
    showSearch: Boolean,
    onUp: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    settingsSelected: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp)
            .testTag("screen_header"),
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
            modifier = Modifier
                .weight(1f)
                .padding(start = if (showUp) 0.dp else 12.dp)
                .testTag("screen_title"),
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
        IconButton(
            onClick = onSettings,
            modifier = Modifier.testTag("header_settings"),
        ) {
            Icon(
                imageVector = if (settingsSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.action_settings),
                tint = if (settingsSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
            )
        }
    }
}

@Composable
internal fun OfflineBanner(onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("offline_banner"),
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
