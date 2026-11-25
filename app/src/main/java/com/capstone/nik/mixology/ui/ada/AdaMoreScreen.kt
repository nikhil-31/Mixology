package com.capstone.nik.mixology.ui.ada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items as lazyListItems
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkListItem
import com.capstone.nik.mixology.ui.components.DrinkViewToggle
import com.capstone.nik.mixology.ui.components.EmptyState
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun AdaMoreRoute(
    query: String,
    exclude: List<String>,
    onBack: () -> Unit,
    onDrinkClick: (Drink) -> Unit,
    viewModel: AdaMoreViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(query, exclude) {
        viewModel.onIntent(AdaMoreIntent.Bind(query, exclude))
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is AdaMoreEffect.OpenDrink -> onDrinkClick(effect.drink)
        }
    }
    AdaMoreScreen(
        state = state,
        onBack = onBack,
        onLoadMore = { viewModel.onIntent(AdaMoreIntent.LoadMore) },
        onRetry = { viewModel.onIntent(AdaMoreIntent.Retry) },
        onDrinkClick = { viewModel.onIntent(AdaMoreIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(AdaMoreIntent.ToggleSaved(it)) },
        onToggleListView = { viewModel.onIntent(AdaMoreIntent.ToggleListView) },
    )
}

@Composable
fun AdaMoreScreen(
    state: AdaMoreUiState,
    onBack: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onToggleListView: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_up_navigation),
                    )
                }
                Text(
                    text = stringResource(R.string.ada_more_title, state.query),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                        .testTag("ada_more_title"),
                )
            }
            DrinkViewToggle(listView = state.listView, onToggleListView = onToggleListView)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.showEmpty -> EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.ada_more_empty),
                        modifier = Modifier.align(Alignment.Center),
                    )
                    state.listView -> AdaMoreList(state, onLoadMore, onRetry, onDrinkClick, onToggleSaved)
                    else -> AdaMoreGrid(state, onLoadMore, onRetry, onDrinkClick, onToggleSaved)
                }
            }
        }
    }
}

@Composable
private fun AdaMoreGrid(
    state: AdaMoreUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val nearEnd by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= info.totalItemsCount - LOAD_AHEAD
        }
    }
    LaunchedEffect(nearEnd, state.hasMore, state.loading) {
        if (nearEnd && state.hasMore && !state.loading) onLoadMore()
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize().testTag("ada_more_grid"),
        contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 50.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(state.drinks, key = { it.id }) { drink ->
            DrinkCard(
                item = drink,
                onClick = { onDrinkClick(drink) },
                onToggleSaved = { onToggleSaved(drink) },
            )
        }
        item(key = "ada_more_footer", span = { GridItemSpan(maxLineSpan) }) {
            AdaMoreFooter(state, onRetry)
        }
    }
}

@Composable
private fun AdaMoreList(
    state: AdaMoreUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= info.totalItemsCount - LOAD_AHEAD
        }
    }
    LaunchedEffect(nearEnd, state.hasMore, state.loading) {
        if (nearEnd && state.hasMore && !state.loading) onLoadMore()
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().testTag("ada_more_list"),
        contentPadding = PaddingValues(bottom = 50.dp),
    ) {
        lazyListItems(state.drinks, key = { it.id }) { drink ->
            DrinkListItem(drink = drink, onDrinkClick = onDrinkClick, onToggleSaved = onToggleSaved)
        }
        item(key = "ada_more_footer") {
            AdaMoreFooter(state, onRetry)
        }
    }
}

/** Spinner while a page loads, or the error with a retry. */
@Composable
private fun AdaMoreFooter(state: AdaMoreUiState, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.error.ifEmpty { stringResource(R.string.ada_error_generic) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
            }
            state.loading -> CircularProgressIndicator(modifier = Modifier.testTag("ada_more_loading"))
        }
    }
}

/** Start the next page this many items before the end. */
private const val LOAD_AHEAD = 6
