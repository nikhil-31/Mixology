package com.capstone.nik.mixology.ui.ada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.DrinkImage
import com.capstone.nik.mixology.ui.components.sourceLabel

/** Ingredients the card lists before "+N more". */
private const val CARD_INGREDIENTS = 6

/**
 * The recipe Ada read from a link or photo. While it's a draft and Ada is
 * waiting, it offers Save and Not now; afterwards it says what happened.
 */
@Composable
fun AdaRecipeCard(
    recipe: AdaRecipe,
    /** Save and Not now work; false while Ada is answering something else. */
    canAnswer: Boolean,
    onSave: () -> Unit,
    onDrop: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dropped = recipe.state == AdaRecipe.State.Dropped
    Card(
        modifier = modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .alpha(if (dropped) 0.6f else 1f)
            .testTag("ada_recipe_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        recipe.photoUrl?.let { url ->
            DrinkImage(
                url = url,
                contentScale = ContentScale.Crop,
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            SourceLine(recipe)
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            recipe.ingredients.take(CARD_INGREDIENTS).forEach { item ->
                Row {
                    Text(
                        text = item.ingredient,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.measure.isNotBlank()) {
                        Text(
                            text = item.measure,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
            val more = recipe.ingredients.size - CARD_INGREDIENTS
            if (more > 0) {
                Text(
                    text = stringResource(R.string.ada_recipe_more_ingredients, more),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            recipe.instructions?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            when (recipe.state) {
                AdaRecipe.State.Draft -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onSave,
                        enabled = canAnswer,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ada_recipe_save"),
                    ) {
                        Text(stringResource(R.string.ada_recipe_save))
                    }
                    OutlinedButton(
                        onClick = onDrop,
                        enabled = canAnswer,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ada_recipe_drop"),
                    ) {
                        Text(stringResource(R.string.ada_recipe_not_now))
                    }
                }
                AdaRecipe.State.Saved -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Outcome(
                        icon = Icons.Filled.CheckCircle,
                        text = stringResource(
                            if (recipe.duplicate) R.string.ada_recipe_duplicate else R.string.ada_recipe_saved,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    if (recipe.recipeId != null) {
                        TextButton(onClick = onOpen, modifier = Modifier.testTag("ada_recipe_open")) {
                            Text(stringResource(R.string.ada_recipe_open))
                        }
                    }
                }
                AdaRecipe.State.Dropped -> Outcome(
                    icon = Icons.Outlined.DoNotDisturbOn,
                    text = stringResource(R.string.ada_recipe_dropped),
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SourceLine(recipe: AdaRecipe) {
    val site = sourceLabel(recipe.source, recipe.sourceUrl)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (site != null) Icons.Outlined.Link else Icons.Outlined.PhotoCamera,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = site?.let { stringResource(R.string.ada_recipe_from, it) }
                ?: stringResource(R.string.ada_recipe_from_photo),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@Composable
private fun Outcome(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.testTag("ada_recipe_outcome"), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
