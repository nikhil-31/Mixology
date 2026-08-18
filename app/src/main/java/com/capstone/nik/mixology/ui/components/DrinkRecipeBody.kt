package com.capstone.nik.mixology.ui.components

import androidx.compose.foundation.background
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
            FavoriteButton(saved = saved, onClick = onToggleSaved, size = 40)
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
        if (steps.isNotEmpty()) {
            RecipeSectionTitle(stringResource(R.string.detail_screen_instructions))
            InstructionBlock(steps = steps)
        }
        if (ingredients.isNotEmpty()) {
            RecipeSectionTitle(stringResource(R.string.detail_screen_ingredients))
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
                    IngredientRow(item)
                }
            }
        }
        extraBottomContent()
    }
}

val DrinkHeroImageHeight = 504.dp
val DrinkHeroViewportHeight = 480.dp

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
private fun RecipeSectionTitle(text: String) {
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
    return if (lines.size > 1) lines else listOf(text)
}
