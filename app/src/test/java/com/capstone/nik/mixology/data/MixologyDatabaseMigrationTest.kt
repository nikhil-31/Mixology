package com.capstone.nik.mixology.data

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class MixologyDatabaseMigrationTest {

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
}
