package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.ApiBarRecommendations
import com.capstone.nik.mixology.Network.remoteModel.CatalogListResponse
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.data.BarDao
import com.capstone.nik.mixology.data.BarIngredientEntity
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkDao
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.ShoppingDao
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.data.groupShoppingSections
import com.capstone.nik.mixology.data.toEntity
import com.capstone.nik.mixology.play.PlayReviewController
import com.capstone.nik.mixology.recordCrash
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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
    private val playReviews: PlayReviewController = PlayReviewController.disabled(),
    private val savedSync: SavedSync = SavedSync.None,
    private val shoppingSync: ShoppingSync = ShoppingSync.None,
) {

    private val remoteLists = MutableStateFlow<Map<String, List<Drink>>>(emptyMap())
    private val catalogTerms = MutableStateFlow<Map<FilterKind, List<String>>>(emptyMap())

    fun observeDrinks(filter: DrinkFilter): Flow<List<Drink>> {
        val drinksFlow = if (filter.showEmptySaved) {
            dao.observeSaved().map { entities -> entities.map { it.toDrink() } }
        } else {
            remoteLists.map { lists -> lists[filter.name].orEmpty() }
        }
        return combine(drinksFlow, dao.observeSavedIds()) { drinks, savedIds ->
            val saved = savedIds.toSet()
            drinks.map { drink -> drink.copy(saved = drink.id in saved) }
        }
    }

    suspend fun fetchAndCache(filter: DrinkFilter): List<Drink> {
        val kind = filter.kind ?: return emptyList()
        val query = filter.query ?: return emptyList()
        val response = when (kind) {
            FilterKind.ALCOHOL -> service.getAlcoholFilter(query)
            FilterKind.GLASS -> service.getGlassFilter(query)
            FilterKind.INGREDIENT -> service.getIngredientFilter(query)
            FilterKind.DRINK_TYPE -> service.getDrinkTypeFilter(query)
        }
        val drinks = drinksFrom(response)
        remoteLists.update { current -> current + (filter.name to drinks) }
        return withSaved(drinks)
    }

    fun observeSavedIds(): Flow<Set<String>> = dao.observeSavedIds().map { it.toSet() }

    fun observeRecentlyViewed(): Flow<List<Drink>> {
        return combine(dao.observeRecentlyViewed(MAX_RECENTLY_VIEWED), dao.observeSavedIds()) { drinks, savedIds ->
            val saved = savedIds.toSet()
            drinks.map { entity -> entity.toDrink(savedOverride = entity.id in saved || entity.saved) }
        }
    }

    suspend fun recordViewed(drink: Drink) {
        if (drink.isUserRecipe) return
        val viewedAt = nextViewedAt()
        dao.recordViewed(drink.toEntity(), viewedAt, MAX_RECENTLY_VIEWED)
    }

    suspend fun cachedDrink(id: String): Drink? = dao.getById(id)?.toDrink()

    suspend fun lookupDrink(id: String): Drink? {
        val remote = service.getDrinkById(id).drinks.orEmpty().firstOrNull()?.toDrink() ?: return null
        val stored = dao.getById(id)
        return remote.copy(
            saved = stored?.saved == true,
            notes = stored?.notes.orEmpty(),
        )
    }

    fun observeCatalog(kind: FilterKind): Flow<List<String>> =
        catalogTerms.map { terms -> terms[kind].orEmpty() }

    suspend fun refreshCatalogs() {
        catalogTerms.value = mapOf(
            FilterKind.DRINK_TYPE to terms(service.listCategories()),
            FilterKind.GLASS to terms(service.listGlasses()),
            FilterKind.INGREDIENT to terms(service.listIngredients()),
            FilterKind.ALCOHOL to terms(service.listAlcoholic()),
        )
    }

    suspend fun search(query: String): List<Drink> {
        val needle = query.trim()
        if (needle.isEmpty()) return emptyList()
        return withSaved(drinksFrom(service.getSearchResults(needle)))
    }

    suspend fun searchByIngredient(query: String): List<Drink> {
        val filter = DrinkFilter.dynamic(FilterKind.INGREDIENT, query)
        return fetchAndCache(filter)
    }

    suspend fun save(drink: Drink) {
        // The server refuses recipe ids; the user's recipes live under My recipes instead.
        if (drink.isUserRecipe) return
        dao.saveDrink(drink.toEntity().copy(saved = true))
        savedSync.push(drink.id, saved = true)
        recordViewed(drink)
        notifyWidgets()
        playReviews.onDrinkSaved()
    }

    suspend fun unsave(id: String) {
        dao.setSaved(id, false)
        savedSync.push(id, saved = false)
        notifyWidgets()
    }

    /** Brings in drinks saved on the account from other devices. */
    suspend fun syncSaved() {
        savedSync.sync()
        notifyWidgets()
    }

    suspend fun updateNotes(id: String, notes: String) {
        dao.updateNotes(id, notes)
    }

    fun observeShopping(): Flow<List<ShoppingItemEntity>> = shoppingDao.observeAll()

    fun observeShoppingSections(): Flow<List<ShoppingSection>> = flow {
        observeShopping().collect { items ->
            val drinks = items.map { it.drinkId }
                .filter { it.isNotEmpty() }
                .distinct()
                .mapNotNull { id -> dao.getById(id)?.toDrink() }
                .associateBy { it.id }
            emit(groupShoppingSections(items, drinks))
        }
    }

    fun observeBar(): Flow<List<String>> = barDao.observeAll().map { items -> items.map { it.name } }

    suspend fun addToBar(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        barDao.insert(BarIngredientEntity(trimmed))
    }

    suspend fun removeFromBar(name: String) {
        barDao.delete(name)
    }

    fun observeBarRecommendations(): Flow<BarRecommendations> {
        return combine(observeBar(), observeSavedIds()) { names, saved ->
            if (names.isEmpty()) {
                BarRecommendations()
            } else {
                try {
                    service.getBarRecommendations(names).toRecommendations(saved)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load bar recommendations", e)
                    recordCrash(e)
                    BarRecommendations()
                }
            }
        }
    }

    suspend fun addToShoppingList(drink: Drink) {
        val existing = shoppingDao.namesForDrink(drink.id).toSet()
        val seen = mutableSetOf<String>()
        drink.ingredients.forEach { measure ->
            val name = measure.ingredient.trim()
            val key = name.lowercase()
            if (name.isEmpty() || key in existing || !seen.add(key)) return@forEach
            shoppingDao.insert(ShoppingItemEntity(name = name, drinkId = drink.id))
        }
        shoppingSync.push()
    }

    /** Brings in the account's shopping list, after sending any local edits. */
    suspend fun syncShopping() {
        shoppingSync.sync()
    }

    suspend fun removeFromShoppingList(drinkId: String) {
        shoppingDao.deleteForDrink(drinkId)
        shoppingSync.push()
    }

    suspend fun setShoppingChecked(id: Long, checked: Boolean) {
        shoppingDao.setChecked(id, checked)
        shoppingSync.push()
    }

    suspend fun removeShoppingItem(id: Long) {
        shoppingDao.delete(id)
        shoppingSync.push()
    }

    suspend fun clearCheckedShoppingItems() {
        shoppingDao.deleteChecked()
        shoppingSync.push()
    }

    fun getSavedSync(): List<Drink> = dao.getSavedSync().map { it.toDrink(savedOverride = true) }

    private fun drinksFrom(response: CocktailDbResponse): List<Drink> {
        return response.drinks.orEmpty()
            .filter { it.hasUsableThumb() }
            .mapNotNull { it.toDrink() }
    }

    private fun terms(response: CatalogListResponse): List<String> {
        return response.drinks.orEmpty().mapNotNull { it.term() }.distinct().sorted()
    }

    private suspend fun withSaved(drinks: List<Drink>): List<Drink> {
        val saved = dao.observeSavedIds().first().toSet()
        return drinks.map { drink -> drink.copy(saved = drink.id in saved) }
    }

    private fun ApiBarRecommendations.toRecommendations(saved: Set<String>): BarRecommendations {
        return BarRecommendations(
            makeable = makeable.mapNotNull { drink -> drink.toDrink(saved = drink.id in saved) },
            almost = almost.mapNotNull { item ->
                val drink = item.drink.toDrink(saved = item.drink.id in saved) ?: return@mapNotNull null
                BarAlmostDrink(drink, item.missing)
            },
        )
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
        private const val TAG = "DrinkRepository"
        const val ACTION_DATABASE_UPDATED = "com.capstone.nik.mixology.action.DATABASE_UPDATED"
        const val MAX_RECENTLY_VIEWED = 30

        fun hasUsableThumb(drink: CocktailDbDrink): Boolean = drink.hasUsableThumb()
    }
}
