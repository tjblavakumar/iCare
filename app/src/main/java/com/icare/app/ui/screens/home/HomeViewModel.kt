package com.icare.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icare.app.data.model.CurrentStatus
import com.icare.app.data.model.EmojiStatus
import com.icare.app.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val currentStatus: CurrentStatus? = null,
    val isUpdating: Boolean = false,
    val showMoreEmojis: Boolean = false,
    val updateMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statusRepository: StatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeStatus()
    }

    private fun observeStatus() {
        viewModelScope.launch {
            statusRepository.observeCurrentStatus().collect { status ->
                _uiState.value = _uiState.value.copy(currentStatus = status)
            }
        }
    }

    fun updateStatus(emojiStatus: EmojiStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            statusRepository.updateStatus(emojiStatus)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateMessage = "Status updated!"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        updateMessage = e.message ?: "Failed to update"
                    )
                }
        }
    }

    fun toggleMoreEmojis() {
        _uiState.value = _uiState.value.copy(showMoreEmojis = !_uiState.value.showMoreEmojis)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(updateMessage = null)
    }
}
