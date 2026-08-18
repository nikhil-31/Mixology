package com.capstone.nik.mixology.ui.shopping

import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val repository: DrinkRepository,
) : MviViewModel<ShoppingIntent, ShoppingUiState, ShoppingEffect>(ShoppingUiState()) {

    init {
        viewModelScope.launch {
            repository.observeShopping().collect { items ->
                setState { copy(items = items) }
            }
        }
    }

    override fun onIntent(intent: ShoppingIntent) {
        when (intent) {
            is ShoppingIntent.Toggle -> viewModelScope.launch {
                repository.setShoppingChecked(intent.item.id, !intent.item.checked)
            }
            is ShoppingIntent.Remove -> viewModelScope.launch {
                repository.removeShoppingItem(intent.item.id)
            }
            ShoppingIntent.ClearChecked -> viewModelScope.launch {
                repository.clearCheckedShoppingItems()
            }
        }
    }
}
