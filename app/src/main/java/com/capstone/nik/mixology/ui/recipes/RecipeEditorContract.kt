package com.capstone.nik.mixology.ui.recipes

import android.net.Uri
import com.capstone.nik.mixology.repository.RecipeDraft
import com.capstone.nik.mixology.ui.model.IngredientMeasure

/** The server's limits for a recipe; the editor checks them before saving. */
object RecipeLimits {
    const val NAME = 80
    const val INGREDIENTS = 15
    const val INGREDIENT = 60
    const val MEASURE = 40
    const val INSTRUCTIONS = 2000
    const val GLASS = 40
    const val CATEGORY = 40
}

/** The drink's type, as the server spells it; [NONE] leaves it unset. */
enum class RecipeType(val apiValue: String?) {
    NONE(null),
    ALCOHOLIC("Alcoholic"),
    NON_ALCOHOLIC("Non alcoholic"),
    OPTIONAL("Optional alcohol"),
    ;

    companion object {
        fun fromApi(value: String?): RecipeType =
            entries.firstOrNull { it.apiValue != null && it.apiValue.equals(value, ignoreCase = true) } ?: NONE
    }
}

/** One ingredient row; [key] keeps the row's identity while rows move. */
data class IngredientField(
    val key: Long,
    val ingredient: String = "",
    val measure: String = "",
) {
    val isBlank: Boolean get() = ingredient.isBlank() && measure.isBlank()
}

/** Everything the user can change; compared with the loaded form to know if it's dirty. */
data class RecipeForm(
    val name: String = "",
    val type: RecipeType = RecipeType.NONE,
    val glass: String = "",
    val category: String = "",
    val instructions: String = "",
    val ingredients: List<IngredientField> = listOf(IngredientField(0)),
    /** A newly picked photo, not uploaded yet. */
    val pickedPhoto: Uri? = null,
    /** The user removed the saved photo. */
    val photoRemoved: Boolean = false,
) {
    fun toDraft() = RecipeDraft(
        name = name.trim(),
        alcoholic = type.apiValue,
        glass = glass.trim(),
        category = category.trim(),
        instructions = instructions.trim(),
        ingredients = ingredients.filterNot { it.isBlank }
            .map { IngredientMeasure(it.ingredient.trim(), it.measure.trim()) },
    )
}

sealed interface FieldError {
    data object Required : FieldError
    data class TooLong(val max: Int) : FieldError
}

data class RecipeErrors(
    val name: FieldError? = null,
    val glass: FieldError? = null,
    val category: FieldError? = null,
    val instructions: FieldError? = null,
    /** Per ingredient row [IngredientField.key]. */
    val ingredients: Map<Long, FieldError> = emptyMap(),
    val measures: Map<Long, FieldError> = emptyMap(),
) {
    val isEmpty: Boolean
        get() = name == null && glass == null && category == null && instructions == null &&
            ingredients.isEmpty() && measures.isEmpty()
}

/** Checks [form] against [RecipeLimits]; blank ingredient rows are ignored. */
fun validateRecipe(form: RecipeForm): RecipeErrors {
    fun tooLong(value: String, max: Int): FieldError? =
        if (value.trim().length > max) FieldError.TooLong(max) else null

    val rows = form.ingredients.filterNot { it.isBlank }
    return RecipeErrors(
        name = if (form.name.isBlank()) FieldError.Required else tooLong(form.name, RecipeLimits.NAME),
        glass = tooLong(form.glass, RecipeLimits.GLASS),
        category = tooLong(form.category, RecipeLimits.CATEGORY),
        instructions = tooLong(form.instructions, RecipeLimits.INSTRUCTIONS),
        ingredients = rows.mapNotNull { row ->
            val error = if (row.ingredient.isBlank()) {
                // A measure with nothing to measure.
                FieldError.Required
            } else {
                tooLong(row.ingredient, RecipeLimits.INGREDIENT)
            }
            error?.let { row.key to it }
        }.toMap(),
        measures = rows.mapNotNull { row ->
            tooLong(row.measure, RecipeLimits.MEASURE)?.let { row.key to it }
        }.toMap(),
    )
}

data class RecipeEditorUiState(
    /** Null while creating; set once the recipe exists on the server. */
    val recipeId: String? = null,
    val loading: Boolean = false,
    val form: RecipeForm = RecipeForm(),
    /** The saved photo's thumb (relative to the API), if any. */
    val savedPhoto: String? = null,
    /** Shown once the user has tried to save. */
    val errors: RecipeErrors = RecipeErrors(),
    val saving: Boolean = false,
    val dirty: Boolean = false,
    /** The server's message, or empty for a generic one. */
    val error: String? = null,
    /** The user is at [com.capstone.nik.mixology.repository.RecipeRepository.MAX_RECIPES]. */
    val atRecipeLimit: Boolean = false,
    /** The recipe saved but its photo didn't; offers Retry. */
    val photoFailed: Boolean = false,
    val confirmDiscard: Boolean = false,
    val confirmDelete: Boolean = false,
) {
    val editing: Boolean get() = recipeId != null
    val canAddIngredient: Boolean get() = form.ingredients.size < RecipeLimits.INGREDIENTS

    /** What the photo preview shows: the new pick, else the saved photo unless removed. */
    val previewPhoto: Any?
        get() = form.pickedPhoto ?: savedPhoto?.takeUnless { form.photoRemoved }
}

sealed interface RecipeEditorIntent {
    /** Opens the editor for recipe [id], or a new recipe when null. */
    data class Bind(val id: String?) : RecipeEditorIntent
    data class NameChanged(val value: String) : RecipeEditorIntent
    data class TypeSelected(val type: RecipeType) : RecipeEditorIntent
    data class GlassChanged(val value: String) : RecipeEditorIntent
    data class CategoryChanged(val value: String) : RecipeEditorIntent
    data class InstructionsChanged(val value: String) : RecipeEditorIntent
    data class IngredientChanged(val key: Long, val value: String) : RecipeEditorIntent
    data class MeasureChanged(val key: Long, val value: String) : RecipeEditorIntent
    data object AddIngredient : RecipeEditorIntent
    data class RemoveIngredient(val key: Long) : RecipeEditorIntent
    data class MoveIngredient(val key: Long, val up: Boolean) : RecipeEditorIntent
    data class PhotoPicked(val uri: Uri) : RecipeEditorIntent
    data object RemovePhoto : RecipeEditorIntent
    data object Save : RecipeEditorIntent
    data object RetryPhoto : RecipeEditorIntent
    /** Leave with the recipe saved and no photo. */
    data object SkipPhoto : RecipeEditorIntent
    /** Back or close: asks first when there are unsaved changes. */
    data object Close : RecipeEditorIntent
    data object ConfirmDiscard : RecipeEditorIntent
    data object DismissDiscard : RecipeEditorIntent
    data object Delete : RecipeEditorIntent
    data object ConfirmDelete : RecipeEditorIntent
    data object DismissDelete : RecipeEditorIntent
}

sealed interface RecipeEditorEffect {
    data object Close : RecipeEditorEffect
    data object Deleted : RecipeEditorEffect
}
