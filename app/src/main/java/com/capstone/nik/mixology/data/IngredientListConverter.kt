package com.capstone.nik.mixology.data

import androidx.room.TypeConverter
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IngredientListConverter {
    private val gson = Gson()
    private val type = object : TypeToken<List<IngredientMeasure>>() {}.type

    @TypeConverter
    fun fromJson(value: String?): List<IngredientMeasure> {
        if (value.isNullOrBlank()) return emptyList()
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun toJson(list: List<IngredientMeasure>?): String? {
        if (list.isNullOrEmpty()) return null
        return gson.toJson(list)
    }
}
