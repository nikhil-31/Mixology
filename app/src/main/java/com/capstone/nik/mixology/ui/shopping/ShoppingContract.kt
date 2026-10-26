package com.capstone.nik.mixology.ui.shopping

import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection

data class ShoppingUiState(
    val sections: List<ShoppingSection> = emptyList(),
) {
    val items: List<ShoppingItemEntity> get() = sections.flatMap { it.items }
}

sealed interface ShoppingIntent {
    data class Toggle(val item: ShoppingItemEntity) : ShoppingIntent
    data class Remove(val item: ShoppingItemEntity) : ShoppingIntent
    data object ClearChecked : ShoppingIntent
}

sealed interface ShoppingEffect
