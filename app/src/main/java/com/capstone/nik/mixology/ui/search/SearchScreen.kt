package com.capstone.nik.mixology.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.capstone.nik.mixology.ui.components.CircularDrinkImage
import com.capstone.nik.mixology.ui.components.FavoriteButton
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
) {
    var searching by remember { mutableStateOf(false) }
    var queryText by remember(state.query) { mutableStateOf(state.query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searching) {
                        TextField(
                            value = queryText,
                            onValueChange = { queryText = it },
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.action_search)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    onSearch(queryText)
                                    searching = false
                                },
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(state.query.ifBlank { stringResource(R.string.action_search) })
                    }
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
                    IconButton(
                        onClick = {
                            if (searching) {
                                onSearch(queryText)
                                searching = false
                            } else {
                                searching = true
                            }
                        },
                    ) {
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
                .padding(padding),
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
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.results, key = { it.drink.idDrink }) { item ->
                        SearchRow(
                            item = item,
                            onClick = { onDrinkClick(item.toCocktail()) },
                            onToggleSaved = { onToggleSaved(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchRow(
    item: SearchResultItem,
    onClick: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularDrinkImage(url = item.drink.strDrinkThumb, size = 40.dp)
        Text(
            text = item.drink.strDrink.orEmpty(),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FavoriteButton(saved = item.saved, onClick = onToggleSaved, size = 40)
    }
}
