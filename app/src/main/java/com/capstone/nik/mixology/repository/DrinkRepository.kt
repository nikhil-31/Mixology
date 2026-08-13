package com.capstone.nik.mixology.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.Drink
import com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME
import com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB
import com.capstone.nik.mixology.data.AlcoholicColumn._ID
import java.io.IOException

enum class FilterKind {
    ALCOHOL,
    GLASS,
    INGREDIENT,
    DRINK_TYPE,
}

class DrinkRepository(
    private val context: Context,
    private val service: CocktailService,
) {

    @Throws(IOException::class)
    fun fetchAndCache(uri: Uri, filter: String, kind: FilterKind) {
        val call = when (kind) {
            FilterKind.ALCOHOL -> service.getAlcoholFilter(filter)
            FilterKind.GLASS -> service.getGlassFilter(filter)
            FilterKind.INGREDIENT -> service.getIngredientFilter(filter)
            FilterKind.DRINK_TYPE -> service.getDrinkTypeFilter(filter)
        }
        val response = call.execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code()}")
        }
        val drinks = response.body()?.drinks ?: return
        insertNewDrinks(uri, drinks)
    }

    private fun insertNewDrinks(uri: Uri, drinks: List<Drink>) {
        val existingIds = existingIds(uri)
        val values = drinks.mapNotNull { drink ->
            val id = drink.idDrink ?: return@mapNotNull null
            if (id in existingIds || !hasUsableThumb(drink)) {
                null
            } else {
                ContentValues().apply {
                    put(_ID, id)
                    put(DRINK_NAME, drink.strDrink)
                    put(DRINK_THUMB, drink.strDrinkThumb)
                }
            }
        }
        if (values.isNotEmpty()) {
            context.contentResolver.bulkInsert(uri, values.toTypedArray())
        }
    }

    private fun existingIds(uri: Uri): Set<String> {
        val ids = HashSet<String>()
        context.contentResolver.query(uri, arrayOf(_ID), null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(_ID)
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(idIndex))
            }
        }
        return ids
    }

    companion object {
        fun hasUsableThumb(drink: Drink): Boolean {
            val thumb = drink.strDrinkThumb
            return !thumb.isNullOrEmpty() && thumb != "null"
        }
    }
}
