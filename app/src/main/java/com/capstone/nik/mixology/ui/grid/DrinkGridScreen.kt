package com.capstone.nik.mixology.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyListItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkListItem
import com.capstone.nik.mixology.ui.components.DrinkResultsHeader
import com.capstone.nik.mixology.ui.components.DrinkResultsNoMatches
import com.capstone.nik.mixology.ui.components.EmptyState
import com.capstone.nik.mixology.ui.components.SavedDrinkCardAspectRatio
import com.capstone.nik.mixology.ui.components.bleedHorizontally
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun DrinkGridRoute(
    filter: DrinkFilter,
    snackbarHostState: SnackbarHostState,
    onDrinkClick: (Drink) -> Unit,
    viewModel: DrinkGridViewModel = hiltViewModel(key = filter.name),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(filter) {
        viewModel.onIntent(DrinkGridIntent.Bind(filter))
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is DrinkGridEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            is DrinkGridEffect.ShowMessageRes -> snackbarHostState.showSnackbar(context.getString(effect.resId))
            is DrinkGridEffect.OpenDrink -> onDrinkClick(effect.drink)
        }
    }

    DrinkGridScreen(
        filter = filter,
        drinks = state.drinks,
        listView = state.listView,
        query = state.query,
        onQueryChanged = { viewModel.onIntent(DrinkGridIntent.QueryChanged(it)) },
        onDrinkClick = { viewModel.onIntent(DrinkGridIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(DrinkGridIntent.ToggleSaved(it)) },
        onToggleListView = { viewModel.onIntent(DrinkGridIntent.ToggleListView) },
    )
}

@Composable
fun DrinkGridScreen(
    filter: DrinkFilter,
    drinks: List<Drink>,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    listView: Boolean = false,
    onToggleListView: () -> Unit = {},
    query: String = "",
    onQueryChanged: (String) -> Unit = {},
) {
    val saved = filter.showEmptySaved
    val visible = if (saved) filterSavedDrinks(drinks, query) else drinks
    val emptySaved = saved && drinks.isEmpty()
    val noMatches = saved && drinks.isNotEmpty() && visible.isEmpty()
    val showList = saved && listView
    val count = if (drinks.isEmpty()) null else pluralStringResource(R.plurals.saved_count, drinks.size, drinks.size)
    // Saved's search field and toggle lead the list, so they scroll away with the drinks.
    val header: @Composable (Modifier) -> Unit = { modifier ->
        DrinkResultsHeader(
            query = query,
            onQueryChanged = onQueryChanged,
            listView = listView,
            onToggleListView = onToggleListView,
            label = count,
            modifier = modifier,
        )
    }
    when {
        emptySaved -> Column(modifier = Modifier.fillMaxSize()) {
            header(Modifier)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                EmptyState(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = stringResource(R.string.saved_empty_title),
                    body = stringResource(R.string.saved_empty_body),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
        showList -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 50.dp),
        ) {
            item(key = HEADER_KEY) { header(Modifier) }
            if (noMatches) {
                item(key = NO_MATCHES_KEY) { DrinkResultsNoMatches() }
            }
            lazyListItems(visible, key = { it.id }) { item ->
                SwipeUnsaveRow(drink = item, onDrinkClick = onDrinkClick, onToggleSaved = onToggleSaved)
            }
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(GridGutter, GridGutter, GridGutter, 50.dp),
            horizontalArrangement = Arrangement.spacedBy(GridGutter),
            verticalArrangement = Arrangement.spacedBy(GridGutter),
        ) {
            if (saved) {
                item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    header(Modifier.bleedHorizontally(GridGutter))
                }
            }
            if (noMatches) {
                item(key = NO_MATCHES_KEY, span = { GridItemSpan(maxLineSpan) }) { DrinkResultsNoMatches() }
            }
            items(visible, key = { it.id }) { item ->
                DrinkCard(
                    item = item,
                    onClick = { onDrinkClick(item) },
                    onToggleSaved = { onToggleSaved(item) },
                    posterAspectRatio = if (saved) SavedDrinkCardAspectRatio else 1f,
                )
            }
        }
    }
}

private val GridGutter = 6.dp
private const val HEADER_KEY = "saved_header"
private const val NO_MATCHES_KEY = "saved_no_matches"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeUnsaveRow(
    drink: Drink,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && drink.saved) {
                onToggleSaved(drink)
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Only reveal the delete affordance while the row is being swiped.
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.content_desc_unsave_cocktail),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        },
    ) {
        // Opaque so the swipe background never shows through the row.
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            DrinkListItem(
                drink = drink,
                onDrinkClick = onDrinkClick,
                onToggleSaved = onToggleSaved,
            )
        }
    }
}
