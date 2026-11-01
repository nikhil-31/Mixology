package com.capstone.nik.mixology.ui.bar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarUnlock
import com.capstone.nik.mixology.ui.components.DrinkListItem
import com.capstone.nik.mixology.ui.components.ConnectionErrorState
import com.capstone.nik.mixology.ui.components.EmptyState
import com.capstone.nik.mixology.ui.components.IngredientImage
import com.capstone.nik.mixology.ui.components.SearchField
import com.capstone.nik.mixology.ui.components.SectionHeader
import com.capstone.nik.mixology.ui.model.ingredientImageUrl
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import kotlinx.coroutines.launch

/** Bars bigger than this collapse behind "Show all", so the drinks stay on screen. */
internal const val BAR_COLLAPSED_CHIP_COUNT = 10

@Composable
fun BarRoute(
    onDrinkClick: (Drink) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: BarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is BarEffect.OpenDrink -> onDrinkClick(effect.drink)
            is BarEffect.ShowRemoved -> {
                // A second removal replaces the first snackbar rather than queueing behind it.
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.bar_removed, effect.name),
                        actionLabel = context.getString(R.string.bar_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(BarIntent.AddIngredient(effect.name))
                    }
                }
            }
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
        onRetry = { viewModel.onIntent(BarIntent.Retry) },
        onRemoveIngredient = { viewModel.onIntent(BarIntent.RemoveIngredient(it)) },
        onAddIngredient = { viewModel.onIntent(BarIntent.AddIngredient(it)) },
        onRefresh = { viewModel.onIntent(BarIntent.Refresh) },
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
    onRetry: () -> Unit = {},
    onRemoveIngredient: (String) -> Unit = onToggleIngredient,
    onAddIngredient: (String) -> Unit = onToggleIngredient,
    onRefresh: () -> Unit = {},
) {
    if (state.picking) {
        BarPicker(
            state = state,
            onClosePicker = onClosePicker,
            onQueryChanged = onQueryChanged,
            onToggleIngredient = onToggleIngredient,
            onRetry = onRetry,
        )
    } else {
        BarHome(
            state = state,
            onOpenPicker = onOpenPicker,
            onRemoveIngredient = onRemoveIngredient,
            onAddIngredient = onAddIngredient,
            onDrinkClick = onDrinkClick,
            onToggleSaved = onToggleSaved,
            onRefresh = onRefresh,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BarHome(
    state: BarUiState,
    onOpenPicker: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onAddIngredient: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onRefresh: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val add: (String) -> Unit = { name ->
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onAddIngredient(name)
    }
    var showAllChips by rememberSaveable { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.loading && state.bar.isEmpty() && state.makeable.isEmpty() ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.bar.isEmpty() -> EmptyState(
                icon = Icons.Outlined.LocalBar,
                title = stringResource(R.string.bar_empty_title),
                body = stringResource(R.string.bar_empty),
                modifier = Modifier.align(Alignment.Center),
                action = { AddIngredientsButton(onOpenPicker, Modifier.testTag("bar_add")) },
            )
            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item(key = "bar-header") {
                        SectionHeader(
                            title = stringResource(R.string.bar_your_bar),
                            supporting = barSummary(state),
                            trailing = {
                                FilledTonalButton(
                                    onClick = onOpenPicker,
                                    contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                                    modifier = Modifier.testTag("bar_add"),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 6.dp).size(18.dp),
                                    )
                                    Text(stringResource(R.string.bar_add))
                                }
                            },
                        )
                    }
                    item(key = "bar-chips") {
                        BarChips(
                            bar = state.bar,
                            expanded = showAllChips,
                            onToggleExpanded = { showAllChips = !showAllChips },
                            onRemove = onRemoveIngredient,
                        )
                    }
                    val unlocks = state.unlocks
                    if (unlocks.isNotEmpty()) {
                        item(key = "bar-unlocks-header") {
                            SectionHeader(
                                title = stringResource(R.string.bar_buy_next_title),
                                supporting = stringResource(R.string.bar_buy_next_info),
                            )
                        }
                        item(key = "bar-unlocks") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(unlocks, key = { "unlock-${it.ingredient}" }) { unlock ->
                                    BarUnlockCard(unlock = unlock, onAdd = { add(unlock.ingredient) })
                                }
                            }
                        }
                    }
                    if (state.noMatches) {
                        item(key = "bar-no-matches") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                                    .testTag("bar_no_matches"),
                            ) {
                                EmptyState(
                                    icon = Icons.Outlined.LocalBar,
                                    title = stringResource(R.string.bar_no_matches_title),
                                    body = stringResource(R.string.bar_no_matches_message),
                                    modifier = Modifier.align(Alignment.Center),
                                    action = {
                                        AddIngredientsButton(onOpenPicker, Modifier.testTag("bar_no_matches_add"))
                                    },
                                )
                            }
                        }
                    }
                    if (state.makeable.isNotEmpty()) {
                        item(key = "makeable-header") {
                            SectionHeader(
                                title = stringResource(R.string.bar_you_can_make),
                                supporting = stringResource(R.string.bar_you_can_make_info),
                            )
                        }
                        items(state.makeable, key = { "makeable-${it.id}" }) { drink ->
                            DrinkListItem(
                                drink = drink,
                                onDrinkClick = onDrinkClick,
                                onToggleSaved = onToggleSaved,
                            )
                        }
                    }
                    if (state.almost.isNotEmpty()) {
                        item(key = "almost-header") {
                            SectionHeader(
                                title = stringResource(R.string.bar_almost),
                                supporting = stringResource(R.string.bar_almost_info),
                            )
                        }
                        items(state.almost, key = { "almost-${it.drink.id}" }) { almost ->
                            DrinkListItem(
                                drink = almost.drink,
                                subtitle = if (almost.missing.isNotEmpty()) {
                                    stringResource(R.string.bar_needs, almost.missing.joinToString())
                                } else {
                                    null
                                },
                                onDrinkClick = onDrinkClick,
                                onToggleSaved = onToggleSaved,
                                footer = if (almost.missing.isNotEmpty()) {
                                    {
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            almost.missing.forEach { name ->
                                                BarAddChip(
                                                    name = name,
                                                    onAdd = { add(name) },
                                                    modifier = Modifier.testTag("bar_almost_add_${almost.drink.id}_$name"),
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** "8 ingredients · 12 ready · 5 almost", with the drink counts left out until they've loaded. */
@Composable
private fun barSummary(state: BarUiState): String {
    val parts = mutableListOf(pluralStringResource(R.plurals.bar_count, state.bar.size, state.bar.size))
    if (!state.loading) {
        parts += pluralStringResource(R.plurals.bar_ready_count, state.makeable.size, state.makeable.size)
        parts += pluralStringResource(R.plurals.bar_almost_count, state.almost.size, state.almost.size)
    }
    return parts.joinToString(" · ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarChips(
    bar: List<String>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRemove: (String) -> Unit,
) {
    val collapsible = bar.size > BAR_COLLAPSED_CHIP_COUNT
    val shown = if (collapsible && !expanded) bar.take(BAR_COLLAPSED_CHIP_COUNT) else bar
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        shown.forEach { name ->
            InputChip(
                selected = true,
                onClick = { onRemove(name) },
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
                        contentDescription = stringResource(R.string.bar_remove_ingredient, name),
                        modifier = Modifier.size(18.dp),
                    )
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("bar_chip_$name"),
            )
        }
        if (collapsible) {
            TextButton(
                onClick = onToggleExpanded,
                modifier = Modifier.testTag("bar_show_all"),
            ) {
                Text(
                    if (expanded) {
                        stringResource(R.string.bar_show_less)
                    } else {
                        stringResource(R.string.bar_show_all, bar.size)
                    },
                )
            }
        }
    }
}

/** "+ Lime · unlocks 4 drinks": an ingredient that would make the most "almost" drinks. */
@Composable
private fun BarUnlockCard(unlock: BarUnlock, onAdd: () -> Unit) {
    val label = stringResource(R.string.bar_add_named, unlock.ingredient)
    val unlocks = pluralStringResource(R.plurals.bar_unlocks, unlock.count, unlock.count)
    ElevatedCard(
        onClick = onAdd,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .testTag("bar_unlock_${unlock.ingredient}")
            .semantics(mergeDescendants = true) { contentDescription = "$label, $unlocks" },
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IngredientImage(
                url = ingredientImageUrl(unlock.ingredient),
                size = 32.dp,
                contentDescription = null,
            )
            Column(modifier = Modifier.widthIn(max = 160.dp)) {
                Text(
                    text = unlock.ingredient,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = unlocks,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/** "+ Lime", a one-tap add for an ingredient a drink is missing. */
@Composable
private fun BarAddChip(name: String, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.bar_add_named, name)
    AssistChip(
        onClick = onAdd,
        label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(32.dp)
            .semantics { contentDescription = label },
    )
}

@Composable
private fun AddIngredientsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp).size(18.dp),
        )
        Text(stringResource(R.string.bar_add_ingredients))
    }
}

@Composable
private fun BarPicker(
    state: BarUiState,
    onClosePicker: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onToggleIngredient: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClosePicker) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.bar_done))
            }
            Text(
                text = stringResource(R.string.bar_add_ingredients),
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            FilledTonalButton(
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
        SearchField(
            value = queryText,
            onValueChange = { value ->
                queryText = value
                onQueryChanged(value)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val terms = state.pickerTerms()
            when {
                state.catalogTerms.isEmpty() && state.error != null -> ConnectionErrorState(
                    error = state.error,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                // Still loading: placeholder rows, not a "nothing found" message.
                state.catalogTerms.isEmpty() -> Column(modifier = Modifier.testTag("bar_picker_loading")) {
                    repeat(10) { BarTermSkeleton() }
                }
                state.visibleTerms.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.network_error_search_no_data_available),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    if (terms.selected.isNotEmpty()) {
                        item(key = "header-in-bar") {
                            BarPickerHeader(stringResource(R.string.bar_picker_in_bar, terms.selected.size))
                        }
                        items(terms.selected, key = { "in-bar-$it" }) { term ->
                            BarTermRow(term = term, selected = true, onToggle = onToggleIngredient)
                            BarTermDivider()
                        }
                        item(key = "header-all") {
                            BarPickerHeader(stringResource(R.string.bar_picker_all))
                        }
                    }
                    items(terms.rest, key = { it }) { term ->
                        BarTermRow(
                            term = term,
                            selected = state.inBar(term),
                            onToggle = onToggleIngredient,
                        )
                        BarTermDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun BarPickerHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun BarTermDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 84.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    )
}

@Composable
private fun BarTermSkeleton() {
    val block = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(48.dp).background(block, CircleShape))
        Box(Modifier.size(width = 140.dp, height = 14.dp).background(block, RoundedCornerShape(4.dp)))
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
            .then(
                if (selected) {
                    Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                } else {
                    Modifier
                },
            )
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
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.bar_in_bar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
