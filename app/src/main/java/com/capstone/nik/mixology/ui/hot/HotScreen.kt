package com.capstone.nik.mixology.ui.hot

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.ui.ada.AdaPlushieFilled
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkCardCompactHeight
import com.capstone.nik.mixology.ui.components.DrinkCardCompactWidth
import com.capstone.nik.mixology.ui.components.DrinkCardRailHeight
import com.capstone.nik.mixology.ui.components.DrinkCardRailWidth
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.FavoriteButton
import com.capstone.nik.mixology.ui.components.SectionHeader
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.theme.PosterBadge

@Composable
fun HotRoute(
    snackbarHostState: SnackbarHostState,
    onDrinkClick: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
    onOpenBar: () -> Unit = {},
    onOpenAda: () -> Unit = {},
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
        onOpenBar = onOpenBar,
        onOpenAda = onOpenAda,
    )
}

@Composable
fun HotScreen(
    state: HotUiState,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onSeeAll: (DrinkFilter) -> Unit,
    onOpenBar: () -> Unit = {},
    onOpenAda: () -> Unit = {},
) {
    val categories = state.visibleCategories
    val featured = featuredDrink(categories)
    val recent = categories.firstOrNull { it.filter == DrinkFilter.RECENTLY_VIEWED }
    val fromRecent = featured != null && recent?.drinks?.firstOrNull()?.id == featured.id
    // The hero already shows the latest recent drink, so the rail skips it.
    val rails = categories.mapNotNull { category ->
        if (category.filter == DrinkFilter.RECENTLY_VIEWED && fromRecent) {
            category.copy(drinks = category.drinks.drop(1)).takeIf { it.drinks.isNotEmpty() }
        } else {
            category
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.loading && categories.isEmpty()) {
            HomeSkeleton()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_list"),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (featured != null) {
                    item(key = "hero") {
                        HomeHero(
                            drink = featured,
                            fromRecent = fromRecent,
                            onOpen = { onDrinkClick(featured) },
                            onToggleSaved = { onToggleSaved(featured) },
                        )
                    }
                }
                item(key = "quick_actions") {
                    QuickActions(onOpenBar = onOpenBar, onOpenAda = onOpenAda)
                }
                items(rails, key = { it.filter.name }) { category ->
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
    fromRecent: Boolean,
    onOpen: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .clickable(onClick = onOpen)
            .testTag("home_hero"),
    ) {
        DrinkImage(
            url = drink.thumb,
            modifier = Modifier.fillMaxSize(),
            contentDescription = drink.name,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(background.copy(alpha = 0.6f), Color.Transparent))),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, background.copy(alpha = 0.85f), background))),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = PosterBadge,
                contentColor = Color.White,
            ) {
                Text(
                    text = stringResource(if (fromRecent) R.string.home_continue else R.string.home_featured),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text(
                text = drink.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.testTag("home_view_recipe"),
                ) {
                    Text(stringResource(R.string.home_view_recipe))
                }
                FavoriteButton(
                    saved = drink.saved,
                    onClick = onToggleSaved,
                    overlay = true,
                    modifier = Modifier.testTag("home_hero_save"),
                )
            }
        }
    }
}

@Composable
private fun QuickActions(
    onOpenBar: () -> Unit,
    onOpenAda: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickActionCard(
            icon = Icons.Filled.LocalBar,
            title = stringResource(R.string.home_quick_bar_title),
            hint = stringResource(R.string.home_quick_bar_hint),
            onClick = onOpenBar,
            modifier = Modifier.weight(1f).testTag("home_quick_bar"),
        )
        QuickActionCard(
            icon = AdaPlushieFilled,
            title = stringResource(R.string.home_quick_ada_title),
            hint = stringResource(R.string.home_quick_ada_hint),
            onClick = onOpenAda,
            modifier = Modifier.weight(1f).testTag("home_quick_ada"),
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
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
    val compact = category.filter == DrinkFilter.RECENTLY_VIEWED
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = category.filter.titleRes?.let { stringResource(it) } ?: category.filter.displayName(),
            trailing = if (category.filter.showSeeAll) {
                {
                    TextButton(onClick = { onSeeAll(category.filter) }) {
                        Text(
                            text = stringResource(R.string.action_see_all),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            } else {
                null
            },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(category.drinks, key = { it.id }) { item ->
                DrinkCard(
                    item = item,
                    onClick = { onDrinkClick(item) },
                    onToggleSaved = { onToggleSaved(item) },
                    modifier = Modifier.width(if (compact) DrinkCardCompactWidth else DrinkCardRailWidth),
                    posterHeight = if (compact) DrinkCardCompactHeight else DrinkCardRailHeight,
                )
            }
        }
    }
}

@Composable
private fun HomeSkeleton() {
    val transition = rememberInfiniteTransition(label = "homeSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "homeSkeletonAlpha",
    )
    val block = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_skeleton"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.05f)
                .background(block),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(108.dp)
                        .background(block, RoundedCornerShape(16.dp)),
                )
            }
        }
        repeat(2) {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                    .size(width = 140.dp, height = 20.dp)
                    .background(block, RoundedCornerShape(6.dp)),
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(width = DrinkCardRailWidth, height = DrinkCardRailHeight)
                            .background(block, RoundedCornerShape(16.dp)),
                    )
                }
            }
        }
    }
}
