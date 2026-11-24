package com.capstone.nik.mixology.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    // A channel holds an effect until the screen collects it, so one sent while the screen is
    // being rebuilt (a rotation) still arrives. Each effect goes to one collector, once.
    private val _effects = Channel<E>(capacity = 64)
    override val effects: Flow<E> = _effects.receiveAsFlow()

    protected val currentState: S get() = _state.value

    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        if (_effects.trySend(effect).isFailure) {
            viewModelScope.launch { _effects.send(effect) }
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
