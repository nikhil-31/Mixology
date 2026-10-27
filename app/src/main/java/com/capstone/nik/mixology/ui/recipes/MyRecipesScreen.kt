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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyListItems
import androidx.compose.material.icons.Icons
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
import com.capstone.nik.mixology.ui.components.DrinkViewToggle
import com.capstone.nik.mixology.ui.components.EmptyState
import com.capstone.nik.mixology.ui.components.SavedDrinkCardAspectRatio
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun MyRecipesRoute(
    onRecipeClick: (Drink) -> Unit,
    onAddRecipe: () -> Unit,
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
        onRetry = { viewModel.onIntent(MyRecipesIntent.Retry) },
        onToggleListView = { viewModel.onIntent(MyRecipesIntent.ToggleListView) },
    )
}

@Composable
fun MyRecipesScreen(
    state: MyRecipesUiState,
    onRecipeClick: (Drink) -> Unit,
    onAddRecipe: () -> Unit,
    onRetry: () -> Unit,
    onToggleListView: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DrinkViewToggle(
                listView = state.listView,
                onToggleListView = onToggleListView,
                label = if (state.recipes.isEmpty()) {
                    null
                } else {
                    pluralStringResource(R.plurals.my_recipes_count, state.recipes.size, state.recipes.size)
                },
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.recipes.isEmpty() && state.loading -> CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("my_recipes_loading"),
                    )
                    state.recipes.isEmpty() && state.error != null -> EmptyState(
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
                            Button(onClick = onAddRecipe) { Text(stringResource(R.string.my_recipes_add)) }
                        },
                    )
                    state.listView -> LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("my_recipes_list"),
                        contentPadding = PaddingValues(bottom = FabClearance),
                    ) {
                        lazyListItems(state.recipes, key = { it.id }) { recipe ->
                            DrinkListItem(drink = recipe, onDrinkClick = onRecipeClick, onToggleSaved = {})
                        }
                    }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().testTag("my_recipes_grid"),
                        contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, FabClearance),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(state.recipes, key = { it.id }) { recipe ->
                            DrinkCard(
                                item = recipe,
                                onClick = { onRecipeClick(recipe) },
                                onToggleSaved = {},
                                posterAspectRatio = SavedDrinkCardAspectRatio,
                            )
                        }
                    }
                }
            }
        }
        // The empty state has its own Add button.
        if (!state.showEmpty) {
            ExtendedFloatingActionButton(
                onClick = onAddRecipe,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.my_recipes_add)) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("my_recipes_add"),
            )
        }
    }
}

/** Room under the last row so the FAB doesn't cover it. */
private val FabClearance = 88.dp
