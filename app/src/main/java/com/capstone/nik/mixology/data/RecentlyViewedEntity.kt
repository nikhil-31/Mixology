package com.capstone.nik.mixology.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_viewed")
data class RecentlyViewedEntity(
    @PrimaryKey val drinkId: String,
    val viewedAt: Long,
)
