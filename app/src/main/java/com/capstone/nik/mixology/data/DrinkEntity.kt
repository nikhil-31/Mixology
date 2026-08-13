package com.capstone.nik.mixology.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.capstone.nik.mixology.Model.Cocktail

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumb: String,
    val saved: Boolean = false,
) {
    fun toCocktail(): Cocktail = Cocktail(id, name, thumb)
}

data class DrinkListItem(
    val id: String,
    val name: String,
    val thumb: String,
    val saved: Boolean,
) {
    fun toCocktail(): Cocktail = Cocktail(id, name, thumb)

    companion object {
        fun from(entity: DrinkEntity, savedIds: Set<String>): DrinkListItem {
            return DrinkListItem(
                id = entity.id,
                name = entity.name,
                thumb = entity.thumb,
                saved = entity.id in savedIds || entity.saved,
            )
        }
    }
}
