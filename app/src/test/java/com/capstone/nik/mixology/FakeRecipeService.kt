package com.capstone.nik.mixology

import com.capstone.nik.mixology.Network.RecipeService
import com.capstone.nik.mixology.Network.remoteModel.RecipeWrite
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.Network.remoteModel.ChatCreditsDto
import com.capstone.nik.mixology.Network.remoteModel.RecipeImportRequest
import com.capstone.nik.mixology.Network.remoteModel.RecipeImportResponse
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException

/** An in-memory `me/recipes`; set [failWith] to answer every call with that HTTP status. */
class FakeRecipeService : RecipeService {
    val recipes = mutableListOf<ApiDrink>()
    /** Every call answers with this status and a `{"detail"}` body while set. */
    var failWith: Int? = null
    /** Every call fails as if the server can't be reached while set. */
    var failWithIo = false
    /** Only photo uploads fail, with this status, while set. */
    var failPhotoWith: Int? = null
    val uploads = mutableListOf<Pair<String, ByteArray>>()
    val writes = mutableListOf<RecipeWrite>()
    val calls = mutableListOf<String>()
    val imports = mutableListOf<RecipeImportRequest>()
    /** What an import saves; importing it again while it's stored is a duplicate. */
    var importReply = ApiDrink(
        id = "u-import",
        name = "Imported Sour",
        thumb = "recipe-photos/import.jpg",
        ingredients = listOf(IngredientMeasure("Whiskey", "2 oz")),
    )
    var importCredits: ChatCreditsDto? = null
    /** Only imports fail, with this status and detail, while set. */
    var failImportWith: Pair<Int, String>? = null
    /** Imports wait on this while set, like a slow post. */
    var importGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null
    private var nextId = 1

    override suspend fun list(): List<ApiDrink> {
        record("list")
        return recipes.toList()
    }

    override suspend fun create(body: RecipeWrite): ApiDrink {
        record("create")
        writes += body
        val recipe = body.toApi("u-${nextId++}", thumb = null)
        recipes.add(0, recipe)
        return recipe
    }

    override suspend fun get(id: String): ApiDrink {
        record("get $id")
        return find(id)
    }

    override suspend fun update(id: String, body: RecipeWrite): ApiDrink {
        record("update $id")
        writes += body
        val existing = find(id)
        // Like the server, an edit keeps the link the recipe was imported from.
        val updated = body.toApi(id, thumb = existing.thumb)
            .copy(sourceUrl = existing.sourceUrl, source = existing.source)
        replace(updated)
        return updated
    }

    override suspend fun delete(id: String) {
        record("delete $id")
        find(id)
        recipes.removeAll { it.id == id }
    }

    override suspend fun uploadPhoto(id: String, jpeg: RequestBody): ApiDrink {
        record("photo $id")
        failPhotoWith?.let { throw httpError(it) }
        val bytes = Buffer().also { jpeg.writeTo(it) }.readByteArray()
        uploads += id to bytes
        val updated = find(id).copy(thumb = "recipe-photos/$id.jpg")
        replace(updated)
        return updated
    }

    override suspend fun deletePhoto(id: String): ApiDrink {
        record("delete photo $id")
        val updated = find(id).copy(thumb = null)
        replace(updated)
        return updated
    }

    override suspend fun importRecipe(body: RecipeImportRequest): RecipeImportResponse {
        record("import")
        importGate?.await()
        failImportWith?.let { (code, detail) -> throw httpError(code, detail) }
        imports += body
        val duplicate = recipes.any { it.id == importReply.id }
        if (!duplicate) recipes.add(0, importReply)
        return RecipeImportResponse(saved = importReply, duplicate = duplicate, credits = importCredits)
    }

    private fun record(call: String) {
        calls += call
        if (failWithIo) throw ConnectException("Failed to connect to /10.0.2.2:8000")
        failWith?.let { throw httpError(it) }
    }

    private fun find(id: String): ApiDrink = recipes.firstOrNull { it.id == id } ?: throw httpError(404)

    private fun replace(recipe: ApiDrink) {
        val index = recipes.indexOfFirst { it.id == recipe.id }
        recipes[index] = recipe
    }

    private fun RecipeWrite.toApi(id: String, thumb: String?) = ApiDrink(
        id = id,
        name = name,
        thumb = thumb,
        alcoholic = alcoholic,
        glass = glass,
        category = category,
        instructions = instructions,
        ingredients = ingredients.map { IngredientMeasure(it.ingredient, it.measure.orEmpty()) },
        sourceUrl = sourceUrl,
    )

    companion object {
        fun httpError(code: Int, detail: String = "Error $code"): HttpException = HttpException(
            Response.error<Any>(code, """{"detail":"$detail"}""".toResponseBody(null)),
        )
    }
}
