package com.capstone.nik.mixology.ui.shopping

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun ShoppingRoute(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onDrinkClick: (Drink) -> Unit = {},
    onFindRecipe: () -> Unit = {},
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val undoLabel = stringResource(R.string.shopping_undo)
    val otherLabel = stringResource(R.string.shopping_other)
    val shareTitle = stringResource(R.string.shopping_share)

    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is ShoppingEffect.ShowUndo -> {
                val message = context.resources.getQuantityString(effect.messageRes, effect.count, effect.count)
                // A newer removal replaces the older prompt, so Undo always means the latest one.
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onIntent(ShoppingIntent.Undo)
            }
        }
    }

    ShoppingScreen(
        state = state,
        onToggle = { viewModel.onIntent(ShoppingIntent.Toggle(it)) },
        onRemove = { viewModel.onIntent(ShoppingIntent.Remove(it)) },
        onClearChecked = { viewModel.onIntent(ShoppingIntent.ClearChecked) },
        onByIngredientChange = { viewModel.onIntent(ShoppingIntent.SetByIngredient(it)) },
        onToggleCombined = { viewModel.onIntent(ShoppingIntent.ToggleCombined(it)) },
        onRemoveCombined = { viewModel.onIntent(ShoppingIntent.RemoveCombined(it)) },
        onRemoveDrink = { viewModel.onIntent(ShoppingIntent.RemoveDrink(it)) },
        onDrinkClick = onDrinkClick,
        onRefresh = { viewModel.onIntent(ShoppingIntent.Refresh) },
        onFindRecipe = onFindRecipe,
        onShare = {
            shoppingShareText(state.sections, state.byIngredient, otherLabel)?.let { text ->
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(send, shareTitle))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    state: ShoppingUiState,
    onToggle: (ShoppingItemEntity) -> Unit,
    onRemove: (ShoppingItemEntity) -> Unit,
    onClearChecked: () -> Unit,
    onByIngredientChange: (Boolean) -> Unit = {},
    onToggleCombined: (CombinedShoppingItem) -> Unit = {},
    onRemoveCombined: (CombinedShoppingItem) -> Unit = {},
    onRemoveDrink: (String) -> Unit = {},
    onDrinkClick: (Drink) -> Unit = {},
    onRefresh: () -> Unit = {},
    onFindRecipe: () -> Unit = {},
    onShare: () -> Unit = {},
) {
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.sections.isEmpty()) {
            ShoppingEmpty(onFindRecipe = onFindRecipe)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "controls") {
                    ShoppingControls(
                        state = state,
                        onByIngredientChange = onByIngredientChange,
                        onClearChecked = onClearChecked,
                        onShare = onShare,
                    )
                }
                if (state.byIngredient) {
                    combinedRows(state.combined, onToggleCombined, onRemoveCombined)
                } else {
                    state.sections.forEach { section ->
                        item(key = "section-${section.drinkId}") {
                            ShoppingSectionHeader(
                                section = section,
                                onDrinkClick = onDrinkClick,
                                onRemoveDrink = onRemoveDrink,
                            )
                        }
                        items(sortCheckedLast(section.items), key = { it.id }) { item ->
                            SwipeToRemove(onRemove = { onRemove(item) }) {
                                ShoppingRow(
                                    name = item.name,
                                    detail = null,
                                    measure = section.measure(item),
                                    checked = item.checked,
                                    onToggle = { onToggle(item) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.combinedRows(
    rows: List<CombinedShoppingItem>,
    onToggle: (CombinedShoppingItem) -> Unit,
    onRemove: (CombinedShoppingItem) -> Unit,
) {
    items(rows, key = { "combined-${it.key}" }) { row ->
        SwipeToRemove(onRemove = { onRemove(row) }) {
            ShoppingRow(
                name = row.name,
                detail = row.drinkNames.joinToString(" · ").takeIf { it.isNotEmpty() },
                measure = row.measure,
                checked = row.checked,
                onToggle = { onToggle(row) },
            )
        }
    }
}

@Composable
private fun ShoppingControls(
    state: ShoppingUiState,
    onByIngredientChange: (Boolean) -> Unit,
    onClearChecked: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val allDone = state.total > 0 && state.checkedCount == state.total
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                SegmentedButton(
                    selected = !state.byIngredient,
                    onClick = { onByIngredientChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("shopping_by_drink"),
                ) { Text(stringResource(R.string.shopping_by_drink)) }
                SegmentedButton(
                    selected = state.byIngredient,
                    onClick = { onByIngredientChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("shopping_by_ingredient"),
                ) { Text(stringResource(R.string.shopping_by_ingredient)) }
            }
            IconButton(
                onClick = onShare,
                enabled = !allDone,
                modifier = Modifier.testTag("shopping_share"),
            ) {
                Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.shopping_share))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (allDone) {
                    stringResource(R.string.shopping_all_done)
                } else {
                    pluralStringResource(R.plurals.shopping_progress, state.total, state.checkedCount, state.total)
                },
                modifier = Modifier.weight(1f).testTag("shopping_progress"),
                style = MaterialTheme.typography.titleSmall,
                color = if (allDone) colors.primary else colors.onSurface,
            )
            if (state.checkedCount > 0) {
                TextButton(onClick = onClearChecked, modifier = Modifier.testTag("shopping_clear_checked")) {
                    Text(stringResource(R.string.shopping_clear_checked))
                }
            }
        }
        LinearProgressIndicator(
            progress = { if (state.total == 0) 0f else state.checkedCount.toFloat() / state.total },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun ShoppingSectionHeader(
    section: ShoppingSection,
    onDrinkClick: (Drink) -> Unit,
    onRemoveDrink: (String) -> Unit,
) {
    val drink = section.drink
    if (drink == null) {
        Text(
            text = stringResource(R.string.shopping_other),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        return
    }
    var menu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDrinkClick(drink) }
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            .testTag("shopping_section_${section.drinkId}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DrinkImage(
            url = drink.thumb,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Text(
            text = drink.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.shopping_more_options))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.shopping_remove_drink)) },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    onClick = {
                        menu = false
                        onRemoveDrink(section.drinkId)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    // The row itself offers Remove to accessibility services.
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        val removeLabel = stringResource(R.string.shopping_remove_item)
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .testTag("shopping_row")
                .semantics {
                    customActions = listOf(CustomAccessibilityAction(removeLabel) { onRemove(); true })
                },
        ) { content() }
    }
}

@Composable
private fun ShoppingRow(
    name: String,
    detail: String?,
    measure: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (checked) colors.onSurfaceVariant else colors.onSurface,
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (measure.isNotEmpty()) {
            Text(
                text = measure,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun ShoppingEmpty(onFindRecipe: () -> Unit) {
    // Scrollable so pull-to-refresh still works on an empty list.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
            .testTag("shopping_empty"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.padding(top = 96.dp))
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.shopping_empty_title),
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.shopping_empty),
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(onClick = onFindRecipe, modifier = Modifier.testTag("shopping_find_recipe")) {
            Text(stringResource(R.string.shopping_find_recipe))
        }
    }
}
