package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.Intent
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.data.BarDao
import com.capstone.nik.mixology.data.BarIngredientEntity
import com.capstone.nik.mixology.data.CatalogSeed
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.ShoppingDao
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val shoppingDao: ShoppingDao,
    private val barDao: BarDao,
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
    suspend fun fetchAndCache(filter: DrinkFilter): List<Drink> {
        val kind = filter.kind ?: return emptyList()
        val query = filter.query ?: return emptyList()
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
        dao.cacheFilterResults(filter.name, entities)
        return entities.map { it.toDrink() }
    }

    fun observeSavedIds(): Flow<Set<String>> = dao.observeSavedIds().map { it.toSet() }

    fun observeRecentlyViewed(): Flow<List<Drink>> {
        return combine(dao.observeRecentlyViewed(MAX_RECENTLY_VIEWED), dao.observeSavedIds()) { drinks, savedIds ->
            val saved = savedIds.toSet()
            drinks.map { entity -> entity.toDrink(savedOverride = entity.id in saved || entity.saved) }
        }
    }

    suspend fun recordViewed(drink: Drink) {
        val viewedAt = nextViewedAt()
        dao.recordViewed(drink.toEntity(), viewedAt, MAX_RECENTLY_VIEWED)
    }

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

    suspend fun localRecipes(): List<Drink> {
        var recipes = dao.getRecipes()
        if (recipes.isEmpty()) {
            CatalogSeed.importIfNeeded(context, dao)
            recipes = dao.getRecipes()
        }
        val saved = dao.observeSavedIds().first().toSet()
        return recipes.map { entity ->
            entity.toDrink(savedOverride = entity.id in saved || entity.saved)
        }
    }

    suspend fun randomDrink(): Drink? = localRecipes().randomOrNull()

    fun observeCatalog(kind: FilterKind): Flow<List<String>> =
        dao.observeCatalog(kind.name).map { terms -> terms.map { it.name } }

    suspend fun refreshCatalogs() {
        FilterKind.entries.forEach { kind ->
            val names = when (kind) {
                FilterKind.ALCOHOL -> service.listAlcoholic()
                FilterKind.GLASS -> service.listGlasses()
                FilterKind.INGREDIENT -> service.listIngredients()
                FilterKind.DRINK_TYPE -> service.listCategories()
            }.drinks.orEmpty().mapNotNull { it.term() }
            if (names.isNotEmpty()) {
                dao.replaceCatalog(kind.name, names)
            }
        }
    }

    @Throws(IOException::class)
    suspend fun search(query: String): List<Drink> {
        val drinks = service.getSearchResults(query).drinks.orEmpty().mapNotNull { it.toDrink() }
        drinks.filter { it.hasRecipe }.forEach { dao.upsertRecipe(it.toEntity()) }
        return drinks
    }

    @Throws(IOException::class)
    suspend fun searchByIngredient(query: String): List<Drink> {
        val filter = DrinkFilter.dynamic(FilterKind.INGREDIENT, query)
        val remoteDrinks = service.getIngredientFilter(query).drinks.orEmpty()
        val entities = remoteDrinks.mapNotNull { drink ->
            if (!drink.hasUsableThumb()) return@mapNotNull null
            drink.toDrink()?.toEntity()
        }
        if (entities.isNotEmpty()) {
            dao.cacheFilterResults(filter.name, entities)
        }
        return entities.map { it.toDrink() }
    }

    suspend fun save(drink: Drink) {
        dao.saveDrink(drink.toEntity().copy(saved = true))
        recordViewed(drink)
        notifyWidgets()
    }

    suspend fun unsave(id: String) {
        dao.setSaved(id, false)
        notifyWidgets()
    }

    suspend fun updateNotes(id: String, notes: String) {
        dao.updateNotes(id, notes)
    }

    fun observeShopping(): Flow<List<ShoppingItemEntity>> = shoppingDao.observeAll()

    fun observeBar(): Flow<List<String>> = barDao.observeAll().map { items -> items.map { it.name } }

    suspend fun addToBar(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        barDao.insert(BarIngredientEntity(trimmed))
    }

    suspend fun removeFromBar(name: String) {
        barDao.delete(name)
    }

    fun observeRecipes(): Flow<List<Drink>> {
        return combine(dao.observeRecipes(), dao.observeSavedIds()) { drinks, savedIds ->
            val saved = savedIds.toSet()
            drinks.map { entity -> entity.toDrink(savedOverride = entity.id in saved || entity.saved) }
        }
    }

    fun observeBarRecommendations(): Flow<BarRecommendations> {
        return combine(observeBar(), observeRecipes()) { bar, drinks ->
            BarMatcher.recommend(drinks, bar)
        }
    }

    suspend fun addToShoppingList(names: List<String>) {
        val existing = shoppingDao.namesLowercase().toSet()
        names.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .filter { it.lowercase() !in existing }
            .forEach { shoppingDao.insert(ShoppingItemEntity(name = it)) }
    }

    suspend fun setShoppingChecked(id: Long, checked: Boolean) {
        shoppingDao.setChecked(id, checked)
    }

    suspend fun removeShoppingItem(id: Long) {
        shoppingDao.delete(id)
    }

    suspend fun clearCheckedShoppingItems() {
        shoppingDao.deleteChecked()
    }

    fun getSavedSync(): List<Drink> = dao.getSavedSync().map { it.toDrink(savedOverride = true) }

    private suspend fun nextViewedAt(): Long {
        val now = System.currentTimeMillis()
        val latest = dao.latestViewedAt() ?: 0L
        return maxOf(now, latest + 1)
    }

    private fun notifyWidgets() {
        context.sendBroadcast(
            Intent(ACTION_DATABASE_UPDATED).setPackage(context.packageName),
        )
    }

    companion object {
        const val ACTION_DATABASE_UPDATED = "com.capstone.nik.mixology.action.DATABASE_UPDATED"
        const val MAX_RECENTLY_VIEWED = 30

        fun hasUsableThumb(drink: CocktailDbDrink): Boolean = drink.hasUsableThumb()
    }
}
