package com.capstone.nik.mixology.data

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.repository.FilterKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class CatalogSeedTest {

    private lateinit var database: MixologyDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, MixologyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        ApplicationProvider.getApplicationContext<Application>()
            .getSharedPreferences("mixology", Context.MODE_PRIVATE)
            .edit()
            .remove(CatalogSeed.PREF_VERSION)
            .apply()
        database.close()
    }

    @Test
    fun importPayload_upsertsRecipesAndCatalogTerms() = runTest {
        val json = """
            {
              "version":1,
              "ingredients":["Gin","Campari"],
              "drinks":[{
                "id":"11003",
                "name":"Negroni",
                "thumb":"https://example.com/n.jpg",
                "category":"Cocktail",
                "alcoholic":"Alcoholic",
                "glass":"Old-fashioned glass",
                "instructions":"Stir.",
                "ingredients":[
                  {"ingredient":"Gin","measure":"1 oz"},
                  {"ingredient":"Campari","measure":"1 oz"}
                ]
              }]
            }
        """.trimIndent()
        val payload = CatalogSeed.parse(json)!!
        CatalogSeed.importPayload(payload, database.drinkDao())

        val drink = database.drinkDao().getById("11003")!!
        assertEquals("Negroni", drink.name)
        assertEquals(listOf("Gin", "Campari"), drink.ingredients?.map { it.ingredient })
        assertEquals(
            listOf("Campari", "Gin"),
            database.drinkDao().observeCatalog(FilterKind.INGREDIENT.name).first().map { it.name },
        )
        assertEquals(
            listOf("Cocktail"),
            database.drinkDao().observeCatalog(FilterKind.DRINK_TYPE.name).first().map { it.name },
        )
    }

    @Test
    fun importIfNeeded_reimportsWhenRecipesMissingEvenIfVersionPrefSet() = runTest {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.getSharedPreferences("mixology", Context.MODE_PRIVATE)
            .edit()
            .putInt(CatalogSeed.PREF_VERSION, CatalogSeed.VERSION)
            .apply()

        CatalogSeed.importIfNeeded(context, database.drinkDao())

        assertTrue(database.drinkDao().getRecipes().isNotEmpty())
        assertEquals(
            CatalogSeed.VERSION,
            context.getSharedPreferences("mixology", Context.MODE_PRIVATE)
                .getInt(CatalogSeed.PREF_VERSION, 0),
        )
    }
}
