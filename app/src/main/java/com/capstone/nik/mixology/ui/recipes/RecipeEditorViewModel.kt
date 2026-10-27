package com.capstone.nik.mixology.ui.recipes

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val photos: RecipePhotoLoader,
    private val analytics: AnalyticsTracker = AnalyticsTracker.forTests(),
) : MviViewModel<RecipeEditorIntent, RecipeEditorUiState, RecipeEditorEffect>(RecipeEditorUiState()) {

    private var bound = false
    /** The form as last loaded or saved; any difference makes the editor dirty. */
    private var savedForm = RecipeForm()
    /** Keys for new ingredient rows. */
    private var nextKey = 1L
    /** The picked photo, encoded once so a retry doesn't redo the work. */
    private var encoded: Pair<Uri, ByteArray>? = null
    /** Validation errors show after the first save attempt. */
    private var submitted = false

    override fun onIntent(intent: RecipeEditorIntent) {
        when (intent) {
            is RecipeEditorIntent.Bind -> bind(intent.id)
            is RecipeEditorIntent.NameChanged -> edit { copy(name = intent.value) }
            is RecipeEditorIntent.TypeSelected -> edit { copy(type = intent.type) }
            is RecipeEditorIntent.GlassChanged -> edit { copy(glass = intent.value) }
            is RecipeEditorIntent.CategoryChanged -> edit { copy(category = intent.value) }
            is RecipeEditorIntent.InstructionsChanged -> edit { copy(instructions = intent.value) }
            is RecipeEditorIntent.IngredientChanged -> editRow(intent.key) { copy(ingredient = intent.value) }
            is RecipeEditorIntent.MeasureChanged -> editRow(intent.key) { copy(measure = intent.value) }
            RecipeEditorIntent.AddIngredient -> addIngredient()
            is RecipeEditorIntent.RemoveIngredient -> removeIngredient(intent.key)
            is RecipeEditorIntent.MoveIngredient -> moveIngredient(intent.key, intent.up)
            is RecipeEditorIntent.PhotoPicked -> edit { copy(pickedPhoto = intent.uri, photoRemoved = false) }
            RecipeEditorIntent.RemovePhoto -> edit { copy(pickedPhoto = null, photoRemoved = true) }
            RecipeEditorIntent.Save -> save()
            RecipeEditorIntent.RetryPhoto -> retryPhoto()
            RecipeEditorIntent.SkipPhoto -> sendEffect(RecipeEditorEffect.Close)
            RecipeEditorIntent.Close -> close()
            RecipeEditorIntent.ConfirmDiscard -> {
                setState { copy(confirmDiscard = false) }
                sendEffect(RecipeEditorEffect.Close)
            }
            RecipeEditorIntent.DismissDiscard -> setState { copy(confirmDiscard = false) }
            RecipeEditorIntent.Delete -> if (currentState.editing) setState { copy(confirmDelete = true) }
            RecipeEditorIntent.ConfirmDelete -> delete()
            RecipeEditorIntent.DismissDelete -> setState { copy(confirmDelete = false) }
        }
    }

    private fun bind(id: String?) {
        if (bound) return
        bound = true
        if (id.isNullOrBlank()) return
        val cached = recipes.recipes.value.firstOrNull { it.id == id }
        if (cached != null) {
            fill(cached)
            return
        }
        setState { copy(recipeId = id, loading = true) }
        viewModelScope.launch {
            try {
                fill(recipes.get(id))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(loading = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun fill(recipe: Drink) {
        val rows = recipe.ingredients.mapIndexed { index, item ->
            IngredientField(index.toLong(), item.ingredient, item.measure)
        }.ifEmpty { listOf(IngredientField(0)) }
        nextKey = rows.size.toLong()
        savedForm = RecipeForm(
            name = recipe.name,
            type = RecipeType.fromApi(recipe.alcoholic),
            glass = recipe.glass.orEmpty(),
            category = recipe.category.orEmpty(),
            instructions = recipe.instructions.orEmpty(),
            ingredients = rows,
        )
        setState {
            copy(
                recipeId = recipe.id,
                loading = false,
                form = savedForm,
                savedPhoto = recipe.thumb.takeIf { recipe.hasUsableThumb() },
                dirty = false,
                error = null,
            )
        }
    }

    private fun edit(change: RecipeForm.() -> RecipeForm) {
        setState {
            val form = form.change()
            copy(
                form = form,
                dirty = form != savedForm,
                errors = if (submitted) validateRecipe(form) else errors,
                error = null,
            )
        }
    }

    private fun editRow(key: Long, change: IngredientField.() -> IngredientField) {
        edit { copy(ingredients = ingredients.map { if (it.key == key) it.change() else it }) }
    }

    private fun addIngredient() {
        if (!currentState.canAddIngredient) return
        val key = nextKey++
        edit { copy(ingredients = ingredients + IngredientField(key)) }
    }

    private fun removeIngredient(key: Long) {
        edit {
            val rest = ingredients.filterNot { it.key == key }
            // Keep one row to type into.
            copy(ingredients = rest.ifEmpty { listOf(IngredientField(nextKey++)) })
        }
    }

    private fun moveIngredient(key: Long, up: Boolean) {
        edit {
            val index = ingredients.indexOfFirst { it.key == key }
            val target = if (up) index - 1 else index + 1
            if (index < 0 || target !in ingredients.indices) return@edit this
            val rows = ingredients.toMutableList()
            rows[index] = rows[target].also { rows[target] = rows[index] }
            copy(ingredients = rows)
        }
    }

    private fun close() {
        val state = currentState
        if (state.dirty && !state.saving) {
            setState { copy(confirmDiscard = true) }
        } else {
            sendEffect(RecipeEditorEffect.Close)
        }
    }

    private fun save() {
        val state = currentState
        if (state.saving || state.loading) return
        submitted = true
        val errors = validateRecipe(state.form)
        val atLimit = !state.editing && recipes.recipes.value.size >= RecipeRepository.MAX_RECIPES
        setState { copy(errors = errors, atRecipeLimit = atLimit, error = null) }
        if (!errors.isEmpty || atLimit) return

        val form = state.form
        val id = state.recipeId
        setState { copy(saving = true, photoFailed = false) }
        viewModelScope.launch {
            val saved = try {
                if (id == null) recipes.create(form.toDraft()) else recipes.update(id, form.toDraft())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(saving = false, error = error.message.orEmpty()) }
                return@launch
            }
            if (id == null) {
                analytics.logRecipeCreated(saved.id, saved.name)
            } else {
                analytics.logRecipeUpdated(saved.id, saved.name)
            }
            // The recipe is on the server now: a retry only resends the photo.
            savedForm = form.copy(pickedPhoto = null, photoRemoved = false)
            setState {
                copy(
                    recipeId = saved.id,
                    savedPhoto = saved.thumb.takeIf { saved.hasUsableThumb() },
                    dirty = false,
                )
            }
            finishWithPhoto(saved.id, form)
        }
    }

    private fun retryPhoto() {
        val id = currentState.recipeId ?: return
        if (currentState.saving) return
        setState { copy(saving = true, photoFailed = false) }
        viewModelScope.launch { finishWithPhoto(id, currentState.form) }
    }

    /** Uploads or deletes the photo as [form] asks, then closes; on failure offers a retry. */
    private suspend fun finishWithPhoto(id: String, form: RecipeForm) {
        val ok = try {
            val picked = form.pickedPhoto
            val updated = when {
                picked != null -> recipes.uploadPhoto(id, encode(picked))
                form.photoRemoved && currentState.savedPhoto != null -> recipes.deletePhoto(id)
                else -> null
            }
            if (updated != null) {
                setState {
                    copy(
                        savedPhoto = updated.thumb.takeIf { updated.hasUsableThumb() },
                        form = this.form.copy(pickedPhoto = null, photoRemoved = false),
                    )
                }
            }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            false
        }
        setState { copy(saving = false, photoFailed = !ok) }
        if (ok) sendEffect(RecipeEditorEffect.Close)
    }

    private suspend fun encode(uri: Uri): ByteArray {
        encoded?.takeIf { it.first == uri }?.let { return it.second }
        return photos.load(uri).also { encoded = uri to it }
    }

    private fun delete() {
        val id = currentState.recipeId ?: return
        val name = currentState.form.name
        setState { copy(confirmDelete = false, saving = true, error = null) }
        viewModelScope.launch {
            try {
                recipes.delete(id)
                analytics.logRecipeDeleted(id, name)
                setState { copy(saving = false, dirty = false) }
                sendEffect(RecipeEditorEffect.Deleted)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(saving = false, error = error.message.orEmpty()) }
            }
        }
    }
}
