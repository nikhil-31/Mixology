package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY checked ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT LOWER(name) FROM shopping_items WHERE drinkId = :drinkId")
    suspend fun namesForDrink(drinkId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ShoppingItemEntity)

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: Long, checked: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun deleteChecked()

    @Query("DELETE FROM shopping_items WHERE drinkId = :drinkId")
    suspend fun deleteForDrink(drinkId: String)

    @Query("SELECT * FROM shopping_items ORDER BY id ASC")
    suspend fun getAll(): List<ShoppingItemEntity>

    @Query("UPDATE shopping_items SET remoteId = :remoteId WHERE id = :id AND remoteId IS NULL")
    suspend fun setRemoteId(id: Long, remoteId: Long)

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItemEntity>)

    @Transaction
    suspend fun replaceAll(items: List<ShoppingItemEntity>) {
        deleteAll()
        insertAll(items)
    }

    /** Swaps in [items] only if the table still holds [expected]; false when a local edit got there first. */
    @Transaction
    suspend fun replaceAll(expected: List<ShoppingItemEntity>, items: List<ShoppingItemEntity>): Boolean {
        if (getAll() != expected) return false
        replaceAll(items)
        return true
    }
}
