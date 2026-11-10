package com.capstone.nik.mixology.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.SportsBar
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.components.ConnectionErrorState
import com.capstone.nik.mixology.ui.components.IngredientImage
import com.capstone.nik.mixology.ui.components.LocalCollapsingHeader
import com.capstone.nik.mixology.ui.components.bleedHorizontally
import com.capstone.nik.mixology.ui.components.collapsingHeaderScroll
import com.capstone.nik.mixology.ui.model.ingredientImageUrl
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import kotlinx.coroutines.launch

private val catalogTabs = listOf(
    FilterKind.INGREDIENT to R.string.nav_title_favourite_ingredients,
    FilterKind.DRINK_TYPE to R.string.nav_title_category,
    FilterKind.GLASS to R.string.nav_title_glass,
    FilterKind.ALCOHOL to R.string.nav_title_alcoholic,
)

@Composable
fun CatalogRoute(
    onOpenFilter: (DrinkFilter) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is CatalogEffect.OpenFilter -> onOpenFilter(effect.filter)
        }
    }
    CatalogScreen(
        state = state,
        onSelectKind = { viewModel.onIntent(CatalogIntent.SelectKind(it)) },
        onQueryChanged = { viewModel.onIntent(CatalogIntent.QueryChanged(it)) },
        onOpenTerm = { viewModel.onIntent(CatalogIntent.OpenTerm(it)) },
        onRefresh = { viewModel.onIntent(CatalogIntent.Refresh) },
        onRetry = { viewModel.onIntent(CatalogIntent.Retry) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onSelectKind: (FilterKind) -> Unit,
    onQueryChanged: (String) -> Unit,
    onOpenTerm: (String) -> Unit,
    onRefresh: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    // The kind chips and search field lead the list, so they scroll away with the terms.
    val header: @Composable () -> Unit = {
        CatalogHeader(state = state, onSelectKind = onSelectKind, onQueryChanged = onQueryChanged)
    }
    val collapsingHeader = LocalCollapsingHeader.current
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            state.terms.isNotEmpty() && catalogUsesGrid(state.terms.size) -> CatalogTermGrid(
                terms = state.visibleTerms,
                kind = state.kind,
                bar = state.bar,
                onOpenTerm = onOpenTerm,
                header = header,
                modifier = Modifier.collapsingHeaderScroll(collapsingHeader),
            )
            state.terms.isNotEmpty() -> CatalogTermIndex(
                terms = state.visibleTerms,
                kind = state.kind,
                bar = state.bar,
                onOpenTerm = onOpenTerm,
                header = header,
                modifier = Modifier.collapsingHeaderScroll(collapsingHeader),
            )
            // Nothing to scroll yet: loading, an error, or an empty catalog.
            else -> Column(modifier = Modifier.fillMaxSize()) {
                header()
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        state.error != null -> ConnectionErrorState(
                            error = state.error,
                            onRetry = onRetry,
                            modifier = Modifier.align(Alignment.Center),
                        )
                        else -> CatalogNoTerms(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogHeader(
    state: CatalogUiState,
    onSelectKind: (FilterKind) -> Unit,
    onQueryChanged: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(catalogTabs, key = { it.first.name }) { (kind, titleRes) ->
                FilterChip(
                    selected = state.kind == kind,
                    onClick = { onSelectKind(kind) },
                    label = { Text(stringResource(titleRes)) },
                )
            }
        }
        var queryText by remember(state.kind) { mutableStateOf(state.query) }
        LaunchedEffect(state.query, state.kind) {
            if (queryText != state.query) {
                queryText = state.query
            }
        }
        TextField(
            value = queryText,
            onValueChange = { value ->
                queryText = value
                onQueryChanged(value)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.action_search)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
    }
}

@Composable
private fun CatalogNoTerms(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.network_error_search_no_data_available),
        modifier = modifier.padding(24.dp),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun CatalogTermGrid(
    terms: List<String>,
    kind: FilterKind,
    bar: Set<String>,
    onOpenTerm: (String) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        // No top padding: the header sits right under the toolbar, as it did above the grid.
        contentPadding = PaddingValues(start = GridPadding, end = GridPadding, bottom = GridPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.bleedHorizontally(GridPadding)) { header() }
        }
        if (terms.isEmpty()) {
            item(key = NO_MATCHES_KEY, span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CatalogNoTerms() }
            }
        }
        items(terms, key = { it }) { term ->
            CatalogTermTile(
                term = term,
                kind = kind,
                inBar = kind == FilterKind.INGREDIENT && term.lowercase() in bar,
                onOpenTerm = onOpenTerm,
            )
        }
    }
}

@Composable
private fun CatalogTermIndex(
    terms: List<String>,
    kind: FilterKind,
    bar: Set<String>,
    onOpenTerm: (String) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sections = groupCatalogTermsByLetter(terms)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showIndex = sections.size >= 6
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp, end = if (showIndex) 28.dp else 0.dp),
        ) {
            item(key = HEADER_KEY) { header() }
            if (terms.isEmpty()) {
                item(key = NO_MATCHES_KEY) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CatalogNoTerms() }
                }
            }
            sections.forEach { section ->
                item(key = "letter-${section.letter}") {
                    Text(
                        text = section.letter,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(section.terms, key = { "${section.letter}-$it" }) { term ->
                    CatalogTermRow(
                        term = term,
                        kind = kind,
                        inBar = kind == FilterKind.INGREDIENT && term.lowercase() in bar,
                        onOpenTerm = onOpenTerm,
                    )
                    HorizontalDivider()
                }
            }
        }
        if (showIndex) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                sections.forEachIndexed { index, section ->
                    // The header is item 0, so each letter is one further down.
                    val target = 1 + sections.take(index).sumOf { it.terms.size + 1 }
                    Text(
                        text = section.letter,
                        modifier = Modifier
                            .clickable {
                                scope.launch { listState.animateScrollToItem(target) }
                            }
                            .padding(vertical = 1.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

private val GridPadding = 16.dp
private const val HEADER_KEY = "catalog_header"
private const val NO_MATCHES_KEY = "catalog_no_matches"

@Composable
private fun CatalogTermTile(
    term: String,
    kind: FilterKind,
    inBar: Boolean,
    onOpenTerm: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenTerm(term) }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CatalogTermIcon(term = term, kind = kind, size = 44.dp)
        Text(
            text = term,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (inBar) {
            InBarLabel()
        }
    }
}

@Composable
private fun CatalogTermRow(
    term: String,
    kind: FilterKind,
    inBar: Boolean,
    onOpenTerm: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTerm(term) }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CatalogTermIcon(term = term, kind = kind, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = term,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (inBar) {
                InBarLabel()
            }
        }
    }
}

@Composable
private fun InBarLabel() {
    Text(
        text = stringResource(R.string.bar_in_bar),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CatalogTermIcon(
    term: String,
    kind: FilterKind,
    size: androidx.compose.ui.unit.Dp,
) {
    if (kind == FilterKind.INGREDIENT) {
        IngredientImage(
            url = ingredientImageUrl(term),
            size = size,
            contentDescription = term,
        )
        return
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = catalogSymbol(kind, term),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun catalogSymbol(kind: FilterKind, term: String): ImageVector {
    val name = term.lowercase()
    return when (kind) {
        FilterKind.INGREDIENT -> Icons.Filled.LocalBar
        FilterKind.ALCOHOL -> when {
            name.contains("non") -> Icons.Filled.WaterDrop
            name.contains("optional") -> Icons.Filled.HelpOutline
            else -> Icons.Filled.LocalBar
        }
        FilterKind.GLASS -> when {
            name.contains("beer") || name.contains("pint") || name.contains("pilsner") -> Icons.Filled.SportsBar
            name.contains("coffee") || name.contains("cup") || name.contains("mug") -> Icons.Filled.Coffee
            else -> Icons.Filled.WineBar
        }
        FilterKind.DRINK_TYPE -> when {
            name.contains("beer") -> Icons.Filled.SportsBar
            name.contains("coffee") || name.contains("tea") || name.contains("cocoa") -> Icons.Filled.LocalCafe
            name.contains("punch") || name.contains("party") -> Icons.Filled.Celebration
            name.contains("shot") -> Icons.Filled.LocalDrink
            name.contains("soft") || name.contains("shake") -> Icons.Filled.WaterDrop
            else -> Icons.Filled.WineBar
        }
    }
}
