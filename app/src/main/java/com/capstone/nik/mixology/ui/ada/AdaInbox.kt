package com.capstone.nik.mixology.ui.ada

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Something for Ada from elsewhere in the app: a message, such as a link Import couldn't
 * read or the bar's "what can I make?", or a photo, such as the bar's shelf. Ada sends it
 * once she's ready and then clears it.
 */
@Singleton
class AdaInbox @Inject constructor() {
    /** A photo waiting for Ada, with [caption] as what the user says with it. */
    data class Photo(val uri: Uri, val caption: String)

    private val _pending = MutableStateFlow<String?>(null)
    val pending: StateFlow<String?> = _pending.asStateFlow()

    private val _pendingPhoto = MutableStateFlow<Photo?>(null)
    val pendingPhoto: StateFlow<Photo?> = _pendingPhoto.asStateFlow()

    fun post(message: String) {
        if (message.isNotBlank()) _pending.value = message.trim()
    }

    fun postPhoto(uri: Uri, caption: String) {
        _pendingPhoto.value = Photo(uri, caption.trim())
    }

    /** The waiting message, removed so it's sent once. */
    fun take(): String? = _pending.value.also { _pending.value = null }

    /** The waiting photo, removed so it's sent once. */
    fun takePhoto(): Photo? = _pendingPhoto.value.also { _pendingPhoto.value = null }
}
