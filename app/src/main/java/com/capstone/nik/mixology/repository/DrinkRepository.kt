package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.Intent
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class FilterKind {
    ALCOHOL,
    GLASS,
    INGREDIENT,
    DRINK_TYPE,
}

@Singleton
class DrinkRepository @Inject constructor(
    private val dao: DrinkDao,
    private val service: CocktailService,
    @ApplicationContext private val context: Context,
) {

    fun observeDrinks(filter: DrinkFilter): Flow<List<Drink>> {
        val drinksFlow = if (filter.showEmptySaved) {
            dao.observeSaved()
        } else {
            dao.observeByFilter(filter.name)
        }
        return combine(drinksFlow, dao.observeSavedIds()) { drinks, savedIds ->
            val saved = savedIds.toSet()
            drinks.map { entity -> entity.toDrink(savedOverride = entity.id in saved || entity.saved) }
        }
    }

    @Throws(IOException::class)
    suspend fun fetchAndCache(filter: DrinkFilter) {
        val kind = filter.kind ?: return
        val query = filter.query ?: return
        val remoteDrinks = when (kind) {
            FilterKind.ALCOHOL -> service.getAlcoholFilter(query)
            FilterKind.GLASS -> service.getGlassFilter(query)
            FilterKind.INGREDIENT -> service.getIngredientFilter(query)
            FilterKind.DRINK_TYPE -> service.getDrinkTypeFilter(query)
        }.drinks.orEmpty()
        val entities = remoteDrinks.mapNotNull { drink ->
            if (!drink.hasUsableThumb()) return@mapNotNull null
            drink.toDrink()?.toEntity()
        }
        if (entities.isNotEmpty()) {
            dao.cacheFilterResults(filter.name, entities)
        }
    }

    fun observeSavedIds(): Flow<Set<String>> = dao.observeSavedIds().map { it.toSet() }

    suspend fun cachedDrink(id: String): Drink? = dao.getById(id)?.toDrink()

    @Throws(IOException::class)
    suspend fun lookupDrink(id: String): Drink? {
        val remote = try {
            service.getDrinkById(id).drinks?.firstOrNull()?.toDrink()
        } catch (e: Exception) {
            return dao.getById(id)?.toDrink()?.takeIf { it.hasRecipe }
                ?: throw IOException("HTTP lookup failed", e)
        }
        if (remote != null) {
            dao.upsertRecipe(remote.toEntity())
        }
        return dao.getById(id)?.toDrink() ?: remote
    }

    @Throws(IOException::class)
    suspend fun randomDrink(): Drink? {
        val remote = service.getRandomixer().drinks?.firstOrNull()?.toDrink() ?: return null
        if (remote.hasRecipe) {
            dao.upsertRecipe(remote.toEntity())
        }
        return dao.getById(remote.id)?.toDrink() ?: remote
    }

    @Throws(IOException::class)
    suspend fun search(query: String): List<Drink> {
        val drinks = service.getSearchResults(query).drinks.orEmpty().mapNotNull { it.toDrink() }
        drinks.filter { it.hasRecipe }.forEach { dao.upsertRecipe(it.toEntity()) }
        return drinks
    }

    suspend fun save(drink: Drink) {
        dao.saveDrink(drink.toEntity().copy(saved = true))
        notifyWidgets()
    }

    suspend fun unsave(id: String) {
        dao.setSaved(id, false)
        notifyWidgets()
    }

    fun getSavedSync(): List<Drink> = dao.getSavedSync().map { it.toDrink(savedOverride = true) }

    private fun notifyWidgets() {
        context.sendBroadcast(
            Intent(ACTION_DATABASE_UPDATED).setPackage(context.packageName),
        )
    }

    companion object {
        const val ACTION_DATABASE_UPDATED = "com.capstone.nik.mixology.action.DATABASE_UPDATED"

        fun hasUsableThumb(drink: CocktailDbDrink): Boolean = drink.hasUsableThumb()
    }
}
