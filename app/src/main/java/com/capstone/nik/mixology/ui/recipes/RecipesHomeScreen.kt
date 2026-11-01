package com.capstone.nik.mixology.ui.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.EmptyState
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import java.util.Locale
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
fun RecipesHomeRoute(
    onDrinkClick: (Drink) -> Unit,
    onAddRecipe: () -> Unit,
    onSeeAllRecipes: () -> Unit,
    onSeeAllSaved: () -> Unit,
    viewModel: RecipesHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is RecipesHomeEffect.OpenDrink -> onDrinkClick(effect.drink)
            RecipesHomeEffect.OpenEditor -> onAddRecipe()
            RecipesHomeEffect.OpenMyRecipes -> onSeeAllRecipes()
            RecipesHomeEffect.OpenSaved -> onSeeAllSaved()
        }
    }
    RecipesHomeScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun RecipesHomeScreen(
    state: RecipesHomeUiState,
    onIntent: (RecipesHomeIntent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().testTag("recipes_home")) {
        when {
            state.isEmpty && (state.loading || !state.loaded) -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).testTag("recipes_home_loading"),
            )
            state.isEmpty && state.error != null -> EmptyState(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.recipes_home_error),
                body = state.error.takeIf { it.isNotBlank() },
                modifier = Modifier.align(Alignment.Center),
                action = {
                    TextButton(onClick = { onIntent(RecipesHomeIntent.Retry) }) {
                        Text(stringResource(R.string.action_retry))
                    }
                },
            )
            state.showEmpty -> EmptyState(
                icon = Icons.Outlined.MenuBook,
                title = stringResource(R.string.recipes_home_empty_title),
                body = stringResource(R.string.recipes_home_empty_body),
                modifier = Modifier.align(Alignment.Center).testTag("recipes_home_empty"),
                action = {
                    Button(onClick = { onIntent(RecipesHomeIntent.AddRecipe) }) {
                        Text(stringResource(R.string.recipes_home_create))
                    }
                },
            )
            else -> RecipesHomeContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun RecipesHomeContent(
    state: RecipesHomeUiState,
    onIntent: (RecipesHomeIntent) -> Unit,
) {
    val slides = remember(state.bar) { heroSlides(state.bar) }
    val featured = remember(state.mine, state.saved) { featuredDrink(state.mine + state.saved) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("recipes_home_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.error != null) {
            item(key = "error") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = state.error.ifBlank { stringResource(R.string.recipes_home_error) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onIntent(RecipesHomeIntent.Retry) }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
        }
        if (slides.isNotEmpty()) {
            item(key = "hero") {
                RecipeHeroCarousel(slides = slides, onOpen = { onIntent(RecipesHomeIntent.OpenDrink(it)) })
            }
        } else {
            featured?.let { drink ->
                item(key = "hero") {
                    RecipeHeroCard(drink = drink, onClick = { onIntent(RecipesHomeIntent.OpenDrink(drink)) })
                }
            }
        }
        item(key = "mine") {
            RecipeSectionCard(
                title = stringResource(R.string.my_recipes_title),
                drinks = state.mine.take(RECIPES_HOME_ROW_LIMIT),
                onSeeAll = if (state.mine.isEmpty()) null else ({ onIntent(RecipesHomeIntent.SeeAllRecipes) }),
                onOpen = { onIntent(RecipesHomeIntent.OpenDrink(it)) },
                testTag = "recipes_home_mine",
            ) {
                if (state.mine.isEmpty()) {
                    OutlinedButton(
                        onClick = { onIntent(RecipesHomeIntent.AddRecipe) },
                        modifier = Modifier.fillMaxWidth().testTag("recipes_home_create"),
                    ) {
                        Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.recipes_home_create))
                    }
                }
            }
        }
        if (state.saved.isNotEmpty()) {
            item(key = "saved") {
                RecipeSectionCard(
                    title = stringResource(R.string.nav_bottom_saved),
                    drinks = state.saved.take(RECIPES_HOME_ROW_LIMIT),
                    onSeeAll = { onIntent(RecipesHomeIntent.SeeAllSaved) },
                    onOpen = { onIntent(RecipesHomeIntent.OpenDrink(it)) },
                    testTag = "recipes_home_saved",
                )
            }
        }
    }
}

@Composable
private fun RecipeHeroCarousel(slides: List<HeroSlide>, onOpen: (Drink) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // The pager sizes each page on its own, so slides with a missing line would make the
        // carousel jump. Invisible copies of every slide, with a 4:3 photo space and one line
        // each of name, missing and ingredients, set the box to the tallest one; each real
        // card fills its page, and its photo takes whatever the text doesn't use.
        Box(modifier = Modifier.fillMaxWidth()) {
            slides.forEach { slide ->
                HeroSlideCard(
                    slide = slide,
                    onOpen = {},
                    showImage = false,
                    modifier = Modifier.alpha(0f).clearAndSetSemantics {},
                )
            }
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.matchParentSize().testTag("recipes_home_hero_pager"),
            ) { page ->
                HeroSlideCard(
                    slide = slides[page],
                    onOpen = onOpen,
                    modifier = Modifier.fillMaxHeight().testTag("recipes_home_hero"),
                    fillHeight = true,
                )
            }
        }
        if (slides.size >= 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(slides.size) { index ->
                    val selected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSlideCard(
    slide: HeroSlide,
    onOpen: (Drink) -> Unit,
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    fillHeight: Boolean = false,
) {
    when (slide) {
        is HeroSlide.CanMake -> RecipeHeroCard(
            drink = slide.drink,
            onClick = { onOpen(slide.drink) },
            badge = stringResource(R.string.recipes_home_hero_can_make),
            modifier = modifier,
            showImage = showImage,
            fillHeight = fillHeight,
        )
        is HeroSlide.Almost -> RecipeHeroCard(
            drink = slide.drink,
            onClick = { onOpen(slide.drink) },
            badge = stringResource(R.string.recipes_home_hero_almost),
            missing = missingSummary(slide.missing).takeIf { it.isNotEmpty() }
                ?.let { stringResource(R.string.recipes_home_hero_missing, it) },
            modifier = modifier,
            showImage = showImage,
            fillHeight = fillHeight,
        )
    }
}

@Composable
private fun RecipeHeroCard(
    drink: Drink,
    onClick: () -> Unit,
    badge: String? = null,
    missing: String? = null,
    modifier: Modifier = Modifier.testTag("recipes_home_hero"),
    /** Off for the invisible copies that only size the carousel. */
    showImage: Boolean = true,
    /** In the carousel: fill the page's height, giving the photo whatever the text doesn't use. */
    fillHeight: Boolean = false,
) {
    val meta = drinkMeta(drink)
    val tag = if (badge != null) {
        badge
    } else if (drink.isUserRecipe) {
        stringResource(R.string.recipes_home_tag_yours)
    } else {
        drink.category?.trim().orEmpty()
    }
    val summary = ingredientSummary(drink)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = if (fillHeight) Modifier.fillMaxHeight() else Modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (fillHeight) Modifier.weight(1f) else Modifier.aspectRatio(4f / 3f)),
            ) {
                if (showImage) {
                    DrinkImage(url = drink.thumbUrl, modifier = Modifier.fillMaxSize())
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (tag.isNotEmpty()) {
                    Text(
                        text = tag.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
                // One line each for the name, what's missing and the ingredients.
                Text(
                    text = drink.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (missing != null) {
                    Text(
                        text = missing,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (summary.isNotEmpty()) {
                    Text(
                        text = "$summary.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeSectionCard(
    title: String,
    drinks: List<Drink>,
    onSeeAll: (() -> Unit)?,
    onOpen: (Drink) -> Unit,
    testTag: String,
    footer: @Composable () -> Unit = {},
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().testTag(testTag),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                if (onSeeAll != null) {
                    TextButton(onClick = onSeeAll, modifier = Modifier.testTag("${testTag}_see_all")) {
                        Text(stringResource(R.string.action_see_all))
                    }
                }
            }
            drinks.forEachIndexed { index, drink ->
                if (index > 0) HorizontalDivider()
                RecipeRow(drink = drink, onClick = { onOpen(drink) })
            }
            footer()
            Spacer(Modifier.size(4.dp))
        }
    }
}

@Composable
private fun RecipeRow(drink: Drink, onClick: () -> Unit) {
    val meta = drinkMeta(drink)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("recipes_home_row_${drink.id}"),
    ) {
        DrinkImage(
            url = drink.thumbUrl,
            contentDescription = null,
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = drink.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta.isNotEmpty()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** "4 ingredients · Highball glass", leaving out whatever the drink doesn't have. */
@Composable
private fun drinkMeta(drink: Drink): String {
    val count = drink.ingredients.size
    val parts = buildList {
        if (count > 0) add(pluralStringResource(R.plurals.recipes_home_ingredients, count, count))
        drink.glass?.trim()?.takeIf { it.isNotEmpty() }?.let(::add)
    }
    return parts.joinToString(" · ")
}
