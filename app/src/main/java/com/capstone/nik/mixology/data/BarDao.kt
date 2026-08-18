package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BarDao {
    @Query("SELECT * FROM bar_ingredients ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<BarIngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: BarIngredientEntity)

    @Query("DELETE FROM bar_ingredients WHERE name = :name")
    suspend fun delete(name: String)
}
