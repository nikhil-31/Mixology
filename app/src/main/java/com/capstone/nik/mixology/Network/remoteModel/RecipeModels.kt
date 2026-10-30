package com.capstone.nik.mixology.Network.remoteModel

data class RecipeIngredientWrite(
    val ingredient: String,
    val measure: String? = null,
)

/** The body of a create or update; the photo is uploaded separately. */
data class RecipeWrite(
    val name: String,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val instructions: String? = null,
    val ingredients: List<RecipeIngredientWrite> = emptyList(),
)

/**
 * `POST me/recipes/import`: a link, the text a share sheet sends, or a pasted
 * recipe. The server finds the link in [text] and reads the rest as the caption.
 */
data class RecipeImportRequest(
    val text: String,
    /** Save the recipe with the post's photo, not just draft it. */
    val save: Boolean = true,
)

data class RecipeImportResponse(
    /** The saved recipe, or the one the user already had from this link. */
    val saved: ApiDrink? = null,
    /** [saved] was imported before; nothing new was added. */
    val duplicate: Boolean = false,
    /** Ada credits left, when the server's model read the post. */
    val credits: ChatCreditsDto? = null,
)
