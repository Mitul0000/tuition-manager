package com.digifello.tutordesk.ui.Screens.Batchlistcontent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tutordesk.data.model.Batch
import com.digifello.tutordesk.data.repository.BatchRepository
import com.digifello.tutordesk.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val batches: List<Batch> = emptyList(),
    val totalEarnedAllTime: Long = 0L,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeBatches()
        observeTotalEarned()
    }

    private fun observeBatches() {
        viewModelScope.launch {
            batchRepository.getBatches()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Could not load batches."
                    )
                }
                .collect { batches ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        batches = batches,
                        errorMessage = null
                    )
                }
        }
    }

    private fun observeTotalEarned() {
        viewModelScope.launch {
            paymentRepository.getTotalEarnedAllTime()
                .catch { }
                .collect { total ->
                    _uiState.value = _uiState.value.copy(totalEarnedAllTime = total)
                }
        }
    }

    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }
}