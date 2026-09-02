package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.Intent
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.data.BarDao
import com.capstone.nik.mixology.data.BarIngredientEntity
import com.capstone.nik.mixology.data.CatalogSeed
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkEntity
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.ShoppingDao
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    suspend fun fetchAndCache(filter: DrinkFilter): List<Drink> {
        val kind = filter.kind ?: return emptyList()
        val query = filter.query ?: return emptyList()
        ensureLocalCatalog()
        val entities = matchingRecipes(kind, query).filter { it.toDrink().hasUsableThumb() }
        dao.cacheFilterResults(filter.name, entities)
        return toDrinks(entities)
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

    suspend fun lookupDrink(id: String): Drink? = cachedDrink(id)

    suspend fun localRecipes(): List<Drink> {
        ensureLocalCatalog()
        return toDrinks(dao.getRecipes())
    }

    suspend fun randomDrink(): Drink? = localRecipes().randomOrNull()

    fun observeCatalog(kind: FilterKind): Flow<List<String>> =
        dao.observeCatalog(kind.name).map { terms -> terms.map { it.name } }

    suspend fun refreshCatalogs() {
        ensureLocalCatalog()
    }

    suspend fun search(query: String): List<Drink> {
        val needle = query.trim()
        if (needle.isEmpty()) return emptyList()
        ensureLocalCatalog()
        return toDrinks(
            dao.getRecipes()
                .filter { it.name.contains(needle, ignoreCase = true) }
                .sortedBy { it.name.lowercase() },
        )
    }

    suspend fun searchByIngredient(query: String): List<Drink> {
        val filter = DrinkFilter.dynamic(FilterKind.INGREDIENT, query)
        return fetchAndCache(filter)
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

    private suspend fun ensureLocalCatalog() {
        if (dao.getRecipes().isEmpty()) {
            CatalogSeed.importIfNeeded(context, dao)
        }
    }

    private suspend fun matchingRecipes(kind: FilterKind, query: String): List<DrinkEntity> {
        return dao.getRecipes().filter { entity ->
            when (kind) {
                FilterKind.ALCOHOL -> entity.alcoholic.matchesFilterValue(query)
                FilterKind.GLASS -> entity.glass.matchesFilterValue(query)
                FilterKind.DRINK_TYPE -> entity.category.matchesFilterValue(query)
                FilterKind.INGREDIENT -> entity.ingredients.orEmpty().any {
                    it.ingredient.matchesFilterValue(query)
                }
            }
        }
    }

    private suspend fun toDrinks(entities: List<DrinkEntity>): List<Drink> {
        val saved = dao.observeSavedIds().first().toSet()
        return entities.map { entity ->
            entity.toDrink(savedOverride = entity.id in saved || entity.saved)
        }
    }

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

private fun String?.matchesFilterValue(query: String): Boolean {
    val value = this?.trim().orEmpty()
    if (value.isEmpty()) return false
    if (value.equals(query, ignoreCase = true)) return true
    return value.replace('_', ' ').equals(query.replace('_', ' '), ignoreCase = true)
}
