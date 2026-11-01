package com.capstone.nik.mixology.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import com.capstone.nik.mixology.Network.CocktailService
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
    private val barSync: BarSync = BarSync.None,
) {

    private val remoteLists = MutableStateFlow<Map<String, List<Drink>>>(emptyMap())
    private val catalogTerms = MutableStateFlow<Map<FilterKind, List<String>>>(emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeDrinks(filter: DrinkFilter): Flow<List<Drink>> {
        val drinksFlow = if (filter.showEmptySaved) {
            savedSync.savedIds.flatMapLatest { ids ->
                if (ids.isEmpty()) flowOf(emptyList()) else dao.observeByIds(ids.toList())
            }.map { entities -> entities.map { it.toDrink(savedOverride = true) } }
        } else {
            remoteLists.map { lists -> lists[filter.name].orEmpty() }
        }
        return combine(drinksFlow, savedSync.savedIds, savedSync.notes) { drinks, saved, notes ->
            drinks.map { drink -> drink.copy(saved = drink.id in saved, notes = notes[drink.id].orEmpty()) }
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

    fun observeSavedIds(): Flow<Set<String>> = savedSync.savedIds

    fun observeRecentlyViewed(): Flow<List<Drink>> {
        return combine(
            dao.observeRecentlyViewed(MAX_RECENTLY_VIEWED),
            savedSync.savedIds,
            savedSync.notes,
        ) { drinks, saved, notes ->
            drinks.map { entity ->
                entity.toDrink(savedOverride = entity.id in saved).copy(notes = notes[entity.id].orEmpty())
            }
        }
    }

    suspend fun recordViewed(drink: Drink) {
        if (drink.isUserRecipe) return
        val viewedAt = nextViewedAt()
        dao.recordViewed(drink.toEntity(), viewedAt, MAX_RECENTLY_VIEWED)
    }

    suspend fun cachedDrink(id: String): Drink? =
        dao.getById(id)?.let { withSaved(it.toDrink()) }

    suspend fun lookupDrink(id: String): Drink? {
        val remote = service.getDrinkById(id).drinks.orEmpty().firstOrNull()?.toDrink() ?: return null
        return withSaved(remote)
    }

    /** The account's notes by drink id, as SavedSync holds them. */
    fun observeNotes(): Flow<Map<String, String>> = savedSync.notes

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
        dao.saveDrink(drink.toEntity())
        savedSync.push(drink.id, saved = true)
        recordViewed(drink)
        notifyWidgets()
        playReviews.onDrinkSaved()
    }

    suspend fun unsave(id: String) {
        savedSync.push(id, saved = false)
        notifyWidgets()
    }

    /** Brings in drinks saved on the account from other devices. */
    suspend fun syncSaved() {
        savedSync.sync()
        notifyWidgets()
    }

    /** Notes live only on the server; nothing is written to Room. */
    fun updateNotes(id: String, notes: String) {
        savedSync.pushNotes(id, notes)
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
        barSync.push(trimmed, owned = true)
    }

    suspend fun removeFromBar(name: String) {
        barDao.delete(name)
        barSync.push(name, owned = false)
    }

    /** Brings the account's bar down from the server; does nothing when signed out. */
    suspend fun syncBar() {
        barSync.pull()
    }

    /**
     * What My Bar can make. Signed in, this is the account's /bar response (the user's own
     * recipes first, then drinks ranked by taste); signed out, or until that first response
     * arrives, it is the public catalog preview for the local bar.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBarRecommendations(): Flow<BarRecommendations> {
        return barSync.recommendations
            .map { it != null }
            .distinctUntilChanged()
            .flatMapLatest { signedIn ->
                if (signedIn) {
                    combine(barSync.recommendations.filterNotNull(), observeSavedIds()) { recs, saved ->
                        recs.withSaved(saved)
                    }
                } else {
                    publicBarRecommendations()
                }
            }
    }

    private fun publicBarRecommendations(): Flow<BarRecommendations> {
        return combine(observeBar(), observeSavedIds()) { names, saved ->
            if (names.isEmpty()) {
                BarRecommendations()
            } else {
                try {
                    val response = service.getBarRecommendations(names)
                    barRecommendationsOf(response.makeable, response.almost, saved)
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

    /** Puts back items taken off the list (Undo). They sync as new rows. */
    suspend fun restoreShoppingItems(items: List<ShoppingItemEntity>) {
        if (items.isEmpty()) return
        shoppingDao.insertAll(items.map { it.copy(id = 0, remoteId = null) })
        shoppingSync.push()
    }

    /** For the widget, which may run before the app has pulled the account's list. */
    suspend fun getSavedSync(): List<Drink> {
        if (savedSync.savedIds.value.isEmpty()) savedSync.sync()
        val ids = savedSync.savedIds.value
        if (ids.isEmpty()) return emptyList()
        val notes = savedSync.notes.value
        return dao.getByIds(ids.toList()).map { it.toDrink(savedOverride = true).copy(notes = notes[it.id].orEmpty()) }
    }

    private fun drinksFrom(response: CocktailDbResponse): List<Drink> {
        return response.drinks.orEmpty()
            .filter { it.hasUsableThumb() }
            .mapNotNull { it.toDrink() }
    }

    private fun terms(response: CatalogListResponse): List<String> {
        return response.drinks.orEmpty().mapNotNull { it.term() }.distinct().sorted()
    }

    /** Applies the account's saved set and notes, which Room doesn't keep. */
    private fun withSaved(drinks: List<Drink>): List<Drink> {
        val saved = savedSync.savedIds.value
        val notes = savedSync.notes.value
        return drinks.map { drink -> drink.copy(saved = drink.id in saved, notes = notes[drink.id].orEmpty()) }
    }

    private fun withSaved(drink: Drink): Drink = withSaved(listOf(drink)).single()

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
