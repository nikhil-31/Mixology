package com.capstone.nik.mixology.ui.recipes

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun RecipeEditorRoute(
    recipeId: String?,
    onClose: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: RecipeEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(recipeId) {
        viewModel.onIntent(RecipeEditorIntent.Bind(recipeId))
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            RecipeEditorEffect.Close -> onClose()
            RecipeEditorEffect.Deleted -> onDeleted()
        }
    }
    BackHandler { viewModel.onIntent(RecipeEditorIntent.Close) }
    RecipeEditorScreen(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeEditorScreen(
    state: RecipeEditorUiState,
    onIntent: (RecipeEditorIntent) -> Unit,
) {
    val form = state.form
    val errors = state.errors
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onIntent(RecipeEditorIntent.PhotoPicked(it)) }
    }
    val launchPicker = {
        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
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
                IconButton(
                    onClick = { onIntent(RecipeEditorIntent.Close) },
                    modifier = Modifier.testTag("recipe_editor_close"),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.paywall_close))
                }
                Text(
                    text = stringResource(
                        if (state.editing) R.string.recipe_editor_edit_title else R.string.recipe_editor_new_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    TextButton(
                        onClick = { onIntent(RecipeEditorIntent.Save) },
                        enabled = !state.loading,
                        modifier = Modifier.testTag("recipe_editor_save"),
                    ) {
                        Text(stringResource(R.string.recipe_save))
                    }
                }
            }
            if (state.loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.photoFailed) {
                    PhotoFailedCard(
                        onRetry = { onIntent(RecipeEditorIntent.RetryPhoto) },
                        onSkip = { onIntent(RecipeEditorIntent.SkipPhoto) },
                    )
                }
                state.error?.let { detail ->
                    ErrorText(detail.ifEmpty { stringResource(R.string.recipe_error_generic) }, "recipe_editor_error")
                }
                if (state.atRecipeLimit) {
                    ErrorText(
                        stringResource(R.string.recipe_error_limit, RecipeRepository.MAX_RECIPES),
                        "recipe_editor_limit",
                    )
                }

                PhotoSection(
                    preview = state.previewPhoto,
                    onPick = launchPicker,
                    onRemove = { onIntent(RecipeEditorIntent.RemovePhoto) },
                )

                OutlinedTextField(
                    value = form.name,
                    onValueChange = { onIntent(RecipeEditorIntent.NameChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_name"),
                    label = { Text(stringResource(R.string.recipe_name)) },
                    singleLine = true,
                    isError = errors.name != null,
                    supportingText = errors.name.message(R.string.recipe_error_name_required)?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )

                FieldLabel(stringResource(R.string.recipe_type))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RecipeType.entries.forEach { type ->
                        FilterChip(
                            selected = form.type == type,
                            onClick = { onIntent(RecipeEditorIntent.TypeSelected(type)) },
                            label = { Text(stringResource(type.labelRes)) },
                            modifier = Modifier.testTag("recipe_type_${type.name.lowercase()}"),
                        )
                    }
                }

                OutlinedTextField(
                    value = form.glass,
                    onValueChange = { onIntent(RecipeEditorIntent.GlassChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_glass"),
                    label = { Text(stringResource(R.string.recipe_glass)) },
                    singleLine = true,
                    isError = errors.glass != null,
                    supportingText = errors.glass.message()?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )
                OutlinedTextField(
                    value = form.category,
                    onValueChange = { onIntent(RecipeEditorIntent.CategoryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_category"),
                    label = { Text(stringResource(R.string.recipe_category)) },
                    singleLine = true,
                    isError = errors.category != null,
                    supportingText = errors.category.message()?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )

                FieldLabel(stringResource(R.string.recipe_ingredients))
                form.ingredients.forEachIndexed { index, row ->
                    IngredientEditorRow(
                        row = row,
                        ingredientError = errors.ingredients[row.key]
                            .message(R.string.recipe_error_ingredient_missing),
                        measureError = errors.measures[row.key].message(),
                        canMoveUp = index > 0,
                        canMoveDown = index < form.ingredients.lastIndex,
                        onIntent = onIntent,
                    )
                }
                if (state.canAddIngredient) {
                    TextButton(
                        onClick = { onIntent(RecipeEditorIntent.AddIngredient) },
                        modifier = Modifier.testTag("recipe_add_ingredient"),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.recipe_add_ingredient))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.recipe_ingredient_limit, RecipeLimits.INGREDIENTS),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedTextField(
                    value = form.instructions,
                    onValueChange = { onIntent(RecipeEditorIntent.InstructionsChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_instructions"),
                    label = { Text(stringResource(R.string.recipe_instructions)) },
                    minLines = 4,
                    isError = errors.instructions != null,
                    supportingText = errors.instructions.message()?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )

                if (state.editing) {
                    OutlinedButton(
                        onClick = { onIntent(RecipeEditorIntent.Delete) },
                        enabled = !state.saving,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 24.dp)
                            .testTag("recipe_delete"),
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.recipe_delete))
                    }
                }
            }
        }
    }

    if (state.confirmDiscard) {
        AlertDialog(
            onDismissRequest = { onIntent(RecipeEditorIntent.DismissDiscard) },
            title = { Text(stringResource(R.string.recipe_discard_title)) },
            text = { Text(stringResource(R.string.recipe_discard_body)) },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(RecipeEditorIntent.ConfirmDiscard) },
                    modifier = Modifier.testTag("recipe_discard_confirm"),
                ) {
                    Text(stringResource(R.string.recipe_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(RecipeEditorIntent.DismissDiscard) }) {
                    Text(stringResource(R.string.recipe_keep_editing))
                }
            },
        )
    }
    if (state.confirmDelete) {
        AlertDialog(
            onDismissRequest = { onIntent(RecipeEditorIntent.DismissDelete) },
            title = { Text(stringResource(R.string.recipe_delete_title)) },
            text = { Text(stringResource(R.string.recipe_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(RecipeEditorIntent.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("recipe_delete_confirm"),
                ) {
                    Text(stringResource(R.string.recipe_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(RecipeEditorIntent.DismissDelete) }) {
                    Text(stringResource(R.string.recipe_cancel))
                }
            },
        )
    }
}

/** The current or newly picked photo, with buttons to change or remove it. */
@Composable
private fun PhotoSection(
    preview: Any?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
) {
    val model = if (preview is String) Drink.resolveThumbUrl(preview) else preview
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPick)
            .testTag("recipe_photo"),
        contentAlignment = Alignment.Center,
    ) {
        if (model != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(model)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.recipe_photo),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AddAPhoto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = stringResource(R.string.recipe_add_photo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
    if (model != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onPick) { Text(stringResource(R.string.recipe_change_photo)) }
            TextButton(
                onClick = onRemove,
                modifier = Modifier.testTag("recipe_remove_photo"),
            ) {
                Text(stringResource(R.string.recipe_remove_photo))
            }
        }
    }
}

@Composable
private fun IngredientEditorRow(
    row: IngredientField,
    ingredientError: String?,
    measureError: String?,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onIntent: (RecipeEditorIntent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recipe_ingredient_row"),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = row.ingredient,
            onValueChange = { onIntent(RecipeEditorIntent.IngredientChanged(row.key, it)) },
            modifier = Modifier.weight(1.4f),
            label = { Text(stringResource(R.string.recipe_ingredient)) },
            singleLine = true,
            isError = ingredientError != null,
            supportingText = ingredientError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )
        OutlinedTextField(
            value = row.measure,
            onValueChange = { onIntent(RecipeEditorIntent.MeasureChanged(row.key, it)) },
            modifier = Modifier.weight(1f),
            label = { Text(stringResource(R.string.recipe_measure)) },
            singleLine = true,
            isError = measureError != null,
            supportingText = measureError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Column {
            IconButton(
                onClick = { onIntent(RecipeEditorIntent.MoveIngredient(row.key, up = true)) },
                enabled = canMoveUp,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.recipe_move_up))
            }
            IconButton(
                onClick = { onIntent(RecipeEditorIntent.MoveIngredient(row.key, up = false)) },
                enabled = canMoveDown,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(R.string.recipe_move_down))
            }
        }
        IconButton(onClick = { onIntent(RecipeEditorIntent.RemoveIngredient(row.key)) }) {
            Icon(
                Icons.Outlined.RemoveCircleOutline,
                contentDescription = stringResource(R.string.recipe_remove_ingredient),
            )
        }
    }
}

/** The recipe saved but its photo didn't: retry, or leave without it. */
@Composable
private fun PhotoFailedCard(onRetry: () -> Unit, onSkip: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recipe_photo_failed"),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.recipe_photo_failed),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(R.string.recipe_photo_failed_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onSkip) { Text(stringResource(R.string.recipe_photo_skip)) }
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.testTag("recipe_photo_retry"),
                ) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ErrorText(text: String, tag: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
    )
}

/** The inline message for a field error; [requiredRes] says what's missing. */
@Composable
private fun FieldError?.message(requiredRes: Int = R.string.recipe_error_name_required): String? = when (this) {
    null -> null
    FieldError.Required -> stringResource(requiredRes)
    is FieldError.TooLong -> stringResource(R.string.recipe_error_too_long, max)
}

private val RecipeType.labelRes: Int
    get() = when (this) {
        RecipeType.NONE -> R.string.recipe_type_none
        RecipeType.ALCOHOLIC -> R.string.recipe_type_alcoholic
        RecipeType.NON_ALCOHOLIC -> R.string.recipe_type_non_alcoholic
        RecipeType.OPTIONAL -> R.string.recipe_type_optional
    }
