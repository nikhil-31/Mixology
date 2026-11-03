package com.capstone.nik.mixology.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.Network.errorMessageRes
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Session
import com.capstone.nik.mixology.data.SessionStore
import com.capstone.nik.mixology.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Progress of a sign-out, export or deletion started from Settings. */
data class AccountUiState(
    val busy: Boolean = false,
    @StringRes val errorRes: Int? = null,
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val auth: AuthRepository,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    val session: StateFlow<Session?> = sessionStore.session

    private val _account = MutableStateFlow(AccountUiState())
    val account: StateFlow<AccountUiState> = _account.asStateFlow()

    private val _exports = Channel<String>(Channel.BUFFERED)
    /** JSON of the account's data, ready to share, once an export finishes. */
    val exports: Flow<String> = _exports.receiveAsFlow()

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }

    fun deleteAccount() {
        accountAction(R.string.settings_delete_account_failed) { auth.deleteAccount() }
    }

    fun exportData() {
        accountAction(R.string.settings_export_failed) { _exports.send(auth.exportData()) }
    }

    private fun accountAction(@StringRes failureRes: Int, action: suspend () -> Unit) {
        if (_account.value.busy) return
        _account.value = AccountUiState(busy = true)
        viewModelScope.launch {
            try {
                action()
                _account.value = AccountUiState()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _account.value = AccountUiState(errorRes = networkMonitor.errorMessageRes(error, failureRes))
            }
        }
    }
}
