package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY checked ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT LOWER(name) FROM shopping_items")
    suspend fun namesLowercase(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun deleteChecked()
}
