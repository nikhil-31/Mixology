package com.capstone.nik.mixology.ui.hot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.DrinkCardRailHeight
import com.capstone.nik.mixology.ui.components.DrinkCardRailWidth
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun HotRoute(
    snackbarHostState: SnackbarHostState,
    onDrinkClick: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
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
        }
    }

    HotScreen(
        state = state,
        onDrinkClick = { viewModel.onIntent(HotIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(HotIntent.ToggleSaved(it)) },
        onSeeAll = { viewModel.onIntent(HotIntent.SeeAll(it)) },
    )
}

@Composable
fun HotScreen(
    state: HotUiState,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
) {
    val categories = state.visibleCategories
    val featured = featuredDrink(categories)
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.loading && categories.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (featured != null) {
                    item(key = "hero") {
                        HomeHero(drink = featured, onOpen = { onDrinkClick(featured) })
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
private fun HomeHero(
    drink: Drink,
    onOpen: () -> Unit,
) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .clickable(onClick = onOpen),
    ) {
        DrinkImage(
            url = drink.thumb,
            modifier = Modifier.fillMaxSize(),
            contentDescription = drink.name,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, background),
                    ),
                ),
        )
        Text(
            text = drink.name,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HotCategoryRow(
    category: HotCategory,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 12.dp,
                bottom = if (category.filter == DrinkFilter.RECENTLY_VIEWED) 12.dp else 0.dp,
            ),
    ) {
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
            if (category.filter.showSeeAll) {
                TextButton(onClick = { onSeeAll(category.filter) }) {
                    Text(
                        text = stringResource(R.string.action_see_all),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                TextButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.clearAndSetSemantics {},
                    colors = ButtonDefaults.textButtonColors(
                        disabledContentColor = Color.Transparent,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.action_see_all),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
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
