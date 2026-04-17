package com.icare.app.ui.screens.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icare.app.data.repository.ConnectionRepository
import com.icare.app.data.repository.ContactWithStatus
import com.icare.app.data.repository.NicknameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CircleUiState(
    val contacts: List<ContactWithStatus> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingContact: ContactWithStatus? = null
)

@HiltViewModel
class CircleViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val nicknameRepository: NicknameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CircleUiState())
    val uiState: StateFlow<CircleUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contacts = connectionRepository.getAcceptedContacts()
                val nicknames = nicknameRepository.getNicknames()
                
                val contactsWithNicknames = contacts.map { contact ->
                    contact.copy(customDisplayName = nicknames[contact.userId])
                }
                
                val sorted = contactsWithNicknames.sortedWith(
                    compareBy<ContactWithStatus> { contact ->
                        when {
                            contact.isInactive -> 3
                            contact.currentStatus?.emojiId == "bad" -> 0
                            contact.currentStatus?.emojiId == "low" -> 1
                            else -> 2
                        }
                    }.thenByDescending { it.currentStatus?.timestamp?.seconds ?: 0 }
                )
                _uiState.value = _uiState.value.copy(
                    contacts = sorted,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun startEditingNickname(contact: ContactWithStatus) {
        _uiState.value = _uiState.value.copy(editingContact = contact)
    }

    fun cancelEditingNickname() {
        _uiState.value = _uiState.value.copy(editingContact = null)
    }

    fun saveNickname(userId: String, nickname: String) {
        viewModelScope.launch {
            nicknameRepository.setNickname(userId, nickname)
            _uiState.value = _uiState.value.copy(editingContact = null)
            loadContacts()
        }
    }
}
