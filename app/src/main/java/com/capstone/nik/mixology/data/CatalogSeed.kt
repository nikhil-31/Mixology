package com.capstone.nik.mixology.data

import android.content.Context
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.google.gson.Gson

data class CatalogSeedPayload(
    val version: Int = 1,
    val drinks: List<CatalogSeedDrink> = emptyList(),
    val ingredients: List<String> = emptyList(),
)

data class CatalogSeedDrink(
    val id: String = "",
    val name: String = "",
    val thumb: String = "",
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val iba: String? = null,
    val instructions: String? = null,
    val video: String? = null,
    val ingredients: List<IngredientMeasure> = emptyList(),
)

object CatalogSeed {
    const val VERSION = 1
    const val ASSET = "catalog/cocktails.json"
    const val PREF_VERSION = "catalog_seed_version"

    private val gson = Gson()

    suspend fun importIfNeeded(context: Context, dao: DrinkDao) {
        val prefs = context.getSharedPreferences("mixology", Context.MODE_PRIVATE)
        if (prefs.getInt(PREF_VERSION, 0) >= VERSION) return
        val payload = readAsset(context) ?: return
        importPayload(payload, dao)
        prefs.edit().putInt(PREF_VERSION, VERSION).apply()
    }

    fun parse(json: String): CatalogSeedPayload? =
        runCatching { gson.fromJson(json, CatalogSeedPayload::class.java) }.getOrNull()

    suspend fun importPayload(payload: CatalogSeedPayload, dao: DrinkDao) {
        val now = System.currentTimeMillis()
        payload.drinks.forEach { drink ->
            val id = drink.id.trim()
            val name = drink.name.trim()
            if (id.isEmpty() || name.isEmpty()) return@forEach
            dao.upsertRecipe(
                DrinkEntity(
                    id = id,
                    name = name,
                    thumb = drink.thumb,
                    alcoholic = drink.alcoholic,
                    glass = drink.glass,
                    category = drink.category,
                    iba = drink.iba,
                    instructions = drink.instructions,
                    video = drink.video,
                    ingredients = drink.ingredients,
                    recipeUpdatedAt = now,
                ),
            )
        }
        val ingredientNames = payload.ingredients
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty {
                payload.drinks.flatMap { drink -> drink.ingredients.map { it.ingredient } }
            }
            .distinct()
        val terms = buildList {
            addAll(ingredientNames.map { CatalogTermEntity(FilterKind.INGREDIENT.name, it) })
            payload.drinks.forEach { drink ->
                term(drink.category, FilterKind.DRINK_TYPE)?.let(::add)
                term(drink.glass, FilterKind.GLASS)?.let(::add)
                term(drink.alcoholic, FilterKind.ALCOHOL)?.let(::add)
            }
        }.distinctBy { it.kind to it.name }
        if (terms.isNotEmpty()) {
            dao.insertCatalogTerms(terms)
        }
    }

    private fun term(name: String?, kind: FilterKind): CatalogTermEntity? {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return CatalogTermEntity(kind.name, trimmed)
    }

    private fun readAsset(context: Context): CatalogSeedPayload? {
        return runCatching {
            context.assets.open(ASSET).bufferedReader().use { parse(it.readText()) }
        }.getOrNull()
    }
}
