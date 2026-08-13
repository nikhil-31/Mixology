package com.capstone.nik.mixology.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Database(
    entities = [DrinkEntity::class, DrinkFilterCrossRef::class],
    version = 1,
    exportSchema = false,
)
abstract class MixologyDatabase : RoomDatabase() {

    abstract fun drinkDao(): DrinkDao

    companion object {
        private const val TAG = "MixologyDatabase"
        private const val DB_NAME = "mixology.db"
        private const val PREFS = "mixology"
        private const val PREF_LEGACY_IMPORTED = "legacy_saved_imported"

        @Volatile
        private var instance: MixologyDatabase? = null

        @JvmStatic
        fun create(context: Context): MixologyDatabase {
            val appContext = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(appContext, MixologyDatabase::class.java, DB_NAME)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Executors.newSingleThreadExecutor().execute {
                                instance?.let { importLegacySavedDrinks(appContext, it.drinkDao()) }
                            }
                        }
                    })
                    .build()
                    .also { instance = it }
            }
        }

        private fun importLegacySavedDrinks(context: Context, dao: DrinkDao) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (prefs.getBoolean(PREF_LEGACY_IMPORTED, false)) return

            val paths = listOf("DrinkDatabase", "DrinkDatabase.db")
                .map { context.getDatabasePath(it) }
                .filter { it.exists() }
            if (paths.isEmpty()) {
                prefs.edit().putBoolean(PREF_LEGACY_IMPORTED, true).apply()
                return
            }

            paths.forEach { file ->
                try {
                    SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { sqlite ->
                        sqlite.rawQuery("SELECT _id, name, thumb FROM Drink_Saved", null)?.use { cursor ->
                            val idIndex = cursor.getColumnIndex("_id")
                            val nameIndex = cursor.getColumnIndex("name")
                            val thumbIndex = cursor.getColumnIndex("thumb")
                            if (idIndex < 0 || nameIndex < 0 || thumbIndex < 0) return@use
                            val drinks = ArrayList<DrinkEntity>()
                            while (cursor.moveToNext()) {
                                val id = cursor.getString(idIndex) ?: continue
                                drinks.add(
                                    DrinkEntity(
                                        id = id,
                                        name = cursor.getString(nameIndex).orEmpty(),
                                        thumb = cursor.getString(thumbIndex).orEmpty(),
                                        saved = true,
                                    ),
                                )
                            }
                            if (drinks.isNotEmpty()) {
                                runBlocking {
                                    dao.insertDrinks(drinks)
                                    drinks.forEach { dao.setSaved(it.id, true) }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not import saved drinks from ${file.name}", e)
                }
            }
            prefs.edit().putBoolean(PREF_LEGACY_IMPORTED, true).apply()
        }
    }
}
