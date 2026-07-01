package com.digifello.tuitionmanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digifello.tuitionmanager.data.model.Batch
import com.digifello.tuitionmanager.data.repository.BatchRepository
import com.digifello.tuitionmanager.data.repository.PaymentRepository
import com.digifello.tuitionmanager.ui.common.UiState
import com.digifello.tuitionmanager.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * One batch combined with its current month's payment status —
 * this is what the Dashboard screen actually displays per card.
 */
data class BatchWithStatus(
    val batch: Batch,
    val paymentStatus: String   // "pending" | "partial" | "paid"
)

class DashboardViewModel(
    private val batchRepository: BatchRepository = BatchRepository(),
    private val paymentRepository: PaymentRepository = PaymentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<BatchWithStatus>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<BatchWithStatus>>> = _uiState.asStateFlow()

    init {
        loadBatches()
    }

    private fun loadBatches() {
        viewModelScope.launch {
            batchRepository.getBatchesFlow().collect { batches ->
                val month = DateUtils.currentMonthKey()
                // For each batch, fetch its current month's payment to know the badge status
                val withStatus = batches.map { batch ->
                    val payments = paymentRepository.getCurrentMonthPayments(listOf(batch.id), month)
                    val status = payments.firstOrNull()?.status ?: "pending"
                    BatchWithStatus(batch, status)
                }
                _uiState.value = UiState.Success(withStatus)
            }
        }
    }
}