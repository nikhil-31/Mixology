package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BarDao {
    @Query("SELECT * FROM bar_ingredients ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<BarIngredientEntity>>

    @Query("SELECT * FROM bar_ingredients")
    suspend fun getAll(): List<BarIngredientEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: BarIngredientEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<BarIngredientEntity>)

    @Query("DELETE FROM bar_ingredients WHERE name = :name")
    suspend fun delete(name: String)

    @Query("DELETE FROM bar_ingredients")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(items: List<BarIngredientEntity>) {
        deleteAll()
        insertAll(items)
    }
}
