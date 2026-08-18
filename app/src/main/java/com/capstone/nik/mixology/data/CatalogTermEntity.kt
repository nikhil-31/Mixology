package com.capstone.nik.mixology.data

import androidx.room.Entity

@Entity(tableName = "catalog_terms", primaryKeys = ["kind", "name"])
data class CatalogTermEntity(
    val kind: String,
    val name: String,
)
