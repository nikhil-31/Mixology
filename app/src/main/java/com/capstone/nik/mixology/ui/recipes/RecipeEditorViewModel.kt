package com.capstone.nik.mixology.ui.recipes

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.connectionError
import com.capstone.nik.mixology.Network.serverDetail
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.ada.RecipeDraftHandoff
import com.capstone.nik.mixology.ui.ada.RecipeDraftRequest
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val photos: RecipePhotoLoader,
    private val analytics: AnalyticsTracker = AnalyticsTracker.forTests(),
    private val handoff: RecipeDraftHandoff = RecipeDraftHandoff(),
    private val networkMonitor: NetworkMonitor = NetworkMonitor.forTests(),
    private val savedState: SavedStateHandle = SavedStateHandle(),
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
    /** Ada's card this editor was opened from ("Edit draft"), told once it's saved. */
    private var draftMessageId: Long? = null
    /** The recipe id the editor was opened for; null for a new recipe. */
    private var boundId: String? = null
    /** Unsaved edits from before the system closed the app, put back once the recipe is loaded. */
    private var restored: Bundle? = savedState.get<Bundle>(KEY_UNSAVED)

    init {
        // Keeps unsaved edits where the system can hand them back; nothing is kept once they're saved.
        viewModelScope.launch {
            state.map { Triple(it.form, it.dirty, it.sourceUrl to it.source) }
                .distinctUntilChanged()
                .collect { (form, dirty) ->
                    // Until the edits handed back have been applied, they are what's worth keeping.
                    if (bound && restored == null) savedState[KEY_UNSAVED] = form.takeIf { dirty }?.let(::unsaved)
                }
        }
    }

    override fun onIntent(intent: RecipeEditorIntent) {
        when (intent) {
            is RecipeEditorIntent.Bind -> bind(intent.id)
            RecipeEditorIntent.RetryLoad -> currentState.recipeId?.takeIf { currentState.loadFailed }?.let(::load)
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
        boundId = id?.takeIf { it.isNotBlank() }
        // Edits to some other recipe aren't this one's.
        restored = restored?.takeIf { it.getString(UNSAVED_ID) == boundId }
        if (id.isNullOrBlank()) {
            val unsaved = restored
            restored = null
            if (unsaved != null) restoreNew(unsaved) else handoff.take()?.let(::fillDraft)
            return
        }
        val cached = recipes.recipes.value.firstOrNull { it.id == id }
        if (cached != null) {
            fill(cached)
            return
        }
        load(id)
    }

    private fun load(id: String) {
        setState { copy(recipeId = id, loading = true, loadFailed = false, error = null, connectionError = null) }
        viewModelScope.launch {
            try {
                fill(recipes.get(id))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failed(error)
                setState { copy(loading = false, loadFailed = true) }
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
        val form = restored?.let(::formFrom) ?: savedForm
        restored = null
        nextKey = maxOf(nextKey, form.ingredients.maxOf { it.key } + 1)
        setState {
            copy(
                recipeId = recipe.id,
                loading = false,
                form = form,
                savedPhoto = recipe.thumb.takeIf { recipe.hasUsableThumb() },
                sourceUrl = recipe.sourceUrl,
                source = recipe.source,
                dirty = form != savedForm,
                error = null,
            )
        }
    }

    /** Puts back a new recipe that was being written, or Ada's draft that was being changed. */
    private fun restoreNew(unsaved: Bundle) {
        val form = formFrom(unsaved)
        nextKey = form.ingredients.maxOf { it.key } + 1
        draftMessageId = unsaved.getLong(UNSAVED_MESSAGE, NO_MESSAGE).takeIf { it != NO_MESSAGE }
        setState {
            copy(
                form = form,
                sourceUrl = unsaved.getString(UNSAVED_SOURCE_URL),
                source = unsaved.getString(UNSAVED_SOURCE),
                dirty = true,
            )
        }
    }

    private fun unsaved(form: RecipeForm) = Bundle().apply {
        putString(UNSAVED_ID, boundId)
        putString(UNSAVED_NAME, form.name)
        putString(UNSAVED_TYPE, form.type.name)
        putString(UNSAVED_GLASS, form.glass)
        putString(UNSAVED_CATEGORY, form.category)
        putString(UNSAVED_INSTRUCTIONS, form.instructions)
        putLongArray(UNSAVED_KEYS, form.ingredients.map { it.key }.toLongArray())
        putStringArrayList(UNSAVED_INGREDIENTS, ArrayList(form.ingredients.map { it.ingredient }))
        putStringArrayList(UNSAVED_MEASURES, ArrayList(form.ingredients.map { it.measure }))
        putBoolean(UNSAVED_PHOTO_REMOVED, form.photoRemoved)
        putString(UNSAVED_PHOTO, form.pickedPhoto?.takeIf { it.scheme == "https" || it.scheme == "http" }?.toString())
        putString(UNSAVED_SOURCE_URL, currentState.sourceUrl)
        putString(UNSAVED_SOURCE, currentState.source)
        putLong(UNSAVED_MESSAGE, draftMessageId ?: NO_MESSAGE)
    }

    // A photo picked on the device isn't kept: the app's access to it doesn't outlive the
    // process. One Ada found on the web is only a link, so it is.
    private fun formFrom(unsaved: Bundle): RecipeForm {
        val keys = unsaved.getLongArray(UNSAVED_KEYS) ?: LongArray(0)
        val ingredients = unsaved.getStringArrayList(UNSAVED_INGREDIENTS).orEmpty()
        val measures = unsaved.getStringArrayList(UNSAVED_MEASURES).orEmpty()
        val rows = keys.mapIndexed { index, key ->
            IngredientField(key, ingredients.getOrNull(index).orEmpty(), measures.getOrNull(index).orEmpty())
        }.ifEmpty { listOf(IngredientField(0)) }
        return RecipeForm(
            name = unsaved.getString(UNSAVED_NAME).orEmpty(),
            type = RecipeType.entries.firstOrNull { it.name == unsaved.getString(UNSAVED_TYPE) } ?: RecipeType.NONE,
            glass = unsaved.getString(UNSAVED_GLASS).orEmpty(),
            category = unsaved.getString(UNSAVED_CATEGORY).orEmpty(),
            instructions = unsaved.getString(UNSAVED_INSTRUCTIONS).orEmpty(),
            ingredients = rows,
            pickedPhoto = unsaved.getString(UNSAVED_PHOTO)?.let(Uri::parse),
            photoRemoved = unsaved.getBoolean(UNSAVED_PHOTO_REMOVED),
        )
    }

    /** A recipe Ada read, ready to change before it's saved: new and unsaved. */
    private fun fillDraft(request: RecipeDraftRequest) {
        val draft = request.recipe
        draftMessageId = request.messageId
        val rows = draft.ingredients.take(RecipeLimits.INGREDIENTS).mapIndexed { index, item ->
            IngredientField(
                index.toLong(),
                item.ingredient.take(RecipeLimits.INGREDIENT),
                item.measure.take(RecipeLimits.MEASURE),
            )
        }.ifEmpty { listOf(IngredientField(0)) }
        nextKey = rows.size.toLong()
        val form = RecipeForm(
            name = draft.name.take(RecipeLimits.NAME),
            type = RecipeType.fromApi(draft.alcoholic),
            glass = draft.glass.orEmpty().take(RecipeLimits.GLASS),
            category = draft.category.orEmpty().take(RecipeLimits.CATEGORY),
            instructions = draft.instructions.orEmpty().take(RecipeLimits.INSTRUCTIONS),
            ingredients = rows,
            pickedPhoto = draft.photoUrl?.let(Uri::parse),
        )
        // savedForm stays empty, so the filled form is unsaved and Back asks first.
        setState {
            copy(
                form = form,
                sourceUrl = draft.sourceUrl,
                source = draft.source,
                dirty = true,
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
        if (state.saving || state.loading || state.loadFailed) return
        submitted = true
        val errors = validateRecipe(state.form)
        val atLimit = !state.editing && recipes.recipes.value.size >= RecipeRepository.MAX_RECIPES
        setState { copy(errors = errors, atRecipeLimit = atLimit, error = null, connectionError = null) }
        if (!errors.isEmpty || atLimit) return

        val form = state.form
        val id = state.recipeId
        setState { copy(saving = true, photoFailed = false) }
        viewModelScope.launch {
            val saved = try {
                if (id == null) {
                    recipes.create(form.toDraft().copy(sourceUrl = state.sourceUrl))
                } else {
                    recipes.update(id, form.toDraft())
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failed(error)
                return@launch
            }
            if (id == null) {
                analytics.logRecipeCreated(saved.id, saved.name)
                draftMessageId?.let { handoff.saved(it, saved.id) }
                draftMessageId = null
            } else {
                analytics.logRecipeUpdated(saved.id, saved.name)
            }
            // The recipe is on the server now: a retry only resends the photo.
            savedForm = form.copy(pickedPhoto = null, photoRemoved = false)
            setState {
                copy(
                    recipeId = saved.id,
                    savedPhoto = saved.thumb.takeIf { saved.hasUsableThumb() },
                    sourceUrl = saved.sourceUrl ?: sourceUrl,
                    source = saved.source ?: source,
                    dirty = false,
                )
            }
            finishWithPhoto(saved.id, form)
        }
    }

    /** A load, save or delete failed: offline and server-down get their own words, not the exception's. */
    private fun failed(error: Exception) {
        val connection = networkMonitor.connectionError(error)
        setState {
            copy(
                saving = false,
                error = error.serverDetail().takeIf { connection == null },
                connectionError = connection,
            )
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
        setState { copy(confirmDelete = false, saving = true, error = null, connectionError = null) }
        viewModelScope.launch {
            try {
                recipes.delete(id)
                analytics.logRecipeDeleted(id, name)
                setState { copy(saving = false, dirty = false) }
                sendEffect(RecipeEditorEffect.Deleted)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                failed(error)
            }
        }
    }

    private companion object {
        const val KEY_UNSAVED = "recipe_editor_unsaved"
        const val UNSAVED_ID = "id"
        const val UNSAVED_NAME = "name"
        const val UNSAVED_TYPE = "type"
        const val UNSAVED_GLASS = "glass"
        const val UNSAVED_CATEGORY = "category"
        const val UNSAVED_INSTRUCTIONS = "instructions"
        const val UNSAVED_KEYS = "keys"
        const val UNSAVED_INGREDIENTS = "ingredients"
        const val UNSAVED_MEASURES = "measures"
        const val UNSAVED_PHOTO_REMOVED = "photo_removed"
        const val UNSAVED_PHOTO = "photo"
        const val UNSAVED_SOURCE_URL = "source_url"
        const val UNSAVED_SOURCE = "source"
        const val UNSAVED_MESSAGE = "message"
        const val NO_MESSAGE = -1L
    }
}
