package com.capstone.nik.mixology.data

import androidx.room.Entity

@Entity(tableName = "drink_filter", primaryKeys = ["drinkId", "filterName"])
data class DrinkFilterCrossRef(
    val drinkId: String,
    val filterName: String,
)
