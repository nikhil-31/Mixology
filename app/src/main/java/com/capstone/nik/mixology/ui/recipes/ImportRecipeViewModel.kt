package com.capstone.nik.mixology.ui.recipes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.connectionError
import com.capstone.nik.mixology.repository.RecipeHttpException
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.ada.AdaInbox
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val networkMonitor: NetworkMonitor,
    private val adaInbox: AdaInbox,
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : MviViewModel<ImportRecipeIntent, ImportRecipeUiState, ImportRecipeEffect>(
    // What was pasted comes back if the system closed the app in the background.
    ImportRecipeUiState(text = savedState.get<String>(KEY_TEXT).orEmpty()),
) {

    private var importJob: Job? = null

    init {
        viewModelScope.launch {
            state.map { it.text }.distinctUntilChanged().collect { savedState[KEY_TEXT] = it }
        }
    }

    override fun onIntent(intent: ImportRecipeIntent) {
        when (intent) {
            is ImportRecipeIntent.TextChanged -> setState {
                copy(text = intent.value.take(IMPORT_TEXT_LIMIT), error = null)
            }
            ImportRecipeIntent.Import -> import()
            is ImportRecipeIntent.Shared -> {
                if (intent.value.isBlank() || currentState.importing) return
                setState { copy(text = intent.value.take(IMPORT_TEXT_LIMIT), error = null) }
                import()
            }
            ImportRecipeIntent.Cancel -> {
                importJob?.cancel()
                importJob = null
                setState { copy(importing = false) }
            }
            ImportRecipeIntent.DismissError -> setState { copy(error = null) }
            ImportRecipeIntent.AskAda -> {
                val text = currentState.text.trim()
                if (text.isEmpty() || currentState.importing) return
                adaInbox.post(text)
                setState { copy(error = null) }
                sendEffect(ImportRecipeEffect.OpenAda)
            }
        }
    }

    private fun import() {
        val state = currentState
        if (!state.canImport) return
        setState { copy(importing = true, error = null) }
        importJob = viewModelScope.launch {
            try {
                val imported = recipes.importRecipe(state.text.trim())
                setState { copy(importing = false) }
                sendEffect(ImportRecipeEffect.Imported(imported.recipe.id, imported.duplicate))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(importing = false, error = importError(error)) }
            }
        }
    }

    private fun importError(error: Exception): ImportRecipeError {
        networkMonitor.connectionError(error)?.let { return ImportRecipeError.NoConnection(it) }
        return (error as? RecipeHttpException)?.toImportError() ?: ImportRecipeError.Generic
    }

    private fun RecipeHttpException.toImportError(): ImportRecipeError = when (code) {
        402 -> ImportRecipeError.OutOfCredits
        // The server couldn't reach the post, or gave up waiting on it.
        502, 504 -> ImportRecipeError.Unreachable
        // Only 422 means the post was read and held no recipe.
        else -> message?.takeIf { it.isNotBlank() }
            ?.let { if (code == 422) ImportRecipeError.Server(it) else ImportRecipeError.Failed(it) }
            ?: ImportRecipeError.Generic
    }

    private companion object {
        const val KEY_TEXT = "import_text"
    }
}
