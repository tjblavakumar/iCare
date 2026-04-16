package com.icare.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icare.app.data.model.Connection
import com.icare.app.data.model.User
import com.icare.app.data.repository.AuthRepository
import com.icare.app.data.repository.ConnectionRepository
import com.icare.app.data.repository.ContactDiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

data class ManageContactsUiState(
    val connections: List<Pair<Connection, User>> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

data class PendingRequestsUiState(
    val requests: List<Pair<Connection, User>> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

data class AddContactUiState(
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val connectionRepository: ConnectionRepository,
    private val contactDiscoveryRepository: ContactDiscoveryRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    private val _manageContactsState = MutableStateFlow(ManageContactsUiState())
    val manageContactsState: StateFlow<ManageContactsUiState> = _manageContactsState.asStateFlow()

    private val _pendingRequestsState = MutableStateFlow(PendingRequestsUiState())
    val pendingRequestsState: StateFlow<PendingRequestsUiState> = _pendingRequestsState.asStateFlow()

    private val _addContactState = MutableStateFlow(AddContactUiState())
    val addContactState: StateFlow<AddContactUiState> = _addContactState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            try {
                val user = authRepository.getCurrentUserData()
                _settingsState.value = _settingsState.value.copy(
                    currentUser = user,
                    isLoading = false
                )
            } catch (e: Exception) {
                _settingsState.value = _settingsState.value.copy(
                    isLoading = false,
                    message = e.message
                )
            }
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            authRepository.updateDisplayName(name)
                .onSuccess {
                    loadCurrentUser()
                    _settingsState.value = _settingsState.value.copy(message = "Name updated!")
                }
                .onFailure { e ->
                    _settingsState.value = _settingsState.value.copy(message = e.message)
                }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.deleteAccount()
                .onSuccess { onComplete() }
                .onFailure { e ->
                    _settingsState.value = _settingsState.value.copy(
                        message = "Failed to delete: ${e.message}"
                    )
                }
        }
    }

    fun loadMyConnections() {
        viewModelScope.launch {
            _manageContactsState.value = _manageContactsState.value.copy(isLoading = true)
            try {
                val connections = connectionRepository.getMyConnections()
                _manageContactsState.value = _manageContactsState.value.copy(
                    connections = connections,
                    isLoading = false
                )
            } catch (e: Exception) {
                _manageContactsState.value = _manageContactsState.value.copy(
                    isLoading = false,
                    message = e.message
                )
            }
        }
    }

    fun removeConnection(connectionId: String) {
        viewModelScope.launch {
            connectionRepository.removeConnection(connectionId)
                .onSuccess {
                    loadMyConnections()
                    _manageContactsState.value = _manageContactsState.value.copy(
                        message = "Contact removed"
                    )
                }
                .onFailure { e ->
                    _manageContactsState.value = _manageContactsState.value.copy(
                        message = e.message
                    )
                }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            _pendingRequestsState.value = _pendingRequestsState.value.copy(isLoading = true)
            try {
                val requests = connectionRepository.getPendingRequestsForMe()
                _pendingRequestsState.value = _pendingRequestsState.value.copy(
                    requests = requests,
                    isLoading = false
                )
            } catch (e: Exception) {
                _pendingRequestsState.value = _pendingRequestsState.value.copy(
                    isLoading = false,
                    message = e.message
                )
            }
        }
    }

    fun acceptRequest(connectionId: String) {
        viewModelScope.launch {
            connectionRepository.acceptConnection(connectionId)
                .onSuccess {
                    loadPendingRequests()
                    _pendingRequestsState.value = _pendingRequestsState.value.copy(
                        message = "Request accepted!"
                    )
                }
                .onFailure { e ->
                    _pendingRequestsState.value = _pendingRequestsState.value.copy(
                        message = e.message
                    )
                }
        }
    }

    fun rejectRequest(connectionId: String) {
        viewModelScope.launch {
            connectionRepository.rejectConnection(connectionId)
                .onSuccess {
                    loadPendingRequests()
                    _pendingRequestsState.value = _pendingRequestsState.value.copy(
                        message = "Request rejected"
                    )
                }
                .onFailure { e ->
                    _pendingRequestsState.value = _pendingRequestsState.value.copy(
                        message = e.message
                    )
                }
        }
    }

    fun searchContact(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _addContactState.value = _addContactState.value.copy(isSearching = true)
            try {
                val results = contactDiscoveryRepository.searchByEmailOrPhone(query)
                _addContactState.value = _addContactState.value.copy(
                    searchResults = results,
                    isSearching = false,
                    message = if (results.isEmpty()) "No users found" else null
                )
            } catch (e: Exception) {
                _addContactState.value = _addContactState.value.copy(
                    isSearching = false,
                    message = e.message
                )
            }
        }
    }

    fun sendConnectionRequest(targetUserId: String) {
        viewModelScope.launch {
            connectionRepository.sendConnectionRequest(targetUserId)
                .onSuccess {
                    _addContactState.value = _addContactState.value.copy(
                        message = "Request sent!"
                    )
                }
                .onFailure { e ->
                    _addContactState.value = _addContactState.value.copy(
                        message = e.message
                    )
                }
        }
    }

    fun clearSettingsMessage() {
        _settingsState.value = _settingsState.value.copy(message = null)
    }

    fun clearAddContactMessage() {
        _addContactState.value = _addContactState.value.copy(message = null)
    }
}
