package com.capstone.nik.mixology.ui.recipes

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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkListItem
import com.capstone.nik.mixology.ui.components.DrinkResultsHeader
import com.capstone.nik.mixology.ui.components.DrinkResultsNoMatches
import com.capstone.nik.mixology.ui.components.DrinkViewToggle
import com.capstone.nik.mixology.ui.components.EmptyState
import com.capstone.nik.mixology.ui.components.SavedDrinkCardAspectRatio
import com.capstone.nik.mixology.ui.components.bleedHorizontally
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun MyRecipesRoute(
    onRecipeClick: (Drink) -> Unit,
    onAddRecipe: () -> Unit,
    onImportRecipe: () -> Unit = {},
    viewModel: MyRecipesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is MyRecipesEffect.OpenRecipe -> onRecipeClick(effect.recipe)
            MyRecipesEffect.OpenEditor -> onAddRecipe()
        }
    }
    MyRecipesScreen(
        state = state,
        onRecipeClick = { viewModel.onIntent(MyRecipesIntent.OpenRecipe(it)) },
        onAddRecipe = { viewModel.onIntent(MyRecipesIntent.AddRecipe) },
        onImportRecipe = onImportRecipe,
        onRetry = { viewModel.onIntent(MyRecipesIntent.Retry) },
        onToggleListView = { viewModel.onIntent(MyRecipesIntent.ToggleListView) },
        onQueryChanged = { viewModel.onIntent(MyRecipesIntent.QueryChanged(it)) },
    )
}

@Composable
fun MyRecipesScreen(
    state: MyRecipesUiState,
    onRecipeClick: (Drink) -> Unit,
    onAddRecipe: () -> Unit,
    onRetry: () -> Unit,
    onToggleListView: () -> Unit,
    onQueryChanged: (String) -> Unit = {},
    onImportRecipe: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val count = pluralStringResource(R.plurals.my_recipes_count, state.recipes.size, state.recipes.size)
        // The search field and toggle lead the list, so they scroll away with the recipes.
        val header: @Composable (Modifier) -> Unit = { modifier ->
            DrinkResultsHeader(
                query = state.query,
                onQueryChanged = onQueryChanged,
                listView = state.listView,
                onToggleListView = onToggleListView,
                label = count,
                modifier = modifier,
                searchModifier = Modifier.testTag("my_recipes_search"),
            )
        }
        when {
            state.recipes.isNotEmpty() && state.listView -> LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("my_recipes_list"),
                contentPadding = PaddingValues(bottom = FabClearance),
            ) {
                item(key = HEADER_KEY) { header(Modifier) }
                if (state.noMatches) {
                    item(key = NO_MATCHES_KEY) { DrinkResultsNoMatches(Modifier.testTag("my_recipes_no_matches")) }
                }
                lazyListItems(state.visible, key = { it.id }) { recipe ->
                    DrinkListItem(drink = recipe, onDrinkClick = onRecipeClick, onToggleSaved = {})
                }
            }
            state.recipes.isNotEmpty() -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().testTag("my_recipes_grid"),
                contentPadding = PaddingValues(GridGutter, GridGutter, GridGutter, FabClearance),
                horizontalArrangement = Arrangement.spacedBy(GridGutter),
                verticalArrangement = Arrangement.spacedBy(GridGutter),
            ) {
                item(key = HEADER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    header(Modifier.bleedHorizontally(GridGutter))
                }
                if (state.noMatches) {
                    item(key = NO_MATCHES_KEY, span = { GridItemSpan(maxLineSpan) }) {
                        DrinkResultsNoMatches(Modifier.testTag("my_recipes_no_matches"))
                    }
                }
                items(state.visible, key = { it.id }) { recipe ->
                    DrinkCard(
                        item = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onToggleSaved = {},
                        posterAspectRatio = SavedDrinkCardAspectRatio,
                    )
                }
            }
            // Nothing to scroll yet: loading, an error, or no recipes at all.
            else -> Column(modifier = Modifier.fillMaxSize()) {
                DrinkViewToggle(listView = state.listView, onToggleListView = onToggleListView)
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.loading -> CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag("my_recipes_loading"),
                        )
                        state.error != null -> EmptyState(
                            icon = Icons.Outlined.CloudOff,
                            title = stringResource(R.string.my_recipes_error),
                            body = state.error.takeIf { it.isNotBlank() },
                            modifier = Modifier.align(Alignment.Center),
                            action = {
                                TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
                            },
                        )
                        state.showEmpty -> EmptyState(
                            icon = Icons.Outlined.EditNote,
                            title = stringResource(R.string.my_recipes_empty_title),
                            body = stringResource(R.string.my_recipes_empty_body),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag("my_recipes_empty"),
                            action = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Button(onClick = onAddRecipe) { Text(stringResource(R.string.my_recipes_add)) }
                                    OutlinedButton(
                                        onClick = onImportRecipe,
                                        modifier = Modifier.testTag("my_recipes_empty_import"),
                                    ) {
                                        Icon(Icons.Outlined.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text(
                                            stringResource(R.string.recipe_import_from_link),
                                            modifier = Modifier.padding(start = 8.dp),
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
        // The empty state has its own Add button.
        if (!state.showEmpty) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmallFloatingActionButton(
                    onClick = onImportRecipe,
                    modifier = Modifier.testTag("my_recipes_import"),
                ) {
                    Icon(Icons.Outlined.Link, contentDescription = stringResource(R.string.recipe_import_from_link))
                }
                ExtendedFloatingActionButton(
                    onClick = onAddRecipe,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.my_recipes_add)) },
                    modifier = Modifier.testTag("my_recipes_add"),
                )
            }
        }
    }
}

/** Room under the last row so the FAB doesn't cover it. */
private val FabClearance = 148.dp
private val GridGutter = 6.dp
private const val HEADER_KEY = "my_recipes_header"
private const val NO_MATCHES_KEY = "my_recipes_no_matches"

