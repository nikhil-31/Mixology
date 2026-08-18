package com.capstone.nik.mixology.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.components.IngredientImage
import com.capstone.nik.mixology.ui.model.ingredientImageUrl
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

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
    )
}

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    onSelectKind: (FilterKind) -> Unit,
    onQueryChanged: (String) -> Unit,
    onOpenTerm: (String) -> Unit,
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
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    items(state.visibleTerms, key = { it }) { term ->
                        CatalogTermRow(
                            term = term,
                            showIngredientImage = state.kind == FilterKind.INGREDIENT,
                            onOpenTerm = onOpenTerm,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogTermRow(
    term: String,
    showIngredientImage: Boolean,
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
        if (showIngredientImage) {
            IngredientImage(
                url = ingredientImageUrl(term),
                size = 48.dp,
                contentDescription = term,
            )
        }
        Text(
            text = term,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
