package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

/**
 * The device's copy of the account's note on a drink, so notes show offline and
 * survive a restart. The server owns them; [com.capstone.nik.mixology.repository.SavedSync]
 * keeps this table in step. Blank notes are not stored.
 */
@Entity(tableName = "drink_notes")
data class DrinkNoteEntity(
    /** A catalog drink id or a user recipe id, which need not be in `drinks`. */
    @PrimaryKey val drinkId: String,
    val notes: String,
)

@Dao
interface DrinkNoteDao {
    @Query("SELECT * FROM drink_notes")
    suspend fun getAll(): List<DrinkNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: DrinkNoteEntity)

    @Query("DELETE FROM drink_notes WHERE drinkId = :drinkId")
    suspend fun delete(drinkId: String)

    @Query("DELETE FROM drink_notes")
    suspend fun clear()

    /** Stores [notes] for the drink; a blank note removes the row. */
    suspend fun set(drinkId: String, notes: String) {
        if (notes.isBlank()) delete(drinkId) else upsert(DrinkNoteEntity(drinkId, notes))
    }

    /** Makes the table match [notes], the account's notes by drink id. */
    @Transaction
    suspend fun replaceAll(notes: Map<String, String>) {
        clear()
        notes.forEach { (drinkId, text) -> if (text.isNotBlank()) upsert(DrinkNoteEntity(drinkId, text)) }
    }
}
