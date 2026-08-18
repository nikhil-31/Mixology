package com.capstone.nik.mixology.ui.shopping

import com.capstone.nik.mixology.data.ShoppingItemEntity

data class ShoppingUiState(
    val items: List<ShoppingItemEntity> = emptyList(),
)

sealed interface ShoppingIntent {
    data class Toggle(val item: ShoppingItemEntity) : ShoppingIntent
    data class Remove(val item: ShoppingItemEntity) : ShoppingIntent
    data object ClearChecked : ShoppingIntent
}

sealed interface ShoppingEffect
