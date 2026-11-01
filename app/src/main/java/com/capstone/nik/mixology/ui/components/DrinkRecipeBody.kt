package com.capstone.nik.mixology.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.repository.BarMatcher
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.capstone.nik.mixology.ui.theme.MixologyDetailsTitle
import com.capstone.nik.mixology.ui.theme.MixologySectionTitle
import com.capstone.nik.mixology.ui.theme.PosterScrimCenter
import com.capstone.nik.mixology.ui.theme.PosterScrimStart
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DrinkRecipeBody(
    name: String,
    alcoholic: String?,
    instructions: String?,
    ingredients: List<IngredientMeasure>,
    saved: Boolean,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier,
    glass: String? = null,
    category: String? = null,
    iba: String? = null,
    extraBottomContent: @Composable () -> Unit = {},
    /** False for the user's own recipes, which can't be saved. */
    showSave: Boolean = true,
    /** Names in My Bar, normalised with [BarMatcher.normalize]. */
    inBar: Set<String> = emptySet(),
    /** Primary actions, shown right under the title and chips. */
    actions: @Composable () -> Unit = {},
) {
    val chips = remember(alcoholic, glass, category, iba) {
        listOfNotNull(
            alcoholic?.trim()?.takeIf { it.isNotEmpty() },
            glass?.trim()?.takeIf { it.isNotEmpty() },
            category?.trim()?.takeIf { it.isNotEmpty() },
            iba?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
    val steps = remember(instructions) { instructionSteps(instructions) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MixologyDetailsTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (showSave) {
                FavoriteButton(saved = saved, onClick = onToggleSaved, size = 40)
            }
        }
        if (chips.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chips.forEachIndexed { index, label ->
                    RecipeChip(label = label, emphasized = index == 0 && !alcoholic.isNullOrBlank())
                }
            }
        }
        actions()
        // Ingredients first: you gather before you mix.
        if (ingredients.isNotEmpty()) {
            RecipeSectionTitle(stringResource(R.string.detail_screen_ingredients))
            val haveCount = if (inBar.isEmpty()) 0 else ingredients.count { BarMatcher.inBar(it.ingredient, inBar) }
            if (haveCount > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.detail_ingredients_have,
                        ingredients.size,
                        haveCount,
                        ingredients.size,
                    ),
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                        .testTag("detail_ingredients_have"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
            ) {
                ingredients.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        )
                    }
                    IngredientRow(item, inBar = inBar.isNotEmpty() && BarMatcher.inBar(item.ingredient, inBar))
                }
            }
        }
        if (steps.isNotEmpty()) {
            RecipeSectionTitle(stringResource(R.string.detail_screen_instructions))
            InstructionBlock(steps = steps)
        }
        extraBottomContent()
    }
}

/** The tallest the hero viewport gets; the image runs [DrinkHeroOverlap] further, under the body. */
val DrinkHeroViewportHeight = 480.dp
val DrinkHeroOverlap = 24.dp
val DrinkHeroImageHeight = DrinkHeroViewportHeight + DrinkHeroOverlap

/** The hero viewport, capped so the title stays well up on short screens. */
@Composable
fun rememberDrinkHeroViewportHeight(): Dp {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    return remember(screenHeight) { minOf(DrinkHeroViewportHeight, screenHeight * 0.55f) }
}

/** Placeholder for the recipe while it loads: chips, ingredient rows and a few lines of text. */
@Composable
fun RecipeSkeleton(modifier: Modifier = Modifier) {
    val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    val color = MaterialTheme.colorScheme.surfaceVariant
    @Composable
    fun Bar(width: Dp, height: Dp, shape: Dp = 8.dp) {
        Box(
            Modifier
                .size(width, height)
                .graphicsLayer { alpha = pulse }
                .clip(RoundedCornerShape(shape))
                .background(color),
        )
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("detail_skeleton"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Bar(84.dp, 30.dp, 20.dp)
            Bar(108.dp, 30.dp, 20.dp)
            Bar(72.dp, 30.dp, 20.dp)
        }
        Box(Modifier.height(8.dp))
        repeat(4) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Bar(40.dp, 40.dp, 20.dp)
                Bar(160.dp, 16.dp)
            }
        }
        Box(Modifier.height(8.dp))
        Bar(300.dp, 14.dp)
        Bar(260.dp, 14.dp)
        Bar(200.dp, 14.dp)
    }
}

@Composable
fun DrinkHeroImage(
    url: String?,
    modifier: Modifier = Modifier.fillMaxWidth().height(DrinkHeroImageHeight),
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        ),
    ) {
        DrinkImage(
            url = url,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PosterScrimCenter, PosterScrimStart),
                    ),
                ),
        )
    }
}

@Composable
internal fun RecipeSectionTitle(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
        style = MixologySectionTitle,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun RecipeChip(
    label: String,
    emphasized: Boolean,
) {
    val container = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun InstructionBlock(steps: List<String>) {
    if (steps.size == 1) {
        Text(
            text = steps.first(),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        return
    }
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        steps.forEachIndexed { index, step ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Text(
                    text = step,
                    modifier = Modifier.weight(1f).padding(top = 2.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

internal fun instructionSteps(instructions: String?): List<String> {
    val text = instructions?.trim().orEmpty()
    if (text.isEmpty()) return emptyList()
    val numbered = Regex("\\s*\\d+\\.\\s+")
        .split(text)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (numbered.size > 1) return numbered
    val lines = text.split(Regex("\\r?\\n+")).map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.size > 1) return lines
    // One paragraph: number its sentences when they read like real steps.
    val sentences = Regex("(?<=[.!?])\\s+(?=[A-Z])")
        .split(text)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val looksLikeSteps = sentences.size in 2..8 && sentences.all { it.length >= MIN_STEP_LENGTH }
    return if (looksLikeSteps) sentences else listOf(text)
}

private const val MIN_STEP_LENGTH = 12
