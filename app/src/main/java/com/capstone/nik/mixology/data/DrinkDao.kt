package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {

    @Query(
        """
        SELECT drinks.* FROM drinks
        INNER JOIN drink_filter ON drinks.id = drink_filter.drinkId
        WHERE drink_filter.filterName = :filterName
        ORDER BY drinks.name
        """,
    )
    fun observeByFilter(filterName: String): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drinks WHERE saved = 1 ORDER BY name")
    fun observeSaved(): Flow<List<DrinkEntity>>

    @Query("SELECT id FROM drinks WHERE saved = 1")
    fun observeSavedIds(): Flow<List<String>>

    @Query("SELECT * FROM drinks WHERE saved = 1 ORDER BY name")
    fun getSavedSync(): List<DrinkEntity>

    @Query("SELECT * FROM drinks WHERE id = :id")
    suspend fun getById(id: String): DrinkEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemberships(memberships: List<DrinkFilterCrossRef>)

    @Query("UPDATE drinks SET saved = :saved WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean)

    @Query("UPDATE drinks SET name = :name, thumb = :thumb WHERE id = :id")
    suspend fun updateIdentity(id: String, name: String, thumb: String)

    @Query(
        """
        UPDATE drinks SET
            name = :name,
            thumb = :thumb,
            alcoholic = :alcoholic,
            glass = :glass,
            category = :category,
            iba = :iba,
            instructions = :instructions,
            video = :video,
            ingredients = :ingredients,
            recipeUpdatedAt = :recipeUpdatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateRecipe(
        id: String,
        name: String,
        thumb: String,
        alcoholic: String?,
        glass: String?,
        category: String?,
        iba: String?,
        instructions: String?,
        video: String?,
        ingredients: List<IngredientMeasure>,
        recipeUpdatedAt: Long,
    )

    @Transaction
    suspend fun cacheFilterResults(filterName: String, drinks: List<DrinkEntity>) {
        drinks.forEach { drink ->
            val existing = getById(drink.id)
            if (existing == null) {
                insertDrinks(listOf(drink))
            } else {
                updateIdentity(drink.id, drink.name, drink.thumb)
            }
        }
        insertMemberships(drinks.map { DrinkFilterCrossRef(it.id, filterName) })
    }

    @Transaction
    suspend fun upsertRecipe(drink: DrinkEntity) {
        val existing = getById(drink.id)
        if (existing == null) {
            insertDrinks(listOf(drink))
        } else {
            updateRecipe(
                id = drink.id,
                name = drink.name.ifBlank { existing.name },
                thumb = drink.thumb.ifBlank { existing.thumb },
                alcoholic = drink.alcoholic,
                glass = drink.glass,
                category = drink.category,
                iba = drink.iba,
                instructions = drink.instructions,
                video = drink.video,
                ingredients = drink.ingredients.orEmpty(),
                recipeUpdatedAt = drink.recipeUpdatedAt,
            )
        }
    }

    @Transaction
    suspend fun saveDrink(drink: DrinkEntity) {
        val existing = getById(drink.id)
        if (existing == null) {
            insertDrinks(listOf(drink.copy(saved = true)))
        } else {
            updateIdentity(
                drink.id,
                drink.name.ifBlank { existing.name },
                drink.thumb.ifBlank { existing.thumb },
            )
            if (drink.instructions != null || !drink.ingredients.isNullOrEmpty()) {
                updateRecipe(
                    id = drink.id,
                    name = drink.name.ifBlank { existing.name },
                    thumb = drink.thumb.ifBlank { existing.thumb },
                    alcoholic = drink.alcoholic ?: existing.alcoholic,
                    glass = drink.glass ?: existing.glass,
                    category = drink.category ?: existing.category,
                    iba = drink.iba ?: existing.iba,
                    instructions = drink.instructions ?: existing.instructions,
                    video = drink.video ?: existing.video,
                    ingredients = drink.ingredients.takeUnless { it.isNullOrEmpty() }
                        ?: existing.ingredients.orEmpty(),
                    recipeUpdatedAt = drink.recipeUpdatedAt.takeIf { it > 0 } ?: existing.recipeUpdatedAt,
                )
            }
        }
        setSaved(drink.id, true)
    }

    @Query("UPDATE drinks SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String)

    @Query("SELECT * FROM catalog_terms WHERE kind = :kind ORDER BY name")
    fun observeCatalog(kind: String): Flow<List<CatalogTermEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogTerms(terms: List<CatalogTermEntity>)

    @Query("DELETE FROM catalog_terms WHERE kind = :kind")
    suspend fun deleteCatalog(kind: String)

    @Transaction
    suspend fun replaceCatalog(kind: String, names: List<String>) {
        deleteCatalog(kind)
        if (names.isNotEmpty()) {
            insertCatalogTerms(names.map { CatalogTermEntity(kind, it) })
        }
    }
}
