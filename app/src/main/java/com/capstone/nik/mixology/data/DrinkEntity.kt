package com.capstone.nik.mixology.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.capstone.nik.mixology.ui.model.IngredientMeasure

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumb: String,
    val saved: Boolean = false,
    val alcoholic: String? = null,
    val glass: String? = null,
    val category: String? = null,
    val iba: String? = null,
    val instructions: String? = null,
    val video: String? = null,
    val ingredients: List<IngredientMeasure>? = null,
    @ColumnInfo(defaultValue = "")
    val notes: String = "",
    @ColumnInfo(defaultValue = "0")
    val recipeUpdatedAt: Long = 0L,
) {
    fun toDrink(savedOverride: Boolean? = null): Drink = Drink(
        id = id,
        name = name,
        thumb = thumb,
        saved = savedOverride ?: saved,
        alcoholic = alcoholic,
        glass = glass,
        category = category,
        iba = iba,
        instructions = instructions,
        video = video,
        ingredients = ingredients.orEmpty(),
        notes = notes,
    )
}

fun Drink.toEntity(): DrinkEntity = DrinkEntity(
    id = id,
    name = name,
    thumb = thumb,
    saved = saved,
    alcoholic = alcoholic,
    glass = glass,
    category = category,
    iba = iba,
    instructions = instructions,
    video = video,
    ingredients = ingredients,
    notes = notes,
    recipeUpdatedAt = if (hasRecipe) System.currentTimeMillis() else 0L,
)
