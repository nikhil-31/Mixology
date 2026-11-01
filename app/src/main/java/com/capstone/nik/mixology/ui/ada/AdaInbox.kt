package com.capstone.nik.mixology.ui.ada

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A message for Ada from elsewhere in the app, such as a link Import couldn't
 * read. Ada sends it once she's ready and then clears it.
 */
@Singleton
class AdaInbox @Inject constructor() {
    private val _pending = MutableStateFlow<String?>(null)
    val pending: StateFlow<String?> = _pending.asStateFlow()

    fun post(message: String) {
        if (message.isNotBlank()) _pending.value = message.trim()
    }

    /** The waiting message, removed so it's sent once. */
    fun take(): String? = _pending.value.also { _pending.value = null }
}
