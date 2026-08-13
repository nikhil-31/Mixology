package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT COUNT(*) FROM drinks WHERE id = :id AND saved = 1")
    fun savedCountSync(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemberships(memberships: List<DrinkFilterCrossRef>)

    @Query("UPDATE drinks SET saved = :saved WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean)

    @Transaction
    suspend fun cacheFilterResults(filterName: String, drinks: List<DrinkEntity>) {
        insertDrinks(drinks)
        insertMemberships(drinks.map { DrinkFilterCrossRef(it.id, filterName) })
    }

    @Transaction
    suspend fun saveDrink(drink: DrinkEntity) {
        insertDrinks(listOf(drink.copy(saved = true)))
        setSaved(drink.id, true)
    }
}
