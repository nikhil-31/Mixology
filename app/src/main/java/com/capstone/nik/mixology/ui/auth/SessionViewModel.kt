package com.capstone.nik.mixology.ui.auth

import androidx.lifecycle.ViewModel
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {
    val session: StateFlow<Session?> = sessionStore.session

    fun signOut() {
        sessionStore.clear()
    }
}
