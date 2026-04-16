package com.icare.app.ui.screens.circle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icare.app.data.model.StatusEntry
import com.icare.app.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactHistoryUiState(
    val history: List<StatusEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ContactHistoryViewModel @Inject constructor(
    private val statusRepository: StatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactHistoryUiState())
    val uiState: StateFlow<ContactHistoryUiState> = _uiState.asStateFlow()

    fun loadHistory(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val history = statusRepository.getStatusHistory(userId, daysBack = 7)
                _uiState.value = _uiState.value.copy(
                    history = history,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
