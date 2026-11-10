package com.capstone.nik.mixology.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.repository.DrinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** What the bottom bar shows besides the tabs: the shopping items still to buy, for the badge. */
@HiltViewModel
class BottomBarViewModel @Inject constructor(
    repository: DrinkRepository,
) : ViewModel() {

    val shoppingLeft: StateFlow<Int> = repository.observeShopping()
        .map { items -> items.count { !it.checked } }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
