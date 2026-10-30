package com.capstone.nik.mixology.ui.recipes

import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.repository.RecipeHttpException
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    private val recipes: RecipeRepository,
) : MviViewModel<ImportRecipeIntent, ImportRecipeUiState, ImportRecipeEffect>(ImportRecipeUiState()) {

    override fun onIntent(intent: ImportRecipeIntent) {
        when (intent) {
            is ImportRecipeIntent.TextChanged -> setState {
                copy(text = intent.value.take(IMPORT_TEXT_LIMIT), error = null)
            }
            ImportRecipeIntent.Import -> import()
        }
    }

    private fun import() {
        val state = currentState
        if (!state.canImport) return
        setState { copy(importing = true, error = null) }
        viewModelScope.launch {
            try {
                val imported = recipes.importRecipe(state.text.trim())
                setState { copy(importing = false) }
                sendEffect(ImportRecipeEffect.Imported(imported.recipe.id, imported.duplicate))
            } catch (error: CancellationException) {
                throw error
            } catch (error: RecipeHttpException) {
                setState { copy(importing = false, error = error.toImportError()) }
            } catch (_: Exception) {
                setState { copy(importing = false, error = ImportRecipeError.Generic) }
            }
        }
    }

    private fun RecipeHttpException.toImportError(): ImportRecipeError = when (code) {
        402 -> ImportRecipeError.OutOfCredits
        // The server couldn't reach the post, or gave up waiting on it.
        502, 504 -> ImportRecipeError.Unreachable
        else -> message?.takeIf { it.isNotBlank() }
            ?.let { ImportRecipeError.Server(it) }
            ?: ImportRecipeError.Generic
    }
}
