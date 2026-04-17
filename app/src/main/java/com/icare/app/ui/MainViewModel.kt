package com.icare.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icare.app.data.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _pendingRequestsCount = MutableStateFlow(0)
    val pendingRequestsCount: StateFlow<Int> = _pendingRequestsCount.asStateFlow()

    init {
        loadPendingRequestsCount()
    }

    fun loadPendingRequestsCount() {
        viewModelScope.launch {
            try {
                val requests = connectionRepository.getPendingRequestsForMe()
                _pendingRequestsCount.value = requests.size
            } catch (e: Exception) {
                _pendingRequestsCount.value = 0
            }
        }
    }
}
