package com.capstone.nik.mixology.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun SearchRoute(
    initialQuery: String,
    onBack: () -> Unit,
    onDrinkClick: (Cocktail) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            viewModel.onIntent(SearchIntent.Search(initialQuery))
        }
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is SearchEffect.ShowMessageRes ->
                snackbarHostState.showSnackbar(context.getString(effect.resId))
            is SearchEffect.OpenDrink -> onDrinkClick(effect.cocktail)
            SearchEffect.NavigateBack -> onBack()
        }
    }

    SearchScreen(
        state = state,
        initialQuery = initialQuery,
        snackbarHostState = snackbarHostState,
        onBack = { viewModel.onIntent(SearchIntent.Back) },
        onSearch = { viewModel.onIntent(SearchIntent.Search(it)) },
        onDrinkClick = { viewModel.onIntent(SearchIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(SearchIntent.ToggleSaved(it)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onDrinkClick: (Cocktail) -> Unit,
    onToggleSaved: (SearchResultItem) -> Unit,
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

    fun emitSearch(value: String) {
        val trimmed = value.trim()
        if (trimmed.length >= SEARCH_MIN_CHARS || trimmed.isEmpty()) {
            onSearch(value)
        }
    }

    fun submitSearch() {
        emitSearch(queryText)
        keyboardController?.hide()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_up_navigation),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { submitSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.action_search),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.empty -> Text(
                    text = stringResource(R.string.network_error_search_no_data_available),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(3.dp, 3.dp, 3.dp, 50.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(
                        items = state.results,
                        key = { item -> item.drink.idDrink ?: item.drink.strDrink.orEmpty() },
                    ) { item ->
                        DrinkCard(
                            item = item.toListItem(),
                            onClick = { onDrinkClick(item.toCocktail()) },
                            onToggleSaved = { onToggleSaved(item) },
                        )
                    }
                }
            }
        }
    }
}
