package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.Intent
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkEntity
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

enum class FilterKind {
    ALCOHOL,
    GLASS,
    INGREDIENT,
    DRINK_TYPE,
}

class DrinkRepository(
    private val dao: DrinkDao,
    private val service: CocktailService,
    private val context: Context,
) {

    fun observeDrinks(filter: DrinkFilter): Flow<List<DrinkListItem>> {
        val drinksFlow = if (filter.showEmptySaved) {
            dao.observeSaved()
        } else {
            dao.observeByFilter(filter.name)
        }
        return combine(drinksFlow, dao.observeSavedIds()) { drinks, savedIds ->
            val saved = savedIds.toSet()
            drinks.map { DrinkListItem.from(it, saved) }
        }
    }

    @Throws(IOException::class)
    suspend fun fetchAndCache(filter: DrinkFilter) {
        val kind = filter.kind ?: return
        val query = filter.query ?: return
        val remoteDrinks = withContext(Dispatchers.IO) {
            val response = when (kind) {
                FilterKind.ALCOHOL -> service.getAlcoholFilter(query)
                FilterKind.GLASS -> service.getGlassFilter(query)
                FilterKind.INGREDIENT -> service.getIngredientFilter(query)
                FilterKind.DRINK_TYPE -> service.getDrinkTypeFilter(query)
            }.execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code()}")
            }
            response.body()?.drinks.orEmpty()
        }
        val entities = remoteDrinks.mapNotNull { drink ->
            val id = drink.idDrink ?: return@mapNotNull null
            if (!hasUsableThumb(drink)) return@mapNotNull null
            DrinkEntity(
                id = id,
                name = drink.strDrink.orEmpty(),
                thumb = drink.strDrinkThumb.orEmpty(),
            )
        }
        if (entities.isNotEmpty()) {
            dao.cacheFilterResults(filter.name, entities)
        }
    }

    fun observeSavedIds(): Flow<Set<String>> = dao.observeSavedIds().map { it.toSet() }

    @Throws(IOException::class)
    suspend fun lookupDrink(id: String): Drink? = withContext(Dispatchers.IO) {
        val response = service.getDrinkById(id).execute()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code()}")
        response.body()?.drinks?.firstOrNull()
    }

    @Throws(IOException::class)
    suspend fun randomDrink(): Drink? = withContext(Dispatchers.IO) {
        val response = service.getRandomixer().execute()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code()}")
        response.body()?.drinks?.firstOrNull()
    }

    @Throws(IOException::class)
    suspend fun search(query: String): List<Drink> = withContext(Dispatchers.IO) {
        val response = service.getSearchResults(query).execute()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code()}")
        response.body()?.drinks.orEmpty()
    }

    suspend fun save(cocktail: Cocktail) {
        dao.saveDrink(
            DrinkEntity(
                id = cocktail.getmDrinkId(),
                name = cocktail.getmDrinkName().orEmpty(),
                thumb = cocktail.getmDrinkThumb().orEmpty(),
                saved = true,
            ),
        )
        notifyWidgets()
    }

    suspend fun unsave(id: String) {
        dao.setSaved(id, false)
        notifyWidgets()
    }

    fun getSavedSync(): List<Cocktail> = dao.getSavedSync().map { it.toCocktail() }

    private fun notifyWidgets() {
        context.sendBroadcast(
            Intent(ACTION_DATABASE_UPDATED).setPackage(context.packageName),
        )
    }

    companion object {
        const val ACTION_DATABASE_UPDATED = "com.capstone.nik.mixology.action.DATABASE_UPDATED"

        fun hasUsableThumb(drink: Drink): Boolean {
            val thumb = drink.strDrinkThumb
            return !thumb.isNullOrEmpty() && thumb != "null"
        }
    }
}
