package com.capstone.nik.mixology.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.TimeZone

/** One ingredient bought on a shopping trip. The same shape is sent to and read from the server. */
data class ShoppingTripItem(
    val name: String = "",
    @SerializedName("drink_id") val drinkId: String? = null,
    @SerializedName("drink_name") val drinkName: String? = null,
    val measure: String? = null,
    @SerializedName("added_to_bar") val addedToBar: Boolean = false,
)

/** A finished shop: what was checked off and which ingredients went into My Bar. */
data class ShoppingTrip(
    /** Local row id. */
    val id: Long,
    /** The server's id; null until the trip has been sent. */
    val remoteId: Long?,
    /** Epoch milliseconds. */
    val at: Long,
    val items: List<ShoppingTripItem>,
) {
    val addedCount: Int get() = items.count { it.addedToBar }
}

@Entity(tableName = "shopping_trips")
data class ShoppingTripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: Long? = null,
    val at: Long,
    /** [ShoppingTripItem]s as JSON. */
    val items: String,
) {
    fun toTrip() = ShoppingTrip(id, remoteId, at, decodeTripItems(items))
}

private val gson = Gson()
private val itemListType = object : TypeToken<List<ShoppingTripItem>>() {}.type

fun encodeTripItems(items: List<ShoppingTripItem>): String = gson.toJson(items)

fun decodeTripItems(json: String): List<ShoppingTripItem> = try {
    gson.fromJson<List<ShoppingTripItem>>(json, itemListType).orEmpty()
} catch (error: JsonParseException) {
    emptyList()
}

/**
 * Reads the server's RFC 3339 times, such as `2026-11-01T14:03:00+00:00` or `…Z`, with or
 * without fractional seconds. Null when [text] isn't one.
 */
fun parseIsoMillis(text: String?): Long? {
    val match = ISO.matchEntire(text?.trim().orEmpty()) ?: return null
    val (year, month, day, hour, minute, second) = match.destructured
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(year.toInt(), month.toInt() - 1, day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
    }
    val fraction = match.groupValues[7].takeIf { it.isNotEmpty() }
        ?.drop(1)?.padEnd(3, '0')?.take(3)?.toLong() ?: 0L
    val zone = match.groupValues[8]
    val offsetMinutes = if (zone.isEmpty() || zone == "Z" || zone == "z") {
        0
    } else {
        val sign = if (zone.startsWith("-")) -1 else 1
        val digits = zone.drop(1).replace(":", "")
        sign * (digits.take(2).toInt() * 60 + digits.drop(2).take(2).toInt())
    }
    return calendar.timeInMillis + fraction - offsetMinutes * 60_000L
}

private val ISO = Regex(
    """(\d{4})-(\d{2})-(\d{2})[Tt ](\d{2}):(\d{2}):(\d{2})(\.\d+)?(Z|z|[+-]\d{2}:?\d{2})?""",
)

@Dao
interface ShoppingTripDao {
    @Query("SELECT * FROM shopping_trips ORDER BY at DESC, id DESC")
    fun observeEntities(): Flow<List<ShoppingTripEntity>>

    fun observeAll(): Flow<List<ShoppingTrip>> = observeEntities().map { rows -> rows.map { it.toTrip() } }

    @Query("SELECT * FROM shopping_trips ORDER BY at DESC, id DESC")
    suspend fun getAll(): List<ShoppingTripEntity>

    @Query("SELECT * FROM shopping_trips WHERE id = :id")
    suspend fun get(id: Long): ShoppingTripEntity?

    @Query("SELECT * FROM shopping_trips WHERE remoteId IS NULL ORDER BY at ASC, id ASC")
    suspend fun pending(): List<ShoppingTripEntity>

    @Insert
    suspend fun insert(trip: ShoppingTripEntity): Long

    @Insert
    suspend fun insertAll(trips: List<ShoppingTripEntity>)

    @Query("UPDATE shopping_trips SET remoteId = :remoteId WHERE id = :id")
    suspend fun setRemoteId(id: Long, remoteId: Long)

    @Query("UPDATE shopping_trips SET at = :at, items = :items WHERE id = :id")
    suspend fun update(id: Long, at: Long, items: String)

    @Query("DELETE FROM shopping_trips WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM shopping_trips WHERE remoteId IN (:remoteIds)")
    suspend fun deleteRemote(remoteIds: List<Long>)

    @Query("DELETE FROM shopping_trips")
    suspend fun deleteAll()

    /**
     * Makes the synced trips match [remote] (server id, time, items). Rows keep their local ids,
     * so a trip open on screen stays put; trips not yet sent are left alone.
     */
    @Transaction
    suspend fun applyRemote(remote: List<ShoppingTripEntity>) {
        val local = getAll().filter { it.remoteId != null }.associateBy { it.remoteId }
        val keep = remote.mapNotNull { it.remoteId }.toSet()
        deleteRemote(local.keys.filterNotNull().filter { it !in keep })
        val added = mutableListOf<ShoppingTripEntity>()
        remote.forEach { row ->
            val existing = local[row.remoteId]
            if (existing == null) added += row.copy(id = 0) else update(existing.id, row.at, row.items)
        }
        if (added.isNotEmpty()) insertAll(added)
    }
}
