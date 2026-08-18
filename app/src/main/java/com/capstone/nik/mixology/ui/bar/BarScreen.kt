package com.capstone.nik.mixology.ui.bar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.FavoriteButton
import com.capstone.nik.mixology.ui.components.IngredientImage
import com.capstone.nik.mixology.ui.model.ingredientImageUrl
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun BarRoute(
    onDrinkClick: (Drink) -> Unit,
    viewModel: BarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is BarEffect.OpenDrink -> onDrinkClick(effect.drink)
        }
    }
    BarScreen(
        state = state,
        onOpenPicker = { viewModel.onIntent(BarIntent.OpenPicker) },
        onClosePicker = { viewModel.onIntent(BarIntent.ClosePicker) },
        onQueryChanged = { viewModel.onIntent(BarIntent.QueryChanged(it)) },
        onToggleIngredient = { viewModel.onIntent(BarIntent.ToggleIngredient(it)) },
        onDrinkClick = { viewModel.onIntent(BarIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(BarIntent.ToggleSaved(it)) },
    )
}

@Composable
fun BarScreen(
    state: BarUiState,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggleIngredient: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    if (state.picking) {
        BarPicker(
            state = state,
            onClosePicker = onClosePicker,
            onQueryChanged = onQueryChanged,
            onToggleIngredient = onToggleIngredient,
        )
    } else {
        BarHome(
            state = state,
            onOpenPicker = onOpenPicker,
            onToggleIngredient = onToggleIngredient,
            onDrinkClick = onDrinkClick,
            onToggleSaved = onToggleSaved,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarHome(
    state: BarUiState,
    onOpenPicker: () -> Unit,
    onToggleIngredient: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.loading && state.bar.isEmpty() && state.makeable.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Button(
                        onClick = onOpenPicker,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("bar_add"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(stringResource(R.string.bar_add))
                    }
                }
                if (state.bar.isNotEmpty()) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.bar.forEach { name ->
                                InputChip(
                                    selected = true,
                                    onClick = { onToggleIngredient(name) },
                                    label = { Text(name) },
                                    leadingIcon = {
                                        IngredientImage(
                                            url = ingredientImageUrl(name),
                                            size = 24.dp,
                                            contentDescription = name,
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(
                                                R.string.bar_remove_ingredient,
                                                name,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.testTag("bar_chip_$name"),
                                )
                            }
                        }
                    }
                }
                if (state.bar.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.bar_empty),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (state.makeable.isNotEmpty()) {
                    item {
                        BarSectionTitle(stringResource(R.string.bar_you_can_make))
                    }
                    items(state.makeable, key = { "makeable-${it.id}" }) { drink ->
                        BarDrinkItem(
                            drink = drink,
                            missing = emptyList(),
                            onDrinkClick = onDrinkClick,
                            onToggleSaved = onToggleSaved,
                        )
                    }
                }
                if (state.almost.isNotEmpty()) {
                    item {
                        BarSectionTitle(stringResource(R.string.bar_almost))
                    }
                    items(state.almost, key = { "almost-${it.drink.id}" }) { almost ->
                        BarDrinkItem(
                            drink = almost.drink,
                            missing = almost.missing,
                            onDrinkClick = onDrinkClick,
                            onToggleSaved = onToggleSaved,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun BarDrinkItem(
    drink: Drink,
    missing: List<String>,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDrinkClick(drink) }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DrinkImage(
                url = drink.thumb,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = drink.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (missing.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.bar_needs, missing.joinToString()),
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FavoriteButton(
                saved = drink.saved,
                onClick = { onToggleSaved(drink) },
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun BarPicker(
    state: BarUiState,
    onClosePicker: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggleIngredient: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.bar_add_ingredients),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TextButton(
                onClick = onClosePicker,
                modifier = Modifier.testTag("bar_done"),
            ) {
                Text(stringResource(R.string.bar_done))
            }
        }
        var queryText by remember { mutableStateOf(state.query) }
        LaunchedEffect(state.query) {
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
                state.catalogTerms.isEmpty() ->
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
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(state.visibleTerms, key = { it }) { term ->
                        BarTermRow(
                            term = term,
                            selected = state.inBar(term),
                            onToggle = onToggleIngredient,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun BarTermRow(
    term: String,
    selected: Boolean,
    onToggle: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(term) }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("bar_term_$term"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IngredientImage(
            url = ingredientImageUrl(term),
            size = 48.dp,
            contentDescription = term,
        )
        Text(
            text = term,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.bar_in_bar),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
