package com.capstone.nik.mixology.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Database(
    entities = [
        DrinkEntity::class,
        DrinkFilterCrossRef::class,
        CatalogTermEntity::class,
        ShoppingItemEntity::class,
        RecentlyViewedEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(IngredientListConverter::class)
abstract class MixologyDatabase : RoomDatabase() {

    abstract fun drinkDao(): DrinkDao

    abstract fun shoppingDao(): ShoppingDao

    companion object {
        private const val TAG = "MixologyDatabase"
        private const val DB_NAME = "mixology.db"
        private const val PREFS = "mixology"
        private const val PREF_LEGACY_IMPORTED = "legacy_saved_imported"

        internal val MIGRATION_1_2 = Migration(1, 2) { db ->
            db.execSQL("ALTER TABLE drinks ADD COLUMN alcoholic TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN glass TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN category TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN iba TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN instructions TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN video TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN ingredients TEXT")
            db.execSQL("ALTER TABLE drinks ADD COLUMN recipeUpdatedAt INTEGER NOT NULL DEFAULT 0")
        }

        internal val MIGRATION_2_3 = Migration(2, 3) { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS catalog_terms (
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(kind, name)
                )
                """.trimIndent(),
            )
        }

        internal val MIGRATION_3_4 = Migration(3, 4) { db ->
            db.execSQL("ALTER TABLE drinks ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS shopping_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    checked INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_shopping_items_name ON shopping_items(name)",
            )
        }

        internal val MIGRATION_4_5 = Migration(4, 5) { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS recently_viewed (
                    drinkId TEXT NOT NULL PRIMARY KEY,
                    viewedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }

        internal val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)

        @Volatile
        private var instance: MixologyDatabase? = null

        @JvmStatic
        fun create(context: Context): MixologyDatabase {
            val appContext = context.applicationContext
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(appContext, MixologyDatabase::class.java, DB_NAME)
                    .addMigrations(*ALL_MIGRATIONS)
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
