package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.ApiErrors
import com.capstone.nik.mixology.Network.remoteModel.RecipeIngredientWrite
import com.capstone.nik.mixology.Network.RecipeService
import com.capstone.nik.mixology.Network.remoteModel.RecipeImportRequest
import com.capstone.nik.mixology.Network.remoteModel.RecipeWrite
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.billing.SubscriptionRepository
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/** A recipe as the editor sends it; blank optional fields are left out. */
data class RecipeDraft(
    val name: String,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val instructions: String? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
)

/** A recipe read from a link or text and saved; [duplicate] when it was saved before. */
data class ImportedRecipe(val recipe: Drink, val duplicate: Boolean)

/** The server refused a recipe call; the message is its `detail` when it sent one. */
class RecipeHttpException(val code: Int, message: String) : Exception(message)

/** The user's own recipes ("My recipes"), kept in memory and synced with the server. */
interface RecipeRepository {
    /** Newest first. Empty until [load] (or another call) brings them in. */
    val recipes: StateFlow<List<Drink>>

    suspend fun load(): List<Drink>

    suspend fun get(id: String): Drink

    suspend fun create(draft: RecipeDraft): Drink

    suspend fun update(id: String, draft: RecipeDraft): Drink

    suspend fun delete(id: String)

    /** [jpeg] is the already scaled and compressed photo. */
    suspend fun uploadPhoto(id: String, jpeg: ByteArray): Drink

    suspend fun deletePhoto(id: String): Drink

    /**
     * Saves the recipe in a social post, a page or pasted [text], with the post's
     * photo. The server may use Ada's credits to read it.
     */
    suspend fun importRecipe(text: String): ImportedRecipe

    companion object {
        const val MAX_RECIPES = 100

        /** For tests and previews: no recipes, and every call fails. */
        val None = object : RecipeRepository {
            override val recipes: StateFlow<List<Drink>> = MutableStateFlow(emptyList())
            override suspend fun load(): List<Drink> = emptyList()
            override suspend fun get(id: String): Drink = error("No recipes")
            override suspend fun create(draft: RecipeDraft): Drink = error("No recipes")
            override suspend fun update(id: String, draft: RecipeDraft): Drink = error("No recipes")
            override suspend fun delete(id: String): Unit = error("No recipes")
            override suspend fun uploadPhoto(id: String, jpeg: ByteArray): Drink = error("No recipes")
            override suspend fun deletePhoto(id: String): Drink = error("No recipes")
            override suspend fun importRecipe(text: String): ImportedRecipe = error("No recipes")
        }
    }
}

@Singleton
class NetworkRecipeRepository @Inject constructor(
    private val service: RecipeService,
    private val subscriptions: SubscriptionRepository,
    private val sessionStore: SessionStore,
) : RecipeRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _recipes = MutableStateFlow<List<Drink>>(emptyList())
    override val recipes: StateFlow<List<Drink>> = _recipes.asStateFlow()

    init {
        scope.launch {
            // Another account must not see the last one's recipes.
            sessionStore.session.collect { session ->
                if (session == null) _recipes.value = emptyList()
            }
        }
    }

    override suspend fun load(): List<Drink> = call {
        service.list().mapNotNull { it.toRecipe() }.also { _recipes.value = it }
    }

    override suspend fun get(id: String): Drink = call {
        service.get(id).requireRecipe().also(::upsert)
    }

    override suspend fun create(draft: RecipeDraft): Drink = call {
        service.create(draft.toWrite()).requireRecipe().also(::upsert)
    }

    override suspend fun update(id: String, draft: RecipeDraft): Drink = call {
        service.update(id, draft.toWrite()).requireRecipe().also(::upsert)
    }

    override suspend fun delete(id: String) {
        try {
            call { service.delete(id) }
        } catch (error: RecipeHttpException) {
            // Already gone on the server: drop it here too.
            if (error.code != 404) throw error
        }
        _recipes.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun uploadPhoto(id: String, jpeg: ByteArray): Drink = call {
        service.uploadPhoto(id, jpeg.toRequestBody(JPEG)).requireRecipe().also(::upsert)
    }

    override suspend fun deletePhoto(id: String): Drink = call {
        service.deletePhoto(id).requireRecipe().also(::upsert)
    }

    override suspend fun importRecipe(text: String): ImportedRecipe {
        val response = try {
            call { service.importRecipe(RecipeImportRequest(text = text)) }
        } catch (error: RecipeHttpException) {
            // 402: this month's Ada credits are spent.
            if (error.code == 402) subscriptions.markChatCreditsSpent()
            throw error
        }
        subscriptions.updateChatCredits(response.credits)
        val recipe = response.saved?.requireRecipe() ?: error("Malformed recipe")
        upsert(recipe)
        return ImportedRecipe(recipe, response.duplicate)
    }

    /** Replaces the recipe in place, or adds it at the top as the newest. */
    private fun upsert(recipe: Drink) {
        _recipes.update { list ->
            if (list.any { it.id == recipe.id }) {
                list.map { if (it.id == recipe.id) recipe else it }
            } else {
                listOf(recipe) + list
            }
        }
    }

    private suspend fun <T> call(block: suspend () -> T): T {
        try {
            return block()
        } catch (error: HttpException) {
            val code = error.code()
            // 401: the session is gone, so sign out. 403: the trial has ended.
            if (code == 401) sessionStore.clear()
            if (code == 403) subscriptions.requireSubscription()
            throw RecipeHttpException(
                code,
                ApiErrors.detail(error.response()?.errorBody()?.string(), error.message()),
            )
        }
    }

    private fun ApiDrink.requireRecipe(): Drink = toRecipe() ?: error("Malformed recipe")

    private companion object {
        val JPEG = "image/jpeg".toMediaType()
    }
}

/** Gson can leave a missing measure null despite the type, so make it safe here. */
@Suppress("USELESS_CAST")
internal fun ApiDrink.toRecipe(): Drink? = toDrink()?.let { drink ->
    drink.copy(
        ingredients = drink.ingredients.map {
            IngredientMeasure((it.ingredient as String?).orEmpty(), (it.measure as String?).orEmpty())
        },
    )
}

internal fun RecipeDraft.toWrite() = RecipeWrite(
    name = name.trim(),
    alcoholic = alcoholic?.takeIf { it.isNotBlank() },
    glass = glass?.trim()?.takeIf { it.isNotEmpty() },
    category = category?.trim()?.takeIf { it.isNotEmpty() },
    instructions = instructions?.trim()?.takeIf { it.isNotEmpty() },
    ingredients = ingredients.map {
        RecipeIngredientWrite(it.ingredient.trim(), it.measure.trim().takeIf { m -> m.isNotEmpty() })
    },
)
