package com.capstone.nik.mixology.ui.hot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkCardRailHeight
import com.capstone.nik.mixology.ui.components.DrinkCardRailWidth
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun HotRoute(
    snackbarHostState: SnackbarHostState,
    onDrinkClick: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
    onBrowseCatalog: () -> Unit = {},
    viewModel: HotViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is HotEffect.ShowMessageRes ->
                snackbarHostState.showSnackbar(context.getString(effect.resId))
            is HotEffect.OpenDrink -> onDrinkClick(effect.drink)
            is HotEffect.OpenFilter -> onSeeAll(effect.filter)
            HotEffect.OpenCatalog -> onBrowseCatalog()
        }
    }

    HotScreen(
        state = state,
        onDrinkClick = { viewModel.onIntent(HotIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(HotIntent.ToggleSaved(it)) },
        onSeeAll = { viewModel.onIntent(HotIntent.SeeAll(it)) },
        onBrowseCatalog = { viewModel.onIntent(HotIntent.BrowseCatalog) },
    )
}

@Composable
fun HotScreen(
    state: HotUiState,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
    onBrowseCatalog: () -> Unit = {},
) {
    val categories = state.visibleCategories
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.loading && categories.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "browse_catalog") {
                    TextButton(
                        onClick = onBrowseCatalog,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.nav_item_browse_catalog),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                items(categories, key = { it.filter.name }) { category ->
                    HotCategoryRow(
                        category = category,
                        onDrinkClick = onDrinkClick,
                        onToggleSaved = onToggleSaved,
                        onSeeAll = onSeeAll,
                    )
                }
            }
        }
    }
}

@Composable
private fun HotCategoryRow(
    category: HotCategory,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = category.filter.titleRes?.let { stringResource(it) } ?: category.filter.displayName(),
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            TextButton(onClick = { onSeeAll(category.filter) }) {
                Text(
                    text = stringResource(R.string.action_see_all),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(category.drinks, key = { it.id }) { item ->
                DrinkCard(
                    item = item,
                    onClick = { onDrinkClick(item) },
                    onToggleSaved = { onToggleSaved(item) },
                    modifier = Modifier.width(DrinkCardRailWidth),
                    posterHeight = DrinkCardRailHeight,
                )
            }
        }
    }
}
