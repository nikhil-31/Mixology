package com.capstone.nik.mixology.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyListItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkListItem
import com.capstone.nik.mixology.ui.components.DrinkViewToggle
import com.capstone.nik.mixology.ui.components.IngredientImage
import com.capstone.nik.mixology.ui.model.ingredientImageUrl
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun SearchRoute(
    initialQuery: String,
    onBack: () -> Unit,
    onDrinkClick: (Drink) -> Unit,
    initialMode: SearchMode = SearchMode.NAME,
    initialFilterKind: FilterKind? = null,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(initialQuery, initialMode, initialFilterKind) {
        viewModel.onIntent(
            SearchIntent.Search(
                query = initialQuery,
                mode = initialMode,
                filterKind = initialFilterKind,
                commit = initialQuery.isNotBlank() || initialFilterKind != null,
            ),
        )
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is SearchEffect.ShowMessageRes ->
                snackbarHostState.showSnackbar(context.getString(effect.resId))
            is SearchEffect.OpenDrink -> onDrinkClick(effect.drink)
            SearchEffect.NavigateBack -> onBack()
        }
    }

    SearchScreen(
        state = state,
        initialQuery = initialQuery,
        snackbarHostState = snackbarHostState,
        onBack = { viewModel.onIntent(SearchIntent.Back) },
        onSearch = { viewModel.onIntent(SearchIntent.Search(it)) },
        onSubmit = { viewModel.onIntent(SearchIntent.Search(it, commit = true)) },
        onModeSelected = { viewModel.onIntent(SearchIntent.SetMode(it)) },
        onSelectSuggestion = { viewModel.onIntent(SearchIntent.SelectSuggestion(it)) },
        onDrinkClick = { viewModel.onIntent(SearchIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(SearchIntent.ToggleSaved(it)) },
        onToggleListView = { viewModel.onIntent(SearchIntent.ToggleListView) },
    )
}

@Composable
fun SearchScreen(
    state: SearchUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onModeSelected: (SearchMode) -> Unit = {},
    onSelectSuggestion: (String) -> Unit = {},
    onSubmit: (String) -> Unit = onSearch,
    onToggleListView: () -> Unit = {},
    initialQuery: String = state.query,
) {
    var queryText by remember {
        mutableStateOf(initialQuery.replace("%20", " "))
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }
    LaunchedEffect(state.query) {
        if (queryText.trim() != state.query) {
            queryText = state.query
        }
    }

    fun emitSearch(value: String) {
        val trimmed = value.trim()
        val minChars = if (state.mode == SearchMode.INGREDIENT) 1 else SEARCH_MIN_CHARS
        if (trimmed.length >= minChars || trimmed.isEmpty()) {
            onSearch(value)
        }
    }

    fun submitSearch() {
        onSubmit(queryText)
        keyboardController?.hide()
    }

    fun pickSuggestion(ingredient: String) {
        queryText = ingredient
        onSelectSuggestion(ingredient)
        keyboardController?.hide()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
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
                TextField(
                    value = queryText,
                    onValueChange = { value ->
                        queryText = value
                        emitSearch(value)
                    },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.action_search)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submitSearch() }),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                )
                IconButton(onClick = { submitSearch() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.action_search),
                    )
                }
            }
            SearchModeChips(
                mode = state.mode,
                onModeSelected = onModeSelected,
            )
            DrinkViewToggle(
                listView = state.listView,
                onToggleListView = onToggleListView,
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.suggestions.isNotEmpty() -> SearchSuggestions(
                        suggestions = state.suggestions,
                        onSelectSuggestion = ::pickSuggestion,
                    )
                    state.empty -> SearchEmptyResults(
                        query = state.query,
                        mode = state.mode,
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                    )
                    state.listView -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 50.dp),
                    ) {
                        lazyListItems(state.results, key = { it.id }) { item ->
                            DrinkListItem(
                                drink = item,
                                onDrinkClick = onDrinkClick,
                                onToggleSaved = onToggleSaved,
                            )
                        }
                    }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(6.dp, 6.dp, 6.dp, 50.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(
                            items = state.results,
                            key = { item -> item.id },
                        ) { item ->
                            DrinkCard(
                                item = item,
                                onClick = { onDrinkClick(item) },
                                onToggleSaved = { onToggleSaved(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestions(
    suggestions: List<String>,
    onSelectSuggestion: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 50.dp),
    ) {
        lazyListItems(suggestions, key = { it }) { term ->
            SearchSuggestionRow(
                term = term,
                onSelect = onSelectSuggestion,
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun SearchSuggestionRow(
    term: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(term) }
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("search_suggestion_$term"),
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
    }
}

@Composable
private fun SearchEmptyResults(
    query: String,
    mode: SearchMode,
    modifier: Modifier = Modifier,
) {
    val hintRes = if (mode == SearchMode.INGREDIENT) {
        R.string.search_empty_hint_ingredient
    } else {
        R.string.search_empty_hint_name
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.LocalBar,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = stringResource(R.string.search_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (query.isNotBlank()) {
            Text(
                text = stringResource(R.string.search_empty_query, query),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = stringResource(hintRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SearchModeChips(
    mode: SearchMode,
    onModeSelected: (SearchMode) -> Unit,
) {
    val modes = listOf(
        SearchMode.NAME to R.string.search_mode_name,
        SearchMode.INGREDIENT to R.string.search_mode_ingredient,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        modes.forEach { (value, titleRes) ->
            FilterChip(
                selected = mode == value,
                onClick = { onModeSelected(value) },
                label = { Text(stringResource(titleRes)) },
            )
        }
    }
}
