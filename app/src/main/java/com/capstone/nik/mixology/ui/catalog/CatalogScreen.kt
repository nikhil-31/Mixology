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
import com.capstone.nik.mixology.ui.components.IngredientImage
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
) {
    Column(modifier = Modifier.fillMaxSize()) {
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
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.loading && state.terms.isEmpty() ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.terms.isEmpty() ->
                        Text(
                            text = stringResource(R.string.network_error_search_no_data_available),
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    state.visibleTerms.isEmpty() ->
                        Text(
                            text = stringResource(R.string.network_error_search_no_data_available),
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    catalogUsesGrid(state.terms.size) -> CatalogTermGrid(
                        terms = state.visibleTerms,
                        kind = state.kind,
                        bar = state.bar,
                        onOpenTerm = onOpenTerm,
                    )
                    else -> CatalogTermIndex(
                        terms = state.visibleTerms,
                        kind = state.kind,
                        bar = state.bar,
                        onOpenTerm = onOpenTerm,
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogTermGrid(
    terms: List<String>,
    kind: FilterKind,
    bar: Set<String>,
    onOpenTerm: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
) {
    val sections = groupCatalogTermsByLetter(terms)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showIndex = sections.size >= 6
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp, end = if (showIndex) 28.dp else 0.dp),
        ) {
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
                    val target = sections.take(index).sumOf { it.terms.size + 1 }
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
