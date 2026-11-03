package com.capstone.nik.mixology.data

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class MixologyDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    /**
     * Creates the database as version [version] from the schema Room exported to
     * app/schemas, so the starting point is exactly what shipped.
     */
    private fun createFromExportedSchema(dbName: String, version: Int): SQLiteDatabase {
        val schemaDir = listOf(File("schemas"), File("app/schemas"))
            .map { File(it, "com.capstone.nik.mixology.data.MixologyDatabase") }
            .first { it.isDirectory }
        val schema = JSONObject(File(schemaDir, "$version.json").readText()).getJSONObject("database")
        context.deleteDatabase(dbName)
        val path = context.getDatabasePath(dbName)
        path.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(path, null)
        val entities = schema.getJSONArray("entities")
        for (i in 0 until entities.length()) {
            val entity = entities.getJSONObject(i)
            val table = entity.getString("tableName")
            db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
            val indices = entity.optJSONArray("indices") ?: continue
            for (j in 0 until indices.length()) {
                db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
            }
        }
        db.execSQL("PRAGMA user_version = $version")
        return db
    }

    /** Opens the database with every migration; Room checks the result against the entities and throws if it's off. */
    private fun openMigrated(dbName: String): MixologyDatabase {
        val database = Room.databaseBuilder(context, MixologyDatabase::class.java, dbName)
            .addMigrations(*MixologyDatabase.ALL_MIGRATIONS)
            .build()
        database.openHelper.writableDatabase
        return database
    }

    @Test
    fun migrate9To10_addsDrinkNotes_andKeepsEverythingElse() {
        val dbName = "migration-9-10.db"
        createFromExportedSchema(dbName, 9).use { db ->
            db.execSQL("INSERT INTO drinks (id, name, thumb, saved, notes, recipeUpdatedAt) VALUES ('11007', 'Margarita', '', 1, '', 0)")
            db.execSQL("INSERT INTO shopping_trips (remoteId, at, items) VALUES (7, 1000, '[]')")
        }

        val database = openMigrated(dbName)
        val db = database.openHelper.writableDatabase
        db.query("SELECT COUNT(*) FROM drink_notes").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT saved FROM drinks WHERE id = '11007'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT remoteId FROM shopping_trips").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(7L, cursor.getLong(0))
        }
        database.close()
    }

    @Test
    fun everyMigrationFrom4_producesTheCurrentSchema() {
        val dbName = "migration-4-10.db"
        createFromExportedSchema(dbName, 4).close()
        openMigrated(dbName).close()
    }

    @Test
    fun migrate3To4_addsNotesAndShoppingTable() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dbName = "migration-3-4.db"
        context.deleteDatabase(dbName)
        val path = context.getDatabasePath(dbName)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { sqlite ->
            sqlite.execSQL(
                """
                CREATE TABLE drinks (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    thumb TEXT NOT NULL,
                    saved INTEGER NOT NULL,
                    alcoholic TEXT,
                    glass TEXT,
                    category TEXT,
                    iba TEXT,
                    instructions TEXT,
                    video TEXT,
                    ingredients TEXT,
                    recipeUpdatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE drink_filter (
                    drinkId TEXT NOT NULL,
                    filterName TEXT NOT NULL,
                    PRIMARY KEY(drinkId, filterName)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE catalog_terms (
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(kind, name)
                )
                """.trimIndent(),
            )
            sqlite.execSQL("PRAGMA user_version = 3")
        }

        val database = Room.databaseBuilder(context, MixologyDatabase::class.java, dbName)
            .addMigrations(*MixologyDatabase.ALL_MIGRATIONS)
            .build()
        database.openHelper.writableDatabase.query("SELECT notes FROM drinks").use { cursor ->
            assertTrue(cursor.columnCount >= 1)
        }
        database.openHelper.writableDatabase.query("SELECT id, name, checked FROM shopping_items").close()
        database.close()
    }

    @Test
    fun migrate4To5_addsRecentlyViewedTable() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dbName = "migration-4-5.db"
        context.deleteDatabase(dbName)
        val path = context.getDatabasePath(dbName)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { sqlite ->
            sqlite.execSQL(
                """
                CREATE TABLE drinks (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    thumb TEXT NOT NULL,
                    saved INTEGER NOT NULL,
                    alcoholic TEXT,
                    glass TEXT,
                    category TEXT,
                    iba TEXT,
                    instructions TEXT,
                    video TEXT,
                    ingredients TEXT,
                    notes TEXT NOT NULL DEFAULT '',
                    recipeUpdatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE drink_filter (
                    drinkId TEXT NOT NULL,
                    filterName TEXT NOT NULL,
                    PRIMARY KEY(drinkId, filterName)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE catalog_terms (
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(kind, name)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE shopping_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    checked INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            sqlite.execSQL("CREATE UNIQUE INDEX index_shopping_items_name ON shopping_items(name)")
            sqlite.execSQL("PRAGMA user_version = 4")
        }

        val database = Room.databaseBuilder(context, MixologyDatabase::class.java, dbName)
            .addMigrations(*MixologyDatabase.ALL_MIGRATIONS)
            .build()
        database.openHelper.writableDatabase.query("SELECT drinkId, viewedAt FROM recently_viewed").close()
        database.close()
    }

    @Test
    fun migrate5To6_addsBarIngredientsTable() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dbName = "migration-5-6.db"
        context.deleteDatabase(dbName)
        val path = context.getDatabasePath(dbName)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { sqlite ->
            sqlite.execSQL(
                """
                CREATE TABLE drinks (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    thumb TEXT NOT NULL,
                    saved INTEGER NOT NULL,
                    alcoholic TEXT,
                    glass TEXT,
                    category TEXT,
                    iba TEXT,
                    instructions TEXT,
                    video TEXT,
                    ingredients TEXT,
                    notes TEXT NOT NULL DEFAULT '',
                    recipeUpdatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE drink_filter (
                    drinkId TEXT NOT NULL,
                    filterName TEXT NOT NULL,
                    PRIMARY KEY(drinkId, filterName)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE catalog_terms (
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(kind, name)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE shopping_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    checked INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            sqlite.execSQL("CREATE UNIQUE INDEX index_shopping_items_name ON shopping_items(name)")
            sqlite.execSQL(
                """
                CREATE TABLE recently_viewed (
                    drinkId TEXT NOT NULL PRIMARY KEY,
                    viewedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            sqlite.execSQL("PRAGMA user_version = 5")
        }

        val database = Room.databaseBuilder(context, MixologyDatabase::class.java, dbName)
            .addMigrations(*MixologyDatabase.ALL_MIGRATIONS)
            .build()
        database.openHelper.writableDatabase.query("SELECT name FROM bar_ingredients").close()
        database.openHelper.writableDatabase.query("SELECT drinkId FROM shopping_items").close()
        database.close()
    }

    @Test
    fun migrate6To7_keepsExistingShoppingRowsWithoutADrink() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dbName = "migration-6-7.db"
        context.deleteDatabase(dbName)
        val path = context.getDatabasePath(dbName)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { sqlite ->
            sqlite.execSQL(
                """
                CREATE TABLE drinks (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    thumb TEXT NOT NULL,
                    saved INTEGER NOT NULL,
                    alcoholic TEXT,
                    glass TEXT,
                    category TEXT,
                    iba TEXT,
                    instructions TEXT,
                    video TEXT,
                    ingredients TEXT,
                    notes TEXT NOT NULL DEFAULT '',
                    recipeUpdatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE drink_filter (
                    drinkId TEXT NOT NULL,
                    filterName TEXT NOT NULL,
                    PRIMARY KEY(drinkId, filterName)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE catalog_terms (
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(kind, name)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE shopping_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    checked INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            sqlite.execSQL("CREATE UNIQUE INDEX index_shopping_items_name ON shopping_items(name)")
            sqlite.execSQL("INSERT INTO shopping_items (name, checked) VALUES ('Lime', 0)")
            sqlite.execSQL(
                """
                CREATE TABLE recently_viewed (
                    drinkId TEXT NOT NULL PRIMARY KEY,
                    viewedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            sqlite.execSQL("CREATE TABLE bar_ingredients (name TEXT NOT NULL PRIMARY KEY)")
            sqlite.execSQL("PRAGMA user_version = 6")
        }

        val database = Room.databaseBuilder(context, MixologyDatabase::class.java, dbName)
            .addMigrations(*MixologyDatabase.ALL_MIGRATIONS)
            .build()
        database.openHelper.writableDatabase.query(
            "SELECT name, drinkId FROM shopping_items",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.getString(0) == "Lime")
            assertTrue(cursor.getString(1).isEmpty())
        }
        database.close()
    }

    @Test
    fun migrate7To8_keepsShoppingRowsAsNotYetSynced() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val dbName = "migration-7-8.db"
        context.deleteDatabase(dbName)
        val path = context.getDatabasePath(dbName)
        path.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(path, null).use { sqlite ->
            sqlite.execSQL(
                """
                CREATE TABLE drinks (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    thumb TEXT NOT NULL,
                    saved INTEGER NOT NULL,
                    alcoholic TEXT,
                    glass TEXT,
                    category TEXT,
                    iba TEXT,
                    instructions TEXT,
                    video TEXT,
                    ingredients TEXT,
                    notes TEXT NOT NULL DEFAULT '',
                    recipeUpdatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE drink_filter (
                    drinkId TEXT NOT NULL,
                    filterName TEXT NOT NULL,
                    PRIMARY KEY(drinkId, filterName)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE catalog_terms (
                    kind TEXT NOT NULL,
                    name TEXT NOT NULL,
                    PRIMARY KEY(kind, name)
                )
                """.trimIndent(),
            )
            sqlite.execSQL(
                """
                CREATE TABLE shopping_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    checked INTEGER NOT NULL,
                    drinkId TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent(),
            )
            sqlite.execSQL("CREATE INDEX index_shopping_items_drinkId ON shopping_items(drinkId)")
            sqlite.execSQL("INSERT INTO shopping_items (name, checked, drinkId) VALUES ('Lime', 1, '11007')")
            sqlite.execSQL(
                """
                CREATE TABLE recently_viewed (
                    drinkId TEXT NOT NULL PRIMARY KEY,
                    viewedAt INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            sqlite.execSQL("CREATE TABLE bar_ingredients (name TEXT NOT NULL PRIMARY KEY)")
            sqlite.execSQL("PRAGMA user_version = 7")
        }

        val database = Room.databaseBuilder(context, MixologyDatabase::class.java, dbName)
            .addMigrations(*MixologyDatabase.ALL_MIGRATIONS)
            .build()
        database.openHelper.writableDatabase.query(
            "SELECT name, checked, drinkId, remoteId FROM shopping_items",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.getString(0) == "Lime")
            assertTrue(cursor.getInt(1) == 1)
            assertTrue(cursor.getString(2) == "11007")
            assertTrue(cursor.isNull(3))
        }
        database.close()
    }
}
