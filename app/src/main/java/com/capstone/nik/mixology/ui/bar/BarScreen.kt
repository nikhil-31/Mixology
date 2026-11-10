package com.capstone.nik.mixology.ui.bar

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import com.capstone.nik.mixology.ui.components.ScrollToTopOnReselect
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import com.capstone.nik.mixology.repository.BarAlmostDrink
import com.capstone.nik.mixology.repository.BarCategory
import com.capstone.nik.mixology.repository.BarTerm
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
import com.capstone.nik.mixology.ui.components.LocalCollapsingHeader
import com.capstone.nik.mixology.ui.components.SearchField
import com.capstone.nik.mixology.ui.components.SectionHeader
import com.capstone.nik.mixology.ui.components.collapsingHeaderScroll
import com.capstone.nik.mixology.ui.model.ingredientImageUrl
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import kotlinx.coroutines.launch

/** Bars bigger than this collapse behind "Show all", so the drinks stay on screen. */
internal const val BAR_COLLAPSED_CHIP_COUNT = 10

/** "You can make" shows this many drinks until "See all" is tapped. */
internal const val BAR_COLLAPSED_DRINK_COUNT = 8

/** The heading for one of the bar's shelves. */
@StringRes
internal fun barCategoryLabel(category: String): Int = when (category) {
    BarCategory.SPIRIT -> R.string.bar_category_spirit
    BarCategory.LIQUEUR -> R.string.bar_category_liqueur
    BarCategory.WINE -> R.string.bar_category_wine
    BarCategory.MIXER -> R.string.bar_category_mixer
    BarCategory.SYRUP -> R.string.bar_category_syrup
    BarCategory.BITTERS -> R.string.bar_category_bitters
    BarCategory.FRESH -> R.string.bar_category_fresh
    else -> R.string.bar_category_other
}

@Composable
fun BarRoute(
    onDrinkClick: (Drink) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onOpenAda: () -> Unit = {},
    viewModel: BarViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // A photo of the shelf goes to Ada, who names the bottles and offers to add them.
    val choosePhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            viewModel.onIntent(BarIntent.SendPhotoToAda(uri, context.getString(R.string.bar_photo_caption)))
        }
    }
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
            is BarEffect.ShowAddedToShopping -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.bar_added_to_shopping, effect.name),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
            is BarEffect.ShowAlreadyOnShopping -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.bar_already_on_shopping, effect.name),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
            BarEffect.OpenAda -> onOpenAda()
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
        onChooseAdd = { viewModel.onIntent(BarIntent.ChooseAdd(it)) },
        onDismissAddChoice = { viewModel.onIntent(BarIntent.DismissAddChoice) },
        onAddToShopping = { name, drinkId -> viewModel.onIntent(BarIntent.AddToShopping(name, drinkId)) },
        onPickerCategory = { viewModel.onIntent(BarIntent.PickerCategoryChanged(it)) },
        onAskAda = { viewModel.onIntent(BarIntent.AskAda(context.getString(R.string.bar_ask_ada_prompt))) },
        onAddFromPhoto = {
            choosePhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
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
    onRemoveIngredient: (String) -> Unit = {},
    onAddIngredient: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
    onChooseAdd: (BarAddChoice) -> Unit = {},
    onDismissAddChoice: () -> Unit = {},
    onAddToShopping: (name: String, drinkId: String) -> Unit = { _, _ -> },
    onPickerCategory: (String?) -> Unit = {},
    onAskAda: () -> Unit = {},
    onAddFromPhoto: () -> Unit = {},
) {
    if (state.picking) {
        // Back closes the picker instead of leaving the tab.
        BackHandler(onBack = onClosePicker)
        BarPicker(
            state = state,
            onClosePicker = onClosePicker,
            onQueryChanged = onQueryChanged,
            onToggleIngredient = onToggleIngredient,
            onRetry = onRetry,
            onCategory = onPickerCategory,
        )
    } else {
        BarHome(
            state = state,
            onOpenPicker = onOpenPicker,
            onRemoveIngredient = onRemoveIngredient,
            onDrinkClick = onDrinkClick,
            onToggleSaved = onToggleSaved,
            onRefresh = onRefresh,
            onChooseAdd = onChooseAdd,
            onRetry = onRetry,
            onAskAda = onAskAda,
            onAddFromPhoto = onAddFromPhoto,
        )
        state.addChoice?.let { choice ->
            BarAddSheet(
                choice = choice,
                onAddToBar = { onAddIngredient(choice.ingredient) },
                onAddToShopping = { onAddToShopping(choice.ingredient, choice.drink?.id.orEmpty()) },
                onDismiss = onDismissAddChoice,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BarHome(
    state: BarUiState,
    onOpenPicker: () -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onRefresh: () -> Unit,
    onChooseAdd: (BarAddChoice) -> Unit,
    onRetry: () -> Unit,
    onAskAda: () -> Unit,
    onAddFromPhoto: () -> Unit,
) {
    var showAllChips by rememberSaveable { mutableStateOf(false) }
    var showAllMakeable by rememberSaveable { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.loading && state.bar.isEmpty() && state.makeable.isEmpty() ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.bar.isEmpty() -> EmptyState(
                icon = Icons.Outlined.LocalBar,
                title = stringResource(R.string.bar_empty_title),
                body = stringResource(R.string.bar_empty),
                modifier = Modifier.align(Alignment.Center),
                action = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AddIngredientsButton(onOpenPicker, Modifier.testTag("bar_add"))
                        TextButton(onClick = onAddFromPhoto, modifier = Modifier.testTag("bar_empty_photo")) {
                            Icon(
                                imageVector = Icons.Outlined.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp).size(18.dp),
                            )
                            Text(stringResource(R.string.bar_add_photo))
                        }
                    }
                },
            )
            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                val listState = rememberLazyListState()
                ScrollToTopOnReselect(listState)
                val withSwaps = remember(state.almost) { state.withSwaps }
                val needsShopping = remember(state.almost) { state.needsShopping }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .collapsingHeaderScroll(LocalCollapsingHeader.current)
                        .testTag("bar_list"),
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
                            groups = state.barGroups.ifEmpty { listOf(BarGroup(BarCategory.OTHER, state.bar)) },
                            expanded = showAllChips,
                            onToggleExpanded = { showAllChips = !showAllChips },
                            onRemove = onRemoveIngredient,
                        )
                    }
                    item(key = "bar-actions") {
                        BarActions(onAskAda = onAskAda, onAddFromPhoto = onAddFromPhoto)
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
                                    BarUnlockCard(
                                        unlock = unlock,
                                        onAdd = {
                                            onChooseAdd(
                                                BarAddChoice(
                                                    ingredient = unlock.ingredient,
                                                    unlockCount = unlock.count,
                                                    nearlyCount = unlock.nearly,
                                                ),
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                    val recommendationsError = state.recommendationsError
                    if (state.recommendationsFailed && recommendationsError != null) {
                        // The drinks couldn't be asked for: say so, rather than "nothing to make".
                        item(key = "bar-recommendations-error") {
                            Box(modifier = Modifier.fillMaxWidth().testTag("bar_recommendations_error")) {
                                ConnectionErrorState(
                                    error = recommendationsError,
                                    onRetry = onRetry,
                                    modifier = Modifier.align(Alignment.Center),
                                )
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
                        val collapsible = state.makeable.size > BAR_COLLAPSED_DRINK_COUNT
                        val shown = if (collapsible && !showAllMakeable) {
                            state.makeable.take(BAR_COLLAPSED_DRINK_COUNT)
                        } else {
                            state.makeable
                        }
                        items(shown, key = { "makeable-${it.id}" }) { drink ->
                            DrinkListItem(
                                drink = drink,
                                subtitle = if (drink.isUserRecipe) {
                                    stringResource(R.string.recipes_home_tag_yours)
                                } else {
                                    null
                                },
                                onDrinkClick = onDrinkClick,
                                onToggleSaved = onToggleSaved,
                            )
                        }
                        if (collapsible) {
                            item(key = "makeable-see-all") {
                                TextButton(
                                    onClick = { showAllMakeable = !showAllMakeable },
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .testTag("bar_makeable_see_all"),
                                ) {
                                    Text(
                                        if (showAllMakeable) {
                                            stringResource(R.string.bar_see_fewer)
                                        } else {
                                            stringResource(R.string.bar_see_all_drinks, state.makeable.size)
                                        },
                                    )
                                }
                            }
                        }
                    }
                    if (withSwaps.isNotEmpty()) {
                        item(key = "swaps-header") {
                            SectionHeader(
                                title = stringResource(R.string.bar_with_swap),
                                supporting = stringResource(R.string.bar_with_swap_info),
                            )
                        }
                        items(withSwaps, key = { "swap-${it.drink.id}" }) { almost ->
                            DrinkListItem(
                                drink = almost.drink,
                                onDrinkClick = onDrinkClick,
                                onToggleSaved = onToggleSaved,
                                footer = {
                                    BarSwapLines(almost, Modifier.testTag("bar_swap_${almost.drink.id}"))
                                },
                            )
                        }
                    }
                    if (needsShopping.isNotEmpty()) {
                        item(key = "almost-header") {
                            SectionHeader(
                                title = stringResource(R.string.bar_almost),
                                supporting = stringResource(R.string.bar_almost_info),
                            )
                        }
                        items(needsShopping, key = { "almost-${it.drink.id}" }) { almost ->
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
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (almost.substitutes.isNotEmpty()) BarSwapLines(almost)
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                almost.missing.forEach { name ->
                                                    BarAddChip(
                                                        name = name,
                                                        onAdd = { onChooseAdd(BarAddChoice(name, drink = almost.drink)) },
                                                        modifier = Modifier.testTag("bar_almost_add_${almost.drink.id}_$name"),
                                                    )
                                                }
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
    if (!state.loading && state.recommendationsError == null) {
        parts += pluralStringResource(R.plurals.bar_ready_count, state.makeable.size, state.makeable.size)
        parts += pluralStringResource(R.plurals.bar_almost_count, state.almost.size, state.almost.size)
    }
    return parts.joinToString(stringResource(R.string.bar_summary_separator))
}

/**
 * The bar's ingredients, shelf by shelf. Shelf names are left out when everything is on
 * one. A long bar shows its first [BAR_COLLAPSED_CHIP_COUNT] until "Show all" is tapped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarChips(
    groups: List<BarGroup>,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRemove: (String) -> Unit,
) {
    val total = groups.sumOf { it.names.size }
    val collapsible = total > BAR_COLLAPSED_CHIP_COUNT
    var room = if (collapsible && !expanded) BAR_COLLAPSED_CHIP_COUNT else total
    val shown = groups.mapNotNull { group ->
        val names = group.names.take(room)
        room -= names.size
        if (names.isEmpty()) null else group.copy(names = names)
    }
    val labelled = groups.size > 1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        shown.forEach { group ->
            if (labelled) {
                Text(
                    text = stringResource(barCategoryLabel(group.category)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { heading() }
                        .testTag("bar_group_${group.category}"),
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                group.names.forEach { name -> BarChip(name = name, onRemove = { onRemove(name) }) }
            }
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
                        stringResource(R.string.bar_show_all, total)
                    },
                )
            }
        }
    }
}

/**
 * One bar ingredient. Only the "x" removes it, so a stray tap on the name can't; the
 * snackbar's Undo covers a tap that was meant for something else.
 */
@Composable
private fun BarChip(name: String, onRemove: () -> Unit) {
    val removeLabel = stringResource(R.string.bar_remove_ingredient, name)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.testTag("bar_chip_$name"),
    ) {
        Row(
            modifier = Modifier.height(40.dp).padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IngredientImage(
                url = ingredientImageUrl(name),
                size = 24.dp,
                contentDescription = null,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp).widthIn(max = 200.dp),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onRemove)
                    .semantics { contentDescription = removeLabel }
                    .testTag("bar_chip_remove_$name"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** "Ask Ada" and "Add from a photo": Ada says what to make, or reads the shelf and stocks the bar. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarActions(onAskAda: () -> Unit, onAddFromPhoto: () -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = onAskAda,
            label = { Text(stringResource(R.string.bar_ask_ada)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            modifier = Modifier.testTag("bar_ask_ada"),
        )
        AssistChip(
            onClick = onAddFromPhoto,
            label = { Text(stringResource(R.string.bar_add_photo)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
            },
            modifier = Modifier.testTag("bar_add_photo"),
        )
    }
}

/** "Use Aperol for Campari · lighter and sweeter", one line for each stand-in the bar has. */
@Composable
private fun BarSwapLines(almost: BarAlmostDrink, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        almost.substitutes.forEach { swap ->
            Text(
                text = if (swap.note.isBlank()) {
                    stringResource(R.string.bar_swap, swap.use, swap.missing)
                } else {
                    stringResource(R.string.bar_swap_note, swap.use, swap.missing, swap.note)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** "+ Lime · unlocks 4 drinks": an ingredient that would make the most "almost" drinks. */
@Composable
private fun BarUnlockCard(unlock: BarUnlock, onAdd: () -> Unit) {
    val label = stringResource(R.string.bar_add_named, unlock.ingredient)
    val unlocks = unlockLabel(unlock.count, unlock.nearly)
    val description = stringResource(R.string.bar_unlock_description, label, unlocks)
    ElevatedCard(
        onClick = onAdd,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .testTag("bar_unlock_${unlock.ingredient}")
            .semantics(mergeDescendants = true) { contentDescription = description },
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

/**
 * "Unlocks 4 drinks" for an ingredient that finishes drinks on its own, else
 * "Gets you closer to 3 drinks" for one that is half of what they're missing.
 */
@Composable
private fun unlockLabel(unlocks: Int, nearly: Int): String = if (unlocks > 0 || nearly == 0) {
    pluralStringResource(R.plurals.bar_unlocks, unlocks, unlocks)
} else {
    pluralStringResource(R.plurals.bar_gets_closer, nearly, nearly)
}

/** "+ Lime", an ingredient a drink is missing; tapping it asks whether it goes on the bar or the list. */
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

/** "Lime · Needed for Margarita": add it to the bar (have it) or the shopping list (buy it). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarAddSheet(
    choice: BarAddChoice,
    onAddToBar: () -> Unit,
    onAddToShopping: () -> Unit,
    onDismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("bar_add_sheet"),
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IngredientImage(
                    url = ingredientImageUrl(choice.ingredient),
                    size = 48.dp,
                    contentDescription = null,
                )
                Column {
                    Text(
                        text = choice.ingredient,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface,
                    )
                    val subtitle = when {
                        choice.drink != null -> stringResource(R.string.bar_add_sheet_needed_for, choice.drink.name)
                        choice.unlockCount > 0 || choice.nearlyCount > 0 ->
                            unlockLabel(choice.unlockCount, choice.nearlyCount)
                        else -> null
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            ListItem(
                headlineContent = { Text(stringResource(R.string.bar_add_sheet_to_bar)) },
                supportingContent = { Text(stringResource(R.string.bar_add_sheet_to_bar_info)) },
                leadingContent = { Icon(Icons.Outlined.LocalBar, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .testTag("bar_add_sheet_bar")
                    .clickable(role = Role.Button) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddToBar()
                    }
                    .padding(horizontal = 8.dp),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.bar_add_sheet_to_shopping)) },
                supportingContent = { Text(stringResource(R.string.bar_add_sheet_to_shopping_info)) },
                leadingContent = { Icon(Icons.Outlined.ShoppingCart, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier
                    .testTag("bar_add_sheet_shopping")
                    .clickable(role = Role.Button) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddToShopping()
                    }
                    .padding(horizontal = 8.dp),
            )
        }
    }
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
    onCategory: (String?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.bar_add_ingredients),
                modifier = Modifier.weight(1f).semantics { heading() },
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
        val categories = remember(state.catalogTerms) { state.pickerCategories() }
        if (categories.isNotEmpty() && state.query.isBlank()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("bar_picker_categories"),
            ) {
                items(categories, key = { it }) { category ->
                    FilterChip(
                        selected = state.pickerCategory == category,
                        onClick = { onCategory(category) },
                        label = { Text(stringResource(barCategoryLabel(category))) },
                        modifier = Modifier.testTag("bar_picker_category_$category"),
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val terms = remember(
                state.visibleTerms,
                state.catalogTerms,
                state.bar,
                state.query,
                state.pickerCategory,
            ) { state.pickerTerms() }
            when {
                state.catalogTerms.isEmpty() && state.error != null && state.query.isBlank() ->
                    ConnectionErrorState(
                        error = state.error,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                // Still loading: placeholder rows, not a "nothing found" message.
                state.catalogTerms.isEmpty() && state.query.isBlank() ->
                    Column(modifier = Modifier.testTag("bar_picker_loading")) {
                        repeat(10) { BarTermSkeleton() }
                    }
                terms.isEmpty -> EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.bar_picker_no_results),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    terms.custom?.let { name ->
                        item(key = "custom") {
                            BarCustomRow(name = name, onAdd = { onToggleIngredient(name) })
                            BarTermDivider()
                        }
                    }
                    items(terms.results, key = { "result-${it.name}" }) { term ->
                        BarTermRow(term = term.name, selected = state.inBar(term.name), onToggle = onToggleIngredient)
                        BarTermDivider()
                    }
                    if (terms.selected.isNotEmpty()) {
                        item(key = "header-in-bar") {
                            BarPickerHeader(stringResource(R.string.bar_picker_in_bar, terms.selected.size))
                        }
                        items(terms.selected, key = { "in-bar-${it.name}" }) { term ->
                            BarTermRow(term = term.name, selected = true, onToggle = onToggleIngredient)
                            BarTermDivider()
                        }
                    }
                    if (terms.popular.isNotEmpty()) {
                        item(key = "header-popular") {
                            BarPickerHeader(stringResource(R.string.bar_picker_popular))
                        }
                        items(terms.popular, key = { "popular-${it.name}" }) { term ->
                            BarTermRow(
                                term = term.name,
                                selected = false,
                                onToggle = onToggleIngredient,
                                tag = "bar_popular_${term.name}",
                            )
                            BarTermDivider()
                        }
                    }
                    terms.sections.forEach { section ->
                        if (section.terms.isEmpty()) return@forEach
                        val title = section.category
                        // The plain list needs its heading only to set it apart from "In your bar".
                        if (title != null || terms.selected.isNotEmpty()) {
                            item(key = "header-${title ?: "all"}") {
                                BarPickerHeader(
                                    stringResource(title?.let(::barCategoryLabel) ?: R.string.bar_picker_all),
                                )
                            }
                        }
                        items(section.terms, key = { "term-${it.name}" }) { term ->
                            BarTermRow(
                                term = term.name,
                                selected = state.inBar(term.name),
                                onToggle = onToggleIngredient,
                            )
                            BarTermDivider()
                        }
                    }
                }
            }
        }
    }
}

/** "Add “Tanqueray”": the query as an ingredient of the user's own, when the catalog has none by that name. */
@Composable
private fun BarCustomRow(name: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onAdd)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("bar_add_custom"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.bar_add_custom, name),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.bar_add_custom_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    tag: String = "bar_term_$term",
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
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IngredientImage(
            url = ingredientImageUrl(term),
            size = 48.dp,
            contentDescription = null,
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
