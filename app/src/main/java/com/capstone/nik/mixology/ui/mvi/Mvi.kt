package com.capstone.nik.mixology.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface MviStore<I, S, E> {
    val state: StateFlow<S>
    val effects: Flow<E>
    fun onIntent(intent: I)
}

abstract class MviViewModel<I, S, E>(initialState: S) : ViewModel(), MviStore<I, S, E> {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<E>(extraBufferCapacity = 64)
    override val effects: Flow<E> = _effects.asSharedFlow()

    protected val currentState: S get() = _state.value

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        if (!_effects.tryEmit(effect)) {
            viewModelScope.launch { _effects.emit(effect) }
        }
    }
}

@Composable
fun <E> CollectMviEffects(
    effects: Flow<E>,
    onEffect: suspend (E) -> Unit,
) {
    val callback by rememberUpdatedState(onEffect)
    LaunchedEffect(effects) {
        effects.collect { effect ->
            launch { callback(effect) }
        }
    }
}
