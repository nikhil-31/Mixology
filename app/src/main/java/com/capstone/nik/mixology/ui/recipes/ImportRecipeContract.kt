package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.Network.ConnectionError

/** The most text the server reads from one import. */
const val IMPORT_TEXT_LIMIT = 5000

/** Why an import didn't save a recipe. */
sealed interface ImportRecipeError {
    /** The server's message, such as "No drink recipe was found there". */
    data class Server(val detail: String) : ImportRecipeError
    /** The server couldn't fetch the post (502/504); pasting its caption may still work. */
    data object Unreachable : ImportRecipeError
    /** The app got no answer from the server: the device is offline or the server is down. */
    data class NoConnection(val error: ConnectionError) : ImportRecipeError
    /** This month's Ada credits are spent. */
    data object OutOfCredits : ImportRecipeError
    data object Generic : ImportRecipeError
}

data class ImportRecipeUiState(
    /** A link, the text a share sheet sends, or a pasted recipe. */
    val text: String = "",
    val importing: Boolean = false,
    val error: ImportRecipeError? = null,
) {
    val canImport: Boolean get() = text.isNotBlank() && !importing
}

sealed interface ImportRecipeIntent {
    /** Typed or pasted text; cut to [IMPORT_TEXT_LIMIT]. */
    data class TextChanged(val value: String) : ImportRecipeIntent
    data object Import : ImportRecipeIntent
}

sealed interface ImportRecipeEffect {
    /** Recipe [id] is saved; [duplicate] when it was imported before. */
    data class Imported(val id: String, val duplicate: Boolean) : ImportRecipeEffect
}
